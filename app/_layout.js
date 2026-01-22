import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { Drawer } from 'expo-router/drawer';
import CustomDrawerContent from '../components/CustomDrawerContent';
import { DhikrProvider } from '../context/DhikrContext';
import { I18nManager } from 'react-native';

// Force LTR layout regardless of device language
I18nManager.allowRTL(false);
I18nManager.forceRTL(false);

export default function Layout() {
    return (
        <DhikrProvider>
            <GestureHandlerRootView style={{ flex: 1 }}>
                <Drawer
                    drawerContent={(props) => <CustomDrawerContent {...props} />}
                    screenOptions={{
                        headerStyle: { backgroundColor: '#00897B' },
                        headerTintColor: '#fff',
                        headerTitleStyle: { fontWeight: 'bold' },
                        drawerStyle: { width: '85%' }, // Wider drawer like the ref
                        drawerActiveTintColor: '#00897B',
                    }}
                >
                    <Drawer.Screen
                        name="index"
                        options={{
                            drawerLabel: 'Tasbih',
                            title: 'Sabbeh',
                            headerShown: false,
                        }}
                    />
                    {/* Sessions screen logic is now handled inside the custom drawer itself changing the context */}
                    <Drawer.Screen
                        name="sessions"
                        options={{
                            drawerItemStyle: { display: 'none' }, // Hide from default list if any fallback happens
                            headerShown: false,
                        }}
                    />
                </Drawer>
            </GestureHandlerRootView>
        </DhikrProvider>
    );
}
