import { useNavigate, useParams } from "react-router-dom";
import { useState, useEffect, useRef } from "react";
import toast from 'react-hot-toast';
import PlayerAvatar from "./PlayerAvatar.jsx";
import { useGameSocket } from "../hooks/useGameSocket";
import api from "../api/axios";
import "./Table.css";
import ConfirmModal from "../components/ConfirmModal";

const getCardImage = (card, playerId, gameState) => {
    // Determine the back of the card based on equipped skin
    const equippedSkin = playerId && gameState?.cardSkins?.[playerId];

    if (!card) {
        return "/cards/back.png"; // Fallback to default
    }

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

    // Modal states
    const [leaveConfirmOpen, setLeaveConfirmOpen] = useState(false);
    const [kickConfirm, setKickConfirm] = useState({ isOpen: false, targetId: null, targetName: "" });

    useEffect(() => {
        if (!user) {
            toast.error("Vui lòng đăng nhập để vào phòng chơi");
            nav("/login");
        }
    }, [user, nav]);

    // Refresh profile on mount to sync Navbar with latest rank
    useEffect(() => {
        if (user?.id) {
            api.get(`/auth/profile/${user.id}`).then(res => {
                localStorage.setItem("user", JSON.stringify(res.data));
                window.dispatchEvent(new Event("storage"));
            }).catch(err => {
                console.error("Failed to refresh profile:", err);
            });
        }
    }, [user?.id]);

    const {
        gameState, error, startGame, toggleReady, resetRoom, kickPlayer, playMove, passTurn, sendAction, leaveRoom, sendChat, sendEmoji, setError
    } = useGameSocket(roomId, playerId);

    // Show errors via toast
    useEffect(() => {
        if (error) {
            toast.error(error);
            setError(null);
        }
    }, [error, setError]);

    const [selectedCards, setSelectedCards] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [showEmojiPicker, setShowEmojiPicker] = useState(false);
    const [interactions, setInteractions] = useState({});
    const [chatHistory, setChatHistory] = useState([]);
    const chatEndRef = useRef(null);

    const scrollToBottom = () => {
        chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    // Initial chat history from game state if available
    useEffect(() => {
        if (gameState?.chatHistory) {
            setChatHistory(gameState.chatHistory.map(chat => ({
                sender: chat.playerName,
                content: chat.message,
                timestamp: chat.timestamp
            })));
        }
    }, [gameState?.chatHistory]);

    useEffect(() => {
        scrollToBottom();
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
                }].slice(-100));
            }
        }
    }, [gameState?.lastInteraction, gameState?.displayNames]);

    // Sync localStorage when game ends (for Navbar display)
    useEffect(() => {
        if (gameState?.winnerId && user && playerId) {
            // Fetch fresh profile to get updated balance and rank
            api.get(`/auth/profile/${user.id}`).then(res => {
                localStorage.setItem("user", JSON.stringify(res.data));
                window.dispatchEvent(new Event("storage"));
            }).catch(err => {
                console.error("Failed to fetch updated profile:", err);
            });
        }
    }, [gameState?.winnerId, user, playerId]);

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

    const handleKickRequest = (targetId) => {
        const targetName = gameState.displayNames[targetId] || "Người chơi";
        setKickConfirm({ isOpen: true, targetId, targetName });
    };

    const others = gameState.playerIds.filter(id => id !== playerId);
    const getPlayerByPos = (pos) => {
        if (pos >= others.length) return null;
        const id = others[pos];
        return (
            <div className="player-position-wrapper">
                <PlayerAvatar
                    playerId={id}
                    displayName={gameState.displayNames[id]}
                    rankTier={gameState.rankTiers[id]}
                    rankPoints={gameState.rankPoints[id]}
                    isHost={gameState.hostId === id}
                    isReady={gameState.readyPlayers.includes(id)}
                    isTurn={gameState.currentPlayerId === id}
                    handSize={gameState.hands[id]?.length || 0}
                    frameEffect={gameState.equippedFrames?.[id]}
                    playerCardFrame={gameState.playerCardFrames?.[id]}
                    cardSkin={gameState.cardSkins?.[id]}
                    avatar={gameState.avatars?.[id]}
                    message={interactions[id]?.type === 'CHAT' ? interactions[id].content : ''}
                    emoji={interactions[id]?.type === 'EMOJI' ? interactions[id].content : ''}
                    emojiTimestamp={interactions[id]?.ts}
                    onKick={gameState.hostId === playerId ? handleKickRequest : null}
                />
                {gameState.gameType.includes("Phỏm") && (
                    <div className="phom-player-cards">
                        <div className="eaten-cards">
                            {gameState.gameData.stolenCards?.[id]?.map((card, i) => (
                                <img key={i} src={getCardImage(card, id, gameState)} alt="eaten" className="stolen-card-mini" />
                            ))}
                        </div>
                        <div className="player-discards">
                            {gameState.gameData.playerTrashPiles?.[id]?.map((card, i) => (
                                <img key={i} src={getCardImage(card, id, gameState)} alt="discarded" className="trash-card-mini" />
                            ))}
                        </div>
                    </div>
                )}
            </div>
        );
    };

    const renderActions = () => {
        if (!gameState.gameStarted) {
            return (
                <div className="ready-start-box">
                    {gameState.hostId === playerId ? (
                        <button className="btn-3d btn-gold" onClick={startGame} disabled={gameState.playerIds.length < 2}>BẮT ĐẦU</button>
                    ) : (
                        <button className={`btn-3d ${gameState.readyPlayers.includes(playerId) ? 'btn-blue' : 'btn-green'}`} onClick={toggleReady}>
                            {gameState.readyPlayers.includes(playerId) ? 'ĐÃ SẴN SÀNG' : 'SẴN SÀNG'}
                        </button>
                    )}
                </div>
            );
        }

        const gType = gameState.gameType || "";
        if (gType.includes("Phỏm")) {
            return (
                <div className="game-action-row">
                    <button className="btn-3d btn-blue-small" onClick={() => sendAction({ type: 'DRAW' })} disabled={!isMyTurn || gameState.gameData.turnStage !== 'DRAW'}>BỐC BÀI</button>
                    <button className="btn-3d btn-green-small" onClick={() => sendAction({ type: 'STEAL' })} disabled={!isMyTurn || gameState.gameData.turnStage !== 'DRAW'}>ĂN BÀI</button>
                    <button className="btn-3d btn-white-small" onClick={() => { sendAction({ type: 'PLAY', cards: selectedCards }); setSelectedCards([]); }} disabled={!isMyTurn || selectedCards.length !== 1 || gameState.gameData.turnStage !== 'DISCARD'}>ĐÁNH</button>
                    <button className="btn-3d btn-gold-small" onClick={() => sendAction({ type: 'MELD', layout: [selectedCards] })} disabled={!isMyTurn || selectedCards.length < 3}>HẠ BÀI</button>
                    <button className="btn-3d btn-blue-small" onClick={() => { sendAction({ type: 'ATTACH', cards: selectedCards, targetId: others[0] }); setSelectedCards([]); }} disabled={!isMyTurn || selectedCards.length === 0}>GỬI BÀI</button>
                </div>
            );
        }
        if (gType.includes("Poker")) {
            return (
                <div className="game-action-row">
                    <button className="btn-3d btn-gray-small" onClick={() => sendAction({ type: 'FOLD' })} disabled={!isMyTurn}>FOLD</button>
                    <button className="btn-3d btn-blue-small" onClick={() => sendAction({ type: 'CHECK' })} disabled={!isMyTurn}>CHECK</button>
                    <button className="btn-3d btn-white-small" onClick={() => sendAction({ type: 'CALL' })} disabled={!isMyTurn}>CALL</button>
                    <button className="btn-3d btn-gold-small" onClick={() => sendAction({ type: 'BET', amount: 100 })} disabled={!isMyTurn}>RAISE</button>
                </div>
            );
        }
        if (gType.includes("Mậu Binh")) {
            return (
                <div className="game-action-row">
                    <button className="btn-3d btn-gold-small" onClick={() => sendAction({ type: 'LAYOUT', layout: [selectedCards.slice(0, 5), selectedCards.slice(5, 10), selectedCards.slice(10, 13)] })} disabled={selectedCards.length !== 13}>XONG</button>
                </div>
            );
        }

        // Default: Tiến Lên Miền Nam
        return (
            <div className="game-action-row">
                <button className="btn-3d btn-gold-small" onClick={() => setSelectedCards([])} disabled={selectedCards.length === 0}>BỎ CHỌN</button>
                <button className="btn-3d btn-gray-small" onClick={passTurn} disabled={!isMyTurn || gameState.tableCards.length === 0}>BỎ LƯỢT</button>
                <button className="btn-3d btn-white-small" onClick={handlePlay} disabled={!isMyTurn || selectedCards.length === 0}>ĐÁNH BÀI</button>
            </div>
        );
    };

    const renderTableCenter = () => {
        const gType = gameState.gameType || "";
        if (gType.includes("Poker")) {
            return (
                <div className="poker-table-center">
                    <div className="poker-pot">Pot: {gameState.gameData.pot}</div>
                    <div className="community-cards">
                        {gameState.tableCards.map((card, i) => (
                            <img key={i} src={getCardImage(card, null, gameState)} alt="c" className="community-card" />
                        ))}
                    </div>
                </div>
            );
        }

        if (gType.includes("Phỏm")) {
            return (
                <div className="phom-table-center">
                    <div className="trash-pile">
                        {gameState.gameData.trashPile?.slice(-3).map((card, i) => (
                            <img key={i} src={getCardImage(card, null, gameState)} alt="c" className="trash-card" />
                        ))}
                    </div>
                </div>
            );
        }

        return (
            <div className="played-cards-stack">
                {gameState.tableCards.length > 0 ? (
                    gameState.tableCards.map((card, i) => (
                        <img key={i} src={getCardImage(card, null, gameState)} alt="c" className="played-card-img" />
                    ))
                ) : (
                    <div className="invite-empty">Mời chơi</div>
                )}
            </div>
        );
    };

    return (
        <div className="casino-table-wrapper">
            {/* Header */}
            <div className="casino-top-bar">
                <button className="top-btn" onClick={() => setLeaveConfirmOpen(true)}>❮</button>
                <div className="room-title">{(gameState.gameType || "TIẾN LÊN").toUpperCase()} [Bàn {roomId}]</div>
                <button className="top-btn">⚙️</button>
            </div>

            <div className="casino-main-scene">
                {/* Oval Table */}
                <div className="oval-table">
                    <div className="table-inner-glow"></div>
                    <div className="table-logo"><div className="logo-text">{(gameState.gameType || "TIẾN LÊN").toUpperCase()}</div></div>
                    <div className="table-center-cards">
                        {renderTableCenter()}
                    </div>
                </div>

                {/* Player Positions */}
                <div className="p-top">{getPlayerByPos(1)}</div>
                <div className="p-left">{getPlayerByPos(0)}</div>
                <div className="p-right">{getPlayerByPos(2)}</div>

                {/* My Area */}
                <div className="my-area-casino">
                    <div className="casino-actions-3d">
                        {renderActions()}
                    </div>

                    <div className="my-footer-row">
                        <div className="my-avatar-v">
                            <PlayerAvatar
                                playerId={playerId}
                                displayName={user?.displayName}
                                rankTier={gameState.rankTiers[playerId] || user?.rankTier}
                                rankPoints={gameState.rankPoints[playerId] || user?.rankPoints}
                                isHost={gameState.hostId === playerId}
                                isMe={true}
                                isTurn={isMyTurn}
                                frameEffect={gameState.equippedFrames?.[playerId]}
                                playerCardFrame={gameState.playerCardFrames?.[playerId]}
                                cardSkin={gameState.cardSkins?.[playerId]}
                                avatar={gameState.avatars?.[playerId] || user?.avatar}
                                message={interactions[playerId]?.type === 'CHAT' ? interactions[playerId].content : ''}
                                emoji={interactions[playerId]?.type === 'EMOJI' ? interactions[playerId].content : ''}
                                emojiTimestamp={interactions[playerId]?.ts}
                            />
                        </div>

                        {gameState.gameType.includes("Phỏm") && (
                            <div className="my-phom-status">
                                <div className="eaten-cards">
                                    {gameState.gameData.stolenCards?.[playerId]?.map((card, i) => (
                                        <img key={i} src={getCardImage(card, playerId, gameState)} alt="eaten" className="stolen-card-mini" />
                                    ))}
                                </div>
                                <div className="player-discards">
                                    {gameState.gameData.playerTrashPiles?.[playerId]?.map((card, i) => (
                                        <img key={i} src={getCardImage(card, playerId, gameState)} alt="discarded" className="trash-card-mini" />
                                    ))}
                                </div>
                            </div>
                        )}
                        <div className="my-hand-casino">
                            {myHand.map((card, i) => (
                                <img
                                    key={i}
                                    src={getCardImage(card, playerId, gameState)}
                                    alt="card"
                                    className={`card-v ${selectedCards.some(sc => JSON.stringify(sc) === JSON.stringify(card)) ? "sel" : ""} ${gameState.cardSkins?.[playerId] || ""}`}
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

            {/* Confirmation Modals */}
            <ConfirmModal
                isOpen={leaveConfirmOpen}
                title="Rời khỏi bàn chơi"
                message="Bạn có chắc chắn muốn rời khỏi bàn và quay lại sảnh không?"
                onConfirm={() => { leaveRoom(); nav("/"); }}
                onCancel={() => setLeaveConfirmOpen(false)}
                confirmText="RỜI ĐI"
                cancelText="Ở LẠI"
                type="danger"
            />

            <ConfirmModal
                isOpen={kickConfirm.isOpen}
                title="Đuổi người chơi"
                message={`Bạn có chắc chắn muốn mời "${kickConfirm.targetName}" ra khỏi phòng không?`}
                onConfirm={() => kickPlayer(kickConfirm.targetId)}
                onCancel={() => setKickConfirm({ isOpen: false, targetId: null, targetName: "" })}
                confirmText="ĐUỔI NGAY"
                cancelText="HỦY BỎ"
                type="danger"
            />
        </div>
    );
}
