"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError("");

        try {
            const res = await fetch("http://localhost:8080/api/user/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(form),
            });

            const data = await res.json();

            if (res.ok) {
                localStorage.setItem("token", data.token);
                localStorage.setItem("role", data.role);
                localStorage.setItem("fullName", data.fullName);

                if (data.role === "ADMIN") {
                    router.push("/admin/dashboard");
                } else {
                    router.push("/dashboard");
                }
            } else {
                setError(data.message || "Autentificarea a eșuat");
            }
        } catch (err) {
            setError("Serverul nu poate fi contactat");
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">
                    🌬️ Respiră inteligent                </div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/landingpage" className="hover:text-teal-400 transition">
                        Home
                    </a>
                </div>
            </nav>

            {/* Form Container */}
            <div className="flex items-center justify-center px-4 pt-28 pb-10">
                <div className="bg-black bg-opacity-60 border border-teal-500 rounded-2xl shadow-xl backdrop-blur-md p-10 w-full max-w-md">
                    <div className="text-center mb-6">
                        <img
                            src="/sky.avif"
                            alt="Air Monitor"
                            className="h-16 w-16 mx-auto mb-4 rounded-full bg-gray-900 bg-opacity-40 ring-1 ring-gray-700 object-cover"
                        />
                        <h1 className="text-3xl font-bold tracking-wide">Bine ai venit!</h1>
                        <p className="text-sm text-gray-400">
                            Monitorizează calitatea aerului din jurul tău
                        </p>
                    </div>
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                            placeholder="Adresă email"
                        />
                        <input
                            type="password"
                            name="password"
                            value={form.password}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                            placeholder="Parolă"
                        />
                        {error && <p className="text-red-500 text-sm">{error}</p>}
                        <button
                            type="submit"
                            className="w-full bg-teal-600 hover:bg-teal-700 text-white font-semibold py-3 rounded-lg transition cursor-pointer"
                        >
                            Autentificare
                        </button>
                    </form>
                    <p className="text-xs text-center mt-4 text-gray-400">
                        Nu ai încă un cont?{" "}
                        <a
                            href="/register"
                            className="text-teal-400 underline hover:text-teal-300"
                        >
                            Înregistrează-te aici
                        </a>
                    </p>
                </div>
            </div>

            {/* Footer */}
            <footer className="py-6 text-center text-gray-400 text-sm">
                © {new Date().getFullYear()} Air Quality Monitor. Toate drepturile rezervate.
            </footer>
        </div>
    );
}
