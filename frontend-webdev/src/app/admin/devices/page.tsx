"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import toast from "react-hot-toast";
import AdminGuard from "@/app/hooks/AdminGuard";

type Device = {
    id: number;
    deviceId: string;
    name: string;
    location: string;
    active: boolean;
};

export default function AdminDevicesPage() {
    const router = useRouter();

    const [devices, setDevices] = useState<Device[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [newDevice, setNewDevice] = useState({ deviceId: "", name: "", location: "" });
    const [createdKey, setCreatedKey] = useState<string | null>(null);
    const [confirmDelete, setConfirmDelete] = useState<Device | null>(null);

    async function fetchDevices() {
        setIsLoading(true);
        const token = localStorage.getItem("token");
        if (!token) {
            toast.error("Nu ești autentificat(ă).");
            router.push("/login");
            return;
        }
        try {
            const res = await fetch("http://localhost:8081/api/admin/sensors", {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(await res.text());
            setDevices(await res.json());
        } catch {
            toast.error("Eroare la încărcarea device-urilor.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        fetchDevices();
    }, []);

    async function handleCreate() {
        const token = localStorage.getItem("token");
        if (!token) return;

        if (!newDevice.deviceId || !newDevice.name || !newDevice.location) {
            toast.error("Completează toate câmpurile.");
            return;
        }

        try {
            const res = await fetch("http://localhost:8081/api/admin/sensors", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ ...newDevice, active: true }),
            });
            if (!res.ok) throw new Error(await res.text());
            const created = await res.json();
            toast.success("Device creat cu succes!");
            setCreatedKey(created.ingestKey);
            setNewDevice({ deviceId: "", name: "", location: "" });
            fetchDevices();
        } catch {
            toast.error("Eroare la adăugarea device-ului.");
        }
    }

    async function handleDelete() {
        if (!confirmDelete) return;
        const token = localStorage.getItem("token");
        if (!token) return;

        try {
            const res = await fetch(
                `http://localhost:8081/api/admin/sensors/${confirmDelete.id}`,
                { method: "DELETE", headers: { Authorization: `Bearer ${token}` } }
            );
            if (!res.ok) throw new Error(await res.text());
            toast.success("Device șters!");
            fetchDevices();
            setConfirmDelete(null);
        } catch {
            toast.error("Eroare la ștergerea device-ului.");
        }
    }

    return (
        <AdminGuard>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-gray-950 text-white py-6 px-4 flex flex-col items-center">
                <div className="w-full max-w-6xl mx-auto">
                    <h1 className="text-4xl font-semibold mb-3 text-center">Administrare Device-uri</h1>
                    <p className="text-gray-400 mb-10 text-center">Adaugă, șterge sau editează device-uri de monitorizare.</p>

                    {/* Form Add */}
                    <div className="bg-gray-800 p-4 rounded-md mb-8 shadow-lg border border-gray-700">
                        <h2 className="text-lg font-semibold mb-3">Adaugă Device Nou</h2>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <input
                                className="px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-white"
                                placeholder="Device ID"
                                value={newDevice.deviceId}
                                onChange={e => setNewDevice({ ...newDevice, deviceId: e.target.value })}
                            />
                            <input
                                className="px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-white"
                                placeholder="Nume"
                                value={newDevice.name}
                                onChange={e => setNewDevice({ ...newDevice, name: e.target.value })}
                            />
                            <input
                                className="px-3 py-2 bg-gray-900 border border-gray-700 rounded-md text-white"
                                placeholder="Locație"
                                value={newDevice.location}
                                onChange={e => setNewDevice({ ...newDevice, location: e.target.value })}
                            />
                        </div>
                        <button
                            onClick={handleCreate}
                            className="mt-4 px-5 py-2 bg-teal-600 hover:bg-teal-700 rounded-md text-white"
                        >
                            Adaugă Device
                        </button>
                    </div>

                    {/* Show Ingest Key */}
                    {createdKey && (
                        <div className="bg-black/70 p-4 mb-8 rounded-md text-center border border-teal-600">
                            <p className="text-teal-400">Cheia de ingest generată pentru device nou:</p>
                            <code className="block mt-2 text-lg text-white">{createdKey}</code>
                            <p className="text-xs text-gray-400 mt-2">⚠️ Salvează această cheie! Nu o vei mai putea vedea ulterior.</p>
                        </div>
                    )}

                    {/* Table */}
                    <div className="overflow-auto shadow-lg border border-gray-700 rounded-md">
                        {isLoading ? (
                            <p className="text-gray-300 text-center mt-10">Se încarcă device-urile...</p>
                        ) : !devices.length ? (
                            <p className="text-gray-400 text-center mt-10">Niciun device găsit.</p>
                        ) : (
                            <table className="min-w-full bg-gray-900 text-sm text-left">
                                <thead className="bg-teal-800 text-white text-xs uppercase">
                                <tr className="text-center">
                                    <th className="px-6 py-3">ID</th>
                                    <th className="px-6 py-3">Device ID</th>
                                    <th className="px-6 py-3">Nume</th>
                                    <th className="px-6 py-3">Locație</th>
                                    <th className="px-6 py-3">Activ</th>
                                    <th className="px-6 py-3">Acțiuni</th>
                                </tr>
                                </thead>
                                <tbody>
                                {devices.map(d => (
                                    <tr key={d.id} className="border-t border-gray-700 hover:bg-gray-800 text-center">
                                        <td className="px-6 py-4">{d.id}</td>
                                        <td className="px-6 py-4">{d.deviceId}</td>
                                        <td className="px-6 py-4">{d.name}</td>
                                        <td className="px-6 py-4">{d.location}</td>
                                        <td className="px-6 py-4">{d.active ? "✔️" : "❌"}</td>
                                        <td className="px-6 py-4 space-x-2">
                                            {/* Pentru edit mai târziu */}
                                            <button
                                                onClick={() => setConfirmDelete(d)}
                                                className="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded-md text-sm"
                                            >
                                                Ștergere
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>

                    {/* Modal Delete */}
                    {confirmDelete && (
                        <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50">
                            <div className="bg-gray-900 p-6 rounded-md w-[90%] max-w-md text-center">
                                <h2 className="text-xl font-semibold mb-4 text-red-400">Confirmare Ștergere</h2>
                                <p className="mb-4">
                                    Vrei să ștergi device-ul <strong>{confirmDelete.deviceId}</strong>?
                                </p>
                                <div className="flex justify-center gap-4">
                                    <button
                                        onClick={() => setConfirmDelete(null)}
                                        className="px-3 py-1 bg-gray-700 hover:bg-gray-600 text-white rounded-md"
                                    >
                                        Anulează
                                    </button>
                                    <button
                                        onClick={handleDelete}
                                        className="px-3 py-1 bg-red-500 hover:bg-red-600 text-white rounded-md"
                                    >
                                        Șterge
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </AdminGuard>
    );
}
