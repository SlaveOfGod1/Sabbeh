import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, Modal, TextInput, Alert } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useDhikr } from '../context/DhikrContext';

export default function CustomDrawerContent(props) {
    const { top, bottom } = useSafeAreaInsets();
    const { dhikrs, currentDhikr, selectDhikr, addDhikr, deleteDhikr, updateDhikr, theme, i18n } = useDhikr();

    const [modalVisible, setModalVisible] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [title, setTitle] = useState('');
    const [subtitle, setSubtitle] = useState('');
    const [target, setTarget] = useState('33');

    const [optionsModalVisible, setOptionsModalVisible] = useState(false);
    const [selectedDhikrForOptions, setSelectedDhikrForOptions] = useState(null);

    // Use theme color
    const primaryColor = theme.colors[1] || '#00897B';

    const handleSelect = (item) => {
        selectDhikr(item);
        props.navigation.closeDrawer();
    };

    const handleLongPress = (item) => {
        setSelectedDhikrForOptions(item);
        setOptionsModalVisible(true);
    };

    const openAddModal = () => {
        setEditingId(null);
        setTitle('');
        setSubtitle('');
        setTarget('33');
        setModalVisible(true);
    };

    const handleOptionEdit = () => {
        setOptionsModalVisible(false);
        if (selectedDhikrForOptions) {
            openEditModal(selectedDhikrForOptions);
        }
    };

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

    const handleOptionDelete = () => {
        setOptionsModalVisible(false);
        if (selectedDhikrForOptions) {
            Alert.alert(
                i18n('deleteDhikr'),
                `${i18n('confirmDelete')}`,
                [
                    { text: i18n('cancel'), style: "cancel" },
                    {
                        text: i18n('delete'),
                        style: "destructive",
                        onPress: () => deleteDhikr(selectedDhikrForOptions.id)
                    }
                ]
            );
        }
    };

    const openEditModal = (item) => {
        setEditingId(item.id);
        setTitle(item.title);
        setSubtitle(item.subtitle);
        setTarget(String(item.target));
        setModalVisible(true);
    };

    const saveDhikr = () => {
        if (!title.trim()) {
            Alert.alert(i18n('error'), "Please enter a name for the Dhikr");
            return;
        }

        if (editingId) {
            updateDhikr(editingId, { title, subtitle, target: parseInt(target) });
        } else {
            addDhikr(title, subtitle, target);
        }
        setModalVisible(false);
    };

    const renderItem = ({ item }) => {
        const isActive = currentDhikr.id === item.id;
        return (
            <TouchableOpacity
                style={[
                    styles.itemCard,
                    theme.isDark && { backgroundColor: '#37474F', borderColor: '#455A64' },
                    isActive && { backgroundColor: primaryColor, borderColor: primaryColor }
                ]}
                onPress={() => handleSelect(item)}
                onLongPress={() => handleLongPress(item)}
                delayLongPress={500}
                activeOpacity={0.8}
            >
                <View>
                    <View style={styles.itemHeaderRow}>
                        <Text style={[styles.itemTitle, theme.isDark && { color: '#ECEFF1' }, isActive && styles.activeItemText]}>
                            {getDhikrTitle(item)}
                        </Text>
                        <View style={[styles.targetBadge, theme.isDark && { backgroundColor: '#455A64' }, isActive && styles.activeTargetBadge]}>
                            <Text style={[styles.targetText, { color: isActive ? '#fff' : (theme.isDark ? '#B0BEC5' : primaryColor) }]}>{item.target}x</Text>
                        </View>
                    </View>
                    {item.subtitle ? (
                        <Text style={[styles.itemSubtitle, theme.isDark && { color: '#B0BEC5' }, isActive && styles.activeItemSubtitle]}>
                            {getDhikrSubtitle(item)}
                        </Text>
                    ) : null}
                </View>
            </TouchableOpacity>
        );
    };

    // Determine header and footer background based on theme for better integration
    // Simplest is to keep them white or match theme?
    // Let's use theme property if available or fallback
    const headerBg = theme.deviceBody || primaryColor; // Using deviceBody for header? No, header is primary usually.

    // Actually, header usually primary color.

    return (
        <View style={{ flex: 1, backgroundColor: theme.isDark ? '#263238' : '#fff' }}>
            {/* Header */}
            <View style={[styles.header, { paddingTop: top + 20, backgroundColor: primaryColor }]}>
                <View style={styles.headerTopRow}>
                    <Text style={styles.headerTitle}>{i18n('appName')}</Text>
                </View>
                <Text style={styles.headerSubtitle}>{i18n('appShortDesc')}</Text>
            </View>

            {/* List */}
            <FlatList
                data={dhikrs}
                keyExtractor={(item) => item.id}
                renderItem={renderItem}
                contentContainerStyle={styles.listContent}
                showsVerticalScrollIndicator={false}
            />

            {/* Footer */}
            <View style={[styles.footer, { paddingBottom: bottom + 13, borderColor: theme.isDark ? '#37474F' : '#EEEEEE' }]}>
                <TouchableOpacity style={[styles.addButton, theme.isDark && { borderColor: '#546E7A', backgroundColor: '#37474F' }]} onPress={openAddModal}>
                    <Text style={[styles.addButtonText, theme.isDark && { color: '#B0BEC5' }]}>+ {i18n('addDhikr')}</Text>
                </TouchableOpacity>
                <Text style={[styles.footerNote, theme.isDark && { color: '#90A4AE' }]}>{i18n('appName')}</Text>
                <Text style={styles.footerSubNote}>v1.0.0</Text>
            </View>

            {/* Modal for Options (Edit/Delete) */}
            <Modal
                animationType="fade"
                transparent={true}
                visible={optionsModalVisible}
                onRequestClose={() => setOptionsModalVisible(false)}
            >
                <TouchableOpacity
                    style={styles.centeredView}
                    activeOpacity={1}
                    onPress={() => setOptionsModalVisible(false)}
                >
                    <View style={[styles.modalView, theme.isDark && { backgroundColor: '#37474F' }]}>
                        <Text style={[styles.modalTitle, { color: theme.isDark ? '#ECEFF1' : primaryColor }]}>
                            {selectedDhikrForOptions?.title}
                        </Text>

                        <TouchableOpacity
                            style={[
                                styles.optionButton,
                                styles.optionButtonEdit,
                                theme.isDark && { backgroundColor: '#455A64', borderColor: '#546E7A' }
                            ]}
                            onPress={handleOptionEdit}
                        >
                            <Text style={[styles.optionButtonText, theme.isDark && { color: '#ECEFF1' }]}>{i18n('editDhikr')}</Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={[
                                styles.optionButton,
                                styles.optionButtonDelete,
                                theme.isDark && { backgroundColor: '#3E2723', borderColor: '#D32F2F' }
                            ]}
                            onPress={handleOptionDelete}
                        >
                            <Text style={[styles.optionButtonText, { color: '#FF5252' }]}>{i18n('deleteDhikr')}</Text>
                        </TouchableOpacity>

                        <TouchableOpacity style={styles.optionButtonCancel} onPress={() => setOptionsModalVisible(false)}>
                            <Text style={[styles.optionButtonCancelText, theme.isDark && { color: '#B0BEC5' }]}>{i18n('cancel')}</Text>
                        </TouchableOpacity>
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Modal for Add/Edit Form */}
            <Modal
                animationType="slide"
                transparent={true}
                visible={modalVisible}
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={styles.centeredView}>
                    <View style={[styles.modalView, theme.isDark && { backgroundColor: '#37474F' }]}>
                        <Text style={[styles.modalTitle, { color: theme.isDark ? '#ECEFF1' : primaryColor }]}>{editingId ? i18n('editDhikr') : i18n('addDhikr')}</Text>

                        <Text style={[styles.inputLabel, theme.isDark && { color: '#B0BEC5' }]}>{i18n('title')}</Text>
                        <TextInput
                            style={[styles.input, theme.isDark && { backgroundColor: '#455A64', borderColor: '#546E7A', color: '#fff' }]}
                            placeholder="e.g. Salawat"
                            placeholderTextColor={theme.isDark ? '#90A4AE' : '#999'}
                            value={title}
                            onChangeText={setTitle}
                        />

                        <Text style={[styles.inputLabel, theme.isDark && { color: '#B0BEC5' }]}>{i18n('subtitle')}</Text>
                        <TextInput
                            style={[styles.input, theme.isDark && { backgroundColor: '#455A64', borderColor: '#546E7A', color: '#fff' }]}
                            placeholder="e.g. Allahumma salli ala..."
                            placeholderTextColor={theme.isDark ? '#90A4AE' : '#999'}
                            value={subtitle}
                            onChangeText={setSubtitle}
                        />

                        <Text style={[styles.inputLabel, theme.isDark && { color: '#B0BEC5' }]}>{i18n('target')}</Text>
                        <TextInput
                            style={[styles.input, theme.isDark && { backgroundColor: '#455A64', borderColor: '#546E7A', color: '#fff' }]}
                            placeholder="33"
                            placeholderTextColor={theme.isDark ? '#90A4AE' : '#999'}
                            value={target}
                            onChangeText={setTarget}
                            keyboardType="numeric"
                        />

                        <View style={styles.modalButtons}>
                            <TouchableOpacity
                                style={[styles.button, styles.buttonClose, theme.isDark && { backgroundColor: '#546E7A' }]}
                                onPress={() => setModalVisible(false)}
                            >
                                <Text style={[styles.textStyle, theme.isDark && { color: '#ECEFF1' }]}>{i18n('cancel')}</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.button, { backgroundColor: primaryColor }]}
                                onPress={saveDhikr}
                            >
                                <Text style={[styles.textStyle, { color: '#fff' }]}>{i18n('save')}</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </Modal>

        </View>
    );
}

