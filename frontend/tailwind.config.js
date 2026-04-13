/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Space Grotesk"', 'system-ui', 'sans-serif'],
        display: ['"Archivo Black"', '"Space Grotesk"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        brutal: '6px 6px 0 0 #000',
        'brutal-sm': '4px 4px 0 0 #000',
      },
    },
  },
  plugins: [],
};
