import React, { useState, useEffect, useRef } from 'react';
import { View, StyleSheet, TouchableOpacity, Text, Dimensions, PanResponder } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';

export default function SimpleColorPicker({ onSelectColor, initialColor }) {
    // Default to middle (Cyan-ish) if no valid initialColor logic 
    // This solves the user request to have the selector in the middle by default
    const sliderWidth = 300;
    const [hue, setHue] = useState(180);
    const [selectedColor, setSelectedColor] = useState(initialColor || '#2EADB3'); // Dimmer Cyan match for 180, 60, 45
    const [markerPosition, setMarkerPosition] = useState(sliderWidth / 2);

    useEffect(() => {
        // Optional: Try to reverse engineer hex to hue position if needed, 
        // but for simplicity we start at middle.
    }, []);

    // Simple HSL to Hex conversion
    const hslToHex = (h, s, l) => {
        l /= 100;
        const a = s * Math.min(l, 1 - l) / 100;
        const f = n => {
            const k = (n + h / 30) % 12;
            const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
            return Math.round(255 * color).toString(16).padStart(2, '0');
        };
        return `#${f(0)}${f(8)}${f(4)}`;
    };

    const updateColor = (x) => {
        if (x < 0) x = 0;
        if (x > sliderWidth) x = sliderWidth;

        setMarkerPosition(x); // Update marker visual position

        const newHue = Math.floor((x / sliderWidth) * 360);
        setHue(newHue);
        // User requested less saturated and more dim colors
        // Reduced Saturation from 100 to 60, Lightness from 50 to 45
        const color = hslToHex(newHue, 60, 45);
        setSelectedColor(color);
        onSelectColor(color);
    };

    const dragStartPos = useRef(0);

    const panResponder = useRef(
        PanResponder.create({
            onStartShouldSetPanResponder: () => true,
            onStartShouldSetPanResponderCapture: () => true,
            onMoveShouldSetPanResponder: () => true,
            onMoveShouldSetPanResponderCapture: () => true,
            onPanResponderGrant: (evt, gestureState) => {
                // Initial touch: jump to that position
                const locationX = evt.nativeEvent.locationX;
                updateColor(locationX);
                dragStartPos.current = locationX;
            },
            onPanResponderMove: (evt, gestureState) => {
                // Move relative to start position
                // Clamping is handled in updateColor
                updateColor(dragStartPos.current + gestureState.dx);
            },
            onPanResponderRelease: (evt, gestureState) => {
                // Final update if needed, normally Move covers it
            }
        })
    ).current;

    return (
        <View style={styles.container}>

            {/* Selected Preview */}
            <View style={[styles.preview, { backgroundColor: selectedColor }]}>
                <Text style={styles.hexText}>{selectedColor.toUpperCase()}</Text>
            </View>

            {/* Hue Slider */}
            <View style={styles.sliderContainer}>
                <LinearGradient
                    colors={['red', 'yellow', 'green', 'cyan', 'blue', 'magenta', 'red']}
                    start={{ x: 0, y: 0.5 }}
                    end={{ x: 1, y: 0.5 }}
                    style={styles.gradient}
                >
                    {/* Touch Area */}
                    <View
                        style={styles.touchArea}
                        {...panResponder.panHandlers}
                    >
                        {/* Marker Line */}
                        <View style={[styles.marker, { left: markerPosition - 10 }]} pointerEvents="none">
                            <View style={styles.markerKnob} />
                        </View>
                    </View>
                </LinearGradient>
            </View>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        padding: 10,
        alignItems: 'center',
        width: '100%',
    },
    label: {
        fontSize: 16,
        fontWeight: '600',
        color: '#546E7A',
        marginBottom: 15,
    },
    sliderContainer: {
        width: 300,
        height: 50,
        borderRadius: 25,
        overflow: 'hidden',
        marginBottom: 15,
        borderWidth: 2,
        borderColor: '#ECEFF1',
        position: 'relative',
    },
    gradient: {
        flex: 1,
        justifyContent: 'center',
    },
    touchArea: {
        width: '100%',
        height: '100%',
    },
    marker: {
        position: 'absolute',
        top: 0,
        bottom: 0,
        width: 20,
        alignItems: 'center',
        justifyContent: 'center',
    },
    markerKnob: {
        width: 6,
        height: '100%',
        backgroundColor: '#fff',
        borderWidth: 1,
        borderColor: 'rgba(0,0,0,0.2)',
        borderRadius: 3,
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.3,
        elevation: 2,
    },
    preview: {
        width: 80,
        height: 80,
        borderRadius: 40,
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 20,
        borderWidth: 4,
        borderColor: '#fff',
        shadowColor: "#000",
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.2,
        elevation: 5,
    },
    hexText: {
        color: '#fff',
        fontWeight: 'bold',
        textShadowColor: 'rgba(0,0,0,0.5)',
        textShadowOffset: { width: 0, height: 1 },
        textShadowRadius: 2,
        fontSize: 12,
    }
});
