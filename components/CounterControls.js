import React from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';

export default function CounterControls({ onCount, onReset }) {
    return (
        <View style={styles.container}>
            {/* Large Count Button */}
            <View style={styles.countButtonWrapper}>
                <TouchableOpacity
                    style={styles.countButton}
                    onPress={onCount}
                    activeOpacity={0.7}
                >
                    <View style={styles.countButtonInner} />
                </TouchableOpacity>
            </View>

            {/* Small Reset Button - Positioned absolutely relative to container, not "intertwined" */}
            <TouchableOpacity
                style={styles.resetButton}
                onPress={onReset}
                activeOpacity={0.7}
            >
                <View style={styles.resetButtonInner} />
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        width: '100%',
        height: 250, // Dedicated area for buttons
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 20,
        position: 'relative', // Context for absolute reset button
    },
    countButtonWrapper: {
        // Wrapper to center the big button
        alignItems: 'center',
        justifyContent: 'center',
    },
    countButton: {
        width: 160,
        height: 160,
        bottom: 55,
        borderRadius: 80,
        backgroundColor: '#1C262B',
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 10,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 8 },
        shadowOpacity: 0.4,
        shadowRadius: 10,
    },
    countButtonInner: {
        width: 140,
        height: 140,
        borderRadius: 70,
        backgroundColor: '#263238',
        borderWidth: 4,
        borderColor: '#37474F',
    },
    resetButton: {
        width: 50,
        height: 50,
        borderRadius: 25,
        backgroundColor: '#1C262B',
        // Position it to the right and slightly below/next to the big button
        position: 'absolute',
        bottom: 240,
        right: 50,
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
    },
    resetButtonInner: {
        width: 36,
        height: 36,
        borderRadius: 18,
        backgroundColor: '#263238',
        borderWidth: 2,
        borderColor: '#37474F',
    },
});
