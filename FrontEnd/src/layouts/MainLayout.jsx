import { useLocation } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import "./MainLayout.css";

export default function MainLayout({ children }) {
    const location = useLocation();
    const isRoomPage = location.pathname.startsWith("/room/");

    return (
        <div className="main-layout">
            {!isRoomPage && <Navbar />}
            <div className={`content ${isRoomPage ? "room-active" : ""}`}>
                {children}
            </div>
            {!isRoomPage && <Footer />}
        </div>
    );
}
