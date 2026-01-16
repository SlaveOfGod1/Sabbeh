import React, { createContext, useState, useContext, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

const DhikrContext = createContext();

const STORAGE_KEY = '@sabbeh_dhikrs_v1';

const INITIAL_DHIKRS = [
    { id: '1', title: 'Tasbih', subtitle: 'SubhanAllah', target: 33 },
    { id: '2', title: 'Tahmid', subtitle: 'Alhamdulillah', target: 33 },
    { id: '3', title: 'Takbir', subtitle: 'Allahu Akbar', target: 34 },
    { id: '4', title: 'Istighfar', subtitle: 'Astaghfirullah', target: 100 },
    { id: '5', title: 'Salawat', subtitle: 'Salawat on Prophet', target: 100 },
    { id: '6', title: 'La ilaha illallah', subtitle: 'Tahlil', target: 100 },
];

// Theme Presets
const THEMES = {
    Teal: { name: 'Teal', colors: ['#4DB6AC', '#00695C'], end: { x: 1, y: 0.18 } },
    Purple: { name: 'Purple', colors: ['#BA68C8', '#4A148C'], end: { x: 1, y: 0.18 } },
    Blue: { name: 'Blue', colors: ['#42A5F5', '#0D47A1'], end: { x: 1, y: 0.18 } },
    Rose: { name: 'Rose', colors: ['#EC407A', '#880E4F'], end: { x: 1, y: 0.18 } },
    Night: {
        name: 'Night',
        colors: ['#0000006f', '#000000ff'], // Dark Gray -> Semi Black
        end: { x: 0, y: 0 },
        isDark: true,
        deviceBody: '#2d363dff', // Dark Blue Grey for device body
        text: '#ECEFF1'
    }
};

export function DhikrProvider({ children }) {
    const [dhikrs, setDhikrs] = useState(INITIAL_DHIKRS);
    const [currentDhikrId, setCurrentDhikrId] = useState('1');
    const [progress, setProgress] = useState({});
    const [theme, setTheme] = useState(THEMES.Teal);

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        saveData();
    }, [dhikrs, progress, currentDhikrId, theme]);

    const loadData = async () => {
        try {
            const stored = await AsyncStorage.getItem(STORAGE_KEY);
            if (stored) {
                const parsed = JSON.parse(stored);
                if (parsed.dhikrs) setDhikrs(parsed.dhikrs);
                if (parsed.progress) setProgress(parsed.progress);
                if (parsed.currentDhikrId) setCurrentDhikrId(parsed.currentDhikrId);
                if (parsed.theme) setTheme(parsed.theme);
            }
        } catch (e) {
            console.log('Failed to load dhikr data');
        }
    };

    const saveData = async () => {
        try {
            const data = {
                dhikrs,
                progress,
                currentDhikrId,
                theme
            };
            await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(data));
        } catch (e) {
            console.log('Failed to save dhikr data');
        }
    };

    const currentDhikr = dhikrs.find(d => d.id === currentDhikrId) || dhikrs[0];
    const currentProgress = progress[currentDhikr.id] || { count: 0, rounds: 0 };

    const selectDhikr = (dhikr) => {
        setCurrentDhikrId(dhikr.id);
    };

    const updateProgress = (countDelta) => {
        const currentId = currentDhikr.id;
        const oldP = progress[currentId] || { count: 0, rounds: 0 };
        const target = currentDhikr.target || 33;

        let newCount = oldP.count + countDelta;
        let newRounds = oldP.rounds;

        // Handle Loop
        if (newCount >= target) {
            newCount = 0;
            newRounds += 1;
        }

        setProgress({
            ...progress,
            [currentId]: { count: newCount, rounds: newRounds }
        });
    };

    const resetCount = () => {
        const currentId = currentDhikr.id;
        const oldP = progress[currentId] || { count: 0, rounds: 0 };
        setProgress({
            ...progress,
            [currentId]: { ...oldP, count: 0 }
        });
    };

    const addDhikr = (title, subtitle, target) => {
        const newDhikr = {
            id: Date.now().toString(),
            title,
            subtitle,
            target: parseInt(target) || 33,
        };
        setDhikrs([...dhikrs, newDhikr]);
        setProgress(prev => ({ ...prev, [newDhikr.id]: { count: 0, rounds: 0 } }));
        setCurrentDhikrId(newDhikr.id);
    };

    const deleteDhikr = (id) => {
        const updated = dhikrs.filter(d => d.id !== id);
        setDhikrs(updated);

        const newProgress = { ...progress };
        delete newProgress[id];
        setProgress(newProgress);

        if (currentDhikrId === id && updated.length > 0) {
            setCurrentDhikrId(updated[0].id);
        }
    };

    const updateDhikr = (id, updates) => {
        setDhikrs(dhikrs.map(d => (d.id === id ? { ...d, ...updates } : d)));
    };

    const applyCustomColor = (mainColor) => {
        // Helper to lighten/darken hex
        // Since we don't have a library, let's do a simple channel manipulation
        // or just rely on a fixed offset if we assume the picker returns pure hues.

        // Let's create a gradient that goes from Darker (Left) -> Lighter (Right) or vice versa.
        // If mainColor is our "base", let's make it the start, and calculate a lighter version for the end.

        const lighten = (color, percent) => {
            const num = parseInt(color.replace("#", ""), 16),
                amt = Math.round(2.55 * percent),
                R = (num >> 16) + amt,
                B = ((num >> 8) & 0x00FF) + amt,
                G = (num & 0x0000FF) + amt;
            return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (B < 255 ? B < 1 ? 0 : B : 255) * 0x100 + (G < 255 ? G < 1 ? 0 : G : 255)).toString(16).slice(1);
        };

        const darken = (color, percent) => {
            const num = parseInt(color.replace("#", ""), 16),
                amt = Math.round(2.55 * percent),
                R = (num >> 16) - amt,
                B = ((num >> 8) & 0x00FF) - amt,
                G = (num & 0x0000FF) - amt;
            return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (B < 255 ? B < 1 ? 0 : B : 255) * 0x100 + (G < 255 ? G < 1 ? 0 : G : 255)).toString(16).slice(1);
        };

        // Create a nice gradient: Darker Start -> Base -> Lighter End? 
        // Or Base -> Lighter.
        // Let's do: [Darker Version, Base Version] to match the rich deep look.

        const darkShade = darken(mainColor, 20); // Darken by 20%
        const lightShade = lighten(mainColor, 20);

        // Let's try [DarkShade, LightShade] for high contrast depth
        const newTheme = {
            name: 'Custom',
            colors: [lightShade, darkShade],
            end: { x: 1, y: 0.18 },
            // Default custom themes to light mode style unless we calculate brightness
            isDark: false,
            deviceBody: '#F5F5F5',
            text: '#E0F2F1' // Text on background
        };
        setTheme(newTheme);
        setTheme(newTheme);
    };

    const [lastTheme, setLastTheme] = useState(THEMES.Teal);

    const toggleNightMode = () => {
        if (theme.name === 'Night') {
            setTheme(lastTheme);
        } else {
            setLastTheme(theme);
            setTheme(THEMES.Night);
        }
    };

    return (
        <DhikrContext.Provider value={{
            dhikrs,
            currentDhikr,
            selectDhikr,
            count: currentProgress.count,
            rounds: currentProgress.rounds,
            updateProgress,
            resetCount,
            addDhikr,
            deleteDhikr,
            updateDhikr,
            theme,
            setTheme,
            applyCustomColor,
            toggleNightMode,
            THEMES
        }}>
            {children}
        </DhikrContext.Provider>
    );
}

export function useDhikr() {
    return useContext(DhikrContext);
}
