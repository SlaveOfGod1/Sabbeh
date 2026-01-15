import React, { useEffect } from 'react';
import { View, StyleSheet, Text, Platform, StatusBar } from 'react-native';
import CounterDisplay from '../components/CounterDisplay';
import CounterControls from '../components/CounterControls';
import { DrawerToggleButton } from '@react-navigation/drawer';
import { useDhikr } from '../context/DhikrContext';

export default function MainScreen() {
    // Use Context for state
    const { currentDhikr, count, rounds, updateProgress, resetCount } = useDhikr();

    return (
        <View style={styles.mainContainer}>
            {/* Custom Header Area */}
            <View style={styles.header}>
                <DrawerToggleButton tintColor="#fff" />
                <Text style={styles.headerTitle}></Text>
                <View style={{ width: 40 }} />
            </View>

            <View style={styles.centerContent}>
                <Text style={styles.title}>{currentDhikr.title}</Text>

                <View style={styles.subtitleRow}>
                    <Text style={styles.subtitle}>{currentDhikr.subtitle}</Text>
                    {/* Display Rounds next to subtitle */}
                    <View style={styles.roundsBadge}>
                        <Text style={styles.roundsText}>Round: {rounds}</Text>
                    </View>
                </View>

                <View style={styles.deviceBody}>
                    <CounterDisplay count={count} />
                    <CounterControls onCount={() => updateProgress(1)} onReset={resetCount} />
                </View>

            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    mainContainer: {
        flex: 1,
        backgroundColor: '#00897B', // Teal Green
        paddingTop: Platform.OS === 'android' ? StatusBar.currentHeight : 40,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingHorizontal: 20,
        alignItems: 'center',
        height: 60,
    },
    headerTitle: {
        color: '#fff',
        fontSize: 18,
        fontWeight: '600',
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
        color: '#B2DFDB',
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
        borderRadius: 60,
        alignItems: 'center',
        justifyContent: 'flex-start',
        paddingTop: 50,
        elevation: 20,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 15 },
        shadowOpacity: 0.3,
        shadowRadius: 15,
    },
});
