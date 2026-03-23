"use client";

export default function UnauthorizedPage() {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-black text-white">
            <h1 className="text-4xl font-bold text-red-500 mb-4">⛔ Acces restricționat</h1>
            <p className="text-gray-300 mb-6">Nu ai permisiunea să accesezi această pagină.</p>
            <a
                href="/dashboard"
                className="px-4 py-2 bg-teal-600 hover:bg-teal-700 rounded-md transition"
            >
                Înapoi la Dashboard
            </a>
        </div>
    );
}
