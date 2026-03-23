'use client';

import { useRouter } from 'next/navigation';

export default function LandingPage() {
    const router = useRouter();
    return (
        <div className="relative min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-800 overflow-hidden flex items-center justify-center px-4">

            {/* Cloudy background layer */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {/* Back cloud layer */}
                <div className="absolute top-0 left-0 w-full h-full bg-[url('/clouds.svg')] bg-repeat-x bg-cover opacity-30 animate-clouds-slow pointer-events-none" />

                {/* Front cloud layer */}
                <div className="absolute top-0 left-0 w-full h-full bg-[url('/clouds.svg')] bg-repeat-x bg-cover opacity-60 animate-clouds pointer-events-none" />
            </div>

            {/* Particle layer */}
            <canvas id="particles-canvas" className="absolute inset-0 pointer-events-none" />

            {/* Hero content */}
            <div className="relative z-10 text-center max-w-2xl text-white space-y-6">
                <h1 className="text-5xl font-extrabold">🌬️ Respiră inteligent 🌬️</h1>
                <p className="text-lg text-gray-300">
                    Descoperă nivelurile periculoase de CO2, benzen sau fum din apropierea ta.
                </p>
                <button
                    onClick={() => router.push('/homepage')}
                    className="bg-teal-500 hover:bg-teal-600 text-white font-semibold px-6 py-3 rounded-full text-lg shadow-lg transition cursor-pointer"
                >
                    Începe
                </button>
            </div>
        </div>
    );
}
