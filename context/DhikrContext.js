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

export function DhikrProvider({ children }) {
    const [dhikrs, setDhikrs] = useState(INITIAL_DHIKRS);
    const [currentDhikrId, setCurrentDhikrId] = useState('1');
    // Store progress separately: { [id]: { count: 0, rounds: 0 } }
    const [progress, setProgress] = useState({});

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        saveData();
    }, [dhikrs, progress, currentDhikrId]);

    const loadData = async () => {
        try {
            const stored = await AsyncStorage.getItem(STORAGE_KEY);
            if (stored) {
                const parsed = JSON.parse(stored);
                if (parsed.dhikrs) setDhikrs(parsed.dhikrs);
                if (parsed.progress) setProgress(parsed.progress);
                if (parsed.currentDhikrId) setCurrentDhikrId(parsed.currentDhikrId);
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
                currentDhikrId
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
            // We usually don't reset rounds unless specific action, but user said "reset" button.
            // Usually small button is just for the current counter loop? 
            // Let's assume just current count. If they want full reset maybe long press reset?
            // For now, reset clears current loop count only.
        });
    };

    // Helper simply to set absolute values if needed
    const setExactCount = (val) => {
        // Not typically used with current logic but keeping interface valid
        // This might be tricky with rounds logic, so let's stick to updateProgress logic in main screen
    };

    const addDhikr = (title, subtitle, target) => {
        const newDhikr = {
            id: Date.now().toString(),
            title,
            subtitle,
            target: parseInt(target) || 33,
        };
        setDhikrs([...dhikrs, newDhikr]);
        // Initialize progress
        setProgress(prev => ({ ...prev, [newDhikr.id]: { count: 0, rounds: 0 } }));
        setCurrentDhikrId(newDhikr.id);
    };

    const deleteDhikr = (id) => {
        const updated = dhikrs.filter(d => d.id !== id);
        setDhikrs(updated);

        // Cleanup progress
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
            updateDhikr
        }}>
            {children}
        </DhikrContext.Provider>
    );
}

export function useDhikr() {
    return useContext(DhikrContext);
}
