import React, { createContext, useState, useContext, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import * as DocumentPicker from 'expo-document-picker';
import { Alert, Platform } from 'react-native';
import { TRANSLATIONS } from '../constants/constants';

const DhikrContext = createContext();

const STORAGE_KEY = '@sabbeh_dhikrs_v1';

const INITIAL_DHIKRS = [
    { id: '1', title: 'Tasbih', subtitle: 'SubhanAllah', target: 33, nameKey: 'd_tasbih', subtitleKey: 's_tasbih' },
    { id: '2', title: 'Tahmid', subtitle: 'Alhamdulillah', target: 33, nameKey: 'd_tahmid', subtitleKey: 's_tahmid' },
    { id: '3', title: 'Takbir', subtitle: 'Allahu Akbar', target: 34, nameKey: 'd_takbir', subtitleKey: 's_takbir' },
    { id: '4', title: 'Istighfar', subtitle: 'Astaghfirullah', target: 100, nameKey: 'd_istighfar', subtitleKey: 's_istighfar' },
    { id: '5', title: 'Salawat', subtitle: 'Salawat on Prophet', target: 100, nameKey: 'd_salawat', subtitleKey: 's_salawat' },
    { id: '6', title: 'La ilaha illallah', subtitle: 'Tahlil', target: 100, nameKey: 'd_tahlil', subtitleKey: 's_tahlil' },
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
    const [language, setLanguage] = useState('en');

    // i18n Helper
    const i18n = (key) => {
        const langData = TRANSLATIONS[language] || TRANSLATIONS['en'];
        return langData[key] || key;
    };

    useEffect(() => {
        loadData();
    }, []);

    useEffect(() => {
        saveData();
    }, [dhikrs, progress, currentDhikrId, theme, language]);

    const loadData = async () => {
        try {
            const stored = await AsyncStorage.getItem(STORAGE_KEY);
            if (stored) {
                const parsed = JSON.parse(stored);
                if (parsed.dhikrs) setDhikrs(parsed.dhikrs);
                if (parsed.progress) setProgress(parsed.progress);
                if (parsed.currentDhikrId) setCurrentDhikrId(parsed.currentDhikrId);
                if (parsed.theme) setTheme(parsed.theme);
                if (parsed.language) setLanguage(parsed.language);
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
                theme,
                language
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
            text: '#E0F2F1'
        };
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

    const exportProfile = async () => {
        try {
            const profileData = {
                version: '1.0.0',
                timestamp: new Date().toISOString(),
                dhikrs,
                progress,
                currentDhikrId,
                theme: theme.name === 'Night' ? lastTheme : theme,
                isNightMode: theme.name === 'Night'
            };

            const jsonString = JSON.stringify(profileData, null, 2);
            console.log("Data prepared");

            if (Platform.OS === 'web') {
                const blob = new Blob([jsonString], { type: 'application/json' });
                const url = URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = 'sabbeh_backup.json';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                return;
            }

            if (Platform.OS === 'android' && FileSystem.StorageAccessFramework) {
                try {
                    console.log("Attempting SAF...");
                    const permissions = await FileSystem.StorageAccessFramework.requestDirectoryPermissionsAsync();
                    if (permissions.granted) {
                        const uri = await FileSystem.StorageAccessFramework.createFileAsync(
                            permissions.directoryUri,
                            'sabbeh_backup.json',
                            'application/json'
                        );
                        await FileSystem.writeAsStringAsync(uri, jsonString, { encoding: FileSystem.EncodingType.UTF8 });
                        await FileSystem.writeAsStringAsync(uri, jsonString, { encoding: FileSystem.EncodingType.UTF8 });
                        Alert.alert(i18n('success'), i18n('exported'), [{ text: i18n('ok') }]);
                        return; // Exit if SAF successful
                    } else {
                        Alert.alert(i18n('error'), "Permission to save file was denied.");
                        return;
                    }
                } catch (safError) {
                    console.log("SAF Failed, falling back to Sharing:", safError);
                    // Fall through to Sharing
                }
            }

            // Fallback for iOS or if SAF fails/missing on Android
            console.log("Using Sharing Fallback");
            const fileUri = FileSystem.documentDirectory + 'sabbeh_backup.json';
            await FileSystem.writeAsStringAsync(fileUri, jsonString);

            if (await Sharing.isAvailableAsync()) {
                await Sharing.shareAsync(fileUri, {
                    mimeType: 'application/json',
                    dialogTitle: i18n('exportProfile'),
                    UTI: 'public.json'
                });
            } else {
                Alert.alert(i18n('error'), "Sharing is not available on this device");
            }
        } catch (error) {
            console.error("Export Error:", error);
            Alert.alert(i18n('error'), "Failed to export profile");
        }
    };

    const importProfile = async () => {
        console.log("Starting Import Profile [DEBUG]");
        try {
            if (Platform.OS === 'web') {
                console.log("Web Import Detected");
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'application/json';
                input.onchange = async (e) => {
                    const file = e.target.files[0];
                    if (!file) return;
                    try {
                        const text = await file.text();
                        console.log("File read success, length:", text.length);
                        const data = JSON.parse(text);

                        if (!data.dhikrs || !data.progress) {
                            alert("Invalid File: This does not look like a valid Sabbeh profile.");
                            return;
                        }

                        if (confirm("Import Profile: This will replace your current dhikrs and progress. Are you sure?")) {
                            setDhikrs(data.dhikrs);
                            setProgress(data.progress);
                            if (data.currentDhikrId) setCurrentDhikrId(data.currentDhikrId);
                            if (data.theme) setTheme(data.theme);
                            alert("Profile imported successfully!");
                        }
                    } catch (err) {
                        console.error("Web parsing error", err);
                        alert("Failed to parse file");
                    }
                };
                input.click();
                return;
            }

            console.log("Native Import Detected");
            const result = await DocumentPicker.getDocumentAsync({
                type: ['application/json', '*/*'], // Broaden type
                copyToCacheDirectory: true
            });
            console.log("Picker Result:", result);

            if (result.canceled) return;

            const fileUri = result.assets[0].uri;
            const fileContent = await FileSystem.readAsStringAsync(fileUri);
            const data = JSON.parse(fileContent);

            if (!data.dhikrs || !data.progress) {
                Alert.alert(i18n('error'), "This does not look like a valid Sabbeh profile.");
                return;
            }

            // Confirm Overwrite
            Alert.alert(
                i18n('importProfile'),
                i18n('confirmImport'),
                [
                    { text: i18n('cancel'), style: "cancel" },
                    {
                        text: i18n('importButton'),
                        style: "destructive",
                        onPress: async () => {
                            setDhikrs(data.dhikrs);
                            setProgress(data.progress);
                            if (data.currentDhikrId) setCurrentDhikrId(data.currentDhikrId);

                            // Restore theme if matches preset or custom? 
                            // For simplicity, let's load it if it looks valid
                            if (data.theme) setTheme(data.theme);

                            // If it was night mode in backup, maybe asking to restore? 
                            // Let's just restore the base theme preference.

                            // Let's just restore the base theme preference.

                            Alert.alert(i18n('success'), i18n('imported'), [{ text: i18n('ok') }]);
                        }
                    }
                ]
            );

        } catch (error) {
            console.error("Import Error:", error);
            Alert.alert(i18n('error'), "Could not parse the profile file.");
        }
    };

    const resetAppData = async () => {
        Alert.alert(
            i18n('resetData'),
            i18n('confirmResetData'),
            [
                { text: i18n('cancel'), style: "cancel" },
                {
                    text: i18n('resetButton'),
                    style: "destructive",
                    onPress: async () => {
                        try {
                            await AsyncStorage.removeItem(STORAGE_KEY);
                            setDhikrs(INITIAL_DHIKRS);
                            setProgress({});
                            setCurrentDhikrId('1');
                            setTheme(THEMES.Teal);
                            Alert.alert(i18n('success'), i18n('resetDataDesc'), [{ text: i18n('ok') }]);
                        } catch (e) {
                            Alert.alert(i18n('error'), "Failed to reset data");
                        }
                    }
                }
            ]
        );
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
            exportProfile,
            importProfile,
            resetAppData,
            THEMES,
            language,
            setLanguage,
            i18n
        }}>
            {children}
        </DhikrContext.Provider>
    );
}

export function useDhikr() {
    return useContext(DhikrContext);
}
