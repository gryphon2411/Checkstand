import { useState, useRef } from 'react';
import { motion, AnimatePresence, useInView } from 'framer-motion';
import { Github, CheckCircle2, Zap, Scan, Hexagon, EyeOff, FileCode } from 'lucide-react';
import QRCode from "react-qr-code";

// --- Constants ---
const DOWNLOAD_URL = "https://github.com/gryphon2411/Checkstand/releases/download/v1.2.0/checkstand-v1.2.0-release.apk";
const REPO_URL = "https://github.com/gryphon2411/Checkstand/releases/tag/v1.2.0";

// --- Custom Hooks ---


// --- Helper Components ---

const Navbar = () => (
    <nav className="flex justify-between items-center py-6 px-4 md:px-0 mb-8 md:mb-12">
        {/* Logo Area */}
        <div className="flex items-center gap-3">
            <img
                src="/Checkstand/logo.png"
                alt="Checkstand"
                className="w-8 h-8 md:w-10 md:h-10 shadow-lg transform rotate-3 rounded-xl"
            />
            <span className="font-bold text-lg md:text-xl text-slate-900 dark:text-white tracking-tight">Checkstand</span>
        </div>

        {/* Trust Signal */}
        <a
            href={REPO_URL}
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-2 text-slate-500 hover:text-electric-blue transition-colors font-medium text-sm bg-slate-100 dark:bg-slate-800 px-3 py-1.5 rounded-full"
        >
            <Github className="w-4 h-4" />
            <span className="hidden md:inline">Source Code</span>
        </a>
    </nav>
);

// --- The "Wow" Demo ---

