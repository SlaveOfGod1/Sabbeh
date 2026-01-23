import React, { useState } from 'react';
import { View, StyleSheet, Text, Platform, StatusBar, Modal, TouchableOpacity } from 'react-native';
import CounterDisplay from '../components/CounterDisplay';
import CounterControls from '../components/CounterControls';
import { useNavigation } from 'expo-router';
import { useDhikr } from '../context/DhikrContext';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import SimpleColorPicker from '../components/SimpleColorPicker';
import SettingsModal from '../components/SettingsModal';

export default function MainScreen() {
    const navigation = useNavigation();
    const { currentDhikr, count, rounds, updateProgress, resetCount, theme, setTheme, THEMES, applyCustomColor, toggleNightMode, i18n } = useDhikr();

    const [themeModalVisible, setThemeModalVisible] = useState(false);
    const [settingsModalVisible, setSettingsModalVisible] = useState(false);

    // Initial color for picker
    const [tempColor, setTempColor] = useState('#42A5F5');

    // Use theme color for icons (darker shade usually at index 1)
    const primaryColor = theme.colors[1] || '#00897B';

    const renderThemeButton = (key, themeOption) => {
        const isActive = theme.name === themeOption.name;
        // Translate theme name
        const displayName = i18n('t_' + themeOption.name.toLowerCase()) || themeOption.name;

        return (
            <TouchableOpacity
                key={key}
                style={[
                    styles.themeOption,
                    theme.isDark && { backgroundColor: '#37474F', borderColor: '#455A64' },
                    isActive && styles.activeThemeOption
                ]}
                onPress={() => setTheme(themeOption)}
            >
                <View style={[styles.themePreview, { backgroundColor: themeOption.colors[1] }]} />
                <Text style={[styles.themeName, theme.isDark && { color: '#B0BEC5' }]}>{displayName}</Text>
            </TouchableOpacity>
        );
    };

    const handleApplyCustom = () => {
        applyCustomColor(tempColor);
        setThemeModalVisible(false); // Close after apply
    };

    // Helper to get translated title
    const getDhikrTitle = (dhikr) => {
        if (dhikr.nameKey) return i18n(dhikr.nameKey);
        // Fallback for existing data using ID
        switch (dhikr.id) {
            case '1': return i18n('d_tasbih');
            case '2': return i18n('d_tahmid');
            case '3': return i18n('d_takbir');
            case '4': return i18n('d_istighfar');
            case '5': return i18n('d_salawat');
            case '6': return i18n('d_tahlil');
            default: return dhikr.title;
        }
    };

    const getDhikrSubtitle = (dhikr) => {
        if (dhikr.subtitleKey) return i18n(dhikr.subtitleKey);
        // Fallback for existing data using ID
        switch (dhikr.id) {
            case '1': return i18n('s_tasbih');
            case '2': return i18n('s_tahmid');
            case '3': return i18n('s_takbir');
            case '4': return i18n('s_istighfar');
            case '5': return i18n('s_salawat');
            case '6': return i18n('s_tahlil');
            default: return dhikr.subtitle;
        }
    };

    return (
        <LinearGradient
            colors={theme.colors}
            start={{ x: 0, y: 0 }}
            end={theme.end}
            style={styles.mainContainer}
        >
            {/* Custom Header Area */}
            <View style={styles.header}>
                <TouchableOpacity style={styles.iconButton} onPress={() => navigation.toggleDrawer()}>
                    <Ionicons name="menu" size={24} color={primaryColor} />
                </TouchableOpacity>

                <View style={styles.headerRightButtons}>
                    <TouchableOpacity style={styles.iconButton} onPress={() => setThemeModalVisible(true)}>
                        <Ionicons name="color-palette-outline" size={24} color={primaryColor} />
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.iconButton} onPress={toggleNightMode}>
                        <Ionicons name={theme.name === 'Night' ? "moon" : "moon-outline"} size={24} color={primaryColor} />
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.iconButton} onPress={() => setSettingsModalVisible(true)}>
                        <Ionicons name="settings-outline" size={24} color={primaryColor} />
                    </TouchableOpacity>
                </View>
            </View>

            <View style={styles.centerContent}>
                <Text style={styles.title}>{getDhikrTitle(currentDhikr)}</Text>

                <View style={styles.subtitleRow}>
                    <Text style={[styles.subtitle, { color: '#E0F2F1' }]}>{getDhikrSubtitle(currentDhikr)}</Text>
                    <View style={styles.roundsBadge}>
                        <Text style={styles.roundsText}>{i18n('rounds')}: {rounds}</Text>
                    </View>
                </View>

                <View style={[styles.deviceBody, { backgroundColor: theme.deviceBody || '#F5F5F5' }]}>
                    <CounterDisplay count={count} />
                    <CounterControls onCount={() => updateProgress(1)} onReset={resetCount} />
                </View>
            </View>

            {/* Theme Modal */}
            <Modal
                animationType="slide"
                transparent={true}
                visible={themeModalVisible}
                onRequestClose={() => setThemeModalVisible(false)}
            >
                <TouchableOpacity
                    style={styles.modalOverlay}
                    activeOpacity={1}
                    onPress={() => setThemeModalVisible(false)}
                >
                    <TouchableOpacity activeOpacity={1} style={[styles.themeModal, { bottom: 0, backgroundColor: theme.isDark ? '#263238' : '#fff' }]}>
                        <View style={styles.modalHandle} />
                        <View style={styles.modalHeader}>
                            <Ionicons name="color-palette-outline" size={20} color={theme.isDark ? '#ECEFF1' : '#37474F'} />
                            <Text style={[styles.modalTitle, theme.isDark && { color: '#ECEFF1' }]}>{i18n('theme')}</Text>
                        </View>

                        <View style={styles.themesGrid}>
                            {Object.entries(THEMES)
                                .filter(([key]) => key !== 'Night')
                                .map(([key, themeOption]) => renderThemeButton(key, themeOption))}
                        </View>

                        <Text style={[styles.sectionTitle, theme.isDark && { color: '#ECEFF1' }]}>{i18n('customColor')}</Text>
                        <View style={styles.customPickerContainer}>
                            <SimpleColorPicker
                                initialColor={tempColor}
                                onSelectColor={(c) => setTempColor(c)}
                            />

                            <TouchableOpacity style={styles.applyButton} onPress={handleApplyCustom}>
                                <Text style={styles.applyButtonText}>{i18n('save')}</Text>
                            </TouchableOpacity>
                        </View>

                    </TouchableOpacity>
                </TouchableOpacity>
            </Modal>

            <SettingsModal
                visible={settingsModalVisible}
                onClose={() => setSettingsModalVisible(false)}
            />
        </LinearGradient >
    );
}

