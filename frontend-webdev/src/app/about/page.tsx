'use client';

export default function AboutPage() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-black to-teal-900 text-white px-6 pb-10">
            {/* Navbar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/30 backdrop-blur-md border-b border-teal-500 text-white px-6 py-4 flex justify-between items-center shadow-md">
                <div className="text-xl font-bold tracking-wide">
                    🌬️ Respiră inteligent                </div>
                <div className="space-x-4 hidden sm:flex">
                    <a href="/homepage" className="hover:text-teal-400 transition">Acasă</a>
                    <a href="/about" className="text-teal-400 font-semibold underline">Despre</a>
                    <a href="/login" className="hover:text-teal-400 transition">LogIn</a>
                </div>
            </nav>

            <div className="max-w-3xl mx-auto pt-24 space-y-12 text-gray-300 text-sm sm:text-base leading-relaxed">
                <div className="text-center space-y-4">
                    <h1 className="text-4xl font-extrabold text-white">📖 Despre noi</h1>
                    <p>
                        Acest proiect a fost creat din dorința de a le oferi oamenilor posibilitatea de a monitoriza
                        în timp real calitatea aerului din jurul lor. Cu ajutorul unui senzor de gaze și a tehnologiei
                        moderne, oricine poate primi alerte imediate atunci când aerul devine periculos pentru sănătate.
                    </p>
                </div>

                <section>
                    <h2 className="text-2xl font-bold text-teal-400 mb-2 text-center">🧪 Gaze detectate</h2>
                    <p>
                        Aplicația monitorizează mai multe substanțe nocive:
                    </p>
                    <ul className="list-disc ml-6 mt-2 space-y-1 text-justify">
                        <li><strong>CO₂ (Dioxid de Carbon):</strong> provoacă oboseală, amețeli și dificultăți de concentrare.</li>
                        <li><strong>NH₃ (Amoniac):</strong> irită căile respiratorii și ochii, mai ales în zone industriale.</li>
                        <li><strong>Benzen:</strong> substanță cancerigenă prezentă în aerul urban și vaporii de combustibil.</li>
                        <li><strong>COV-uri & fum:</strong> compuși toxici care afectează sistemul respirator și pot declanșa astm.</li>
                    </ul>
                </section>

                <section>
                    <h2 className="text-2xl font-bold text-teal-400 mb-4 text-center">🌍 De ce e important să monitorizăm aerul?</h2>

                    <p>
                        Poluarea aerului este una dintre cele mai mari amenințări globale asupra sănătății. Conform OMS,
                        peste <strong>7 milioane de oameni</strong> mor anual din cauza expunerii la particule fine și gaze toxice.
                        Aerul poluat contribuie la boli respiratorii, cardiovasculare, AVC și cancer pulmonar.
                    </p>

                    <p className="mt-4">🌫️ Iată câteva orașe afectate:</p>
                    <ul className="list-disc ml-6 mt-2 space-y-1 text-justify">
                        <li><strong>Delhi (India):</strong> PM2.5 mai mare de 300 µg/m³ – extrem de periculos</li>
                        <li><strong>Beijing (China):</strong> smog dens, vizibilitate redusă</li>
                        <li><strong>Lahore (Pakistan):</strong> între cele mai poluate orașe</li>
                        <li><strong>Cairo (Egipt):</strong> niveluri mari de CO₂ și praf</li>
                        <li><strong>București (România):</strong> depășește des limita OMS pentru PM2.5</li>
                    </ul>

                    <p className="mt-4 text-center">
                        Monitorizarea constantă permite comunităților să evite expunerea, să protejeze grupele vulnerabile
                        și să susțină politici de mediu informate.
                    </p>
                </section>

                <section>
                    <h2 className="text-2xl font-bold text-teal-400 mb-2 text-center">✨ Ce vă oferim?</h2>
                    <ul className="list-disc ml-6 mt-2 space-y-1 text-justify">
                        <li>Afișează în timp real valorile detectate de senzor.</li>
                        <li>Trimite alerte când pragurile de siguranță sunt depășite.</li>
                        <li>Permite setarea de praguri personalizate pe locație.</li>
                        <li>Salvează datele și creează grafice pentru analiză.</li>
                        <li>Îți oferă o imagine clară a calității aerului în timp.</li>
                    </ul>
                </section>

                <div className="text-center text-sm text-gray-400 pt-6">
                    Realizată cu 💙 pentru un aer mai curat și o lume mai sănătoasă.
                </div>
            </div>
        </div>
    );
}