const ReceiptDemo = () => {
    const [isHovered, setIsHovered] = useState(false);
    const containerRef = useRef(null);
    const isInView = useInView(containerRef, { amount: 0.6 });

    // Auto-trigger on mobile scroll (if in view), or hover on desktop
    const isActive = isHovered || isInView;

    return (
        <div
            ref={containerRef}
            className="relative w-full max-w-sm mx-auto cursor-pointer group h-[450px] flex items-center justify-center"
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
            onClick={() => setIsHovered(!isHovered)}
        >
            {/* Background Gradients */}
            <div className="absolute inset-0 bg-blue-500/5 blur-3xl rounded-full transform translate-y-10 group-hover:bg-sparkle-yellow/20 transition-colors duration-700"></div>

            {/* State A: Crumpled Paper (Simulated via CSS) */}
            {!isActive && (
                <motion.div
                    layoutId="receipt-container"
                    className="absolute inset-0 flex items-center justify-center"
                    initial={{ rotate: -2, scale: 0.95, opacity: 1 }}
                    animate={{ rotate: -2, scale: 0.95, opacity: 1 }}
                    exit={{ opacity: 0 }}
                >
                    <div className="w-72 h-96 bg-slate-100 border border-slate-300 shadow-sm p-6 flex flex-col gap-4 overflow-hidden rounded-sm filter brightness-95">
                        <div className="h-8 bg-slate-200 w-1/3 mx-auto rounded"></div>
                        <div className="h-4 bg-slate-200 w-full rounded mt-8"></div>
                        <div className="h-4 bg-slate-200 w-3/4 rounded"></div>
                        <div className="h-4 bg-slate-200 w-5/6 rounded"></div>
                        <div className="h-px bg-slate-300 w-full my-4 border-dashed border-t border-slate-400"></div>
                        <div className="h-6 bg-slate-200 w-1/2 ml-auto rounded"></div>

                        {/* Crumple effect overlay */}
                        <div className="absolute inset-0 bg-gradient-to-br from-black/5 to-transparent pointer-events-none"></div>
                    </div>

                    {/* Hint Badge */}
                    <div className="absolute -bottom-6 bg-white dark:bg-slate-800 shadow-xl px-4 py-2 rounded-full text-xs font-bold flex items-center gap-2 animate-bounce">
                        <Scan className="w-4 h-4 text-electric-blue" />
                        {isInView ? "Scanning..." : "Hover to Scan"}
                    </div>
                </motion.div>
            )}

            {/* State B: Verified 3D Card */}
            <AnimatePresence>
                {isActive && (
                    <motion.div
                        layoutId="receipt-container"
                        className="z-10 w-80 bg-white dark:bg-slate-800 rounded-3xl shadow-card p-6 overflow-hidden relative border border-slate-100 dark:border-slate-700"
                        initial={{ rotate: 0, scale: 0.95, opacity: 0, y: 10 }}
                        animate={{ rotate: 0, scale: 1, opacity: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        transition={{ type: "spring", stiffness: 200, damping: 20 }}
                    >
                        {/* Scanning Beam Animation */}
                        <motion.div
                            initial={{ top: -20 }}
                            animate={{ top: "200%" }}
                            transition={{ duration: 1.5, ease: "easeInOut" }}
                            className="absolute left-0 right-0 h-2 bg-sparkle-yellow/50 shadow-[0_0_20px_rgba(255,193,7,0.6)] z-20 blur-sm"
                        />

                        {/* Header */}
                        <div className="flex justify-between items-start mb-6">
                            <div className="flex gap-3 items-center">
                                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                                    <Hexagon className="w-6 h-6 fill-current" />
                                </div>
                                <div>
                                    <h3 className="font-bold text-slate-900 dark:text-white">Walmart</h3>
                                    <p className="text-xs text-slate-500 font-medium">Verified Merchant</p>
                                </div>
                            </div>
                            <div className="bg-green-100 text-green-700 p-1.5 rounded-full">
                                <CheckCircle2 className="w-4 h-4" />
                            </div>
                        </div>

                        {/* List */}
                        <div className="space-y-4 mb-6">
                            <div className="flex justify-between text-sm">
                                <span className="text-slate-600 dark:text-slate-300">Bananas (Organic)</span>
                                <span className="font-bold">$2.15</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-slate-600 dark:text-slate-300">Coffee (Starbucks)</span>
                                <span className="font-bold">$5.40</span>
                            </div>
                            <div className="bg-slate-50 dark:bg-slate-900 p-3 rounded-2xl flex justify-between items-center font-bold">
                                <span>Total Paid</span>
                                <span className="text-lg text-slate-900 dark:text-white">$7.55</span>
                            </div>
                        </div>

                        {/* Footer Info */}
                        <div className="flex justify-between items-center text-[10px] text-slate-400 font-medium uppercase tracking-wider">
                            <span>Processed on Device (3s)</span>
                            <span className="flex items-center gap-1">
                                <motion.div
                                    animate={{ scale: [1, 1.2, 1] }}
                                    transition={{ repeat: Infinity, duration: 2 }}
                                >
                                    <Zap className="w-3 h-3 text-sparkle-yellow fill-sparkle-yellow" />
                                </motion.div>
                                Gemma 3n
                            </span>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}

// --- Trust Section ---

const TrustSection = () => (
    <div className="grid md:grid-cols-3 gap-6 mt-16 md:mt-24">
        <div className="bg-white dark:bg-slate-800 p-6 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700">
            <div className="w-10 h-10 bg-blue-50 dark:bg-blue-900/30 rounded-full flex items-center justify-center mb-4">
                <EyeOff className="w-5 h-5 text-electric-blue" />
            </div>
            <h3 className="font-bold text-slate-900 dark:text-white mb-2">Zero Cloud</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400">Your data never leaves your phone. 100% offline processing.</p>
        </div>

        <div className="bg-white dark:bg-slate-800 p-6 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700">
            <div className="w-10 h-10 bg-yellow-50 dark:bg-yellow-900/30 rounded-full flex items-center justify-center mb-4">
                <Zap className="w-5 h-5 text-sparkle-yellow" />
            </div>
            <h3 className="font-bold text-slate-900 dark:text-white mb-2">AI De-Masking</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400">Turns cryptic "QSR LLC" into clear "Taco Bell" automatically.</p>
        </div>

        <div className="bg-white dark:bg-slate-800 p-6 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700">
            <div className="w-10 h-10 bg-green-50 dark:bg-green-900/30 rounded-full flex items-center justify-center mb-4">
                <FileCode className="w-5 h-5 text-green-600" />
            </div>
            <h3 className="font-bold text-slate-900 dark:text-white mb-2">Open Source</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400">Auditable code. No hidden trackers. Verified by community.</p>
        </div>
    </div>
);

// --- Main App ---

function App() {
    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-50 font-sans selection:bg-sparkle-yellow selection:text-black">

            <main className="container mx-auto px-6 max-w-6xl">
                <Navbar />

                <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center pt-4 md:pt-16 pb-32">
                    {/* Left: Copy & Actions */}
                    <div className="space-y-8 text-center md:text-left z-10 order-last md:order-first">
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.6 }}
                        >
                            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight leading-[1.1] text-slate-900 dark:text-white mb-6">
                                Stop Guessing Where Your <span className="text-electric-blue">Cash Went.</span>
                            </h1>
                            <p className="text-lg md:text-xl text-slate-500 dark:text-slate-400 max-w-lg mx-auto md:mx-0 leading-relaxed font-medium">
                                The offline AI scanner that de-masks your bank statement. Powered by Gemma 3n. <span className="text-slate-900 dark:text-white">Private by design.</span>
                            </p>
                        </motion.div>

                        {/* DESKTOP ONLY: QR Code Card */}
                        <div className="hidden md:flex bg-white dark:bg-slate-800 p-6 rounded-3xl shadow-lg border border-slate-100 dark:border-slate-700 w-fit">
                            <div className="bg-white p-2 rounded-xl">
                                <QRCode value={DOWNLOAD_URL} size={120} />
                            </div>
                            <div className="ml-6 flex flex-col justify-center text-left">
                                <span className="font-bold text-lg text-slate-900 dark:text-white">Scan to Install</span>
                                <span className="text-sm text-slate-500 mb-3 block">Android (APK)</span>
                                <div className="text-[10px] uppercase tracking-wider font-bold text-slate-400 bg-slate-100 dark:bg-slate-700 px-2 py-1 rounded w-fit">
                                    Version 1.2.0 • Open Source
                                </div>
                            </div>
                        </div>

                        {/* Disclaimer for Beta */}
                        <p className="text-xs text-slate-400 italic md:text-left mx-auto md:mx-0 max-w-xs md:max-w-none">
                            Note: As a Beta, you may need to 'Allow from this source' to install.
                        </p>
                    </div>

                    {/* Right: Demo */}
                    <div className="order-first md:order-last">
                        <ReceiptDemo />
                    </div>
                </div>

                <TrustSection />

                <footer className="py-12 text-center text-slate-400 text-sm">
                    <p>&copy; {new Date().getFullYear()} Checkstand. All rights reserved.</p>
                    <a
                        href={REPO_URL}
                        className="inline-flex items-center justify-center gap-2 mt-4 hover:text-electric-blue transition-colors"
                        target="_blank"
                        rel="noreferrer"
                    >
                        <Github className="w-4 h-4" />
                        <span>View on GitHub</span>
                    </a>
                </footer>
            </main>

            {/* MOBILE ONLY: Sticky Bottom Bar */}
            <div className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 p-4 pb-safe shadow-[0_-5px_20px_rgba(0,0,0,0.1)]">
                <a
                    href={DOWNLOAD_URL}
                    className="w-full bg-electric-blue text-white font-bold text-xl h-[72px] flex items-center justify-center rounded-2xl shadow-lg active:scale-95 transition-transform"
                >
                    Download App (v1.2.0)
                </a>
            </div>
        </div>
    )
}

export default App
