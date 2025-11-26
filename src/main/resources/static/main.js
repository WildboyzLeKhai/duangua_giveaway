const API_URL = 'http://localhost:8080/api';
let stompClient = null;

// State
let myName = "";
let myRoomId = "";
let roomAdmin = "";
let currentHorses = [];
let mySelectedHorseId = null; // Theo dõi xem mình đang chọn con nào

// --- 1. LOBBY LOGIC ---

function createRoom() {
    myName = document.getElementById('playerName').value.trim();
    if (!myName) return alert("Vui lòng nhập tên!");

    fetch(`${API_URL}/create-room`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ adminName: myName })
    })
        .then(res => res.json())
        .then(roomInfo => enterGameRoom(roomInfo))
        .catch(e => alert("Lỗi tạo phòng: " + e));
}

function joinRoom() {
    myName = document.getElementById('playerName').value.trim();
    const roomId = document.getElementById('roomIdInput').value.trim();
    if (!myName || !roomId) return alert("Nhập đủ Tên và Mã phòng!");

    fetch(`${API_URL}/room/${roomId}`)
        .then(res => {
            if(!res.ok) throw new Error("Phòng không tồn tại!");
            return res.json();
        })
        .then(roomInfo => enterGameRoom(roomInfo))
        .catch(e => alert(e.message));
}

function enterGameRoom(roomInfo) {
    myRoomId = roomInfo.roomId;
    roomAdmin = roomInfo.adminName;
    currentHorses = roomInfo.horses;

    document.getElementById('lobby-view').classList.add('hidden');
    document.getElementById('game-view').classList.remove('hidden');

    document.getElementById('displayRoomId').innerText = myRoomId;
    document.getElementById('displayAdmin').innerText = roomAdmin;
    document.getElementById('displayMe').innerText = myName;

    // Hiển thị nút Deselect
    const deselectBtn = document.createElement('button');
    deselectBtn.innerText = "❌ Hủy chọn ngựa";
    deselectBtn.className = "btn btn-red";
    deselectBtn.style.marginLeft = "10px";
    deselectBtn.onclick = deselectHorse;
    document.getElementById('selection-area').querySelector('h3').appendChild(deselectBtn);

    if (myName === roomAdmin) {
        document.getElementById('admin-panel').classList.remove('hidden');
    }

    renderGrid();
    connectSocket(myRoomId);
}

// --- 2. SOCKET LOGIC ---

function connectSocket(roomId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function (frame) {
        console.log('Connected to Room: ' + roomId);

        // 1. Horse Update (Taken OR Released)
        stompClient.subscribe(`/topic/room/${roomId}/horse.update`, function (msg) {
            updateHorse(JSON.parse(msg.body));
        });

        // 2. Race Result
        stompClient.subscribe(`/topic/room/${roomId}/race.result`, function (msg) {
            startAnimation(JSON.parse(msg.body));
        });

        // 3. Reset
        stompClient.subscribe(`/topic/room/${roomId}/game.reset`, function () {
            alert("Admin đã reset game!");
            fetch(`${API_URL}/room/${myRoomId}`)
                .then(r => r.json())
                .then(info => {
                    currentHorses = info.horses;
                    mySelectedHorseId = null;
                    document.getElementById('track-area').classList.add('hidden');
                    document.getElementById('selection-area').classList.remove('hidden');
                    renderGrid();
                });
        });
    });
}

// --- 3. GAMEPLAY LOGIC ---

function selectHorse(number) {
    fetch(`${API_URL}/select`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            roomId: myRoomId,
            horseNumber: number,
            ownerName: myName
        })
    }).then(async res => {
        if(!res.ok) alert(await res.text());
        else mySelectedHorseId = number;
    });
}

function deselectHorse() {
    fetch(`${API_URL}/deselect`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ roomId: myRoomId, ownerName: myName })
    }).then(async res => {
        if(!res.ok) alert(await res.text());
        else mySelectedHorseId = null;
    });
}

function startRace() {
    const duration = document.getElementById('raceDuration').value;
    fetch(`${API_URL}/start`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            roomId: myRoomId,
            adminName: myName,
            durationSeconds: parseInt(duration) || 10
        })
    }).then(async res => {
        if(!res.ok) alert(await res.text());
    });
}

