package com.horserace;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
public class HorseApp {
    public static void main(String[] args) {
        SpringApplication.run(HorseApp.class, args);
    }
}

// --- ENTITIES ---
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
class Room {
    @Id
    private String roomId;
    private String adminName;
    private boolean isRacing;
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "horses")
class Horse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int horseNumber;
    private String roomId;
    private String status = "AVAILABLE";
    private String ownerName;
}

// --- DTOs ---
@Data
class CreateRoomRequest { private String adminName; }

@Data
class JoinRoomRequest { private String roomId; private String userName; }

@Data
class SelectRequest { private String roomId; private int horseNumber; private String ownerName; }

@Data
class StartRaceRequest { private String roomId; private String adminName; private int durationSeconds; }

@Data
@AllArgsConstructor
class RaceResult {
    private int winnerNumber;
    private String winnerName;
    private int durationSeconds;
}

@Data
@AllArgsConstructor
class RoomInfo {
    private String roomId;
    private String adminName;
    private boolean isRacing;
    private List<Horse> horses;
}

// --- REPOSITORIES ---
interface RoomRepository extends JpaRepository<Room, String> {}

interface HorseRepository extends JpaRepository<Horse, Long> {
    List<Horse> findByRoomId(String roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Horse> findByRoomIdAndHorseNumber(String roomId, int horseNumber);

    // Tìm ngựa đang được chọn bởi người chơi
    Optional<Horse> findByRoomIdAndOwnerName(String roomId, String ownerName);
}

// --- WEBSOCKET CONFIG ---
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}

