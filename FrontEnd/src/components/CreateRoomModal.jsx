import React, { useState } from 'react';
import './CreateRoomModal.css';

export default function CreateRoomModal({ isOpen, onClose, onCreate }) {
    const [name, setName] = useState('');
    const [password, setPassword] = useState('');
    const [maxPlayers, setMaxPlayers] = useState(4);
    const minBet = 0; // Default to 0 as coin betting is removed

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!name.trim()) return;
        onCreate({ name, minBet, password, maxPlayers });
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h2>Tạo Phòng Mới</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Tên phòng</label>
                        <input
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            placeholder="Nhập tên phòng..."
                            required
                        />
                    </div>


                    <div className="form-group">
                        <label>Số lượng người chơi</label>
                        <div className="player-limit-selector">
                            {[2, 3, 4].map(num => (
                                <button
                                    key={num}
                                    type="button"
                                    className={maxPlayers === num ? 'active' : ''}
                                    onClick={() => setMaxPlayers(num)}
                                >
                                    {num} Người
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Mật khẩu (Để trống nếu không muốn đặt)</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Mật khẩu phòng..."
                        />
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="cancel-btn" onClick={onClose}>Hủy</button>
                        <button type="submit" className="create-btn">Tạo Phòng ngay</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
