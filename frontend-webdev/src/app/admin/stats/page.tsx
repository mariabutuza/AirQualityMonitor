"use client";

import { useEffect, useState } from "react";
import AdminGuard from "@/app/hooks/AdminGuard";
import {
    PieChart,
    Pie,
    Cell,
    Tooltip,
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Legend,
} from "recharts";

type Sensor = { id: number; name: string; location: string; active: boolean };

export default function AdminStatsPage() {
    const [deviceLocations, setDeviceLocations] = useState<{ name: string; value: number }[]>([]);
    const [sensors, setSensors] = useState<Sensor[]>([]);
    const [selectedSensor, setSelectedSensor] = useState<number | null>(null);
    const [co2Data, setCo2Data] = useState<any[]>([]);
    const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042"];

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) return;

        async function fetchData() {
            try {
                const resSensors = await fetch("http://localhost:8081/api/admin/sensors", {
                    headers: { Authorization: `Bearer ${token}` },
                });

                if (resSensors.ok) {
                    const sensorsData: Sensor[] = await resSensors.json();
                    setSensors(sensorsData);

                    const grouped: Record<string, number> = {};
                    sensorsData.forEach((s) => {
                        const key = s.location.trim().toLowerCase();
                        grouped[key] = (grouped[key] || 0) + 1;
                    });

                    setDeviceLocations(
                        Object.entries(grouped).map(([loc, count]) => ({
                            name: loc.charAt(0).toUpperCase() + loc.slice(1),
                            value: count,
                        }))
                    );

                    if (sensorsData.length > 0) {
                        setSelectedSensor(sensorsData[0].id);
                    }
                }
            } catch (err) {
                console.error("Eroare la fetch senzori:", err);
            }
        }

        fetchData();
    }, []);

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token || !selectedSensor) return;

        async function fetchCO2() {
            try {
                const now = new Date();
                const to = now.toISOString();
                const from = new Date(now.getTime() - 24 * 60 * 60 * 1000).toISOString();

                const resCO2 = await fetch(
                    `http://localhost:8081/api/sensors/${selectedSensor}/aggregate?metricType=AIR_CO2_EQ_PPM&from=${from}&to=${to}&bucket=PT1H`,
                    { headers: { Authorization: `Bearer ${token}` } }
                );

                if (resCO2.ok) {
                    const data = await resCO2.json();
                    console.log("📊 Răspuns CO₂ aggregate:", data);

                    if (data && Array.isArray(data.points)) {
                        setCo2Data(
                            data.points.map((p: any) => {
                                const ts = new Date(p.ts);

                                return {
                                    time: ts.toLocaleTimeString("ro-RO", {
                                        hour: "2-digit",
                                        minute: "2-digit",
                                    }),
                                    avg: p.value,
                                };
                            })
                        );
                        console.log("📊 co2Data procesat:", data.points.map((p: any) => ({
                            timestamp: p.timestamp,
                            avg: p.avg
                        })));
                        console.log("📊 RAW points:", data.points);


                    } else {
                        setCo2Data([]);
                    }
                }
                } catch (err) {
                console.error("Eroare la fetch CO₂:", err);
            }
        }

        fetchCO2();
    }, [selectedSensor]);

    return (
        <AdminGuard>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-gray-950 text-white p-6">
                <h1 className="text-3xl font-bold mb-6">📊 Statistici Admin</h1>

                {/* Distribuție Device-uri */}
                <div className="bg-gray-800 p-6 rounded-xl mb-8">
                    <h2 className="text-xl font-semibold mb-4">📡 Distribuție Device-uri pe locații</h2>
                    {deviceLocations.length === 0 ? (
                        <p className="text-gray-400">Nu există device-uri înregistrate.</p>
                    ) : (
                        <PieChart width={400} height={300}>
                            <Pie
                                data={deviceLocations}
                                dataKey="value"
                                nameKey="name"
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                label
                            >
                                {deviceLocations.map((_, index) => (
                                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                        </PieChart>
                    )}
                </div>

                {/* Evoluția CO₂ */}
                <div className="bg-gray-800 p-6 rounded-xl mb-8">
                    <h2 className="text-xl font-semibold mb-4">🌫️ Evoluția CO₂ în ultimele 24h</h2>

                    {/* Selector senzor */}
                    <div className="mb-4">
                        <label className="mr-2">Alege senzor:</label>
                        <select
                            value={selectedSensor ?? ""}
                            onChange={(e) => setSelectedSensor(Number(e.target.value))}
                            className="bg-gray-700 text-white px-2 py-1 rounded"
                        >
                            {sensors.map((s) => (
                                <option key={s.id} value={s.id}>
                                    {s.name} ({s.location})
                                </option>
                            ))}
                        </select>
                    </div>

                    {co2Data.length === 0 ? (
                        <p className="text-gray-400">⚠️ Nu există date pentru acest senzor în ultimele 24h.</p>
                    ) : (
                        <LineChart width={600} height={300} data={co2Data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="time" />
                            <YAxis />
                            <Tooltip />
                            <Legend />
                            <Line type="monotone" dataKey="avg" stroke="#ff7300" name="CO₂ ppm" />
                        </LineChart>
                    )}
                </div>
            </div>
        </AdminGuard>
    );
}