function resetGame() {
    if(confirm("Reset game?")) {
        fetch(`${API_URL}/reset`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ roomId: myRoomId, adminName: myName })
        });
    }
}

// --- 4. RENDER & ANIMATION ---

function renderGrid() {
    const grid = document.getElementById('horseGrid');
    grid.innerHTML = '';
    currentHorses.sort((a,b) => a.horseNumber - b.horseNumber);

    currentHorses.forEach(h => {
        const div = document.createElement('div');
        // Nếu status = TAKEN thì đỏ, AVAILABLE thì xanh
        div.className = `horse-card ${h.status === 'TAKEN' ? 'taken' : ''}`;

        // Highlight ngựa của mình
        if (h.ownerName === myName) {
            div.style.border = "4px solid #f1c40f"; // Viền vàng cho ngựa của mình
            mySelectedHorseId = h.horseNumber;
        }

        div.id = `horse-${h.horseNumber}`;
        div.innerHTML = `
            <div class="horse-emoji">🐎</div>
            <div class="horse-id">#${h.horseNumber}</div>
            <div class="horse-owner">${h.ownerName || '-'}</div>
        `;
        div.onclick = () => {
            // Cho phép click để chọn (backend tự xử lý việc đổi ngựa)
            if (h.ownerName !== myName) selectHorse(h.horseNumber);
        };
        grid.appendChild(div);
    });
}

function updateHorse(horse) {
    const idx = currentHorses.findIndex(h => h.horseNumber === horse.horseNumber);
    if(idx !== -1) currentHorses[idx] = horse;

    const el = document.getElementById(`horse-${horse.horseNumber}`);
    if(el) {
        // Cập nhật class dựa trên status mới
        if (horse.status === 'TAKEN') {
            el.className = 'horse-card taken';
            el.querySelector('.horse-owner').innerText = horse.ownerName;

            // Highlight nếu là mình chọn
            if (horse.ownerName === myName) {
                el.style.border = "4px solid #f1c40f";
                mySelectedHorseId = horse.horseNumber;
            } else {
                el.style.border = "2px solid #ddd"; // Reset border nếu người khác chọn
            }
        } else {
            // Trường hợp RELEASED (về AVAILABLE)
            el.className = 'horse-card'; // Mặc định là xanh (do CSS)
            el.querySelector('.horse-owner').innerText = '-';
            el.style.border = "2px solid #ddd";

            if (mySelectedHorseId === horse.horseNumber) mySelectedHorseId = null;
        }
    }
}

function startAnimation(result) {
    document.getElementById('selection-area').classList.add('hidden');
    document.getElementById('track-area').classList.remove('hidden');
    const container = document.getElementById('trackContainer');
    container.innerHTML = '';

    // LOGIC MỚI: Chỉ lấy những con ngựa TAKEN để đua
    let racers = currentHorses.filter(h => h.status === 'TAKEN');
    racers.sort((a,b) => a.horseNumber - b.horseNumber);

    if (racers.length === 0) return alert("Lỗi: Không có ngựa nào tham gia!");

    racers.forEach(h => {
        const lane = document.createElement('div');
        lane.className = 'track-lane';

        // Highlight lane của mình
        if (h.ownerName === myName) lane.style.backgroundColor = "#2c3e50";

        lane.innerHTML = `
            <div class="finish-line"></div>
            <div class="racer" id="racer-${h.horseNumber}" style="left:0%">
                🐎 <span style="color:white; font-size:12px">${h.horseNumber} (${h.ownerName})</span>
            </div>
        `;
        container.appendChild(lane);
    });

    setTimeout(() => {
        racers.forEach(h => {
            const el = document.getElementById(`racer-${h.horseNumber}`);
            const isWin = h.horseNumber === result.winnerNumber;
            const dist = isWin ? 90 : (50 + Math.random() * 30);
            el.style.transition = `left ${result.durationSeconds}s cubic-bezier(0.25, 0.1, 0.25, 1)`;
            el.style.left = `${dist}%`;
        });

        setTimeout(() => {
            document.getElementById('winnerNumber').innerText = result.winnerNumber;
            document.getElementById('winnerInfo').innerText = result.winnerName;
            document.getElementById('winnerModal').classList.remove('hidden');
        }, (result.durationSeconds * 1000) + 500);

    }, 100);
}

function closeModal() {
    document.getElementById('winnerModal').classList.add('hidden');
}