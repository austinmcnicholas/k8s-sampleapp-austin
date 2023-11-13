import { Appearance } from "react-native";

export const darkMode = Appearance.getColorScheme() === "dark";

export const pageRed = {
    header: {
        container: {
            // All ViewStyle properties are allowed
            backgroundColor: darkMode ? '#9a0000' : 'red',
        },

    },
    statusBar: {
        // Android only
            backgroundColor: darkMode ? '#9a0000' : 'red',
    },
}

export const pageGreen = {
    header: {
        container: {
            // All ViewStyle properties are allowed
            backgroundColor: darkMode ? '#006700' : 'green',
        },

    },
    statusBar: {
        // Android only
        backgroundColor: darkMode ? '#006700' : 'green',
    },
}

export const pageGrey = {
    header: {
        container: {
            // All ViewStyle properties are allowed
            backgroundColor: 'gray',
        },

    },
    statusBar: {
        // Android only
        backgroundColor: 'gray',
    },
}

export const pageBlue = {
    header: {
        container: {
            // All ViewStyle properties are allowed
            backgroundColor: darkMode ? '#00009a' : 'blue',
        },

    },
    statusBar: {
        // Android only
            backgroundColor: darkMode ? '#00009a' : 'blue',
    },
}