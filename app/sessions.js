import React from 'react';
import { View, Text, StyleSheet, FlatList } from 'react-native';

const MOCK_SESSIONS = [
    { id: '1', date: '2023-10-27', count: 33, label: 'After Maghrib' },
    { id: '2', date: '2023-10-27', count: 100, label: 'Morning Adhkar' },
    { id: '3', date: '2023-10-26', count: 99, label: 'Evening Adhkar' },
    { id: '4', date: '2023-10-25', count: 33, label: 'After Isha' },
];

export default function SessionsScreen() {
    return (
        <View style={styles.container}>
            <Text style={styles.title}>Praying Sessions</Text>
            <FlatList
                data={MOCK_SESSIONS}
                keyExtractor={(item) => item.id}
                contentContainerStyle={styles.listContent}
                renderItem={({ item }) => (
                    <View style={styles.card}>
                        <View style={styles.cardHeader}>
                            <Text style={styles.cardTitle}>{item.label}</Text>
                            <Text style={styles.cardDate}>{item.date}</Text>
                        </View>
                        <Text style={styles.count}>{item.count} Tasbihs</Text>
                    </View>
                )}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#00897B',
        padding: 20,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#fff',
        marginBottom: 20,
        marginTop: 10,
    },
    listContent: {
        paddingBottom: 20,
    },
    card: {
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderRadius: 12,
        padding: 15,
        marginBottom: 10,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 5,
    },
    cardTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#263238',
    },
    cardDate: {
        fontSize: 12,
        color: '#78909C',
    },
    count: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#00897B',
    },
});
