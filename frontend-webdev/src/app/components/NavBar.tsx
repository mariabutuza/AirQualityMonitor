'use client';
import { useEffect, useState } from "react";

export default function Navbar() {
    const [loggedIn, setLoggedIn] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");
        setLoggedIn(!!token);
    }, []);

    return (
        <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
            <div className="text-xl font-bold tracking-wide">
                🌬️ Respiră inteligent            </div>

            <div className="space-x-4 hidden sm:flex">
                <a href="/homepage" className="hover:text-teal-400 transition">Acasă</a>
                <a href="/about" className="hover:text-teal-400 transition">Despre</a>

                {loggedIn ? (
                    <>
                        <a href="/dashboard" className="hover:text-teal-400 transition">Dashboard</a>
                        <a href="/profilepage" className="hover:text-teal-400 transition">Profil</a>
                        <button
                            onClick={() => {
                                localStorage.removeItem("token");
                                window.location.href = "/landingpage";
                            }}
                            className="hover:text-red-400 transition"
                        >
                            Logout
                        </button>
                    </>
                ) : (
                    <>
                        <a href="/login" className="hover:text-teal-400 transition">Login</a>
                        <a href="/register" className="hover:text-teal-400 transition">Înregistrare</a>
                    </>
                )}
            </div>
        </nav>
    );
}
