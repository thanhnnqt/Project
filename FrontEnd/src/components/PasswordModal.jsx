import React, { useState } from 'react';
import './CreateRoomModal.css';

export default function PasswordModal({ isOpen, onClose, onConfirm, roomName }) {
    const [password, setPassword] = useState('');

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        onConfirm(password);
        setPassword('');
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <h2>Vào Phòng</h2>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>
                <p style={{ color: '#9ca3af', marginBottom: '20px' }}>
                    Phòng <strong>{roomName}</strong> yêu cầu mật khẩu để tham gia.
                </p>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Mật khẩu</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Nhập mật khẩu..."
                            autoFocus
                            required
                        />
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="cancel-btn" onClick={onClose}>Hủy</button>
                        <button type="submit" className="create-btn">Vào Chơi</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
