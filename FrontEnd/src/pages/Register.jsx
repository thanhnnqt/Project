import { useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from 'react-hot-toast';
import api from "../api/axios";
import "./Register.css";

export default function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        displayName: "",
        username: "",
        password: "",
        confirmPassword: "",
        email: "",
        age: "",
        phoneNumber: ""
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setError("");
    };

    const handleRegister = async (e) => {
        e.preventDefault();

        // Basic frontend validation
        if (formData.password !== formData.confirmPassword) {
            setError("Mật khẩu xác nhận không khớp!");
            return;
        }

        setLoading(true);
        try {
            await api.post("/auth/register", formData);
            toast.success("Chúc mừng! Bạn đã đăng ký thành công.");
            navigate("/login");
        } catch (err) {
            setError(err.response?.data || "Đã có lỗi xảy ra vui lòng thử lại sau.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-container">
            <div className="register-card">
                <h2>Tạo tài khoản mới</h2>

                {error && <div style={{ color: "#ef4444", marginBottom: "20px", textAlign: "center", fontSize: "14px" }}>{error}</div>}

                <form onSubmit={handleRegister}>
                    <div className="form-group">
                        <label>Tên hiển thị</label>
                        <input name="displayName" type="text" placeholder="Nhập tên của bạn" required onChange={handleChange} />
                    </div>

                    <div className="form-group">
                        <label>Tên đăng nhập</label>
                        <input name="username" type="text" placeholder="Nhập tên đăng nhập" required onChange={handleChange} />
                    </div>

                    <div className="form-group">
                        <label>Email</label>
                        <input name="email" type="email" placeholder="Nhập email của bạn" required onChange={handleChange} />
                    </div>

                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "15px" }}>
                        <div className="form-group">
                            <label>Mật khẩu</label>
                            <input name="password" type="password" placeholder="Mật khẩu" required onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Xác nhận mật khẩu</label>
                            <input name="confirmPassword" type="password" placeholder="Nhập lại" required onChange={handleChange} />
                        </div>
                    </div>

                    <div style={{ display: "grid", gridTemplateColumns: "1fr 2fr", gap: "15px" }}>
                        <div className="form-group">
                            <label>Tuổi</label>
                            <input name="age" type="number" placeholder="Tuổi" required onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Số điện thoại</label>
                            <input name="phoneNumber" type="text" placeholder="Số điện thoại" required onChange={handleChange} />
                        </div>
                    </div>

                    <button type="submit" className="btn-register-submit" disabled={loading}>
                        {loading ? "Đang xử lý..." : "Đăng ký ngay"}
                    </button>
                </form>

                <div style={{ marginTop: "20px", textAlign: "center", color: "#9ca3af", fontSize: "14px" }}>
                    Đã có tài khoản? <span onClick={() => navigate("/login")} style={{ color: "#fbbf24", cursor: "pointer" }}>Đăng nhập</span>
                </div>
            </div>
        </div>
    );
}
