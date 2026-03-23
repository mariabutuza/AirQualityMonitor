"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import AdminGuard from "@/app/hooks/AdminGuard";

export default function AdminDashboard() {
    const [fullName, setFullName] = useState("");
    const [userCount, setUserCount] = useState<number | null>(null);
    const [deviceCount, setDeviceCount] = useState<number | null>(null);
    const [criticalLocations, setCriticalLocations] = useState<number>(0);
    const [lastUpdate, setLastUpdate] = useState<string>("...");

    useEffect(() => {
        const storedName = localStorage.getItem("fullName") || "Admin";
        setFullName(storedName);
    }, []);

    useEffect(() => {
        async function fetchStats() {
            const token = localStorage.getItem("token");
            if (!token) return;

            try {
                const resUsers = await fetch(
                    "http://localhost:8080/api/user/all?page=0&size=1",
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                if (resUsers.ok) {
                    const data = await resUsers.json();
                    setUserCount(data.totalElements); // 👈 din Page<User>
                }

                const resDevices = await fetch("http://localhost:8081/api/admin/sensors", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (resDevices.ok) {
                    const data = await resDevices.json();
                    setDeviceCount(data.length);
                }

                const resLocations = await fetch("http://localhost:8081/api/admin/sensors/distinct-locations/count", {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (resLocations.ok) {
                    const count = await resLocations.json();
                    setCriticalLocations(count);
                }

                setLastUpdate(
                    new Date().toLocaleTimeString("ro-RO", {
                        hour: "2-digit",
                        minute: "2-digit",
                    })
                );
            } catch (err) {
                console.error("Eroare la fetch stats:", err);
            }
        }

        fetchStats();
        const interval = setInterval(fetchStats, 60000);
        return () => clearInterval(interval);
    }, []);

    return (
        <AdminGuard>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white">
                {/* Navbar */}
                <nav className="bg-black/30 backdrop-blur-md border-b border-teal-500 px-6 py-4 flex justify-between items-center shadow-md fixed w-full z-50">
                    <div className="text-xl font-bold tracking-wide flex items-center gap-2">
                        🌬️ <span>Air Quality Monitor - Admin</span>
                    </div>
                    <div className="space-x-4 flex items-center text-sm">
                        <span className="hidden sm:inline text-gray-300">👤 {fullName}</span>
                        <Link href="/dashboard" className="hover:text-teal-400 transition">
                            Vezi ca user
                        </Link>
                        <Link href="/login" className="hover:text-red-400 transition">
                            Logout
                        </Link>
                    </div>
                </nav>

                {/* Content */}
                <main className="pt-28 px-6 pb-16 max-w-7xl mx-auto">
                    <p className="text-sm text-gray-400 mb-1">Admin / Dashboard</p>

                    <h1 className="text-4xl font-bold mb-2">Dashboard Admin</h1>
                    <p className="text-gray-300 mb-8">
                        Bine ai venit! Alege una dintre secțiunile de mai jos pentru a administra aplicația.
                    </p>

                    {/* Quick Stats */}
                    <div className="bg-black/40 border border-teal-600 p-4 rounded-xl mb-10 flex flex-wrap gap-6 text-sm justify-around text-gray-300 shadow-inner">
                        <div>
                            👥{" "}
                            <span className="text-white font-semibold">
                {userCount ?? "..."}
              </span>{" "}
                            Utilizatori
                        </div>
                        <div>
                            📟{" "}
                            <span className="text-white font-semibold">
                {deviceCount ?? "..."}
              </span>{" "}
                            Device-uri active
                        </div>
                        <div>
                            🌫️{" "}
                            <span className="text-white font-semibold">
                {criticalLocations}
              </span>{" "}
                            Locații critice
                        </div>
                        <div>
                            🕒 Ultima actualizare: <span className="text-white">{lastUpdate}</span>
                        </div>
                    </div>

                    {/* Cards Grid */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                        <Link href="/admin/users">
                            <div className="cursor-pointer bg-gray-900/80 hover:bg-teal-800/30 p-6 rounded-xl border border-teal-500 shadow-md hover:shadow-teal-400/30 transition duration-300 hover:scale-[1.03]">
                                <div className="text-3xl mb-3">👥</div>
                                <h2 className="text-xl font-semibold mb-1">Utilizatori</h2>
                                <p className="text-gray-400 text-sm">
                                    Vezi toți utilizatorii, editează si aplica modificari pe roluri.
                                </p>
                            </div>
                        </Link>

                        <Link href="/admin/devices">
                            <div className="cursor-pointer bg-gray-900/80 hover:bg-teal-800/30 p-6 rounded-xl border border-teal-500 shadow-md hover:shadow-teal-400/30 transition duration-300 hover:scale-[1.03]">
                                <div className="text-3xl mb-3">📡</div>
                                <h2 className="text-xl font-semibold mb-1">Device-uri</h2>
                                <p className="text-gray-400 text-sm">
                                    Adaugă sau editează device-uri de monitorizare a aerului.
                                </p>
                            </div>
                        </Link>

                        <Link href="/admin/stats">
                            <div className="cursor-pointer bg-gray-900/80 hover:bg-teal-800/30 p-6 rounded-xl border border-teal-500 shadow-md hover:shadow-teal-400/30 transition duration-300 hover:scale-[1.03]">
                                <div className="text-3xl mb-3">📊</div>
                                <h2 className="text-xl font-semibold mb-1">Statistici</h2>
                                <p className="text-gray-400 text-sm">
                                    Vizualizează date despre calitatea aerului și activitatea utilizatorilor.
                                </p>
                            </div>
                        </Link>
                    </div>
                </main>
            </div>
        </AdminGuard>
    );
}
