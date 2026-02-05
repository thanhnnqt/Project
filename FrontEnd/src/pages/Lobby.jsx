import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import toast from 'react-hot-toast';
import api from "../api/axios";
import CreateRoomModal from "../components/CreateRoomModal";
import PasswordModal from "../components/PasswordModal";

export default function Lobby() {
    const nav = useNavigate();
    const { gameTypeId } = useParams();
    const [rooms, setRooms] = useState([]);
    const [loading, setLoading] = useState(true);

    // Modal states
    const [isCreateModalOpen, setCreateModalOpen] = useState(false);
    const [isPasswordModalOpen, setPasswordModalOpen] = useState(false);
    const [selectedRoom, setSelectedRoom] = useState(null);

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

    const handleCreateRoom = async (roomData) => {
        try {
            const res = await api.post("/rooms", {
                ...roomData,
                gameTypeId: parseInt(gameTypeId),
                hostId: user.id
            });
            setCreateModalOpen(false);
            nav(`/room/${res.data.id}`);
        } catch (err) {
            toast.error("Lỗi khi tạo phòng: " + (err.response?.data || err.message));
        }
    };

    const handleJoinClick = (room) => {
        if (room.hasPassword) {
            setSelectedRoom(room);
            setPasswordModalOpen(true);
        } else {
            nav(`/room/${room.id}`);
        }
    };

    const handlePasswordConfirm = async (password) => {
        try {
            await api.post(`/rooms/${selectedRoom.id}/verify-password?password=${password}`);
            setPasswordModalOpen(false);
            nav(`/room/${selectedRoom.id}`);
        } catch (err) {
            toast.error(err.response?.data || "Mật khẩu không chính xác");
        }
    };

    if (loading) return <div style={{ padding: "100px", textAlign: "center", color: "#9ca3af" }}>Đang tải danh sách phòng...</div>;

    return (
        <div style={{ padding: "40px 20px", maxWidth: "1200px", margin: "0 auto" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "32px" }}>
                <div>
                    <h2 style={{ margin: 0, color: "#fff", fontSize: "28px", fontWeight: "800" }}>Sảnh Chờ</h2>
                    <p style={{ color: "#9ca3af", margin: "4px 0 0 0" }}>Chọn một phòng để bắt đầu sát phạt</p>
                </div>
                <button
                    onClick={() => setCreateModalOpen(true)}
                    style={{
                        background: "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)",
                        color: "#000",
                        border: "none",
                        padding: "14px 28px",
                        borderRadius: "12px",
                        cursor: "pointer",
                        fontWeight: "bold",
                        boxShadow: "0 4px 15px rgba(245, 158, 11, 0.3)",
                        transition: "transform 0.2s"
                    }}
                    onMouseOver={e => e.currentTarget.style.transform = 'scale(1.05)'}
                    onMouseOut={e => e.currentTarget.style.transform = 'scale(1)'}
                >
                    + Tạo phòng mới
                </button>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))", gap: "24px" }}>
                {rooms.length > 0 ? (
                    rooms.map(r => (
                        <div key={r.id} style={{
                            background: "rgba(31, 41, 55, 0.6)",
                            backdropFilter: "blur(12px)",
                            padding: "24px",
                            borderRadius: "16px",
                            border: "1px solid rgba(255, 255, 255, 0.05)",
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            transition: "all 0.3s ease"
                        }}>
                            <div>
                                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px" }}>
                                    <h3 style={{ color: "#fbbf24", margin: 0, fontSize: "20px" }}>{r.name}</h3>
                                    {r.hasPassword && (
                                        <div style={{
                                            background: "rgba(251, 191, 36, 0.15)",
                                            padding: "5px",
                                            borderRadius: "8px",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            border: "1px solid rgba(251, 191, 36, 0.3)"
                                        }}>
                                            <svg
                                                width="16" height="16"
                                                viewBox="0 0 24 24"
                                                fill="none"
                                                stroke="#fbbf24"
                                                strokeWidth="3"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                title="Phòng riêng tư"
                                            >
                                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" fill="rgba(251, 191, 36, 0.2)"></rect>
                                                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                                            </svg>
                                        </div>
                                    )}
                                </div>
                                <div style={{
                                    marginTop: "12px",
                                    fontSize: "13px",
                                    background: "rgba(0,0,0,0.2)",
                                    padding: "4px 10px",
                                    borderRadius: "20px",
                                    display: "inline-block",
                                    color: r.currentPlayerCount >= r.maxPlayers ? "#ef4444" : "#10b981",
                                    fontWeight: "700"
                                }}>
                                    👤 {r.currentPlayerCount}/{r.maxPlayers}
                                </div>
                            </div>
                            <button
                                onClick={() => handleJoinClick(r)}
                                disabled={r.currentPlayerCount >= r.maxPlayers}
                                style={{
                                    background: r.currentPlayerCount >= r.maxPlayers ? "#374151" : "#ef4444",
                                    color: "white",
                                    border: "none",
                                    padding: "12px 24px",
                                    borderRadius: "12px",
                                    cursor: r.currentPlayerCount >= r.maxPlayers ? "not-allowed" : "pointer",
                                    fontWeight: "bold",
                                    boxShadow: r.currentPlayerCount >= r.maxPlayers ? "none" : "0 4px 12px rgba(239, 68, 68, 0.3)"
                                }}
                            >
                                {r.currentPlayerCount >= r.maxPlayers ? "Đầy" : "Vào chơi"}
                            </button>
                        </div>
                    ))
                ) : (
                    <div style={{ color: "#9ca3af", gridColumn: "1/-1", textAlign: "center", padding: "40px" }}>
                        Hiện chưa có phòng nào khả dụng. Hãy tạo phòng ngay!
                    </div>
                )}
            </div>

            <CreateRoomModal
                isOpen={isCreateModalOpen}
                onClose={() => setCreateModalOpen(false)}
                onCreate={handleCreateRoom}
            />

            <PasswordModal
                isOpen={isPasswordModalOpen}
                onClose={() => setPasswordModalOpen(false)}
                roomName={selectedRoom?.name}
                onConfirm={handlePasswordConfirm}
            />
        </div>
    );
}
