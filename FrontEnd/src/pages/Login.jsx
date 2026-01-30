import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Login.css";

export default function Login() {
    const navigate = useNavigate();
    const [credentials, setCredentials] = useState({
        username: "",
        password: ""
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setCredentials({ ...credentials, [e.target.name]: e.target.value });
        setError("");
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await api.post("/auth/login", credentials);
            // In real app, response.data would contain JWT token
            localStorage.setItem("user", JSON.stringify(response.data));
            alert("Đăng nhập thành công! Chào mừng bạn quay trở lại.");
            navigate("/");
        } catch (err) {
            setError(err.response?.data || "Tên đăng nhập hoặc mật khẩu không chính xác.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2>Đăng Nhập</h2>

                {error && <div style={{ color: "#ef4444", marginBottom: "20px", textAlign: "center", fontSize: "14px" }}>{error}</div>}

                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Tên đăng nhập</label>
                        <input
                            name="username"
                            type="text"
                            placeholder="Nhập tên đăng nhập"
                            required
                            onChange={handleChange}
                        />
                    </div>

                    <div className="form-group">
                        <label>Mật khẩu</label>
                        <input
                            name="password"
                            type="password"
                            placeholder="Nhập mật khẩu"
                            required
                            onChange={handleChange}
                        />
                    </div>

                    <button type="submit" className="btn-login-submit" disabled={loading}>
                        {loading ? "Đang xác thực..." : "Đăng Nhập"}
                    </button>
                </form>

                <div style={{ marginTop: "20px", textAlign: "center", color: "#9ca3af", fontSize: "14px" }}>
                    Chưa có tài khoản? <span onClick={() => navigate("/register")} style={{ color: "#fbbf24", cursor: "pointer" }}>Đăng ký ngay</span>
                </div>
            </div>
        </div>
    );
}
