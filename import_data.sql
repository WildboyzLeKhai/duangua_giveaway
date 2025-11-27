<<<<<<< HEAD
-- --------------------------------------------------------
-- Import dữ liệu Horse Racing Game
-- Database: railway
-- --------------------------------------------------------

-- Xóa tables cũ nếu có
DROP TABLE IF EXISTS horses;
DROP TABLE IF EXISTS rooms;

-- Tạo table rooms
CREATE TABLE IF NOT EXISTS `rooms` (
    `is_racing` bit(1) NOT NULL,
    `admin_name` varchar(255) DEFAULT NULL,
    `room_id` varchar(255) NOT NULL,
    PRIMARY KEY (`room_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Tạo table horses
CREATE TABLE IF NOT EXISTS `horses` (
    `horse_number` int(11) NOT NULL,
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `owner_name` varchar(255) DEFAULT NULL,
    `room_id` varchar(255) DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- --------------------------------------------------------
-- INSERT DỮ LIỆU ROOMS
-- --------------------------------------------------------
INSERT INTO `rooms` (`is_racing`, `admin_name`, `room_id`) VALUES
    (b'1', 'wild', '2322');

-- --------------------------------------------------------
-- INSERT DỮ LIỆU HORSES - ĐẦY ĐỦ 50 CON NGỰA
-- --------------------------------------------------------
INSERT INTO `horses` (`horse_number`, `id`, `owner_name`, `room_id`, `status`) VALUES
                                                                                   (1, 1, NULL, '2322', 'AVAILABLE'),
                                                                                   (2, 2, NULL, '2322', 'AVAILABLE'),
                                                                                   (3, 3, NULL, '2322', 'AVAILABLE'),
                                                                                   (4, 4, NULL, '2322', 'AVAILABLE'),
                                                                                   (5, 5, NULL, '2322', 'AVAILABLE'),
                                                                                   (6, 6, NULL, '2322', 'AVAILABLE'),
                                                                                   (7, 7, NULL, '2322', 'AVAILABLE'),
                                                                                   (8, 8, NULL, '2322', 'AVAILABLE'),
                                                                                   (9, 9, NULL, '2322', 'AVAILABLE'),
                                                                                   (10, 10, NULL, '2322', 'AVAILABLE'),
                                                                                   (11, 11, NULL, '2322', 'AVAILABLE'),
                                                                                   (12, 12, NULL, '2322', 'AVAILABLE'),
                                                                                   (13, 13, NULL, '2322', 'AVAILABLE'),
                                                                                   (14, 14, NULL, '2322', 'AVAILABLE'),
                                                                                   (15, 15, NULL, '2322', 'AVAILABLE'),
                                                                                   (16, 16, NULL, '2322', 'AVAILABLE'),
                                                                                   (17, 17, NULL, '2322', 'AVAILABLE'),
                                                                                   (18, 18, NULL, '2322', 'AVAILABLE'),
                                                                                   (19, 19, NULL, '2322', 'AVAILABLE'),
                                                                                   (20, 20, NULL, '2322', 'AVAILABLE'),
                                                                                   (21, 21, NULL, '2322', 'AVAILABLE'),
                                                                                   (22, 22, NULL, '2322', 'AVAILABLE'),
                                                                                   (23, 23, NULL, '2322', 'AVAILABLE'),
                                                                                   (24, 24, NULL, '2322', 'AVAILABLE'),
                                                                                   (25, 25, NULL, '2322', 'AVAILABLE'),
                                                                                   (26, 26, NULL, '2322', 'AVAILABLE'),
                                                                                   (27, 27, NULL, '2322', 'AVAILABLE'),
                                                                                   (28, 28, NULL, '2322', 'AVAILABLE'),
                                                                                   (29, 29, 'wild', '2322', 'TAKEN'),
                                                                                   (30, 30, NULL, '2322', 'AVAILABLE'),
                                                                                   (31, 31, NULL, '2322', 'AVAILABLE'),
                                                                                   (32, 32, NULL, '2322', 'AVAILABLE'),
                                                                                   (33, 33, NULL, '2322', 'AVAILABLE'),
                                                                                   (34, 34, NULL, '2322', 'AVAILABLE'),
                                                                                   (35, 35, NULL, '2322', 'AVAILABLE'),
                                                                                   (36, 36, NULL, '2322', 'AVAILABLE'),
                                                                                   (37, 37, NULL, '2322', 'AVAILABLE'),
                                                                                   (38, 38, NULL, '2322', 'AVAILABLE'),
                                                                                   (39, 39, NULL, '2322', 'AVAILABLE'),
                                                                                   (40, 40, NULL, '2322', 'AVAILABLE'),
                                                                                   (41, 41, NULL, '2322', 'AVAILABLE'),
                                                                                   (42, 42, NULL, '2322', 'AVAILABLE'),
                                                                                   (43, 43, NULL, '2322', 'AVAILABLE'),
                                                                                   (44, 44, NULL, '2322', 'AVAILABLE'),
                                                                                   (45, 45, NULL, '2322', 'AVAILABLE'),
                                                                                   (46, 46, NULL, '2322', 'AVAILABLE'),
                                                                                   (47, 47, NULL, '2322', 'AVAILABLE'),
                                                                                   (48, 48, NULL, '2322', 'AVAILABLE'),
                                                                                   (49, 49, NULL, '2322', 'AVAILABLE'),
                                                                                   (50, 50, NULL, '2322', 'AVAILABLE');

-- --------------------------------------------------------
-- KIỂM TRA DỮ LIỆU
-- --------------------------------------------------------
SELECT 'Rooms count:' as 'Check', COUNT(*) as Count FROM rooms
UNION ALL
=======
-- --------------------------------------------------------
-- Import dữ liệu Horse Racing Game
-- Database: railway
-- --------------------------------------------------------

-- Xóa tables cũ nếu có
DROP TABLE IF EXISTS horses;
DROP TABLE IF EXISTS rooms;

-- Tạo table rooms
CREATE TABLE IF NOT EXISTS `rooms` (
    `is_racing` bit(1) NOT NULL,
    `admin_name` varchar(255) DEFAULT NULL,
    `room_id` varchar(255) NOT NULL,
    PRIMARY KEY (`room_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Tạo table horses
CREATE TABLE IF NOT EXISTS `horses` (
    `horse_number` int(11) NOT NULL,
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `owner_name` varchar(255) DEFAULT NULL,
    `room_id` varchar(255) DEFAULT NULL,
    `status` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- --------------------------------------------------------
-- INSERT DỮ LIỆU ROOMS
-- --------------------------------------------------------
INSERT INTO `rooms` (`is_racing`, `admin_name`, `room_id`) VALUES
    (b'1', 'wild', '2322');

-- --------------------------------------------------------
-- INSERT DỮ LIỆU HORSES - ĐẦY ĐỦ 50 CON NGỰA
-- --------------------------------------------------------
INSERT INTO `horses` (`horse_number`, `id`, `owner_name`, `room_id`, `status`) VALUES
                                                                                   (1, 1, NULL, '2322', 'AVAILABLE'),
                                                                                   (2, 2, NULL, '2322', 'AVAILABLE'),
                                                                                   (3, 3, NULL, '2322', 'AVAILABLE'),
                                                                                   (4, 4, NULL, '2322', 'AVAILABLE'),
                                                                                   (5, 5, NULL, '2322', 'AVAILABLE'),
                                                                                   (6, 6, NULL, '2322', 'AVAILABLE'),
                                                                                   (7, 7, NULL, '2322', 'AVAILABLE'),
                                                                                   (8, 8, NULL, '2322', 'AVAILABLE'),
                                                                                   (9, 9, NULL, '2322', 'AVAILABLE'),
                                                                                   (10, 10, NULL, '2322', 'AVAILABLE'),
                                                                                   (11, 11, NULL, '2322', 'AVAILABLE'),
                                                                                   (12, 12, NULL, '2322', 'AVAILABLE'),
                                                                                   (13, 13, NULL, '2322', 'AVAILABLE'),
                                                                                   (14, 14, NULL, '2322', 'AVAILABLE'),
                                                                                   (15, 15, NULL, '2322', 'AVAILABLE'),
                                                                                   (16, 16, NULL, '2322', 'AVAILABLE'),
                                                                                   (17, 17, NULL, '2322', 'AVAILABLE'),
                                                                                   (18, 18, NULL, '2322', 'AVAILABLE'),
                                                                                   (19, 19, NULL, '2322', 'AVAILABLE'),
                                                                                   (20, 20, NULL, '2322', 'AVAILABLE'),
                                                                                   (21, 21, NULL, '2322', 'AVAILABLE'),
                                                                                   (22, 22, NULL, '2322', 'AVAILABLE'),
                                                                                   (23, 23, NULL, '2322', 'AVAILABLE'),
                                                                                   (24, 24, NULL, '2322', 'AVAILABLE'),
                                                                                   (25, 25, NULL, '2322', 'AVAILABLE'),
                                                                                   (26, 26, NULL, '2322', 'AVAILABLE'),
                                                                                   (27, 27, NULL, '2322', 'AVAILABLE'),
                                                                                   (28, 28, NULL, '2322', 'AVAILABLE'),
                                                                                   (29, 29, 'wild', '2322', 'TAKEN'),
                                                                                   (30, 30, NULL, '2322', 'AVAILABLE'),
                                                                                   (31, 31, NULL, '2322', 'AVAILABLE'),
                                                                                   (32, 32, NULL, '2322', 'AVAILABLE'),
                                                                                   (33, 33, NULL, '2322', 'AVAILABLE'),
                                                                                   (34, 34, NULL, '2322', 'AVAILABLE'),
                                                                                   (35, 35, NULL, '2322', 'AVAILABLE'),
                                                                                   (36, 36, NULL, '2322', 'AVAILABLE'),
                                                                                   (37, 37, NULL, '2322', 'AVAILABLE'),
                                                                                   (38, 38, NULL, '2322', 'AVAILABLE'),
                                                                                   (39, 39, NULL, '2322', 'AVAILABLE'),
                                                                                   (40, 40, NULL, '2322', 'AVAILABLE'),
                                                                                   (41, 41, NULL, '2322', 'AVAILABLE'),
                                                                                   (42, 42, NULL, '2322', 'AVAILABLE'),
                                                                                   (43, 43, NULL, '2322', 'AVAILABLE'),
                                                                                   (44, 44, NULL, '2322', 'AVAILABLE'),
                                                                                   (45, 45, NULL, '2322', 'AVAILABLE'),
                                                                                   (46, 46, NULL, '2322', 'AVAILABLE'),
                                                                                   (47, 47, NULL, '2322', 'AVAILABLE'),
                                                                                   (48, 48, NULL, '2322', 'AVAILABLE'),
                                                                                   (49, 49, NULL, '2322', 'AVAILABLE'),
                                                                                   (50, 50, NULL, '2322', 'AVAILABLE');

-- --------------------------------------------------------
-- KIỂM TRA DỮ LIỆU
-- --------------------------------------------------------
SELECT 'Rooms count:' as 'Check', COUNT(*) as Count FROM rooms
UNION ALL
>>>>>>> 5cd32bf6ad52b4f6f2ccf65694ff4b865c7119dd
SELECT 'Horses count:', COUNT(*) FROM horses;