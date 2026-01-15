import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function CounterDisplay({ count }) {
    // Pad count with zeros to look like 000
    const formattedCount = String(count).padStart(3, '0');

    return (
        <View style={styles.container}>
            <View style={styles.screenFrame}>
                <View style={styles.glassEffect}>
                    <Text style={styles.label}>Total Count</Text>
                    <Text style={styles.countText}>{formattedCount}</Text>
                    <View style={styles.labelsRow}>
                        <Text style={styles.subLabel}>COUNT</Text>
                        <Text style={styles.subLabel}>RESET</Text>
                    </View>
                </View>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: 'center',
        marginBottom: 40,
    },
    screenFrame: {
        backgroundColor: '#263238', // Dark body of the screen part
        borderRadius: 20,
        width: 240,
        height: 180,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 5,
    },
    glassEffect: {
        // Simulate the LCD glass/background
        width: '90%',
        height: '80%',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingVertical: 15,
    },
    label: {
        color: '#B0BEC5',
        fontSize: 12,
        letterSpacing: 1,
        fontFamily: 'monospace', // Fallback until we load custom font
    },
    countText: {
        color: '#dae3e5', // LCD text color often greyish or black on grey, but ref is dark bg with light text? 
        // Wait, ref image 2 shows: Dark Blue/Black housing. Inner screen is Greyish/Greenish LCD color with Black text? 
        // Actually looking closely at ref image 2:
        // It's a dark panel. Inside is a LIGHTER screen (LCD color).
        // The numbers are OUTLINED or dark. 
        // Let's match Ref Image 2 exactly.
        // Ref 2: Dark Panel (#2C3E50 approx). Screen: #95A5A6 (Greyish). Text: #2C3E50 (Dark).
        fontSize: 56,
        fontWeight: 'bold',
        fontFamily: 'monospace',
        letterSpacing: 4,
    },
    labelsRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        position: 'absolute',
        bottom: 20,
        width: '80%',
    },
    subLabel: {
        color: '#80CBC4',
        fontSize: 10,
    }
});
