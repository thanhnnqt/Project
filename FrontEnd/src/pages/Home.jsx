import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Home.css";

export default function Home() {
    const [gameTypes, setGameTypes] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        api.get("/game-types")
            .then(res => {
                setGameTypes(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Lỗi khi tải thể loại game:", err);
                setLoading(false);
            });
    }, []);

    return (
        <div className="home-container">
            {/* HERO */}
            <section className="hero">
                <h1>
                    Chào mừng đến với <span>Royal Cards</span>
                </h1>
                <p>
                    Chọn trò chơi của bạn và bắt đầu hành trình chinh phục đỉnh cao ngay hôm nay!
                </p>
            </section>

            {/* GAME SELECTION */}
            <section className="game-selection" style={{ padding: "40px 20px", maxWidth: "1200px", margin: "0 auto" }}>
                <h2 style={{ color: "#fff", marginBottom: "30px", textAlign: "center", fontSize: "32px" }}>Chọn thể loại game</h2>

                {loading ? (
                    <div style={{ color: "#9ca3af", textAlign: "center" }}>Đang tải thể loại game...</div>
                ) : (
                    <div style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
                        gap: "25px"
                    }}>
                        {gameTypes.map(game => (
                            <div
                                key={game.id}
                                className="game-card"
                                onClick={() => navigate(`/lobby/${game.id}`)}
                                style={{
                                    background: "rgba(31, 41, 55, 0.8)",
                                    borderRadius: "20px",
                                    padding: "30px",
                                    textAlign: "center",
                                    cursor: "pointer",
                                    border: "1px solid rgba(255, 255, 255, 0.1)",
                                    transition: "all 0.3s ease",
                                    display: "flex",
                                    flexDirection: "column",
                                    alignItems: "center"
                                }}
                            >
                                <div style={{
                                    width: "80px",
                                    height: "80px",
                                    background: "rgba(251, 191, 36, 0.1)",
                                    borderRadius: "50%",
                                    display: "flex",
                                    justifyContent: "center",
                                    alignItems: "center",
                                    marginBottom: "20px",
                                    fontSize: "40px"
                                }}>
                                    ♠️
                                </div>
                                <h3 style={{ color: "#fff", fontSize: "22px", marginBottom: "12px" }}>{game.name}</h3>
                                <p style={{ color: "#9ca3af", fontSize: "14px", lineHeight: "1.5" }}>
                                    {game.minPlayers}-{game.maxPlayers} người chơi. Tham gia ngay để thể hiện kỹ năng của bạn!
                                </p>
                                <button style={{
                                    marginTop: "20px",
                                    padding: "10px 24px",
                                    background: "#fbbf24",
                                    color: "#111827",
                                    border: "none",
                                    borderRadius: "8px",
                                    fontWeight: "700",
                                    cursor: "pointer"
                                }}>
                                    Chọn Bàn
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* STATS */}
            <section className="stats" style={{ marginTop: "60px" }}>
                <div className="stat-box">
                    <h2>1,234</h2>
                    <p>Người chơi online</p>
                </div>

                <div className="stat-box">
                    <h2>567</h2>
                    <p>Phòng đang chơi</p>
                </div>

                <div className="stat-box">
                    <h2>10K+</h2>
                    <p>Người dùng</p>
                </div>
            </section>
        </div>
    );
}
