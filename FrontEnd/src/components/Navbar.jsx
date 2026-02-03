import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import "./Navbar.css";

export default function Navbar() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    useEffect(() => {
        // Sync user state from localStorage
        const checkUser = () => {
            const userString = localStorage.getItem("user");
            setUser(userString ? JSON.parse(userString) : null);
        };

        checkUser();
        // Listen for changes (e.g. from other tabs or manual updates)
        window.addEventListener('storage', checkUser);

        // Custom interval check for same-tab updates if not using a global state manager
        const interval = setInterval(checkUser, 1000);

        return () => {
            window.removeEventListener('storage', checkUser);
            clearInterval(interval);
        };
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("user");
        setUser(null);
        navigate("/login");
    };

    return (
        <nav className="navbar">
            <div className="navbar-container">
                <Link to="/" className="logo">
                    ♠ Royal<span>Cards</span>
                </Link>

                <div className="menu">
                    <Link to="/" className="menu-item">Trang chủ</Link>
                    <Link to="/shop" className="menu-item">Cửa hàng</Link>
                    <Link to="/about" className="menu-item">Giới thiệu</Link>
                </div>

                <div className="auth">
                    {user ? (
                        <div className="user-nav-info">
                            <div className="user-rank-info">
                                <span className="rank-badge">{user.rankTier}</span>
                                <span className="rank-points">{user.rankPoints} DXH</span>
                            </div>
                            <span className="user-name">Chào, {user.displayName}</span>
                            <span className="user-balance">💰 {user.balance} Xu</span>
                            <button onClick={handleLogout} className="btn-logout">Đăng xuất</button>
                        </div>
                    ) : (
                        <>
                            <Link to="/login" className="btn-login">Đăng nhập</Link>
                            <Link to="/register" className="btn-register">Đăng ký</Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}