// --- CONTROLLER ---
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class GameController {

    private final RoomRepository roomRepository;
    private final HorseRepository horseRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 1. Tạo Phòng
    @PostMapping("/create-room")
    @Transactional
    public RoomInfo createRoom(@RequestBody CreateRoomRequest req) {
        if (req.getAdminName() == null || req.getAdminName().isBlank())
            throw new RuntimeException("Cần nhập tên Admin!");

        String roomId = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999));
        Room room = new Room(roomId, req.getAdminName(), false);
        roomRepository.save(room);

        List<Horse> horses = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            horses.add(new Horse(null, i, roomId, "AVAILABLE", null));
        }
        horseRepository.saveAll(horses);

        return new RoomInfo(roomId, req.getAdminName(), false, horses);
    }

    // 2. Vào Phòng
    @GetMapping("/room/{roomId}")
    public RoomInfo joinRoom(@PathVariable String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));
        List<Horse> horses = horseRepository.findByRoomId(roomId);
        return new RoomInfo(room.getRoomId(), room.getAdminName(), room.isRacing(), horses);
    }

    // 3. Chọn Ngựa (TỰ ĐỘNG ĐỔI NGỰA NẾU ĐÃ CHỌN TRƯỚC ĐÓ)
    @PostMapping("/select")
    @Transactional
    public Horse selectHorse(@RequestBody SelectRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (room.isRacing()) throw new RuntimeException("Đang đua, không thể chọn!");

        String ownerName = req.getOwnerName().trim();

        // LOGIC MỚI: Kiểm tra xem người này có đang giữ con ngựa nào không?
        Optional<Horse> currentHorseOpt = horseRepository.findByRoomIdAndOwnerName(req.getRoomId(), ownerName);

        if (currentHorseOpt.isPresent()) {
            Horse currentHorse = currentHorseOpt.get();
            // Nếu chọn lại chính con đang giữ -> Không làm gì cả
            if (currentHorse.getHorseNumber() == req.getHorseNumber()) {
                return currentHorse;
            }
            // Nếu chọn con khác -> Nhả con cũ ra (Reset về AVAILABLE)
            currentHorse.setStatus("AVAILABLE");
            currentHorse.setOwnerName(null);
            horseRepository.save(currentHorse);

            // Gửi socket để cập nhật giao diện (con cũ chuyển sang màu xanh)
            messagingTemplate.convertAndSend("/topic/room/" + req.getRoomId() + "/horse.update", currentHorse);
        }

        // Lock & Chọn Ngựa Mới
        Horse horse = horseRepository.findByRoomIdAndHorseNumber(req.getRoomId(), req.getHorseNumber())
                .orElseThrow(() -> new RuntimeException("Ngựa không tồn tại"));

        if (!"AVAILABLE".equals(horse.getStatus())) {
            throw new RuntimeException("Chậm tay rồi! Ngựa số " + req.getHorseNumber() + " đã bị chọn.");
        }

        horse.setStatus("TAKEN");
        horse.setOwnerName(ownerName);
        horseRepository.save(horse);

        // Bắn socket update con mới (chuyển sang màu đỏ)
        messagingTemplate.convertAndSend("/topic/room/" + req.getRoomId() + "/horse.update", horse);
        return horse;
    }

    // 4. Hủy chọn ngựa (Deselect)
    @PostMapping("/deselect")
    @Transactional
    public String deselectHorse(@RequestBody SelectRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (room.isRacing()) throw new RuntimeException("Đang đua, không thể hủy!");

        String ownerName = req.getOwnerName().trim();
        Optional<Horse> currentHorse = horseRepository.findByRoomIdAndOwnerName(req.getRoomId(), ownerName);

        if (currentHorse.isPresent()) {
            Horse horse = currentHorse.get();
            horse.setStatus("AVAILABLE");
            horse.setOwnerName(null);
            horseRepository.save(horse);

            // Gửi socket update
            messagingTemplate.convertAndSend("/topic/room/" + req.getRoomId() + "/horse.update", horse);
            return "Đã hủy chọn ngựa số " + horse.getHorseNumber();
        }
        return "Bạn chưa chọn ngựa nào";
    }

    // 5. Bắt đầu đua (Winner nằm trong nhóm TAKEN)
    @PostMapping("/start")
    @Transactional
    public RaceResult startRace(@RequestBody StartRaceRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        if (!room.getAdminName().equals(req.getAdminName())) {
            throw new RuntimeException("Bạn không phải Admin phòng này!");
        }
        if (room.isRacing()) throw new RuntimeException("Đang đua rồi!");

        // Lấy danh sách những người tham gia
        List<Horse> participants = horseRepository.findByRoomId(req.getRoomId()).stream()
                .filter(h -> "TAKEN".equals(h.getStatus()))
                .collect(Collectors.toList());

        if (participants.isEmpty()) throw new RuntimeException("Chưa có ai chọn ngựa!");

        // Set trạng thái đua
        room.setRacing(true);
        roomRepository.save(room);

        // LOGIC MỚI: Random trong danh sách participants
        int winnerIndex = ThreadLocalRandom.current().nextInt(participants.size());
        Horse winner = participants.get(winnerIndex);

        RaceResult result = new RaceResult(winner.getHorseNumber(), winner.getOwnerName(), req.getDurationSeconds());

        messagingTemplate.convertAndSend("/topic/room/" + req.getRoomId() + "/race.result", result);
        return result;
    }

    // 6. Reset Game
    @PostMapping("/reset")
    @Transactional
    public String resetGame(@RequestBody StartRaceRequest req) {
        Room room = roomRepository.findById(req.getRoomId()).orElseThrow();
        if (!room.getAdminName().equals(req.getAdminName())) throw new RuntimeException("Not Admin");

        List<Horse> horses = horseRepository.findByRoomId(req.getRoomId());
        horses.forEach(h -> {
            h.setStatus("AVAILABLE");
            h.setOwnerName(null);
        });
        horseRepository.saveAll(horses);

        room.setRacing(false);
        roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/room/" + req.getRoomId() + "/game.reset", "RESET");
        return "OK";
    }
    // Import dữ liệu đầy đủ từ SQL
    @PostMapping("/import-full-data")
    @Transactional
    public String importFullData() {
        try {
            // Xóa dữ liệu cũ
            horseRepository.deleteAll();
            roomRepository.deleteAll();

            // Tạo room
            Room room = new Room("2322", "wild", true);
            roomRepository.save(room);

            // Tạo danh sách 50 horses theo đúng SQL
            List<Horse> horses = Arrays.asList(
                    new Horse(null, 1, "2322", "AVAILABLE", null),
                    new Horse(null, 2, "2322", "AVAILABLE", null),
                    new Horse(null, 3, "2322", "AVAILABLE", null),
                    new Horse(null, 4, "2322", "AVAILABLE", null),
                    new Horse(null, 5, "2322", "AVAILABLE", null),
                    new Horse(null, 6, "2322", "AVAILABLE", null),
                    new Horse(null, 7, "2322", "AVAILABLE", null),
                    new Horse(null, 8, "2322", "AVAILABLE", null),
                    new Horse(null, 9, "2322", "AVAILABLE", null),
                    new Horse(null, 10, "2322", "AVAILABLE", null),
                    new Horse(null, 11, "2322", "AVAILABLE", null),
                    new Horse(null, 12, "2322", "AVAILABLE", null),
                    new Horse(null, 13, "2322", "AVAILABLE", null),
                    new Horse(null, 14, "2322", "AVAILABLE", null),
                    new Horse(null, 15, "2322", "AVAILABLE", null),
                    new Horse(null, 16, "2322", "AVAILABLE", null),
                    new Horse(null, 17, "2322", "AVAILABLE", null),
                    new Horse(null, 18, "2322", "AVAILABLE", null),
                    new Horse(null, 19, "2322", "AVAILABLE", null),
                    new Horse(null, 20, "2322", "AVAILABLE", null),
                    new Horse(null, 21, "2322", "AVAILABLE", null),
                    new Horse(null, 22, "2322", "AVAILABLE", null),
                    new Horse(null, 23, "2322", "AVAILABLE", null),
                    new Horse(null, 24, "2322", "AVAILABLE", null),
                    new Horse(null, 25, "2322", "AVAILABLE", null),
                    new Horse(null, 26, "2322", "AVAILABLE", null),
                    new Horse(null, 27, "2322", "AVAILABLE", null),
                    new Horse(null, 28, "2322", "AVAILABLE", null),
                    new Horse(null, 29, "2322", "TAKEN", "wild"),  // Ngựa số 29 đã được chọn
                    new Horse(null, 30, "2322", "AVAILABLE", null),
                    new Horse(null, 31, "2322", "AVAILABLE", null),
                    new Horse(null, 32, "2322", "AVAILABLE", null),
                    new Horse(null, 33, "2322", "AVAILABLE", null),
                    new Horse(null, 34, "2322", "AVAILABLE", null),
                    new Horse(null, 35, "2322", "AVAILABLE", null),
                    new Horse(null, 36, "2322", "AVAILABLE", null),
                    new Horse(null, 37, "2322", "AVAILABLE", null),
                    new Horse(null, 38, "2322", "AVAILABLE", null),
                    new Horse(null, 39, "2322", "AVAILABLE", null),
                    new Horse(null, 40, "2322", "AVAILABLE", null),
                    new Horse(null, 41, "2322", "AVAILABLE", null),
                    new Horse(null, 42, "2322", "AVAILABLE", null),
                    new Horse(null, 43, "2322", "AVAILABLE", null),
                    new Horse(null, 44, "2322", "AVAILABLE", null),
                    new Horse(null, 45, "2322", "AVAILABLE", null),
                    new Horse(null, 46, "2322", "AVAILABLE", null),
                    new Horse(null, 47, "2322", "AVAILABLE", null),
                    new Horse(null, 48, "2322", "AVAILABLE", null),
                    new Horse(null, 49, "2322", "AVAILABLE", null),
                    new Horse(null, 50, "2322", "AVAILABLE", null)
            );

            horseRepository.saveAll(horses);

            return "Import thành công! Đã import đầy đủ:\n" +
                    "- Phòng: 2322 (Admin: wild, Đang đua: true)\n" +
                    "- 50 con ngựa (Ngựa số 29 đã được chọn bởi wild)\n" +
                    "- Dữ liệu đầy đủ từ database local";
        } catch (Exception e) {
            return "Import thất bại: " + e.getMessage();
        }
    }
}

//