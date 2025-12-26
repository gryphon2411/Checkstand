import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence, useInView } from 'framer-motion';
import { Github, Zap, Scan, EyeOff, FileCode, Book, Smartphone, Fingerprint, Receipt, Check } from 'lucide-react';
import QRCode from "react-qr-code";

// --- Constants ---
const DOWNLOAD_URL = "https://github.com/gryphon2411/Checkstand/releases/download/v1.2.0/checkstand-v1.2.0-release.apk";
const REPO_URL = "https://github.com/gryphon2411/Checkstand";
const KAGGLE_URL = "https://www.kaggle.com/competitions/google-gemma-3n-hackathon/writeups/checkstand-ai-powered-financial-empowerment-throug";

// --- Custom Hooks ---

const useIsMobile = () => {
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        const checkMobile = () => setIsMobile(window.matchMedia("(max-width: 768px)").matches);
        checkMobile();
        window.addEventListener("resize", checkMobile);
        return () => window.removeEventListener("resize", checkMobile);
    }, []);

    return isMobile;
};

// --- Helper Components ---

const Navbar = () => (
    <nav className="flex justify-between items-center py-6 px-4 md:px-0 mb-8 md:mb-12">
        {/* Logo Area */}
        <div className="flex items-center gap-3">
            <img
                src="/Checkstand/logo.png"
                alt="Checkstand"
                className="w-10 h-10 md:w-12 md:h-12 shadow-lg rounded-xl object-contain bg-white"
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

// --- "How It Works" Section ---

const HowItWorks = () => {
    const steps = [
        {
            title: "Snap Receipt",
            desc: "Open the app and take a quick photo of any crumpled, blurry receipt.",
            icon: <Smartphone className="w-6 h-6 text-white" />,
            color: "bg-blue-500",
            image: "/Checkstand/step1_scan.jpg"
        },
        {
            title: "AI De-masking",
            desc: "Gemma 3n runs locally to identify the merchant (e.g., 'Target') and total.",
            icon: <Fingerprint className="w-6 h-6 text-white" />,
            color: "bg-sparkle-yellow",
            image: "/Checkstand/step2_verify.jpg"
        },
        {
            title: "Export Data",
            desc: "Get a clean JSON or CSV for your budget. No cloud syncing required.",
            icon: <Receipt className="w-6 h-6 text-white" />,
            color: "bg-green-500",
            image: "/Checkstand/step3_csv.jpg"
        }
    ];

    return (
        <div className="mt-24 md:mt-32">
            <div className="text-center mb-16">
                <h2 className="text-3xl md:text-5xl font-bold text-slate-900 dark:text-white mb-4">How it Works</h2>
                <p className="text-slate-500 dark:text-slate-400 max-w-2xl mx-auto">
                    Three steps to financial clarity. Zero data leaving your device.
                </p>
            </div>

            <div className="space-y-12 md:space-y-24">
                {steps.map((step, index) => (
                    <div key={index} className={`flex flex-col md:flex-row gap-8 md:gap-16 items-center ${index % 2 === 1 ? 'md:flex-row-reverse' : ''}`}>

                        {/* Text (Always First on Mobile via Default Flex Col) */}
                        <div className="flex-1 text-center md:text-left space-y-4">
                            <div className={`w-12 h-12 ${step.color} rounded-2xl flex items-center justify-center shadow-lg mx-auto md:mx-0 transform -rotate-3`}>
                                {step.icon}
                            </div>
                            <h3 className="text-2xl font-bold text-slate-900 dark:text-white">
                                <span className="text-electric-blue mr-2">{index + 1}.</span>
                                {step.title}
                            </h3>
                            <p className="text-lg text-slate-500 dark:text-slate-400 leading-relaxed">
                                {step.desc}
                            </p>
                        </div>

                        {/* Image / Phone Frame */}
                        <div className="flex-1 w-full max-w-xs md:max-w-sm mx-auto">
                            <div className="relative aspect-[9/18] bg-white border-[8px] border-slate-900 dark:border-slate-700 rounded-[2.5rem] shadow-2xl overflow-hidden">


                                {/* Screen Content */}
                                <div className="absolute inset-0 bg-slate-900 z-10 flex flex-col">
                                    <img
                                        src={step.image}
                                        alt={step.title}
                                        className="w-full h-full object-cover object-top"
                                    />
                                </div>
                            </div>
                        </div>

                    </div>
                ))}
            </div>
        </div>
    );
};

// --- The "Wow" Demo ---

const ReceiptDemo = () => {
    const [isActive, setIsActive] = useState(false);
    const containerRef = useRef(null);
    const isInView = useInView(containerRef, { amount: 0.4 });
    const isMobile = useIsMobile();

    // Reset when scrolled out of view
    useEffect(() => {
        if (!isInView) {
            setIsActive(false);
        }
    }, [isInView]);

    return (
        <div
            ref={containerRef}
            className="relative w-full max-w-sm mx-auto cursor-pointer group h-[450px] flex items-center justify-center"
            onMouseEnter={() => !isMobile && setIsActive(true)}
            onMouseLeave={() => !isMobile && setIsActive(false)}
            onClick={() => setIsActive(!isActive)}
        >
            {/* Background Gradients */}
            <div className={`absolute inset-0 bg-blue-500/5 blur-3xl rounded-full transform translate-y-10 transition-colors duration-700 ${isActive ? 'bg-sparkle-yellow/20' : ''}`}></div>

            {/* State A: Crumpled Paper (Always Rendered as Base) */}
            <motion.div
                className="absolute inset-0 flex items-center justify-center"
                initial={{ rotate: -2, scale: 0.95, opacity: 1 }}
                animate={{
                    rotate: isActive ? 0 : -2,
                    scale: 0.95,
                    opacity: 1
                }}
            >
                <div className="relative w-72 h-96 bg-slate-100 border border-slate-300 shadow-sm p-6 flex flex-col gap-4 overflow-hidden rounded-sm filter brightness-95">
                    <div className="h-8 bg-slate-200 w-1/3 mx-auto rounded"></div>
                    <div className="h-4 bg-slate-200 w-full rounded mt-8"></div>
                    <div className="h-4 bg-slate-200 w-3/4 rounded"></div>
                    <div className="h-4 bg-slate-200 w-5/6 rounded"></div>
                    <div className="h-px bg-slate-300 w-full my-4 border-dashed border-t border-slate-400"></div>
                    <div className="h-6 bg-slate-200 w-1/2 ml-auto rounded"></div>

                    {/* Crumple effect overlay */}
                    <div className="absolute inset-0 bg-gradient-to-br from-black/5 to-transparent pointer-events-none"></div>

                    {/* ACTIVE STATE: Blur Overlay + Camera Icon + Laser */}
                    <AnimatePresence>
                        {isActive && (
                            <>
                                {/* Blur + Icon */}
                                <motion.div
                                    initial={{ opacity: 0 }}
                                    animate={{ opacity: 1 }}
                                    exit={{ opacity: 0 }}
                                    className="absolute inset-0 bg-slate-900/10 backdrop-blur-[2px] z-10 flex items-center justify-center"
                                >
                                    <div className="bg-white/20 p-4 rounded-full backdrop-blur-md border border-white/30">
                                        <Scan className="w-8 h-8 text-slate-800 dark:text-white opacity-80" />
                                    </div>
                                </motion.div>

                                {/* Scanning Laser Beam */}
                                <motion.div
                                    initial={{ top: -20, opacity: 1 }}
                                    animate={{ top: "120%" }}
                                    exit={{ opacity: 0 }}
                                    transition={{ duration: 1.2, ease: "linear" }}
                                    className="absolute left-0 right-0 h-1 bg-electric-blue shadow-[0_0_15px_rgba(41,121,255,0.8)] z-20"
                                />
                            </>
                        )}
                    </AnimatePresence>
                </div>

                {/* Hint Badge (Always visible when inactive) */}
                {!isActive && (
                    <div className="absolute -bottom-6 bg-white dark:bg-slate-800 shadow-xl px-4 py-2 rounded-full text-xs font-bold flex items-center gap-2 animate-bounce">
                        <Scan className="w-4 h-4 text-electric-blue" />
                        {isMobile ? "Tap to Scan" : "Hover to Scan"}
                    </div>
                )}
            </motion.div>


            {/* State B: Verified 3D Card (Enters AFTER Scan) */}
            <AnimatePresence>
                {isActive && (
                    <motion.div
                        className="z-30 w-80 bg-slate-900 rounded-3xl shadow-2xl overflow-hidden relative border border-slate-700 font-sans text-slate-50"
                        initial={{ rotate: 0, scale: 0.9, opacity: 0, y: 30 }}
                        animate={{ rotate: 0, scale: 1, opacity: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95, transition: { duration: 0.2 } }}
                        transition={{
                            delay: 1.1, // Wait for laser (1.2s approx)
                            type: "spring",
                            stiffness: 260,
                            damping: 20
                        }}
                    >
                        {/* Top Bar / Summary */}
                        <div className="p-6 pb-2">
                            <div className="flex justify-between items-center mb-6">
                                <h3 className="text-xl font-bold">Recent Receipts</h3>
                                <div className="w-10 h-10 rounded-xl bg-blue-500/20 flex items-center justify-center text-blue-400">
                                    <Receipt className="w-6 h-6" />
                                </div>
                            </div>

                            {/* Stats Box */}
                            <div className="bg-slate-800/50 rounded-2xl p-4 border border-slate-700/50 flex justify-between items-center mb-6">
                                <div>
                                    <div className="text-[10px] uppercase tracking-wider text-slate-400 mb-1 font-semibold">Total Spent</div>
                                    <div className="text-2xl font-bold text-blue-400">$689.72</div>
                                </div>
                                <div className="text-right">
                                    <div className="text-[10px] uppercase tracking-wider text-slate-400 mb-1 font-semibold">Receipts</div>
                                    <div className="text-2xl font-bold">2</div>
                                </div>
                            </div>
                        </div>

                        {/* List Header */}
                        <div className="px-6 py-2 bg-slate-800/80 border-y border-slate-700/50 text-[10px] uppercase tracking-wider font-semibold text-slate-400 flex justify-between">
                            <span className="w-12">Date</span>
                            <span className="flex-1">Merchant</span>
                            <span className="w-16 text-right">Amount</span>
                        </div>

                        {/* Recent List */}
                        <div className="bg-slate-800/30">
                            {/* Item 1: The Scanned Receipt (Highlighted) */}
                            <div
                                className="px-6 py-4 flex items-center justify-between border-b border-slate-700/50"
                            >
                                <span className="text-sm text-slate-400 w-12 font-medium">11/29</span>
                                <span className="text-sm font-bold flex-1 truncate mr-2">Amazon.com</span>
                                <div className="flex items-center gap-2 justify-end">
                                    <span className="text-sm font-bold text-blue-400">$54.99</span>
                                    <div className="bg-blue-500/20 p-1 rounded-full">
                                        <Check className="w-3 h-3 text-blue-400" />
                                    </div>
                                </div>
                            </div>


                            {/* Item 2: Historical */}
                            <div className="px-6 py-4 flex items-center justify-between opacity-50">
                                <span className="text-sm text-slate-400 w-12 font-medium">08/01</span>
                                <span className="text-sm font-bold flex-1 truncate mr-2">Craigs Land...</span>
                                <div className="flex items-center gap-2 justify-end">
                                    <span className="text-sm font-bold">$634.73</span>
                                    <div className="bg-blue-500/20 p-1 rounded-full">
                                        <Check className="w-3 h-3 text-blue-400" />
                                    </div>
                                </div>
                            </div>
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

                <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center pt-4 md:pt-16 pb-12">
                    {/* Left: Copy & Actions */}
                    <div className="space-y-8 text-center md:text-left z-10">
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
                    <div>
                        <ReceiptDemo />
                    </div>
                </div>

                <HowItWorks />
                <TrustSection />

                <footer className="pt-24 pb-44 md:pb-12 text-center text-slate-400 text-sm">
                    <p className="mb-4">&copy; {new Date().getFullYear()} Checkstand. All rights reserved.</p>

                    <div className="flex flex-col md:flex-row items-center justify-center gap-4 md:gap-8">
                        <a
                            href={KAGGLE_URL}
                            className="inline-flex items-center gap-2 hover:text-electric-blue transition-colors font-medium text-slate-500 bg-white dark:bg-slate-800 px-4 py-2 rounded-full border border-slate-100 dark:border-slate-700 shadow-sm"
                            target="_blank"
                            rel="noreferrer"
                        >
                            <Book className="w-4 h-4" />
                            Read Engineering Case Study (Kaggle)
                        </a>

                        <a
                            href={REPO_URL}
                            className="inline-flex items-center gap-2 hover:text-electric-blue transition-colors text-slate-500"
                            target="_blank"
                            rel="noreferrer"
                        >
                            <Github className="w-4 h-4" />
                            View on GitHub
                        </a>
                    </div>
                </footer>

            </main>

            {/* MOBILE ONLY: Sticky Bottom Bar */}
            <div className="md:hidden fixed bottom-0 left-0 right-0 z-[100] bg-white dark:bg-slate-900 border-t border-slate-200 dark:border-slate-800 p-4 pb-safe shadow-[0_-5px_20px_rgba(0,0,0,0.1)]">
                <a
                    href={DOWNLOAD_URL}
                    className="w-full bg-electric-blue text-white font-bold text-xl h-[72px] flex items-center justify-center rounded-2xl shadow-lg active:scale-95 transition-transform"
                >
                    Download App (v1.2.0)
                </a>
            </div>
        </div>
    );
}

export default App;
