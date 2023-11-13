import { brand, contrast, font, input } from "../../../theme/native/custom-variables";
import { Appearance } from "react-native";

export const darkMode = Appearance.getColorScheme() === "dark";

export const com_mendix_widget_native_progresscircle_ProgressCircle ={
    container: {
        // All ViewStyle properties are allowed
    },
    circle: {
        // Only the size & borderWidth & borderColor properties are allowed
        size: 80,
        borderWidth: 1,
        borderColor: darkMode ? contrast.low : '#ccc',
    },
    fill: {
        // Only the width & backgroundColor & lineCapRounded properties are allowed
        backgroundColor: brand.primary,
        width: 8, // Thickness,
        lineCapRounded: true,
    },
    text: {
        // All TextStyle properties are allowed
        color: darkMode ? '#fff': contrast.regular,
        fontSize: font.size,
        fontWeight: font.weightSemiBold,
        fontFamily: font.family,
    },
    validationMessage: {
        // All TextStyle properties are allowed
        color: input.errorColor,
        fontSize: font.size,
        fontFamily: font.family,
    },
}

export const profileBtn = {
    container: {
        backgroundColor: "transparent",
        borderWidth: 0,
        borderRadius: 0,
        width: 20,
        paddingVertical: 0,
        paddingHorizontal: 0,
        margin: 0,
    },
    icon: {
        color:'#fff',
        size:20
    }
}