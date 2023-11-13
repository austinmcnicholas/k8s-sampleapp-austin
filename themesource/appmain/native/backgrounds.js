import { Appearance } from "react-native";
import { contrast } from "../../../theme/native/custom-variables";

export const darkMode = Appearance.getColorScheme() === "dark";

let bg = {
  grey: '#D7D7D7',
  lightgrey: '#EBEBEB',
  lightergrey: '#F4F4F4',
}

export const backgroundGrey = {
  container: {
    backgroundColor: darkMode ? contrast.high : bg.grey
  },
}

export const backgroundLightGrey = {
  container: {
    backgroundColor: darkMode ? contrast.lowest : bg.lightgrey
  },
}

export const backgroundLighterGrey = {
  container: {
    backgroundColor: darkMode ? contrast.low : bg.lightergrey
  },
}