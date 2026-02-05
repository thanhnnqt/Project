import React from 'react';
import './ConfirmModal.css';

export default function ConfirmModal({
    isOpen,
    title,
    message,
    onConfirm,
    onCancel,
    confirmText = "Xác nhận",
    cancelText = "Hủy bỏ",
    type = "danger" // danger (red), primary (blue/gold)
}) {
    if (!isOpen) return null;

    return (
        <div className="confirm-modal-overlay">
            <div className="confirm-modal-box">
                <div className="confirm-modal-header">
                    <h3>{title}</h3>
                </div>
                <div className="confirm-modal-body">
                    <p>{message}</p>
                </div>
                <div className="confirm-modal-footer">
                    <button className="confirm-btn-cancel" onClick={onCancel}>{cancelText}</button>
                    <button
                        className={`confirm-btn-action ${type}`}
                        onClick={() => {
                            onConfirm();
                            onCancel();
                        }}
                    >
                        {confirmText}
                    </button>
                </div>
            </div>
        </div>
    );
}
