import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, Modal, TextInput, Alert } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useDhikr } from '../context/DhikrContext';

export default function CustomDrawerContent(props) {
    const { top, bottom } = useSafeAreaInsets();
    const { dhikrs, currentDhikr, selectDhikr, addDhikr, deleteDhikr, updateDhikr, theme } = useDhikr();

    const [modalVisible, setModalVisible] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [title, setTitle] = useState('');
    const [subtitle, setSubtitle] = useState('');
    const [target, setTarget] = useState('33');

    const [optionsModalVisible, setOptionsModalVisible] = useState(false);
    const [selectedDhikrForOptions, setSelectedDhikrForOptions] = useState(null);

    // Use theme color (darker shade usually at index 1 for headers in our logic, or 0? 
    // In index.js: colors={['#4DB6AC', '#00695C']} -> Dark Green is second.
    // Actually wait, usually gradient is Left -> Right.
    // Let's use theme.colors[1] as the "Primary Strong" color.
    const primaryColor = theme.colors[1] || '#00897B';
    const secondaryColor = theme.colors[0] || '#4DB6AC';

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

    const handleOptionDelete = () => {
        setOptionsModalVisible(false);
        if (selectedDhikrForOptions) {
            Alert.alert(
                "Delete Dhikr",
                `Are you sure you want to delete "${selectedDhikrForOptions.title}"?`,
                [
                    { text: "Cancel", style: "cancel" },
                    {
                        text: "Delete",
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
            Alert.alert("Error", "Please enter a name for the Dhikr");
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
                style={[styles.itemCard, isActive && { backgroundColor: primaryColor, borderColor: primaryColor }]}
                onPress={() => handleSelect(item)}
                onLongPress={() => handleLongPress(item)}
                delayLongPress={500}
                activeOpacity={0.8}
            >
                <View>
                    <View style={styles.itemHeaderRow}>
                        <Text style={[styles.itemTitle, isActive && styles.activeItemText]}>{item.title}</Text>
                        <View style={[styles.targetBadge, isActive && styles.activeTargetBadge]}>
                            <Text style={[styles.targetText, { color: isActive ? '#fff' : primaryColor }]}>{item.target}x</Text>
                        </View>
                    </View>
                    {item.subtitle ? (
                        <Text style={[styles.itemSubtitle, isActive && styles.activeItemSubtitle]}>
                            {item.subtitle}
                        </Text>
                    ) : null}
                </View>
            </TouchableOpacity>
        );
    };

    return (
        <View style={{ flex: 1, backgroundColor: '#fff' }}>
            {/* Header */}
            <View style={[styles.header, { paddingTop: top + 20, backgroundColor: primaryColor }]}>
                <View style={styles.headerTopRow}>
                    <Text style={styles.headerTitle}>Prayer Sessions</Text>
                </View>
                <Text style={styles.headerSubtitle}>Select your dhikr practice</Text>
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
            <View style={[styles.footer, { paddingBottom: bottom + 20 }]}>
                <TouchableOpacity style={styles.addButton} onPress={openAddModal}>
                    <Text style={styles.addButtonText}>+ Add Custom Dhikr</Text>
                </TouchableOpacity>
                <Text style={styles.footerNote}>May Allah accept your dhikr</Text>
                <Text style={styles.footerSubNote}>Long press custom dhikr to edit/delete</Text>
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
                    <View style={styles.modalView}>
                        <Text style={[styles.modalTitle, { color: primaryColor }]}>
                            Manage "{selectedDhikrForOptions?.title}"
                        </Text>

                        <TouchableOpacity style={[styles.optionButton, styles.optionButtonEdit]} onPress={handleOptionEdit}>
                            <Text style={styles.optionButtonText}>Edit Dhikr</Text>
                        </TouchableOpacity>

                        <TouchableOpacity style={[styles.optionButton, styles.optionButtonDelete]} onPress={handleOptionDelete}>
                            <Text style={[styles.optionButtonText, { color: '#D32F2F' }]}>Delete Dhikr</Text>
                        </TouchableOpacity>

                        <TouchableOpacity style={styles.optionButtonCancel} onPress={() => setOptionsModalVisible(false)}>
                            <Text style={styles.optionButtonCancelText}>Cancel</Text>
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
                    <View style={styles.modalView}>
                        <Text style={[styles.modalTitle, { color: primaryColor }]}>{editingId ? "Edit Dhikr" : "Add Custom Dhikr"}</Text>

                        <Text style={styles.inputLabel}>Title</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="e.g. Salawat"
                            value={title}
                            onChangeText={setTitle}
                        />

                        <Text style={styles.inputLabel}>Subtitle (Optional)</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="e.g. Allahumma salli ala..."
                            value={subtitle}
                            onChangeText={setSubtitle}
                        />

                        <Text style={styles.inputLabel}>Count per Round</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="33"
                            value={target}
                            onChangeText={setTarget}
                            keyboardType="numeric"
                        />

                        <View style={styles.modalButtons}>
                            <TouchableOpacity
                                style={[styles.button, styles.buttonClose]}
                                onPress={() => setModalVisible(false)}
                            >
                                <Text style={styles.textStyle}>Cancel</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.button, { backgroundColor: primaryColor }]}
                                onPress={saveDhikr}
                            >
                                <Text style={[styles.textStyle, { color: '#fff' }]}>Save</Text>
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
        padding: 20,
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
        marginBottom: 20,
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
