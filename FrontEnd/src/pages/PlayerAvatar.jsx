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
    onKick,
    frameEffect, // Avatar frame class
    playerCardFrame, // Large card frame class
    cardSkin, // Custom card skin class/ID
    avatar // custom avatar URL
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
        // Dacă tier đã là chuỗi (ví dụ: "Đồng IV") thì trả về luôn
        if (typeof tier === 'string' && tier.trim().length > 0) {
            return tier;
        }
        // Fallback dacă tier là số hoặc không xác định
        const ranks = ["Sắt", "Đồng", "Bạc", "Vàng", "Bạch Kim", "Kim Cương", "Cao Thủ", "Thách Đấu"];
        return ranks[tier] || "Sắt";
    };

    const getRankFrameClass = (tier) => {
        const name = getRankName(tier).toLowerCase();
        if (name.includes("thách đấu")) return "frame-rank-challenger";
        if (name.includes("cao thủ")) return "frame-rank-master";
        if (name.includes("kim cương")) return "frame-rank-diamond";
        if (name.includes("bạch kim")) return "frame-rank-platinum";
        if (name.includes("vàng")) return "frame-rank-gold";
        if (name.includes("bạc")) return "frame-rank-silver";
        if (name.includes("đồng")) return "frame-rank-bronze";
        if (name.includes("sắt")) return "frame-rank-iron";
        return "";
    };

    const effectiveFrame = frameEffect || getRankFrameClass(rankTier);

    return (
        <div className={`player-horizontal-container ${isTurn ? 'is-turn' : ''} ${isMe ? 'is-me' : ''}`}>
            {/* Chat/Emoji Bubbles */}
            {displayMsg && <div className="bubble-msg-horizontal">{displayMsg}</div>}
            {displayEmoji && <div className="bubble-emoji-horizontal">{displayEmoji}</div>}

            <div className="player-layout-wrapper">
                {/* 1. Thẻ thông tin nằm ngang */}
                <div className="horizontal-gold-card">
                    {/* Render Card Frames & Effects */}
                    {effectiveFrame?.includes('epic') && (
                        <div className={`card-frame-epic-wrapper card-frame-epic-${effectiveFrame.split('-').pop()}`}></div>
                    )}
                    {effectiveFrame?.includes('ruby') && (
                        <div className={`card-frame-ruby-wrapper card-frame-ruby-${effectiveFrame.split('-').pop()}`}></div>
                    )}
                    {playerCardFrame && (
                        <div className={`player-card-frame-wrapper ${playerCardFrame}`}></div>
                    )}
                    {isTurn && (
                        <svg className="timer-svg" viewBox="0 0 254 104">
                            <rect
                                className="timer-rect"
                                x="2"
                                y="2"
                                width="250"
                                height="100"
                                rx="12"
                                ry="12"
                            />
                        </svg>
                    )}
                    <div className="gold-card-inner">
                        {/* Avatar hình tròn */}
                        <div className="avatar-main-wrapper">
                            {effectiveFrame && !effectiveFrame.includes('card-frame') && (
                                <div className={`avatar-frame-container ${effectiveFrame}`}></div>
                            )}
                            <div className="avatar-circle-box">
                                <img
                                    src={
                                        avatar
                                            ? (avatar.startsWith('http') ? avatar : (import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080') + avatar)
                                            : (isMe ? "/avatars/avatar_me.png" : `/avatars/avatar_${(playerId % 5) + 1}.png`)
                                    }
                                    alt="avatar"
                                    onError={(e) => e.target.src = "https://www.w3schools.com/howto/img_avatar.png"}
                                />
                            </div>
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
                                <img src="/cards/back.png" alt="back" className={cardSkin} />
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
