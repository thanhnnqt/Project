import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from 'react-hot-toast';
import MainLayout from "./layouts/MainLayout";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Lobby from "./pages/Lobby";
import Room from "./pages/Room";
import Shop from "./pages/Shop";
import Profile from "./pages/Profile";

export default function App() {
    return (
        <BrowserRouter>
            <Toaster position="top-right" reverseOrder={false} />
            <MainLayout>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/shop" element={<Shop />} />
                    <Route path="/profile" element={<Profile />} />
                    <Route path="/lobby/:gameTypeId" element={<Lobby />} />
                    <Route path="/room/:id" element={<Room />} />
                </Routes>
            </MainLayout>
        </BrowserRouter>
    );
}