const styles = StyleSheet.create({
    header: {
        paddingHorizontal: 20,
        paddingBottom: 20,
        borderBottomRightRadius: 0,
    },
    headerTopRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 10,
    },
    headerTitle: {
        color: '#fff',
        fontSize: 20,
        fontWeight: 'bold',
    },
    headerSubtitle: {
        color: '#E0F2F1',
        fontSize: 14,
    },
    listContent: {
        padding: 15,
    },
    itemCard: {
        backgroundColor: '#fff',
        borderRadius: 12,
        padding: 15,
        marginBottom: 10,
        borderWidth: 1,
        borderColor: '#EEEEEE',
        elevation: 2,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.1,
        shadowRadius: 2,
    },
    itemHeaderRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 5,
    },
    itemTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#37474F',
    },
    activeItemText: {
        color: '#fff',
    },
    itemSubtitle: {
        fontSize: 13,
        color: '#78909C',
    },
    activeItemSubtitle: {
        color: '#B2DFDB',
    },
    targetBadge: {
        backgroundColor: '#ECEFF1',
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 12,
    },
    activeTargetBadge: {
        backgroundColor: 'rgba(255,255,255,0.2)',
    },
    targetText: {
        fontSize: 12,
        fontWeight: 'bold',
    },
    footer: {
        padding: 15,
        borderTopWidth: 1,
        borderTopColor: '#EEEEEE',
        alignItems: 'center',
    },
    addButton: {
        width: '100%',
        paddingVertical: 15,
        borderWidth: 1,
        borderColor: '#E0E0E0',
        borderRadius: 12,
        borderStyle: 'dashed',
        alignItems: 'center',
        marginBottom: 10,
        backgroundColor: '#FAFAFA',
    },
    addButtonText: {
        color: '#546E7A',
        fontWeight: '600',
    },
    footerNote: {
        color: '#455A64',
        fontSize: 12,
        marginBottom: 2,
    },
    footerSubNote: {
        color: '#90A4AE',
        fontSize: 11,
    },
    // Modal Styles
    centeredView: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "rgba(0,0,0,0.5)",
    },
    modalView: {
        margin: 20,
        backgroundColor: "white",
        borderRadius: 20,
        padding: 25,
        width: '85%',
        shadowColor: "#000",
        shadowOffset: {
            width: 0,
            height: 2
        },
        shadowOpacity: 0.25,
        shadowRadius: 4,
        elevation: 5
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 15,
        textAlign: 'center',
    },
    inputLabel: {
        fontSize: 14,
        color: '#546E7A',
        marginBottom: 5,
        marginTop: 10,
    },
    input: {
        borderWidth: 1,
        borderColor: '#CFD8DC',
        borderRadius: 8,
        padding: 10,
        fontSize: 16,
        color: '#263238',
        backgroundColor: '#F7F9F9',
    },
    modalButtons: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginTop: 25,
    },
    button: {
        flex: 1,
        borderRadius: 10,
        padding: 12,
        elevation: 2,
        marginHorizontal: 5,
        alignItems: 'center',
    },
    buttonClose: {
        backgroundColor: '#ECEFF1',
    },
    textStyle: {
        color: "black",
        fontWeight: "bold",
        textAlign: "center"
    },
    // Options Modal Styles
    optionButton: {
        width: '100%',
        padding: 15,
        borderRadius: 10,
        alignItems: 'center',
        marginBottom: 10,
        borderWidth: 1,
        borderColor: '#ECEFF1',
    },
    optionButtonEdit: {
        backgroundColor: '#F7F9F9',
    },
    optionButtonDelete: {
        backgroundColor: '#FFEBEE',
        borderColor: '#FFCDD2',
    },
    optionButtonText: {
        fontSize: 16,
        fontWeight: '600',
        color: '#455A64',
    },
    optionButtonCancel: {
        padding: 10,
        marginTop: 5,
    },
    optionButtonCancelText: {
        color: '#90A4AE',
        fontSize: 14,
    },
});
