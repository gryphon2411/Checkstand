import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Shield, ChevronDown, ChevronUp, Github, CheckCircle2, Zap, Scan, Hexagon } from 'lucide-react';

// --- Custom Hooks ---

const useOS = () => {
    const [os, setOS] = useState<'ios' | 'android' | 'desktop'>('desktop');

    useEffect(() => {
        const userAgent = navigator.userAgent.toLowerCase();
        if (/iphone|ipad|ipod/.test(userAgent)) {
            setOS('ios');
        } else if (/android/.test(userAgent)) {
            setOS('android');
        } else {
            setOS('desktop');
        }
    }, []);

    return os;
};

const useCampaign = () => {
    const [headline, setHeadline] = useState("Stop Guessing Where Your Cash Went.");

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        if (params.get('utm_campaign') === 'privacy') {
            setHeadline("The Only Financial App That Doesn't Spy on You.");
        }
    }, []);

    return headline;
};

// --- Helper Components ---

const CTAButton = ({ text, className = "", onClick }: { text: string, className?: string, onClick?: () => void }) => (
    <button
        onClick={onClick}
        data-testid="hero-cta"
        className={`bg-electric-blue text-white font-bold text-lg md:text-xl px-10 h-16 flex items-center justify-center rounded-3xl shadow-soft hover:shadow-electric-blue/40 hover:-translate-y-1 transition-all active:scale-95 ${className}`}
    >
        {text}
    </button>
);

const Navbar = () => (
    <nav className="flex justify-between items-center py-6 px-4 md:px-0 mb-12">
        {/* Logo Area */}
        <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-electric-blue rounded-xl flex items-center justify-center shadow-lg transform rotate-3">
                <span className="text-white font-bold text-xl">C</span>
            </div>
            <span className="font-bold text-xl text-slate-900 dark:text-white tracking-tight">Checkstand</span>
        </div>

        {/* AI Active Status */}
        <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 px-4 py-2 rounded-2xl">
            <div className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-sparkle-yellow opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-sparkle-yellow"></span>
            </div>
            <span className="text-xs font-bold text-slate-600 dark:text-slate-300">AI Active</span>
        </div>
    </nav>
);

// --- The "Wow" Demo ---

