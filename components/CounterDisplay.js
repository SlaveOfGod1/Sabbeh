import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';

export default function CounterDisplay({ count }) {
    // Pad count with zeros to look like 000
    const formattedCount = String(count).padStart(3, '0');

    return (
        <View style={styles.container}>
            <LinearGradient
                colors={['#37474F', '#102027']}
                start={{ x: 0, y: 0 }}
                end={{ x: 0.5, y: 0.5 }} // Dark Blue Grey to almost Black gradient
                style={styles.housingGradient}
            >
                <Text style={styles.topLabel}>Total Count</Text>

                <View style={styles.lcdScreen}>
                    <Text style={styles.countText}>{formattedCount}</Text>
                </View>

                <View style={styles.labelsRow}>
                    <Text style={styles.subLabel}>COUNT</Text>
                    <Text style={styles.subLabel}>RESET</Text>
                </View>
            </LinearGradient>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        marginBottom: 30,
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.4,
        shadowRadius: 8,
    },
    housingGradient: {
        width: 260,
        height: 220,
        borderRadius: 30,
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingVertical: 25,
    },
    topLabel: {
        color: '#B0BEC5',
        fontSize: 10,
        letterSpacing: 1.5,
        fontWeight: '600',
        textTransform: 'uppercase',
    },
    lcdScreen: {
        width: '85%',
        height: '55%',
        backgroundColor: '#CFD8DC', // Lighter Grey/Greenish for LCD
        borderRadius: 15,
        alignItems: 'center',
        justifyContent: 'center',
        borderWidth: 4,
        borderColor: '#263238', // Dark border around screen
    },
    countText: {
        color: '#263238', // Dark text on light screen
        fontSize: 64,
        fontWeight: 'bold',
        fontFamily: 'monospace',
        letterSpacing: 6,
        // Provide a text shadow to simulate LCD segment separation if possible, 
        // but standard font is OK for now.
    },
    labelsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        width: '80%',
    },
    subLabel: {
        color: '#90A4AE',
        fontSize: 10,
        fontWeight: '600',
        letterSpacing: 1,
    }
});
