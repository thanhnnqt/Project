import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from 'react-hot-toast';
import api from "../api/axios";
import PlayerAvatar from "./PlayerAvatar";
import "./Profile.css";
import "./PlayerAvatar.css";

export default function Profile() {
    const [user, setUser] = useState(null);
    const [shopItems, setShopItems] = useState([]);
    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState("RANK"); // RANK, SHOP, or CARD_FRAMES
    const navigate = useNavigate();

    useEffect(() => {
        const userString = localStorage.getItem("user");
        if (!userString) {
            navigate("/login");
            return;
        }
        const userData = JSON.parse(userString);
        setUser(userData);

        // Fetch latest user data from server (rank, points, etc.)
        api.get(`/auth/profile/${userData.id}`)
            .then(res => {
                setUser(res.data);
                localStorage.setItem("user", JSON.stringify(res.data));
            })
            .catch(err => console.error("Lỗi đồng bộ user:", err));

        // Fetch all shop items to find rank frames
        api.get("/shop/items")
            .then(res => setShopItems(res.data))
            .catch(err => console.error("Lỗi tải khung:", err));

        // Fetch inventory
        fetchInventory(userData.id);
    }, [navigate]);

    const fetchInventory = (playerId) => {
        api.get(`/shop/inventory/${playerId}`)
            .then(res => {
                setInventory(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Lỗi tải kho đồ:", err);
                setLoading(false);
            });
    };

    const handleClaimOrEquip = (item) => {
        const isOwned = inventory.some(inv => inv.shopItem.id === item.id);

        if (!isOwned) {
            // Claim rank frame (0 xu)
            api.post("/shop/purchase", { playerId: user.id, itemId: item.id })
                .then(res => {
                    toast.success("Đã nhận khung!");
                    fetchInventory(user.id);
                })
                .catch(err => toast.error(err.response?.data || "Lỗi khi nhận khung"));
        } else {
            // Equip frame
            api.post("/shop/equip", { playerId: user.id, itemId: item.id })
                .then(() => {
                    toast.success("Đã trang bị khung mới!");
                    fetchInventory(user.id);
                })
                .catch(err => toast.error(err.response?.data || "Lỗi khi trang bị"));
        }
    };

    const handleAvatarUpload = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);

        api.post(`/auth/upload-avatar/${user.id}`, formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        })
            .then(res => {
                toast.success("Đã tải ảnh đại diện mới!");
                setUser(res.data);
                localStorage.setItem("user", JSON.stringify(res.data));
            })
            .catch(err => {
                console.error("Lỗi upload avatar:", err);
                toast.error("Lỗi khi tải ảnh đại diện: " + (err.response?.data || err.message));
            });
    };

    const getEquippedId = (type) => {
        return inventory.find(inv => inv.shopItem.type === type && inv.isEquipped)?.shopItem.id;
    };

    if (!user) return null;

    const rankFrames = shopItems.filter(item => item.price === 0 && item.type === 'AVATAR_FRAME');
    const ownedItems = inventory.map(inv => inv.shopItem);
    const shopFrames = ownedItems.filter(item => item.price > 0 && item.type === 'AVATAR_FRAME');
    const playerCardFrames = ownedItems.filter(item => item.type === 'PLAYER_CARD_FRAME');
    const cardSkins = ownedItems.filter(item => item.type === 'CARD_SKIN');

    const getGlobalPoints = (userData) => {
        if (!userData) return 0;
        const ranks = [
            "Sắt IV", "Sắt III", "Sắt II", "Sắt I",
            "Đồng IV", "Đồng III", "Đồng II", "Đồng I",
            "Bạc IV", "Bạc III", "Bạc II", "Bạc I",
            "Vàng IV", "Vàng III", "Vàng II", "Vàng I",
            "Bạch Kim IV", "Bạch Kim III", "Bạch Kim II", "Bạch Kim I",
            "Kim Cương IV", "Kim Cương III", "Kim Cương II", "Kim Cương I",
            "Cao Thủ", "Thách Đấu"
        ];
        const tierIndex = ranks.indexOf(userData.rankTier);
        const index = tierIndex === -1 ? 0 : tierIndex;
        return (index * 100) + (userData.rankPoints || 0);
    };

    const userGlobalPoints = getGlobalPoints(user);

    return (
        <div className="profile-container">
            <div className="profile-card">
                <div className="profile-sidebar">
                    <div className="profile-avatar-preview">
                        <PlayerAvatar
                            playerId={user.id}
                            displayName={user.displayName}
                            rankTier={user.rankTier}
                            rankPoints={user.rankPoints}
                            isMe={true}
                            frameEffect={inventory.find(inv => inv.isEquipped && inv.shopItem.type === 'AVATAR_FRAME')?.shopItem.imageUrl}
                            playerCardFrame={inventory.find(inv => inv.isEquipped && inv.shopItem.type === 'PLAYER_CARD_FRAME')?.shopItem.imageUrl}
                            avatar={user.avatar}
                        />
                        <div className="avatar-upload-section">
                            <label htmlFor="avatar-upload" className="btn-upload-avatar">
                                📷 Đổi ảnh
                            </label>
                            <input
                                id="avatar-upload"
                                type="file"
                                accept="image/*"
                                onChange={handleAvatarUpload}
                                style={{ display: 'none' }}
                            />
                        </div>
                    </div>
                    <div className="profile-stats">
                        <div className="stat-item">
                            <span className="stat-label">Hạng hiện tại</span>
                            <span className="stat-value rank-color">{user.rankTier}</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Điểm hạng (DXH) + Tích lũy</span>
                            <span className="stat-value">{user.rankPoints?.toLocaleString()} ({userGlobalPoints} Total)</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-label">Số dư</span>
                            <span className="stat-value gold-color">💰 {user.balance?.toLocaleString()} Xu</span>
                        </div>
                    </div>
                </div>

                <div className="profile-main">
                    <div className="profile-header">
                        <h1>Cá nhân</h1>
                        <p>Quản lý vật phẩm và khung danh hiệu của bạn</p>
                    </div>

                    <div className="filter-tabs">
                        <button
                            className={`tab-btn ${activeTab === 'RANK' ? 'active' : ''}`}
                            onClick={() => setActiveTab('RANK')}
                        >
                            Danh Hiệu
                        </button>
                        <button
                            className={`tab-btn ${activeTab === 'SHOP' ? 'active' : ''}`}
                            onClick={() => setActiveTab('SHOP')}
                        >
                            Khung Đã Mua
                        </button>
                        <button
                            className={`tab-btn ${activeTab === 'CARD_FRAMES' ? 'active' : ''}`}
                            onClick={() => setActiveTab('CARD_FRAMES')}
                        >
                            Khung Thẻ Bài
                        </button>
                        <button
                            className={`tab-btn ${activeTab === 'CARD_SKIN' ? 'active' : ''}`}
                            onClick={() => setActiveTab('CARD_SKIN')}
                        >
                            Lá Bài
                        </button>
                    </div>

                    <div className="frames-grid">
                        {(() => {
                            const itemsToShow = activeTab === 'RANK' ? rankFrames :
                                (activeTab === 'SHOP' ? shopFrames :
                                    (activeTab === 'CARD_FRAMES' ? playerCardFrames : cardSkins));
                            if (itemsToShow.length === 0) {
                                return (
                                    <div className="empty-frames-message">
                                        Bạn chưa sở hữu vật phẩm nào trong mục này. Hãy ghé <a href="/shop">Cửa hàng</a> nhé!
                                    </div>
                                );
                            }
                            return itemsToShow.map(item => {
                                const isOwned = inventory.some(inv => inv.shopItem.id === item.id);
                                const isEquipped = getEquippedId(item.type) === item.id;
                                const isLocked = item.minRankPoints > userGlobalPoints;

                                return (
                                    <div key={item.id} className={`frame-item-card ${isLocked ? 'locked' : ''} ${item.type === 'PLAYER_CARD_FRAME' ? 'player-card-frame-preview' : ''}`}>
                                        <div className="frame-preview">
                                            {item.type === 'AVATAR_FRAME' && (
                                                <div className={`avatar-frame-container ${item.imageUrl}`}></div>
                                            )}
                                            {item.type === 'PLAYER_CARD_FRAME' && (
                                                <div className={`player-card-frame-wrapper ${item.imageUrl}`} style={{ top: 0, left: 0, right: 0, bottom: 0 }}></div>
                                            )}
                                            {item.type === 'CARD_SKIN' ? (
                                                <div className="card-skin-showcase mini">
                                                    <div className="card-skin-back">
                                                        <img src="/cards/back.png" alt="back" className={item.imageUrl} />
                                                    </div>
                                                </div>
                                            ) : (
                                                <img
                                                    src={
                                                        user?.avatar
                                                            ? (user.avatar.startsWith('http') ? user.avatar : (import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080') + user.avatar)
                                                            : "https://www.w3schools.com/howto/img_avatar.png"
                                                    }
                                                    alt="preview"
                                                />
                                            )}
                                            {isLocked && <div className="lock-overlay">🔒 {item.minRankPoints} DXH</div>}
                                        </div>
                                        <div className="frame-info">
                                            <h4>{item.name}</h4>
                                            <button
                                                className={`btn-action ${isEquipped ? 'active' : ''}`}
                                                disabled={isLocked || isEquipped}
                                                onClick={() => handleClaimOrEquip(item)}
                                            >
                                                {isEquipped ? 'Sử dụng' : (isOwned ? 'Thay ngay' : 'Mở khóa')}
                                            </button>
                                        </div>
                                    </div>
                                );
                            });
                        })()}
                    </div>
                </div>
            </div>
        </div>
    );
}
