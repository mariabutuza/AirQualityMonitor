'use client';
import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const testimonials = [
    {
        name: 'Andreea P.',
        role: 'Director școală',
        text: 'De când folosim aplicația în școala noastră, părinții sunt mai liniștiți și copiii mai protejați.',
        rating: 5,
    },
    {
        name: 'Radu T.',
        role: 'Manager IT',
        text: 'Folosesc AirQuality pentru biroul meu. Când aerul devine periculos, știu exact ce măsuri să iau.',
        rating: 4,
    },
    {
        name: 'Simona M.',
        role: 'Mamă a doi copii',
        text: 'Aplicația m-a ajutat să aflu cât de periculos era aerul din camera copiilor. Acum știu când să aerisesc.',
        rating: 5,
    },
];

export default function HomePage() {
    const [currentIndex, setCurrentIndex] = useState(0);
    const next = () => setCurrentIndex((currentIndex + 1) % testimonials.length);
    const prev = () => setCurrentIndex((currentIndex - 1 + testimonials.length) % testimonials.length);
    const current = testimonials[currentIndex];

    return (
        <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-4 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">🌬️ Respiră inteligent</div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/homepage" className="text-teal-400 underline font-semibold">Acasă</a>
                    <a href="/about" className="hover:text-teal-400 transition">Despre</a>
                    <a href="/login" className="hover:text-teal-400 transition">Login</a>
                </div>
            </nav>

            <main className="pt-32 px-4 sm:px-6 max-w-6xl mx-auto flex-grow space-y-20">
                {/* Secțiune introductivă */}
                <section className="text-center space-y-6">
                    <h2 className="text-4xl font-bold">Respiră conștient. Trăiește mai sigur.</h2>
                    <p className="text-gray-300 max-w-3xl mx-auto">
                        Calitatea aerului nu se vede, dar îți poate afecta sănătatea în fiecare clipă. Air Quality Monitor îți oferă o fereastră spre ceea ce nu poți simți:
                        monitorizează în timp real gazele dăunătoare din jurul tău și te ajută să iei decizii mai bune pentru tine, familia ta și comunitatea ta.
                    </p>
                </section>

                {/* Beneficii */}
                <section className="grid md:grid-cols-2 gap-10 text-left">
                    <div>
                        <h3 className="text-xl font-semibold text-teal-400">🧠 Cunoaște aerul pe care îl respiri</h3>
                        <p className="text-sm text-gray-300 mt-1">
                            Vezi nivelurile de CO₂, amoniac și benzen din locația ta, în timp real. Nicio zi nu e la fel.
                        </p>
                    </div>
                    <div>
                        <h3 className="text-xl font-semibold text-teal-400">📊 Vizualizează datele</h3>
                        <p className="text-sm text-gray-300 mt-1">
                            Analizează grafic cum evoluează calitatea aerului. Urmărește, compară, acționează.
                        </p>
                    </div>
                    <div>
                        <h3 className="text-xl font-semibold text-teal-400">🚦 Primește alerte inteligente</h3>
                        <p className="text-sm text-gray-300 mt-1">
                            Setează praguri personalizate și fii notificat automat când aerul devine periculos.
                        </p>
                    </div>
                    <div>
                        <h3 className="text-xl font-semibold text-teal-400">📍 Adaptează-te pe locație</h3>
                        <p className="text-sm text-gray-300 mt-1">
                            Monitorizarea este specifică locului în care te afli – acasă, la birou sau într-o altă zonă expusă.
                        </p>
                    </div>
                </section>

                {/* Testimoniale cu animații */}
                <section className="text-center bg-black bg-opacity-60 border border-teal-500 rounded-xl p-8 shadow-lg max-w-4xl mx-auto">
                    <h2 className="text-2xl font-bold text-teal-400 mb-6">🗣️ Ce spun utilizatorii?</h2>
                    <div className="relative">
                        <AnimatePresence mode="wait">
                            <motion.div
                                key={currentIndex}
                                initial={{ opacity: 0, x: 100 }}
                                animate={{ opacity: 1, x: 0 }}
                                exit={{ opacity: 0, x: -100 }}
                                transition={{ duration: 0.5 }}
                                className="p-6"
                            >
                                <p className="text-sm text-gray-300 italic mb-2">“{current.text}”</p>
                                <p className="text-teal-400 font-semibold">— {current.name}, {current.role}</p>
                                <div className="flex justify-center mt-2">
                                    {[...Array(5)].map((_, i) => (
                                        <span key={i} className={i < current.rating ? 'text-yellow-400' : 'text-gray-600'}>★</span>
                                    ))}
                                </div>
                            </motion.div>
                        </AnimatePresence>
                        <div className="flex justify-between mt-4 px-6">
                            <button onClick={prev} className="text-teal-300 hover:text-teal-400">← Înapoi</button>
                            <button onClick={next} className="text-teal-300 hover:text-teal-400">Înainte →</button>
                        </div>
                    </div>
                </section>
            </main>

            {/* Footer */}
            <footer className="mt-10 py-6 border-t border-teal-700 text-center text-gray-400 text-sm">
                © {new Date().getFullYear()} Air Quality Monitor. Toate drepturile rezervate.
            </footer>
        </div>
    );
}