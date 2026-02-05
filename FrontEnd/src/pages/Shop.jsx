import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from 'react-hot-toast';
import api from "../api/axios";
import "./Shop.css";
import "./PlayerAvatar.css"; // Reuse frame animations
import ConfirmModal from "../components/ConfirmModal";

export default function Shop() {
    const [shopItems, setShopItems] = useState([]);
    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    // Modal state for purchase confirmation
    const [confirmModal, setConfirmModal] = useState({ isOpen: false, item: null });

    useEffect(() => {
        // ... existing useEffect logic ...
        const userString = localStorage.getItem("user");
        if (!userString) {
            navigate("/login");
            return;
        }
        const userData = JSON.parse(userString);
        setUser(userData);

        // Fetch shop items
        api.get("/shop/items")
            .then(res => {
                setShopItems(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Lỗi khi tải cửa hàng:", err);
                setLoading(false);
            });

        // Fetch player inventory
        fetchInventory(userData.id);
    }, [navigate]);

    const fetchInventory = (playerId) => {
        // ... existing fetchInventory logic ...
        api.get(`/shop/inventory/${playerId}`)
            .then(res => {
                setInventory(res.data);
            })
            .catch(err => {
                console.error("Lỗi khi tải kho đồ:", err);
            });
    };

    const handlePurchaseClick = (item) => {
        if (!user) return;
        if (user.balance < item.price) {
            toast.error("Không đủ xu để mua vật phẩm này!");
            return;
        }
        if (inventory.some(inv => inv.shopItem.id === item.id)) {
            toast.error("Bạn đã sở hữu vật phẩm này!");
            return;
        }
        setConfirmModal({ isOpen: true, item });
    };

    const confirmPurchase = () => {
        const { item } = confirmModal;
        if (!item) return;

        api.post("/shop/purchase", { playerId: user.id, itemId: item.id })
            .then(res => {
                toast.success(res.data.message);
                // Update user balance
                const updatedUser = { ...user, balance: res.data.newBalance };
                setUser(updatedUser);
                localStorage.setItem("user", JSON.stringify(updatedUser));

                // Refresh inventory
                fetchInventory(user.id);
            })
            .catch(err => {
                toast.error(err.response?.data || "Có lỗi xảy ra khi mua vật phẩm");
            });
    };

    const handleEquip = (itemId) => {
        api.post("/shop/equip", { playerId: user.id, itemId })
            .then(() => {
                toast.success("Đã trang bị vật phẩm!");
                fetchInventory(user.id);
            })
            .catch(err => toast.error(err.response?.data || "Lỗi khi trang bị"));
    };

    const isOwned = (itemId) => {
        return inventory.some(inv => inv.shopItem.id === itemId);
    };

    const getEquippedId = (type) => {
        return inventory.find(inv => inv.shopItem.type === type && inv.isEquipped)?.shopItem.id;
    };

    const getRarityColor = (rarity) => {
        switch (rarity) {
            case "COMMON": return "#9ca3af";
            case "RARE": return "#3b82f6";
            case "EPIC": return "#a855f7";
            case "LEGENDARY": return "#f59e0b";
            default: return "#6b7280";
        }
    };

    const [activeCategory, setActiveCategory] = useState("AVATAR_FRAME"); // AVATAR_FRAME, PLAYER_CARD_FRAME, CARD_SKIN

    const categories = [
        { id: "AVATAR_FRAME", name: "Khung Avatar" },
        { id: "PLAYER_CARD_FRAME", name: "Khung Thẻ Bài" },
        { id: "CARD_SKIN", name: "Lá Bài" }
    ];

    const filteredItems = shopItems.filter(item => item.type === activeCategory);

    return (
        <div className="shop-container">
            <div className="shop-header">
                <h1>🛒 Cửa Hàng</h1>
                <div className="user-balance-display">
                    <span>Số dư của bạn:</span>
                    <span className="balance-amount">💰 {user?.balance || 0} Xu</span>
                </div>
            </div>

            <div className="shop-categories">
                {categories.map(cat => (
                    <button
                        key={cat.id}
                        className={`category-tab ${activeCategory === cat.id ? 'active' : ''}`}
                        onClick={() => setActiveCategory(cat.id)}
                    >
                        {cat.name}
                    </button>
                ))}
            </div>

            {loading ? (
                <div className="loading">Đang tải cửa hàng...</div>
            ) : (
                <div className="shop-grid">
                    {filteredItems.length === 0 ? (
                        <div className="empty-category">Không có vật phẩm nào trong mục này.</div>
                    ) : (
                        filteredItems.map(item => (
                            <div key={item.id} className="shop-item-card">
                                <div className={`item-preview-box ${item.type === 'CARD_SKIN' ? 'card-skin-preview' : ''}`}>
                                    {item.type === 'AVATAR_FRAME' ? (
                                        <>
                                            <div className={`avatar-frame-container ${item.imageUrl}`}></div>
                                            <img
                                                src={
                                                    user?.avatar
                                                        ? (user.avatar.startsWith('http') ? user.avatar : (import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080') + user.avatar)
                                                        : "https://www.w3schools.com/howto/img_avatar.png"
                                                }
                                                alt="preview"
                                            />
                                        </>
                                    ) : item.type === 'PLAYER_CARD_FRAME' ? (
                                        <>
                                            <div className={`player-card-frame-wrapper ${item.imageUrl}`} style={{ top: 0, left: 0, right: 0, bottom: 0 }}></div>
                                            <img
                                                src={
                                                    user?.avatar
                                                        ? (user.avatar.startsWith('http') ? user.avatar : (import.meta.env.VITE_API_BASE_URL?.replace('/api', '') || 'http://localhost:8080') + user.avatar)
                                                        : "https://www.w3schools.com/howto/img_avatar.png"
                                                }
                                                alt="preview"
                                            />
                                        </>
                                    ) : (
                                        <div className="card-skin-showcase">
                                            <div className="card-skin-front">
                                                <img src="/cards/ace_of_spades.png" alt="front" />
                                                <div className="card-skin-overlay"></div>
                                            </div>
                                            <div className="card-skin-back">
                                                <img src="/cards/back.png" alt="back" className={item.imageUrl} />
                                            </div>
                                        </div>
                                    )}
                                </div>
                                <div
                                    className="item-rarity"
                                    style={{ color: getRarityColor(item.rarity) }}
                                >
                                    {item.rarity}
                                </div>
                                <h3>{item.name}</h3>
                                <p className="item-description">{item.description}</p>
                                <div className="item-type">
                                    {item.type === 'AVATAR_FRAME' ? 'Khung Avatar' : (item.type === 'PLAYER_CARD_FRAME' ? 'Khung Thẻ Bài' : 'Mẫu Lá Bài')}
                                </div>
                                <div className="item-footer">
                                    <span className="item-price">💰 {item.price} Xu</span>
                                    {isOwned(item.id) ? (
                                        <button
                                            className={`btn-equip ${getEquippedId(item.type) === item.id ? 'active' : ''}`}
                                            onClick={() => handleEquip(item.id)}
                                            disabled={getEquippedId(item.type) === item.id}
                                        >
                                            {getEquippedId(item.type) === item.id ? 'Đang dùng' : 'Trang bị'}
                                        </button>
                                    ) : (
                                        <button
                                            className="btn-purchase"
                                            onClick={() => handlePurchaseClick(item)}
                                        >
                                            Mua ngay
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}

            <ConfirmModal
                isOpen={confirmModal.isOpen}
                title="Xác nhận mua"
                message={`Bạn có chắc chắn muốn mua "${confirmModal.item?.name}" với giá ${confirmModal.item?.price} Xu?`}
                onConfirm={confirmPurchase}
                onCancel={() => setConfirmModal({ isOpen: false, item: null })}
                confirmText="MUA NGAY"
                cancelText="CÂN NHẮC LẠI"
                type="primary"
            />
        </div>
    );
}
