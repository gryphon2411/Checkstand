/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                'electric-blue': '#4285F4',
                'sparkle-yellow': '#FFC107',
            },
            boxShadow: {
                'soft': '0 10px 40px -10px rgba(41, 121, 255, 0.2)',
                'card': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
            }
        },
    },
    plugins: [],
}
