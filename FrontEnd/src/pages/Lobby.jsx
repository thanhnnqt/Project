import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../api/axios";

export default function Lobby() {
    const nav = useNavigate();
    const { gameTypeId } = useParams();
    const [rooms, setRooms] = useState([]);
    const [loading, setLoading] = useState(true);

    const userString = localStorage.getItem("user");
    const user = userString ? JSON.parse(userString) : null;

    useEffect(() => {
        if (!user) {
            nav("/login");
            return;
        }

        const fetchRooms = () => {
            api.get(`/rooms?gameTypeId=${gameTypeId}`)
                .then(res => {
                    setRooms(res.data);
                    setLoading(false);
                })
                .catch(err => {
                    console.error("Lỗi khi tải phòng chơi:", err);
                    setLoading(false);
                });
        };

        fetchRooms();
        const interval = setInterval(fetchRooms, 3000); // Tự động làm mới danh sách sau 3s
        return () => clearInterval(interval);
    }, [gameTypeId, nav]);

    const handleCreateRoom = async () => {
        const roomName = prompt("Nhập tên phòng chơi:");
        if (!roomName) return;

        try {
            const res = await api.post("/rooms", {
                name: roomName,
                gameTypeId: parseInt(gameTypeId),
                minBet: 10, // Mặc định
                hostId: user.id
            });
            nav(`/room/${res.data.id}`);
        } catch (err) {
            alert("Lỗi khi tạo phòng: " + (err.response?.data || err.message));
        }
    };

    if (loading) return <div style={{ padding: "100px", textAlign: "center", color: "#9ca3af" }}>Đang tải danh sách phòng...</div>;

    return (
        <div style={{ padding: "40px 20px", maxWidth: "1200px", margin: "0 auto" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "24px" }}>
                <h2 style={{ margin: 0, color: "#fff" }}>Danh sách phòng chơi</h2>
                <button
                    onClick={handleCreateRoom}
                    style={{
                        background: "#fbbf24",
                        color: "#000",
                        border: "none",
                        padding: "12px 24px",
                        borderRadius: "8px",
                        cursor: "pointer",
                        fontWeight: "bold"
                    }}
                >
                    + Tạo phòng mới
                </button>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "20px" }}>
                {rooms.length > 0 ? (
                    rooms.map(r => (
                        <div key={r.id} style={{
                            background: "rgba(31, 41, 55, 0.8)",
                            padding: "24px",
                            borderRadius: "12px",
                            border: "1px solid rgba(255, 255, 255, 0.1)",
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            position: "relative"
                        }}>
                            <div>
                                <h3 style={{ color: "#fbbf24", margin: "0 0 8px 0" }}>{r.name}</h3>
                                <p style={{ color: "#9ca3af", fontSize: "14px", margin: 0 }}>
                                    Game: {r.gameTypeName}
                                </p>
                                <p style={{ color: "#9ca3af", fontSize: "14px", margin: "4px 0 0 0" }}>
                                    Cược: {r.minBet} Xu
                                </p>
                                <div style={{
                                    marginTop: "12px",
                                    fontSize: "13px",
                                    color: r.currentPlayerCount >= r.maxPlayers ? "#ef4444" : "#10b981",
                                    fontWeight: "600"
                                }}>
                                    Người chơi: {r.currentPlayerCount}/{r.maxPlayers}
                                </div>
                            </div>
                            <button
                                onClick={() => nav(`/room/${r.id}`)}
                                disabled={r.currentPlayerCount >= r.maxPlayers}
                                style={{
                                    background: r.currentPlayerCount >= r.maxPlayers ? "#4b5563" : "#ef4444",
                                    color: "white",
                                    border: "none",
                                    padding: "10px 20px",
                                    borderRadius: "8px",
                                    cursor: r.currentPlayerCount >= r.maxPlayers ? "not-allowed" : "pointer",
                                    fontWeight: "600"
                                }}
                            >
                                {r.currentPlayerCount >= r.maxPlayers ? "Đầy" : "Vào chơi"}
                            </button>
                        </div>
                    ))
                ) : (
                    <div style={{ color: "#9ca3af" }}>Hiện chưa có phòng nào khả dụng. Hãy tạo phòng ngay!</div>
                )}
            </div>
        </div>
    );
}
