import "./Footer.css";

export default function Footer() {
    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-section about">
                    <h3 className="footer-logo">♠ Royal<span>Cards</span></h3>
                    <p>Trải nghiệm game bài đỉnh cao, đồ họa sắc nét, cộng đồng đông đảo và công bằng tuyệt đối.</p>
                </div>

                <div className="footer-section links">
                    <h4>Liên kết nhanh</h4>
                    <ul>
                        <li><a href="/">Trang chủ</a></li>
                        <li><a href="/lobby">Phòng chơi</a></li>
                        <li><a href="/rank">Bảng xếp hạng</a></li>
                        <li><a href="/about">Giới thiệu</a></li>
                    </ul>
                </div>

                <div className="footer-section contact">
                    <h4>Hỗ trợ</h4>
                    <p>Email: support@royalcards.com</p>
                    <p>Hotline: 1900 888 999</p>
                    <div className="socials">
                        <span className="social-icon">Facebook</span>
                        <span className="social-icon">Telegram</span>
                    </div>
                </div>
            </div>
            <div className="footer-bottom">
                &copy; 2026 RoyalCards Entertainment. All rights reserved.
            </div>
        </footer>
    );
}
