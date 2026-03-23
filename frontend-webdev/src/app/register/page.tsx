'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { Toaster, toast } from 'react-hot-toast';

type FormState = {
    email: string;
    password: string;
    fullName: string;
};

type Errors = {
    email?: string;
    password?: string;
    fullName?: string;
};

export default function RegisterPage() {
    const router = useRouter();
    const [form, setForm] = useState<FormState>({ email: '', password: '', fullName: '' });
    const [errors, setErrors] = useState<Errors>({});

    const validate = (): boolean => {
        const newErrors: Errors = {};

        if (!form.fullName.trim()) {
            newErrors.fullName = 'Numele complet este obligatoriu.';
        } else if (!/^[a-zA-ZăâîșțĂÂÎȘȚ\s-]{3,}$/.test(form.fullName.trim())) {
            newErrors.fullName = 'Introduceți un nume valid (minim 3 litere).';
        }

        if (!form.email) {
            newErrors.email = 'Emailul este obligatoriu.';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
            newErrors.email = 'Introduceți o adresă de email validă.';
        }

        if (!form.password) {
            newErrors.password = 'Parola este obligatorie.';
        } else if (form.password.length < 3) {
            newErrors.password = 'Parola trebuie să aibă cel puțin 8 caractere.';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
        setErrors({ ...errors, [e.target.name]: undefined });
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!validate()) {
            toast.error('Verifică erorile din formular.');
            return;
        }

        try {
            const res = await fetch('http://localhost:8080/api/user/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form),
            });

            if (res.ok) {
                const data = await res.json();
                localStorage.setItem('token', data.token);
                toast.success('Cont creat cu succes! Redirecționare...');
                setTimeout(() => router.push('/login'), 2000);
            } else if (res.status === 409) {
                toast.error('Acest email este deja înregistrat.');
            } else {
                const data = await res.json();
                toast.error(data.message || 'Înregistrarea a eșuat.');
            }
        } catch (err) {
            toast.error('Serverul nu poate fi contactat. Încearcă mai târziu.');
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">🌬️ Respiră inteligent</div>
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
                        <h1 className="text-3xl font-bold tracking-wide">Bine ai venit</h1>
                        <p className="text-sm text-gray-400">Monitorizează calitatea aerului din zona ta</p>
                    </div>
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <input
                                type="text"
                                name="fullName"
                                value={form.fullName}
                                onChange={handleChange}
                                className={`w-full px-4 py-3 bg-gray-800 border ${
                                    errors.fullName ? 'border-red-500' : 'border-gray-600'
                                } rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500`}
                                placeholder="Nume complet"
                            />
                            {errors.fullName && <p className="text-red-400 text-xs mt-1">{errors.fullName}</p>}
                        </div>

                        <div>
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
                                className={`w-full px-4 py-3 bg-gray-800 border ${
                                    errors.email ? 'border-red-500' : 'border-gray-600'
                                } rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500`}
                                placeholder="Adresă email"
                            />
                            {errors.email && <p className="text-red-400 text-xs mt-1">{errors.email}</p>}
                        </div>

                        <div>
                            <input
                                type="password"
                                name="password"
                                value={form.password}
                                onChange={handleChange}
                                className={`w-full px-4 py-3 bg-gray-800 border ${
                                    errors.password ? 'border-red-500' : 'border-gray-600'
                                } rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500`}
                                placeholder="Parolă"
                            />
                            {errors.password && <p className="text-red-400 text-xs mt-1">{errors.password}</p>}
                        </div>

                        <button
                            type="submit"
                            className="w-full bg-teal-600 hover:bg-teal-700 text-white font-semibold py-3 rounded-lg transition cursor-pointer"
                        >
                            Înregistrează-te
                        </button>
                    </form>
                    <p className="text-xs text-center mt-4 text-gray-400">
                        Ai deja un cont?{' '}
                        <a href="/login" className="text-teal-400 underline hover:text-teal-300">
                            Autentifică-te aici
                        </a>
                    </p>
                </div>
            </div>

            {/* Footer */}
            <footer className="py-6 text-center text-gray-400 text-sm">
                © {new Date().getFullYear()} Air Quality Monitor. Toate drepturile rezervate.
            </footer>

            {/* Toast */}
            <Toaster position="top-center" reverseOrder={false} />
        </div>
    );
}
