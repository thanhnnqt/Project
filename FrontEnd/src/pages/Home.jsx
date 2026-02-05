import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Home.css";

export default function Home() {
    const [gameTypes, setGameTypes] = useState([]);
    const [leaderboard, setLeaderboard] = useState([]);
    const [loading, setLoading] = useState(true);
    const [leaderboardLoading, setLeaderboardLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        // Fetch game types
        api.get("/game-types")
            .then(res => {
                setGameTypes(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Lỗi khi tải thể loại game:", err);
                setLoading(false);
            });

        // Fetch leaderboard
        api.get("/leaderboard?limit=10")
            .then(res => {
                setLeaderboard(res.data);
                setLeaderboardLoading(false);
            })
            .catch(err => {
                console.error("Lỗi khi tải bảng xếp hạng:", err);
                setLeaderboardLoading(false);
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

            {/* MAIN CONTENT - TWO COLUMNS */}
            <section className="content-grid">
                {/* LEFT COLUMN - GAME SELECTION */}
                <div className="game-selection-section">
                    <h2>Chọn thể loại game</h2>

                    {loading ? (
                        <div className="loading-text">Đang tải thể loại game...</div>
                    ) : (
                        <div className="game-list">
                            {gameTypes.map(game => (
                                <div
                                    key={game.id}
                                    className="game-list-item"
                                    onClick={() => navigate(`/lobby/${game.id}`)}
                                >
                                    <div className="game-list-icon">♠️</div>
                                    <div className="game-list-info">
                                        <h3>{game.name}</h3>
                                        <p>{game.minPlayers}-{game.maxPlayers} người chơi</p>
                                    </div>
                                    <button className="game-list-btn">Vào Chơi →</button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* RIGHT COLUMN - LEADERBOARD */}
                <div className="leaderboard-section">
                    <h2>🏆 Bảng Xếp Hạng</h2>

                    {leaderboardLoading ? (
                        <div className="loading-text">Đang tải bảng xếp hạng...</div>
                    ) : (
                        <div className="leaderboard-list">
                            {leaderboard.map((player, index) => (
                                <div key={player.id} className="leaderboard-item">
                                    <div className="rank-badge">
                                        {index === 0 && "🥇"}
                                        {index === 1 && "🥈"}
                                        {index === 2 && "🥉"}
                                        {index > 2 && `#${index + 1}`}
                                    </div>
                                    <div className="player-info">
                                        <div className="player-name">{player.displayName || player.username}</div>
                                        <div className="player-tier">{player.rankTier}</div>
                                    </div>
                                    <div className="player-points">{player.rankPoints} điểm</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}
