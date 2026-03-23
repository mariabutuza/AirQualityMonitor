'use client';

import { useEffect, useState } from 'react';

type Threshold = {
    location: string;
    metricType: string;
    thresholdValue: number;
};

export default function ThresholdPage() {
    const [thresholds, setThresholds] = useState<Threshold[]>([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const locations = ['Mărăști', 'Centru', 'Între Lacuri'];

    const fetchThresholds = async () => {
        setLoading(true);
        const token = localStorage.getItem('token');
        try {
            const res = await fetch('http://localhost:8081/api/thresholds', {
                headers: { Authorization: `Bearer ${token}` },
            });
            const json = await res.json();
            const mapped: Threshold[] = locations.map((loc) => {
                const found = json.find((t: any) => t.location === loc && t.metricType === "AIR_CO2_EQ_PPM");
                return {
                    location: loc,
                    metricType: "AIR_CO2_EQ_PPM",
                    thresholdValue: found ? found.thresholdValue : 0};
            });
            setThresholds(mapped);
        } catch (err) {
            setMessage('Failed to load thresholds.');
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (loc: string, value: number) => {
        setThresholds((prev) =>
            prev.map((t) => (t.location === loc ? { ...t, thresholdValue: value } : t))
        );
    };

    const saveThreshold = async (loc: string) => {
        const token = localStorage.getItem('token');
        const threshold = thresholds.find((t) => t.location === loc);
        if (!threshold) return;

        try {
            const res = await fetch('http://localhost:8081/api/thresholds', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    location: threshold.location,
                    thresholdValue: threshold.thresholdValue,
                    metricType: threshold.metricType,
                    comparator: "GREATER_THAN",
                    enabled: true,
                }),
            });

            if (res.ok) {
                setMessage(`✅ Threshold for ${loc} saved.`);
            } else {
                setMessage(`❌ Failed to save for ${loc}.`);
            }
        } catch {
            setMessage(`⚠️ Error saving threshold.`);
        }
    };

    useEffect(() => {
        fetchThresholds();
    }, []);

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white p-6">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">
                    🌬️ Respiră inteligent                </div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/dashboard" className="hover:text-teal-400 transition">Acasă</a>
                    <a href="/about" className="hover:text-teal-400 transition">Despre</a>
                    <a href="/profilepage" className="hover:text-teal-400 transition">Profil</a>
                    <button
                        onClick={() => {
                            localStorage.removeItem('token');
                            window.location.href = '/login';
                        }}
                        className="hover:text-red-400 transition"
                    >
                        Logout
                    </button>
                </div>
            </nav>

            <div className="max-w-5xl mx-auto mt-24 space-y-10">
                <h2 className="text-3xl font-bold text-center mb-6">Praguri de alertă</h2>

                {loading ? (
                    <p className="text-teal-400 text-center">Loading thresholds...</p>
                ) : (
                    <div className="space-y-6">
                        {thresholds.map((t) => (
                            <div key={t.location} className="bg-black bg-opacity-60 border border-teal-500 rounded-xl p-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                                <div>
                                    <p className="text-xl font-semibold text-teal-300">{t.location}</p>
                                    <input
                                        type="number"
                                        min={0}
                                        value={isNaN(t.thresholdValue) ? '' : t.thresholdValue}
                                        onChange={(e) => handleChange(t.location, parseFloat(e.target.value))}
                                    />
                                </div>
                                <button
                                    onClick={() => saveThreshold(t.location)}
                                    className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-6 py-2 rounded-lg transition"
                                >
                                    Save
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {message && (
                    <p className="mt-6 text-center text-sm text-teal-400">{message}</p>
                )}
            </div>
        </div>
    );
}
