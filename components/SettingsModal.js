import { View, Text, StyleSheet, Modal, TouchableOpacity, Linking, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useDhikr } from '../context/DhikrContext';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function SettingsModal({ visible, onClose }) {
    const { theme, exportProfile, importProfile, i18n, language, setLanguage, resetAppData } = useDhikr();
    const insets = useSafeAreaInsets();

    const primaryColor = theme.colors[1] || '#00897B';
    const isDark = theme.isDark;

    const bgColor = isDark ? '#263238' : '#F5F5F5';
    const cardColor = isDark ? '#37474F' : '#FFFFFF';
    const textColor = isDark ? '#ECEFF1' : '#37474F';
    const subTextColor = isDark ? '#B0BEC5' : '#78909C';

    const LANGUAGES = [
        { code: 'en', label: 'English' },
        { code: 'ar', label: 'العربية' },
        { code: 'tr', label: 'Türkçe' },
        { code: 'zh', label: '中文' },
        { code: 'ms', label: 'Melayu' },
        { code: 'ur', label: 'اردو' },
        { code: 'ja', label: '日本語' },
    ];

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
                    <Text style={[styles.headerTitle, { color: textColor }]}>{i18n('settings')}</Text>
                    <View style={{ width: 24 }} />
                </View>

                <ScrollView style={styles.scrollContent} showsVerticalScrollIndicator={true}>
                    <View style={styles.content}>

                        {/* Language Section */}
                        <View style={styles.section}>
                            <View style={styles.sectionHeader}>
                                <Ionicons name="language-outline" size={20} color={textColor} />
                                <Text style={[styles.sectionTitle, { color: textColor }]}>{i18n('language')}</Text>
                            </View>
                            <View style={[styles.card, { backgroundColor: cardColor, flexDirection: 'row', flexWrap: 'wrap', gap: 8 }]}>
                                {LANGUAGES.map(lang => (
                                    <TouchableOpacity
                                        key={lang.code}
                                        style={[
                                            styles.langButton,
                                            {
                                                backgroundColor: language === lang.code ? primaryColor : (isDark ? '#455A64' : '#EEEEEE'),
                                                borderColor: language === lang.code ? primaryColor : 'transparent'
                                            }
                                        ]}
                                        onPress={() => setLanguage(lang.code)}
                                    >
                                        <Text style={{
                                            color: language === lang.code ? '#fff' : textColor,
                                            fontSize: 14,
                                            fontWeight: language === lang.code ? 'bold' : 'normal'
                                        }}>
                                            {lang.label}
                                        </Text>
                                    </TouchableOpacity>
                                ))}
                            </View>
                        </View>

                        {/* About Section */}
                        <View style={styles.section}>
                            <View style={styles.sectionHeader}>
                                <Ionicons name="information-circle-outline" size={20} color={textColor} />
                                <Text style={[styles.sectionTitle, { color: textColor }]}>{i18n('about')}</Text>
                            </View>

                            <View style={[styles.card, { backgroundColor: cardColor }]}>
                                <Text style={[styles.appName, { color: textColor }]}>{i18n('appName')}</Text>
                                <Text style={[styles.appDesc, { color: subTextColor }]}>
                                    {i18n('appDesc')}
                                </Text>
                                <Text style={[styles.version, { color: subTextColor }]}>Version 1.0.0</Text>
                            </View>
                        </View>

                        {/* Profile Management Section */}
                        <View style={styles.section}>
                            <Text style={[styles.sectionTitle, { color: textColor, marginBottom: 10 }]}>{i18n('profileManagement')}</Text>

                            <TouchableOpacity style={[styles.actionButton, { backgroundColor: cardColor }]} onPress={() => { console.log('Export Button Pressed'); exportProfile(); }}>
                                <View style={styles.iconContainer}>
                                    <Ionicons name="download-outline" size={24} color={textColor} />
                                </View>
                                <View style={styles.actionTextContainer}>
                                    <Text style={[styles.actionTitle, { color: textColor }]}>{i18n('exportProfile')}</Text>
                                    <Text style={[styles.actionSubtitle, { color: subTextColor }]}>{i18n('exportDesc')}</Text>
                                </View>
                            </TouchableOpacity>

                            <TouchableOpacity style={[styles.actionButton, { backgroundColor: cardColor }]} onPress={() => { console.log('Import Button Pressed'); importProfile(); }}>
                                <View style={styles.iconContainer}>
                                    <Ionicons name="cloud-upload-outline" size={24} color={textColor} />
                                </View>
                                <View style={styles.actionTextContainer}>
                                    <Text style={[styles.actionTitle, { color: textColor }]}>{i18n('importProfile')}</Text>
                                    <Text style={[styles.actionSubtitle, { color: subTextColor }]}>{i18n('importDesc')}</Text>
                                </View>
                            </TouchableOpacity>

                            <View style={[styles.infoBox, { backgroundColor: isDark ? 'rgba(66, 165, 245, 0.1)' : '#E3F2FD' }]}>
                                <Text style={[styles.infoText, { color: isDark ? '#90CAF9' : '#1565C0' }]}>
                                    {i18n('profileNote')}
                                </Text>
                            </View>

                            {/* GitHub Footer */}
                            <TouchableOpacity
                                style={styles.githubButton}
                                onPress={() => Linking.openURL('https://github.com/SlaveOfGod1/Sabbeh')}
                            >
                                <Ionicons name="logo-github" size={24} color={subTextColor} />
                                <Text style={[styles.githubText, { color: subTextColor }]}>{i18n('github')}</Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                style={styles.githubButton}
                                onPress={() => Linking.openURL('https://github.com/SlaveOfGod1/Sabbeh/issues')}
                            >
                                <Ionicons name="bug-outline" size={24} color={subTextColor} />
                                <Text style={[styles.githubText, { color: subTextColor }]}>{i18n('reportBug')}</Text>
                            </TouchableOpacity>

                            {/* Reset Data Button */}
                            <TouchableOpacity
                                style={[styles.resetButton, { backgroundColor: isDark ? '#3E2723' : '#FFEBEE', borderColor: isDark ? '#D32F2F' : '#FFCDD2' }]}
                                onPress={() => resetAppData()}
                            >
                                <Ionicons name="trash-outline" size={20} color="#FF5252" />
                                <Text style={styles.resetButtonText}>{i18n('resetData')}</Text>
                            </TouchableOpacity>
                        </View>
                        <View style={{ height: 40 }} />
                    </View>
                </ScrollView>
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
    },
    langButton: {
        paddingVertical: 8,
        paddingHorizontal: 12,
        borderRadius: 20,
        borderWidth: 1,
        marginBottom: 4
    },
    resetButton: {
        marginTop: 30,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 15,
        borderRadius: 12,
        borderWidth: 1,
        gap: 8,
    },
    resetButtonText: {
        color: '#FF5252',
        fontSize: 16,
        fontWeight: '600',
    }
});
