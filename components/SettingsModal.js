import React from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, Linking } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDhikr } from '../context/DhikrContext';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function SettingsModal({ visible, onClose }) {
    const { theme, exportProfile, importProfile } = useDhikr();
    const insets = useSafeAreaInsets();

    const primaryColor = theme.colors[1] || '#00897B';
    const isDark = theme.isDark;

    const bgColor = isDark ? '#263238' : '#F5F5F5';
    const cardColor = isDark ? '#37474F' : '#FFFFFF';
    const textColor = isDark ? '#ECEFF1' : '#37474F';
    const subTextColor = isDark ? '#B0BEC5' : '#78909C';

    return (
        <Modal
            animationType="slide"
            transparent={true}
            visible={visible}
            onRequestClose={onClose}
        >
            <View style={[styles.container, { backgroundColor: bgColor }]}>
                {/* Header */}
                <View style={[styles.header, { paddingTop: insets.top + 20, backgroundColor: cardColor }]}>
                    <TouchableOpacity onPress={onClose} style={styles.backButton}>
                        <Ionicons name="arrow-back" size={24} color={textColor} />
                    </TouchableOpacity>
                    <Text style={[styles.headerTitle, { color: textColor }]}>Settings</Text>
                    <View style={{ width: 24 }} />
                </View>

                <View style={styles.content}>

                    {/* About Section */}
                    <View style={styles.section}>
                        <View style={styles.sectionHeader}>
                            <Ionicons name="information-circle-outline" size={20} color={textColor} />
                            <Text style={[styles.sectionTitle, { color: textColor }]}>About</Text>
                        </View>

                        <View style={[styles.card, { backgroundColor: cardColor }]}>
                            <Text style={[styles.appName, { color: textColor }]}>Sabbeh - Islamic Rosary App</Text>
                            <Text style={[styles.appDesc, { color: subTextColor }]}>
                                A digital counter for your dhikr and prayers. Free Of Ads And Trackers, Made for the sake of Allah.
                            </Text>
                            <Text style={[styles.version, { color: subTextColor }]}>Version 1.0.0</Text>
                        </View>
                    </View>

                    {/* Profile Management Section */}
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: textColor, marginBottom: 10 }]}>Profile Management</Text>

                        <TouchableOpacity style={[styles.actionButton, { backgroundColor: cardColor }]} onPress={() => { console.log('Export Button Pressed'); exportProfile(); }}>
                            <View style={styles.iconContainer}>
                                <Ionicons name="download-outline" size={24} color={textColor} />
                            </View>
                            <View style={styles.actionTextContainer}>
                                <Text style={[styles.actionTitle, { color: textColor }]}>Export Profile</Text>
                                <Text style={[styles.actionSubtitle, { color: subTextColor }]}>Save your data as JSON file</Text>
                            </View>
                        </TouchableOpacity>

                        <TouchableOpacity style={[styles.actionButton, { backgroundColor: cardColor }]} onPress={() => { console.log('Import Button Pressed'); importProfile(); }}>
                            <View style={styles.iconContainer}>
                                <Ionicons name="cloud-upload-outline" size={24} color={textColor} />
                            </View>
                            <View style={styles.actionTextContainer}>
                                <Text style={[styles.actionTitle, { color: textColor }]}>Import Profile</Text>
                                <Text style={[styles.actionSubtitle, { color: subTextColor }]}>Load data from JSON file</Text>
                            </View>
                        </TouchableOpacity>

                        <View style={[styles.infoBox, { backgroundColor: isDark ? 'rgba(66, 165, 245, 0.1)' : '#E3F2FD' }]}>
                            <Text style={[styles.infoText, { color: isDark ? '#90CAF9' : '#1565C0' }]}>
                                <Text style={{ fontWeight: 'bold' }}>Profile includes:</Text> Custom dhikrs, theme settings, dark/light mode preference, and count history for each session.
                            </Text>
                        </View>

                        {/* GitHub Footer */}
                        <TouchableOpacity
                            style={styles.githubButton}
                            onPress={() => Linking.openURL('https://github.com/StartYourProject/Sabbeh')}
                        >
                            <Ionicons name="logo-github" size={24} color={subTextColor} />
                            <Text style={[styles.githubText, { color: subTextColor }]}>View Source Code on GitHub</Text>
                        </TouchableOpacity>
                    </View>

                </View>
            </View>
        </Modal>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 20,
        paddingBottom: 20,
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
    },
    content: {
        padding: 20,
    },
    section: {
        marginBottom: 30,
    },
    sectionHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        marginBottom: 10,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '600',
    },
    card: {
        padding: 20,
        borderRadius: 16,
        // elevation: 1,
    },
    appName: {
        fontSize: 16,
        fontWeight: 'bold',
        marginBottom: 8,
    },
    appDesc: {
        fontSize: 14,
        lineHeight: 20,
        marginBottom: 15,
    },
    version: {
        fontSize: 12,
    },
    actionButton: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
        borderRadius: 16,
        marginBottom: 12,
    },
    iconContainer: {
        width: 40,
        alignItems: 'center',
    },
    actionTextContainer: {
        flex: 1,
        marginLeft: 10,
    },
    actionTitle: {
        fontSize: 16,
        fontWeight: '500',
        marginBottom: 2,
    },
    actionSubtitle: {
        fontSize: 13,
    },
    infoBox: {
        padding: 15,
        borderRadius: 12,
        marginTop: 5,
    },
    infoText: {
        fontSize: 13,
        lineHeight: 18,
    },
    githubButton: {
        marginTop: 20,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'row',
        gap: 8,
        padding: 10,
    },
    githubText: {
        fontSize: 14,
        fontWeight: '500',
    }
});
