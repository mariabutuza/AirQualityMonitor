'use client';

import { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Title,
    Tooltip,
    Legend,
} from 'chart.js';
import toast, { Toaster } from 'react-hot-toast';

ChartJS.register(LineElement, PointElement, LinearScale, CategoryScale, Title, Tooltip, Legend);

type Threshold = {
    metricType: string;
    thresholdValue: number;
    location: string;
};

type TimeSeriesPoint = {
    timestamp: string;
    value: number;
};

type LatestResponse = {
    timestamp: string;
    value: number;
};

const ZONES: { [key: string]: number } = {
    Marasti: 1,
    Centru: 2,
    //'Intre Lacuri': 3,
};

export default function DashboardPage() {
    const [zone, setZone] = useState<string>('Marasti');
    const [sensorId, setSensorId] = useState<number>(ZONES['Marasti']);
    const [threshold, setThreshold] = useState<Threshold | null>(null);
    const [latest, setLatest] = useState<LatestResponse | null>(null);
    const [series, setSeries] = useState<TimeSeriesPoint[]>([]);
    const [status, setStatus] = useState('Loading...');

    const metricType = 'AIR_CO2_EQ_PPM';

    const fetchThreshold = async () => {
        const token = localStorage.getItem("token");
        const res = await fetch("http://localhost:8081/api/thresholds", {
            headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) return;
        const json: Threshold[] = await res.json();

        const normalize = (str: string) =>
            str.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();

        let match = json.find(
            (t) =>
                t.metricType === metricType &&
                normalize(t.location) === normalize(zone)
        );

        if (!match) {
            match = json.find(
                (t: any) => t.metricType === metricType && t.default === true
            );
        }

        if (match) {
            setThreshold(match);
        } else {
            setThreshold(null);
        }
    };

    const fetchLatest = async () => {
        const token = localStorage.getItem('token');
        const res = await fetch(
            `http://localhost:8081/api/sensors/${sensorId}/latest?metricType=${metricType}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        if (!res.ok) return;
        const json: LatestResponse = await res.json();
        setLatest(json);
    };

    const fetchAggregate = async () => {
        const token = localStorage.getItem('token');
        const now = new Date();
        const past = new Date(now.getTime() - 30 * 60000);

        const res = await fetch(
            `http://localhost:8081/api/sensors/${sensorId}/aggregate?metricType=${metricType}&from=${past.toISOString()}&to=${now.toISOString()}&bucket=PT1M`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        if (!res.ok) return;
        const json = await res.json();
        setSeries(json.points || []);
    };

    const handleDownloadReport = async (fileType: 'pdf' | 'csv') => {
        const token = localStorage.getItem('token');
        const now = new Date();
        const past = new Date(now.getTime() - 30 * 60000);

        const res = await fetch(
            `http://localhost:8081/api/sensors/${sensorId}/report?metricType=${metricType}&from=${past.toISOString()}&to=${now.toISOString()}&fileType=${fileType}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        if (!res.ok) {
            toast.error('❌ Eroare la descărcarea raportului.');
            return;
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `report.${fileType}`;
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        toast.success(`Raport ${fileType.toUpperCase()} descărcat cu succes!`);
    };

    const handleSendReportEmail = async (fileType: 'pdf' | 'csv') => {
        const token = localStorage.getItem('token');
        const now = new Date();
        const past = new Date(now.getTime() - 30 * 60000);

        try {
            const res = await fetch(
                `http://localhost:8081/api/sensors/${sensorId}/send-report?metricType=${metricType}&from=${past.toISOString()}&to=${now.toISOString()}&fileType=${fileType}`,
                { method: 'POST', headers: { Authorization: `Bearer ${token}` } }
            );

            if (res.ok) {
                toast.success(`Raport ${fileType.toUpperCase()} a fost trimis pe email!`);
            } else {
                toast.error('❌ Eroare la trimiterea raportului pe email.');
            }
        } catch {
            toast.error('⚠️ Conexiune eșuată. Încearcă din nou.');
        }
    };

    useEffect(() => {
        setSensorId(ZONES[zone]);
        setStatus("Loading...");
    }, [zone]);

    useEffect(() => {
        fetchThreshold();
    }, [metricType]);

    useEffect(() => {
        fetchLatest();
        fetchAggregate();
        const interval = setInterval(() => {
            fetchLatest();
            fetchAggregate();
        }, 5000);
        return () => clearInterval(interval);
    }, [sensorId]);

    useEffect(() => {
        if (!latest || !threshold) {
            setStatus("⚠️ Niciun prag găsit pentru zona selectată");
            return;
        }
        const isDanger = latest.value > threshold.thresholdValue;
        setStatus(isDanger ? "🚨 În pericol" : "✅ În siguranță");
    }, [latest, threshold]);


    useEffect(() => {
        fetchThreshold();
    }, [metricType, zone]);

    const chartData = {
        labels: series.map((d) => {
            if (!d.timestamp) return '';
            const iso = d.timestamp.includes('T') ? d.timestamp : d.timestamp.replace(' ', 'T');
            return new Date(iso).toLocaleTimeString();
        }),
        datasets: [
            {
                label: 'CO₂ echivalent (ppm)',
                data: series.map((d) => d.value),
                borderColor: '#14b8a6',
                fill: false,
                tension: 0.2,
                pointRadius: 3,
            },
            {
                label: `Threshold (${threshold?.thresholdValue ?? "N/A"})`,
                data: series.map(() => threshold?.thresholdValue ?? 0),
                borderColor: "red",
                borderDash: [5, 5],
                pointRadius: 0,
            },
        ],
    };

    const chartOptions = {
        responsive: true,
        plugins: {
            legend: { display: true },
            title: { display: true, text: `Evoluție CO₂ - ultimele 30 minute` },
        },
        scales: {
            y: { suggestedMin: 0, suggestedMax: 5000 },
        },
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white px-4 py-8">
            <Toaster position="top-center" />

            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">🌬️ Respiră inteligent</div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/dashboard" className="text-teal-400 font-semibold underline">Acasă</a>
                    <a href="/profilepage" className="hover:text-teal-400 transition">Profil</a>
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

            {/* Conținut */}
            <div className="max-w-5xl mx-auto mt-24 space-y-10">
                {/* Header */}
                <div className="text-center space-y-4">
                    <h2 className="text-4xl font-extrabold text-white">🌫️ Panou de monitorizare a calității aerului</h2>
                    <p className="text-gray-300">Datele afișate sunt valorile măsurate (ppm) pentru zona selectată.</p>
                </div>

                {/* Alegere zonă */}
                <div className="flex flex-col items-center gap-2 mt-6">
                    <p className="text-sm text-gray-400">Selectează zona:</p>
                    <select
                        value={zone}
                        onChange={(e) => setZone(e.target.value)}
                        className="bg-gray-800 text-white border border-teal-500 rounded-lg px-4 py-2"
                    >
                        <option value="Marasti">Mărăști</option>
                        <option value="Centru">Centru</option>
                        {/*<option value="Intre Lacuri">Între Lacuri</option>*/}
                    </select>
                </div>

                {/* Ultima valoare */}
                <div className="bg-black bg-opacity-60 border border-teal-500 rounded-2xl shadow-xl p-8 text-center">
                    {latest ? (
                        <>
                            <p className="text-sm text-gray-300">🕒 {new Date(latest.timestamp).toLocaleString()}</p>
                            <p className="text-6xl font-bold mt-4 text-teal-400">{latest.value.toFixed(2)} ppm</p>
                            <p className={`mt-4 text-2xl ${status.includes('Danger') ? 'text-red-500' : 'text-green-400'}`}>{status}</p>
                        </>
                    ) : (
                        <p className="text-teal-400">No data available.</p>
                    )}
                </div>

                {/* Buton setare praguri */}
                <div className="text-center mt-10">
                    <a
                        href="/thresholds"
                        className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-6 py-3 rounded-lg inline-block"
                    >
                        ⚙️ Setează praguri personalizate
                    </a>
                </div>

                {/* Grafic */}
                <div className="bg-black bg-opacity-60 border border-teal-500 rounded-2xl p-6">
                    <Line data={chartData} options={chartOptions} />
                </div>

                {/* Raport */}
                <div className="bg-black bg-opacity-60 border border-teal-500 rounded-xl p-6 text-center mt-10 space-y-6">
                    <h3 className="text-2xl font-bold text-teal-400 mb-4">📥 Generează raport</h3>

                    {/* Download local */}
                    <div>
                        <h4 className="text-lg text-gray-300 mb-2">Descarcă raport</h4>
                        <div className="flex flex-col sm:flex-row justify-center gap-4">
                            <button onClick={() => handleDownloadReport('pdf')} className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-6 py-3 rounded-lg">📄 PDF</button>
                            <button onClick={() => handleDownloadReport('csv')} className="bg-teal-500 hover:bg-teal-600 text-white font-semibold px-6 py-3 rounded-lg">📊 CSV</button>
                        </div>
                    </div>

                    {/* Send email */}
                    <div>
                        <h4 className="text-lg text-gray-300 mb-2">Trimite pe email</h4>
                        <div className="flex flex-col sm:flex-row justify-center gap-4">
                            <button onClick={() => handleSendReportEmail('pdf')} className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-6 py-3 rounded-lg">📧 PDF pe email</button>
                            <button onClick={() => handleSendReportEmail('csv')} className="bg-teal-600 hover:bg-teal-700 text-white font-semibold px-6 py-3 rounded-lg">📧 CSV pe email</button>
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <footer className="py-6 text-center text-gray-400 text-sm">
                    © {new Date().getFullYear()} Air Quality Monitor. Toate drepturile rezervate.
                </footer>
            </div>
        </div>
    );
}