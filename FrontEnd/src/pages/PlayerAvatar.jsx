import React, { useEffect, useState } from 'react';
import './PlayerAvatar.css';

export default function PlayerAvatar({
    playerId,
    displayName,
    rankTier,
    rankPoints,
    isHost,
    isReady,
    isTurn,
    handSize,
    isMe,
    message,
    emoji,
    emojiTimestamp,
    onKick
}) {
    const [displayMsg, setDisplayMsg] = useState('');
    const [displayEmoji, setDisplayEmoji] = useState('');

    useEffect(() => {
        if (message) {
            setDisplayMsg(message);
            const timer = setTimeout(() => setDisplayMsg(''), 5000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    useEffect(() => {
        if (emoji) {
            setDisplayEmoji(emoji);
            const timer = setTimeout(() => setDisplayEmoji(''), 3000);
            return () => clearTimeout(timer);
        }
    }, [emoji, emojiTimestamp]);

    const getRankName = (tier) => {
        // Nếu tier đã là chuỗi (ví dụ: "Đồng IV") thì trả về luôn
        if (typeof tier === 'string' && tier.trim().length > 0) {
            return tier;
        }
        // Fallback nếu tier là số hoặc không xác định
        const ranks = ["Sắt", "Đồng", "Bạc", "Vàng", "Bạch Kim", "Kim Cương", "Cao Thủ", "Thách Đấu"];
        return ranks[tier] || "Sắt";
    };

    return (
        <div className={`player-horizontal-container ${isTurn ? 'is-turn' : ''} ${isMe ? 'is-me' : ''}`}>
            {/* Chat/Emoji Bubbles */}
            {displayMsg && <div className="bubble-msg-horizontal">{displayMsg}</div>}
            {displayEmoji && <div className="bubble-emoji-horizontal">{displayEmoji}</div>}

            <div className="player-layout-wrapper">
                {/* 1. Thẻ thông tin nằm ngang */}
                <div className="horizontal-gold-card">
                    {isTurn && (
                        <svg className="timer-svg" viewBox="0 0 244 84">
                            <rect
                                className="timer-rect"
                                x="2"
                                y="2"
                                width="240"
                                height="80"
                                rx="12"
                                ry="12"
                            />
                        </svg>
                    )}
                    <div className="gold-card-inner">
                        {/* Avatar hình tròn */}
                        <div className="avatar-circle-box">
                            <img
                                src={isMe ? "/avatars/avatar_me.png" : `/avatars/avatar_${(playerId % 5) + 1}.png`}
                                alt="avatar"
                                onError={(e) => e.target.src = "https://www.w3schools.com/howto/img_avatar.png"}
                            />
                        </div>

                        {/* Thông tin bên phải Avatar */}
                        <div className="player-info-content">
                            <div className="info-top-row">
                                <span className="player-name-text">{displayName}</span>
                                {isHost && <span className="host-icon-mini">👑</span>}
                            </div>
                            <div className="info-bottom-row">
                                <span className="rank-label">{getRankName(rankTier)}</span>
                                <span className="dxh-points">{rankPoints?.toLocaleString() || 0} DXH</span>
                            </div>
                        </div>

                        {onKick && !isMe && <button className="kick-btn-mini" onClick={() => onKick(playerId)}>×</button>}
                    </div>

                    {!isMe && isReady && <div className="ready-text-mini">SẴN SÀNG</div>}
                </div>

                {/* 2. Xếp bài đối thủ (Nếu không phải là tôi và đang trong ván) */}
                {!isMe && handSize > 0 && (
                    <div className="opponent-hand-fan-horizontal">
                        {[...Array(Math.min(handSize, 8))].map((_, i) => (
                            <div key={i} className="fan-card-small" style={{ marginLeft: i === 0 ? 0 : '-30px', zIndex: i }}>
                                <img src="/cards/back.png" alt="back" />
                                {i === Math.min(handSize, 8) - 1 && (
                                    <div className="card-count-badge-small">{handSize}</div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
