"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import toast from "react-hot-toast";
import AdminGuard from "@/app/hooks/AdminGuard";

type User = { id: number; fullName: string; email: string; role: string };
type SortOption = "createdAt,desc" | "createdAt,asc" | "fullName,asc" | "fullName,desc";

export default function AdminUsersPage() {
    const router = useRouter();

    const [users, setUsers] = useState<User[]>([]);
    const [search, setSearch] = useState("");
    const [role, setRole] = useState("ALL");
    const [emailDomain, setEmailDomain] = useState("");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");
    const [sort, setSort] = useState<SortOption>("createdAt,desc");
    const [page, setPage] = useState(0);
    const [size] = useState(5);
    const [totalPages, setTotalPages] = useState(1);
    const [editUser, setEditUser] = useState<User | null>(null);
    const [confirmDelete, setConfirmDelete] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [currentUserEmail, setCurrentUserEmail] = useState<string | null>(null);

    useEffect(() => {
        setCurrentUserEmail(localStorage.getItem("currentUserEmail"));
    }, []);

    async function fetchUsers() {
        setIsLoading(true);
        const token = localStorage.getItem("token");
        if (!token) {
            toast.error("Nu ești autentificat(ă).");
            router.push("/login");
            setIsLoading(false);
            return;
        }
        const params = new URLSearchParams({
            search,
            role: role !== "ALL" ? role : "",
            emailDomain,
            from: fromDate,
            to: toDate,
            sort,
            page: page.toString(),
            size: size.toString(),
        });
        try {
            const res = await fetch(`http://localhost:8080/api/user/all?${params}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(await res.text());
            const data = await res.json();
            setUsers(data.content);
            setTotalPages(data.totalPages);
        } catch {
            toast.error("Eroare la încărcarea utilizatorilor.");
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        fetchUsers();
    }, [search, role, emailDomain, fromDate, toDate, sort, page]);

    const resetFilters = () => {
        setSearch("");
        setRole("ALL");
        setEmailDomain("");
        setFromDate("");
        setToDate("");
        setSort("createdAt,desc");
        setPage(0);
    };

    const handleUpdate = async () => {
        if (!editUser) return;
        const token = localStorage.getItem("token");
        if (!token) return;
        try {
            const res = await fetch(`http://localhost:8080/api/user/update/${editUser.id}`, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(editUser),
            });
            if (!res.ok) throw new Error(await res.text());
            const updated = await res.json();
            toast.success("Email actualizat cu succes!");
            if (currentUserEmail && editUser.email !== currentUserEmail) {
                toast("Email schimbat. Te vom deconecta...", { icon: "🔄" });
                setTimeout(() => {
                    localStorage.removeItem("token");
                    localStorage.removeItem("currentUserEmail");
                    router.push("/login");
                }, 2000);
            } else {
                fetchUsers();
            }
            setEditUser(null);
        } catch {
            toast.error("Eroare la actualizare.");
        }
    };

    const handleDelete = async () => {
        if (!confirmDelete) return;
        const token = localStorage.getItem("token");
        if (!token) return;
        try {
            const res = await fetch(
                `http://localhost:8080/api/user/delete/${confirmDelete.id}`,
                {
                    method: "DELETE",
                    headers: { Authorization: `Bearer ${token}` },
                }
            );
            if (!res.ok) throw new Error(await res.text());
            toast.success("Utilizator șters.");
            fetchUsers();
            setConfirmDelete(null);
        } catch {
            toast.error("Eroare la ștergere.");
        }
    };

    return (
        <AdminGuard>
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-gray-950 text-white py-6 px-4 flex flex-col items-center">
            <div className="w-full max-w-7xl mx-auto px-2">
                <h1 className="text-4xl font-semibold mb-3 text-center">Administrare Utilizatori</h1>
                <p className="text-gray-400 mb-10 text-center">Filtrează, sortează și gestionează utilizatorii cu ușurință.</p>

                {/* Filtre compacte pe un singur rând */}
                <div className="flex gap-6 mb-6 whitespace-nowrap w-full justify-center">
                    {/* Inputuri scurte, fără margini excesive */}
                    <div className="min-w-[200px]">
                        <label className="text-xs text-gray-300 mb-1 block">Căutare</label>
                        <input
                            type="text"
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                            placeholder="ex: Maria, admin, @gmail"
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white placeholder-gray-500"
                        />
                    </div>
                    <div className="min-w-[160px]">
                        <label className="text-xs text-gray-300 mb-1 block">Sortare</label>
                        <select
                            value={sort}
                            onChange={e => setSort(e.target.value as SortOption)}
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white"
                        >
                            <option value="createdAt,desc">Cei mai recenți</option>
                            <option value="createdAt,asc">Cei mai vechi</option>
                            <option value="fullName,asc">Nume A → Z</option>
                            <option value="fullName,desc">Nume Z → A</option>
                        </select>
                    </div>
                    <div className="min-w-[120px]">
                        <label className="text-xs text-gray-300 mb-1 block">Rol</label>
                        <select
                            value={role}
                            onChange={e => setRole(e.target.value)}
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white"
                        >
                            <option value="ALL">Toate</option>
                            <option value="USER">USER</option>
                            <option value="ADMIN">ADMIN</option>
                        </select>
                    </div>
                    <div className="min-w-[150px]">
                        <label className="text-xs text-gray-300 mb-1 block">Domeniu email</label>
                        <input
                            type="text"
                            value={emailDomain}
                            onChange={e => setEmailDomain(e.target.value)}
                            placeholder="ex: gmail.com"
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white placeholder-gray-500"
                        />
                    </div>
                    <div className="min-w-[170px]">
                        <label className="text-xs text-gray-300 mb-1 block">Din data</label>
                        <input
                            type="datetime-local"
                            value={fromDate}
                            onChange={e => setFromDate(e.target.value)}
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white"
                        />
                    </div>
                    <div className="min-w-[170px]">
                        <label className="text-xs text-gray-300 mb-1 block">Până la</label>
                        <input
                            type="datetime-local"
                            value={toDate}
                            onChange={e => setToDate(e.target.value)}
                            className="w-full px-2 py-1 bg-gray-800 border border-gray-700 rounded-md text-white"
                        />
                    </div>
                </div>

                {/* Reset filtre */}
                <div className="flex justify-center mb-12">
                    <button
                        onClick={resetFilters}
                        className="bg-teal-600 hover:bg-teal-700 text-white px-5 py-1 rounded-md text-sm"
                    >
                        Reset filtre
                    </button>
                </div>

                {/* Tabelul utilizatorilor */}
                <div className="overflow-auto shadow-lg border border-gray-700 rounded-md">
                    {isLoading ? (
                        <p className="text-gray-300 text-center mt-10">Se încarcă utilizatorii...</p>
                    ) : !users.length ? (
                        <p className="text-gray-400 text-center mt-10">Niciun utilizator găsit.</p>
                    ) : (
                        <table className="min-w-full bg-gray-900 text-sm text-left">
                            <thead className="bg-teal-800 text-white text-xs uppercase">
                            <tr className="text-center">
                                <th className="px-6 py-3">ID</th>
                                <th className="px-6 py-3">Nume</th>
                                <th className="px-6 py-3">Email</th>
                                <th className="px-6 py-3">Rol</th>
                                <th className="px-6 py-3">Acțiuni</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map(u => (
                                <tr key={u.id} className="border-t border-gray-700 hover:bg-gray-800 text-center">
                                    <td className="px-6 py-4">{u.id}</td>
                                    <td className="px-6 py-4">{u.fullName}</td>
                                    <td className="px-6 py-4">{u.email}</td>
                                    <td className="px-6 py-4">{u.role}</td>
                                    <td className="px-6 py-4 space-x-2">
                                        <button
                                            onClick={() => setEditUser(u)}
                                            className="bg-teal-600 hover:bg-teal-700 text-white px-2 py-1 rounded-md text-sm"
                                        >
                                            Editare
                                        </button>
                                        <button
                                            onClick={() => setConfirmDelete(u)}
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

                {/* Paginare sub tabel */}
                <div className="flex justify-center items-center gap-4 mt-3 mb-8">
                    <button
                        onClick={() => setPage(p => Math.max(p - 1, 0))}
                        disabled={page === 0}
                        className="px-4 py-1 bg-teal-600 hover:bg-teal-700 disabled:opacity-50 rounded-md text-white"
                    >
                        Previous
                    </button>
                    <span className="text-gray-300">Page {page + 1} of {totalPages}</span>
                    <button
                        onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
                        disabled={page >= totalPages - 1}
                        className="px-4 py-1 bg-teal-600 hover:bg-teal-700 disabled:opacity-50 rounded-md text-white"
                    >
                        Next
                    </button>
                </div>

                {/* Modal Edit */}
                {editUser && (
                    <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50">
                        <div className="bg-gray-900 p-6 rounded-md w-[90%] max-w-md">
                            <h2 className="text-xl font-semibold mb-4">Editează Utilizator</h2>
                            <input
                                className="w-full mb-3 px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white"
                                value={editUser.fullName}
                                onChange={e => setEditUser({ ...editUser, fullName: e.target.value })}
                            />
                            <input
                                className="w-full mb-3 px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white"
                                value={editUser.email}
                                onChange={e => setEditUser({ ...editUser, email: e.target.value })}
                            />
                            <select
                                className="w-full mb-4 px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white"
                                value={editUser.role}
                                onChange={e => setEditUser({ ...editUser, role: e.target.value })}
                            >
                                <option value="USER">USER</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                            <div className="flex justify-end gap-2">
                                <button
                                    onClick={() => setEditUser(null)}
                                    className="px-3 py-1 bg-gray-700 hover:bg-gray-600 text-white rounded-md"
                                >
                                    Anulează
                                </button>
                                <button
                                    onClick={handleUpdate}
                                    className="px-3 py-1 bg-teal-500 hover:bg-teal-600 text-white rounded-md"
                                >
                                    Salvează
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Modal Delete */}
                {confirmDelete && (
                    <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50">
                        <div className="bg-gray-900 p-6 rounded-md w-[90%] max-w-md text-center">
                            <h2 className="text-xl font-semibold mb-4 text-red-400">Confirmare Ștergere</h2>
                            <p className="mb-4">
                                Vrei să ștergi utilizatorul <strong>{confirmDelete.email}</strong>?
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
