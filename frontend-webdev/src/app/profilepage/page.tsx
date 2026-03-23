'use client';

import { useEffect, useState } from 'react';

type User = {
    id: number;
    firstname: string;
    lastname: string;
    email: string;
};

export default function ProfilePage() {
    const [user, setUser] = useState<User | null>(null);
    const [form, setForm] = useState({ firstname: '', lastname: '', email: '', password: '' });
    const [message, setMessage] = useState('');

    const fetchProfile = async () => {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch('http://localhost:8080/api/user/me', {
                headers: { Authorization: `Bearer ${token}` },
            });
            const data = await res.json();

            const [firstname = '', lastname = ''] = data.fullName?.split(' ') || [];

            setUser(data);
            setForm({
                firstname,
                lastname,
                email: data.email,
                password: '',
            });
        } catch {
            setMessage('Failed to load profile.');
        }
    };


    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleUpdate = async (e: React.FormEvent) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`http://localhost:8080/api/user/me/update`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    fullName: `${form.firstname} ${form.lastname}`,
                    email: form.email,
                    password: form.password,
                }),
            });
            if (res.ok) {
                setMessage('Profile updated successfully!');
                fetchProfile();
            } else {
                const error = await res.json();
                setMessage(error.message || 'Update failed.');
            }
        } catch {
            setMessage('Server error.');
        }
    };


    useEffect(() => {
        fetchProfile();
    }, []);

    if (!user) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white">
                <p>Loading profile...</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 flex items-center justify-center px-4">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">
                    🌬️ Respiră inteligent                </div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/dashboard" className="hover:text-teal-400 transition">Acasă</a>
                    <a href="/profilepage" className="text-teal-400 underline font-semibold">Profil</a>
                    <a href="/abouthome" className="hover:text-teal-400 transition">Despre</a>
                    <button
                        onClick={() => {
                            localStorage.removeItem('token');
                            window.location.href = '/landingpage';
                        }}
                        className="hover:text-red-400 transition"
                    >
                        Logout
                    </button>
                </div>
            </nav>
            <div
                className="bg-black bg-opacity-60 border border-teal-500 rounded-2xl shadow-xl backdrop-blur-md p-10 w-full max-w-lg text-white">
                <h2 className="text-3xl font-bold text-center mb-6">Profil</h2>
                <form onSubmit={handleUpdate} className="space-y-5">
                    <input
                        type="text"
                        name="firstname"
                        value={form.firstname}
                        onChange={handleChange}
                        required
                        placeholder="First Name"
                        className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                    <input
                        type="text"
                        name="lastname"
                        value={form.lastname}
                        onChange={handleChange}
                        required
                        placeholder="Last Name"
                        className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                    <input
                        type="email"
                        name="email"
                        value={form.email}
                        onChange={handleChange}
                        required
                        placeholder="Email"
                        className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                    <input
                        type="password"
                        name="password"
                        value={form.password}
                        onChange={handleChange}
                        placeholder="Parola nouă (se va lăsa câmp gol, dacă nu se modifică)"
                        className="w-full px-4 py-3 bg-gray-800 border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                    {message && <p className="text-sm text-center text-teal-300">{message}</p>}
                    <button
                        type="submit"
                        className="w-full bg-teal-600 hover:bg-teal-700 text-white font-semibold py-3 rounded-lg transition"
                    >
                        Actualizează profil
                    </button>
                </form>
            </div>
        </div>
    );
}
