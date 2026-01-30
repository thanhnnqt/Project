import { useNavigate, useParams } from "react-router-dom";
import { useState, useEffect, useRef } from "react";
import PlayerAvatar from "./PlayerAvatar.jsx";
import { useGameSocket } from "../hooks/useGameSocket";
import api from "../api/axios";
import "./Table.css";

const getCardImage = (card) => {
    if (!card) return "/cards/back.png";
    const rankMapping = {
        "THREE": "3", "FOUR": "4", "FIVE": "5", "SIX": "6", "SEVEN": "7",
        "EIGHT": "8", "NINE": "9", "TEN": "10",
        "JACK": "jack", "QUEEN": "queen", "KING": "king", "ACE": "ace", "TWO": "2"
    };
    const suitMapping = {
        "SPADE": "spades", "CLUB": "clubs", "DIAMOND": "diamonds", "HEART": "hearts"
    };
    return `/cards/${rankMapping[card.rank]}_of_${suitMapping[card.suit]}.png`;
};

export default function Table() {
    const nav = useNavigate();
    const { id } = useParams();
    const roomId = id;

    const userString = localStorage.getItem("user");
    const user = userString ? JSON.parse(userString) : null;
    const playerId = user?.id;

    useEffect(() => {
        if (!user) {
            alert("Vui lòng đăng nhập để vào phòng chơi");
            nav("/login");
        }
    }, [user, nav]);

    const {
        gameState, error, startGame, toggleReady, resetRoom, kickPlayer, playMove, passTurn, leaveRoom, sendChat, sendEmoji, setError
    } = useGameSocket(roomId, playerId);

    const [selectedCards, setSelectedCards] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [showEmojiPicker, setShowEmojiPicker] = useState(false);
    const [interactions, setInteractions] = useState({});
    const [chatHistory, setChatHistory] = useState([]);
    const chatEndRef = useRef(null);

    const scrollToBottom = () => {
        chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (chatHistory.length > 0) {
            scrollToBottom();
        }
    }, [chatHistory]);

    // Handle incoming interactions (Chat/Emoji)
    useEffect(() => {
        if (gameState?.lastInteraction) {
            const { type, playerId: pId, content, timestamp } = gameState.lastInteraction;
            setInteractions(prev => ({
                ...prev,
                [pId]: { type, content, ts: timestamp || Date.now() }
            }));

            if (type === 'CHAT') {
                setChatHistory(prev => [...prev, {
                    sender: gameState.displayNames[pId] || "Người chơi",
                    content,
                    timestamp: timestamp || Date.now()
                }].slice(-50));
            }
        }
    }, [gameState?.lastInteraction, gameState?.displayNames]);

    // Profile update on win
    useEffect(() => {
        if (gameState?.winnerId && user) {
            api.get(`/api/auth/profile/${user.id}`).then(res => {
                localStorage.setItem("user", JSON.stringify(res.data));
                window.dispatchEvent(new Event("storage"));
            });
        }
    }, [gameState?.winnerId]);

    if (!gameState) return <div className="loading-casino">Vào bàn...</div>;

    const myHand = gameState.hands[playerId] || [];
    const isMyTurn = gameState.currentPlayerId === playerId;

    const handleCardClick = (card) => {
        if (selectedCards.some(sc => JSON.stringify(sc) === JSON.stringify(card))) {
            setSelectedCards(selectedCards.filter(c => JSON.stringify(c) !== JSON.stringify(card)));
        } else {
            setSelectedCards([...selectedCards, card]);
        }
    };

    const handlePlay = () => {
        if (selectedCards.length === 0) return;
        playMove(selectedCards);
        setSelectedCards([]);
    };

    const handleChatSubmit = (e) => {
        e.preventDefault();
        const msg = chatInput.trim();
        if (!msg) return;
        sendChat(msg);
        setChatInput("");
    };

    const emojis = ["👍", "❤️", "😂", "😡", "😮", "😴", "🔥", "🤡"];

    const others = gameState.playerIds.filter(id => id !== playerId);
    const getPlayerByPos = (pos) => {
        if (pos >= others.length) return null;
        const id = others[pos];
        return (
            <PlayerAvatar
                playerId={id}
                displayName={gameState.displayNames[id]}
                rankTier={gameState.rankTiers[id]}
                rankPoints={gameState.rankPoints[id]}
                isHost={gameState.hostId === id}
                isReady={gameState.readyPlayers.includes(id)}
                isTurn={gameState.currentPlayerId === id}
                handSize={gameState.hands[id]?.length || 0}
                message={interactions[id]?.type === 'CHAT' ? interactions[id].content : ''}
                emoji={interactions[id]?.type === 'EMOJI' ? interactions[id].content : ''}
                emojiTimestamp={interactions[id]?.ts}
                onKick={gameState.hostId === playerId ? kickPlayer : null}
            />
        );
    };

    return (
        <div className="casino-table-wrapper">
            {/* Header */}
            <div className="casino-top-bar">
                <button className="top-btn" onClick={() => { if (window.confirm("Thoát?")) { leaveRoom(); nav("/"); } }}>❮</button>
                <div className="room-title">TIẾN LÊN [Bàn {roomId}]</div>
                <button className="top-btn">⚙️</button>
            </div>

            <div className="casino-main-scene">
                {/* Oval Table */}
                <div className="oval-table">
                    <div className="table-inner-glow"></div>
                    <div className="table-logo"><div className="logo-text">TIẾN LÊN</div></div>
                    <div className="table-center-cards">
                        {gameState.tableCards.length > 0 ? (
                            <div className="played-cards-stack">
                                {gameState.tableCards.map((card, i) => (
                                    <img key={i} src={getCardImage(card)} alt="c" className="played-card-img" />
                                ))}
                            </div>
                        ) : (
                            <div className="invite-empty">Mời chơi</div>
                        )}
                    </div>
                </div>

                {/* Player Positions */}
                <div className="p-top">{getPlayerByPos(1)}</div>
                <div className="p-left">{getPlayerByPos(0)}</div>
                <div className="p-right">{getPlayerByPos(2)}</div>

                {/* My Area */}
                <div className="my-area-casino">
                    <div className="casino-actions-3d">
                        {!gameState.gameStarted ? (
                            <div className="ready-start-box">
                                {gameState.hostId === playerId ? (
                                    <button className="btn-3d btn-gold" onClick={startGame} disabled={gameState.playerIds.length < 2}>BẮT ĐẦU</button>
                                ) : (
                                    <button className={`btn-3d ${gameState.readyPlayers.includes(playerId) ? 'btn-blue' : 'btn-green'}`} onClick={toggleReady}>
                                        {gameState.readyPlayers.includes(playerId) ? 'ĐÃ SẴN SÀNG' : 'SẴN SÀNG'}
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="game-action-row">
                                <button className="btn-3d btn-gold-small" onClick={() => setSelectedCards([])} disabled={selectedCards.length === 0}>BỎ CHỌN</button>
                                <button className="btn-3d btn-gray-small" onClick={passTurn} disabled={!isMyTurn || gameState.tableCards.length === 0}>BỎ LƯỢT</button>
                                <button className="btn-3d btn-white-small" onClick={handlePlay} disabled={!isMyTurn || selectedCards.length === 0}>ĐÁNH BÀI</button>
                            </div>
                        )}
                    </div>

                    <div className="my-footer-row">
                        <div className="my-avatar-v">
                            <PlayerAvatar
                                playerId={playerId}
                                displayName={user?.displayName}
                                rankTier={user?.rankTier}
                                rankPoints={user?.rankPoints}
                                isHost={gameState.hostId === playerId}
                                isMe={true}
                                isTurn={isMyTurn}
                                message={interactions[playerId]?.type === 'CHAT' ? interactions[playerId].content : ''}
                                emoji={interactions[playerId]?.type === 'EMOJI' ? interactions[playerId].content : ''}
                                emojiTimestamp={interactions[playerId]?.ts}
                            />
                        </div>
                        <div className="my-hand-casino">
                            {myHand.map((card, i) => (
                                <img
                                    key={i}
                                    src={getCardImage(card)}
                                    alt="card"
                                    className={`card-v ${selectedCards.some(sc => JSON.stringify(sc) === JSON.stringify(card)) ? "sel" : ""}`}
                                    onClick={() => handleCardClick(card)}
                                />
                            ))}
                        </div>
                    </div>
                </div>

                {/* Interaction Panel */}
                <div className="casino-side-icons">
                    <div className="interaction-panel-casino">
                        <div className="chat-history-wrapper">
                            {chatHistory.map((chat, idx) => (
                                <div key={idx} className="chat-msg-item">
                                    <span className="chat-msg-sender">{chat.sender}:</span>
                                    <span>{chat.content}</span>
                                </div>
                            ))}
                            <div ref={chatEndRef} />
                        </div>
                        <div className="chat-input-row-casino">
                            <form onSubmit={handleChatSubmit}>
                                <input
                                    type="text"
                                    placeholder="Trò chuyện..."
                                    value={chatInput}
                                    onChange={(e) => setChatInput(e.target.value)}
                                />
                            </form>
                            <div className="emoji-trigger-btn" onClick={() => setShowEmojiPicker(!showEmojiPicker)}>😀</div>
                            {showEmojiPicker && (
                                <div className="casino-emoji-box">
                                    {emojis.map(e => (
                                        <span key={e} onClick={() => { sendEmoji(e); setShowEmojiPicker(false); }}>{e}</span>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Victory Overlay */}
                {gameState.winnerId && (
                    <div className="casino-end-overlay">
                        <div className="casino-end-card">
                            <h2>{gameState.winnerId === playerId ? "THẮNG LỚN!" : "THUA CUỘC"}</h2>
                            <p>{gameState.displayNames[gameState.winnerId]} đã chiến thắng</p>
                            {gameState.hostId === playerId && <button className="btn-3d btn-gold" onClick={resetRoom}>VÁN MỚI</button>}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