const styles = StyleSheet.create({
    mainContainer: {
        flex: 1,
        paddingTop: Platform.OS === 'android' ? StatusBar.currentHeight : 40,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: 20,
        alignItems: 'center',
        height: 60,
    },
    headerRightButtons: {
        flexDirection: 'row',
        gap: 12,
    },
    iconButton: {
        width: 44,
        height: 44,
        borderRadius: 22,
        top: 5,
        backgroundColor: 'rgba(255,255,255,0.9)',
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.2,
        shadowRadius: 3,
    },
    centerContent: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingBottom: 50,
    },
    title: {
        color: '#fff',
        fontSize: 28,
        fontWeight: 'bold',
        marginBottom: 5,
    },
    subtitleRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 30,
        gap: 10,
    },
    subtitle: {
        fontSize: 16,
        fontWeight: '500',
    },
    roundsBadge: {
        backgroundColor: 'rgba(0,0,0,0.2)',
        paddingHorizontal: 10,
        paddingVertical: 5,
        borderRadius: 15,
    },
    roundsText: {
        color: '#E0F2F1',
        fontSize: 12,
        fontWeight: 'bold',
    },
    deviceBody: {
        backgroundColor: '#F5F5F5',
        width: 320,
        height: 500,
        borderRadius: 90,
        alignItems: 'center',
        justifyContent: 'flex-start',
        paddingTop: 50,
        elevation: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 15 },
        shadowOpacity: 0.3,
        shadowRadius: 15,
    },
    // Modal Styles
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0,0,0,0.3)',
        justifyContent: 'flex-end',
    },
    themeModal: {
        backgroundColor: '#fff',
        borderTopLeftRadius: 25,
        borderTopRightRadius: 25,
        padding: 25,
        paddingBottom: 40,
        maxHeight: '80%', // Allow scrolling if needed, or simply constrain height
        elevation: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: -2 },
        shadowOpacity: 0.1,
        shadowRadius: 10,
    },
    modalHandle: {
        width: 40,
        height: 4,
        backgroundColor: '#CFD8DC',
        borderRadius: 2,
        alignSelf: 'center',
        marginBottom: 20,
    },
    modalHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 20,
        gap: 10,
    },
    modalTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#37474F',
    },
    themesGrid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
        gap: 15,
        marginBottom: 25,
    },
    themeOption: {
        flexDirection: 'row',
        alignItems: 'center',
        width: '45%',
        padding: 10,
        borderRadius: 12,
        backgroundColor: '#FAFAFA', // We override this in render if dark? Or just change base style logic
        borderWidth: 1,
        borderColor: '#ECEFF1',
        gap: 10,
    },
    activeThemeOption: {
        borderColor: '#00897B',
        backgroundColor: '#E0F2F1',
    },
    themePreview: {
        width: 24,
        height: 24,
        borderRadius: 12,
    },
    themeName: {
        fontSize: 14,
        fontWeight: '500',
        color: '#455A64',
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '500',
        color: '#37474F',
        marginBottom: 15,
    },
    customPickerContainer: {
        alignItems: 'center',
    },
    applyButton: {
        backgroundColor: '#37474F',
        paddingVertical: 12,
        paddingHorizontal: 30,
        borderRadius: 20,
        alignItems: 'center',
        marginTop: 20,
        width: '100%',
    },
    applyButtonText: {
        color: '#fff',
        fontWeight: 'bold',
        fontSize: 16,
    }

});
