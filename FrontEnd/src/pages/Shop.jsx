import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Shop.css";

export default function Shop() {
    const [shopItems, setShopItems] = useState([]);
    const [inventory, setInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
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
        api.get(`/shop/inventory/${userData.id}`)
            .then(res => {
                setInventory(res.data);
            })
            .catch(err => {
                console.error("Lỗi khi tải kho đồ:", err);
            });
    }, [navigate]);

    const handlePurchase = (itemId, itemPrice) => {
        if (!user) return;

        if (user.balance < itemPrice) {
            alert("Không đủ xu để mua vật phẩm này!");
            return;
        }

        if (inventory.some(inv => inv.shopItem.id === itemId)) {
            alert("Bạn đã sở hữu vật phẩm này!");
            return;
        }

        api.post("/shop/purchase", { playerId: user.id, itemId })
            .then(res => {
                alert(res.data.message);
                // Update user balance
                const updatedUser = { ...user, balance: res.data.newBalance };
                setUser(updatedUser);
                localStorage.setItem("user", JSON.stringify(updatedUser));

                // Refresh inventory
                api.get(`/shop/inventory/${user.id}`)
                    .then(res => setInventory(res.data));
            })
            .catch(err => {
                alert(err.response?.data || "Có lỗi xảy ra khi mua vật phẩm");
            });
    };

    const isOwned = (itemId) => {
        return inventory.some(inv => inv.shopItem.id === itemId);
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

    return (
        <div className="shop-container">
            <div className="shop-header">
                <h1>🛒 Cửa Hàng</h1>
                <div className="user-balance-display">
                    <span>Số dư của bạn:</span>
                    <span className="balance-amount">💰 {user?.balance || 0} Xu</span>
                </div>
            </div>

            {loading ? (
                <div className="loading">Đang tải cửa hàng...</div>
            ) : (
                <div className="shop-grid">
                    {shopItems.map(item => (
                        <div key={item.id} className="shop-item-card">
                            <div className="item-icon">{item.imageUrl}</div>
                            <div
                                className="item-rarity"
                                style={{ color: getRarityColor(item.rarity) }}
                            >
                                {item.rarity}
                            </div>
                            <h3>{item.name}</h3>
                            <p className="item-description">{item.description}</p>
                            <div className="item-type">{item.type === 'AVATAR_FRAME' ? 'Khung Avatar' : 'Bộ Bài'}</div>
                            <div className="item-footer">
                                <span className="item-price">💰 {item.price} Xu</span>
                                <button
                                    className={`btn-purchase ${isOwned(item.id) ? 'owned' : ''}`}
                                    onClick={() => handlePurchase(item.id, item.price)}
                                    disabled={isOwned(item.id)}
                                >
                                    {isOwned(item.id) ? '✓ Đã sở hữu' : 'Mua ngay'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