const ReceiptDemo = () => {
    const [isHovered, setIsHovered] = useState(false);

    return (
        <div
            className="relative w-full max-w-sm mx-auto cursor-pointer group h-[500px] flex items-center justify-center"
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
            onClick={() => setIsHovered(!isHovered)} // Tap for mobile
        >
            {/* Background Gradients */}
            <div className="absolute inset-0 bg-blue-500/5 blur-3xl rounded-full transform translate-y-10 group-hover:bg-sparkle-yellow/20 transition-colors duration-700"></div>

            {/* State A: Crumpled Paper (Simulated via CSS) */}
            {!isHovered && (
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
                        Hover to Scan
                    </div>
                </motion.div>
            )}

            {/* State B: Verified 3D Card */}
            <AnimatePresence>
                {isHovered && (
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
                            <span>Processed on Device</span>
                            <span className="flex items-center gap-1">
                                <Zap className="w-3 h-3 text-sparkle-yellow fill-sparkle-yellow" />
                                Gemma 3n
                            </span>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}

const TechSpecs = () => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <div className="max-w-2xl mx-auto mt-16">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="w-full bg-white dark:bg-slate-800 rounded-3xl p-5 shadow-sm hover:shadow-md transition-shadow flex justify-between items-center group border border-slate-100 dark:border-slate-700"
            >
                <div className="flex items-center gap-3">
                    <div className="bg-slate-100 dark:bg-slate-700 p-2 rounded-full group-hover:bg-electric-blue/10 transition-colors">
                        <Zap className="w-5 h-5 text-slate-500 group-hover:text-electric-blue transition-colors" />
                    </div>
                    <span className="font-bold text-slate-700 dark:text-slate-200">Technical Specs</span>
                </div>
                {isOpen ? <ChevronUp className="text-slate-400" /> : <ChevronDown className="text-slate-400" />}
            </button>

            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="overflow-hidden bg-slate-50 dark:bg-slate-900/50 mx-4 rounded-b-3xl"
                    >
                        <div className="p-6 pt-2 space-y-4 text-sm text-slate-600 dark:text-slate-300">
                            <div className="flex items-center gap-3">
                                <div className="w-1.5 h-1.5 rounded-full bg-electric-blue"></div>
                                <span><strong>Algorithm:</strong> Google Gemma 3n (4-bit quantized)</span>
                            </div>
                            <div className="flex items-center gap-3">
                                <div className="w-1.5 h-1.5 rounded-full bg-electric-blue"></div>
                                <span><strong>Context:</strong> 128k tokens for item extraction</span>
                            </div>
                            <div className="flex items-center gap-3">
                                <div className="w-1.5 h-1.5 rounded-full bg-electric-blue"></div>
                                <span><strong>Privacy:</strong> Local MediaPipe Inference Pipeline</span>
                            </div>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    )
}

const CookieToast = () => {
    const [visible, setVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => setVisible(false), 5000);
        return () => clearTimeout(timer);
    }, []);

    return (
        <AnimatePresence>
            {visible && (
                <motion.div
                    initial={{ y: 50, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    exit={{ y: 50, opacity: 0 }}
                    className="fixed bottom-6 left-6 z-50 bg-slate-900 text-white pl-4 pr-6 py-3 rounded-full shadow-2xl flex items-center gap-3 max-w-sm"
                >
                    <Shield className="w-4 h-4 text-electric-blue fill-electric-blue" />
                    <p className="text-xs font-bold">No cookies. No tracking. Pure privacy.</p>
                </motion.div>
            )}
        </AnimatePresence>
    );
}

// --- Main App ---

function App() {
    const os = useOS();
    const headline = useCampaign();

    const getButtonText = () => {
        switch (os) {
            case 'ios': return "Join TestFlight Beta";
            case 'android': return "Download Signed APK";
            default: return "Scan QR to Install";
        }
    };

    const ctaText = getButtonText();

    const handleCTAClick = () => {
        if (os === 'desktop') {
            alert("QR Code Modal Placeholder");
        } else {
            console.log("Navigating to download...");
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-50 font-sans selection:bg-sparkle-yellow selection:text-black">

            <main className="container mx-auto px-6 max-w-6xl">
                <Navbar />

                <div className="grid md:grid-cols-2 gap-12 lg:gap-20 items-center pt-8 md:pt-16 pb-32">
                    {/* Left: Copy */}
                    <div className="space-y-8 text-center md:text-left z-10">
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.6 }}
                        >
                            <div className="inline-flex items-center gap-2 bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 px-4 py-1.5 rounded-full text-xs font-bold border border-yellow-200 dark:border-yellow-700/50 mb-2">
                                <span>🏆 Winner: Google Gemma 3n Impact Challenge</span>
                            </div>
                            <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight leading-[1.1] text-slate-900 dark:text-white">
                                {headline.includes("Spy") ?
                                    <span>The Only <span className="text-electric-blue">Privacy App</span> That Doesn't Spy.</span> :
                                    <span>Stop Guessing Where Your <span className="text-electric-blue">Cash Went.</span></span>
                                }
                            </h1>
                        </motion.div>

                        <motion.p
                            className="text-lg md:text-xl text-slate-500 dark:text-slate-400 max-w-lg mx-auto md:mx-0 leading-relaxed"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.2, duration: 0.6 }}
                        >
                            Turn paper chaos into financial truth in 3 seconds. Powered by Gemma 3n. <span className="font-bold text-slate-900 dark:text-white">100% Offline.</span>
                        </motion.p>

                        {/* Desktop CTA */}
                        <motion.div
                            className="hidden md:flex flex-col gap-4 items-start"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.4 }}
                        >
                            <CTAButton text={ctaText} onClick={handleCTAClick} />
                            <div className="flex items-center gap-4 text-xs font-medium text-slate-400 pl-2">
                                <span className="flex items-center gap-1"><CheckCircle2 className="w-3 h-3" /> Open Source Code</span>
                                <span className="flex items-center gap-1"><Shield className="w-3 h-3" /> No Credit Card</span>
                            </div>
                        </motion.div>
                    </div>

                    {/* Right: Demo */}
                    <div className="order-first md:order-last">
                        <ReceiptDemo />
                    </div>
                </div>

                <TechSpecs />

                <footer className="py-12 text-center">
                    <a
                        href="https://github.com/gryphon2411/Checkstand"
                        className="inline-flex items-center justify-center gap-2 text-slate-400 hover:text-electric-blue transition-colors font-medium text-sm"
                        target="_blank"
                        rel="noreferrer"
                    >
                        <Github className="w-4 h-4" />
                        <span>Audit our Code on GitHub</span>
                    </a>
                </footer>
            </main>

            {/* Mobile Sticky CTA */}
            <div className="md:hidden fixed bottom-6 left-4 right-4 z-50 pb-safe">
                <CTAButton text={ctaText} onClick={handleCTAClick} className="w-full shadow-2xl" />
            </div>

            <CookieToast />
        </div>
    )
}

export default App
