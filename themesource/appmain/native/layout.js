import { Appearance } from "react-native";
import { font, brand, spacing, input, contrast, navigation } from "../../../theme/native/custom-variables";

export const darkMode = Appearance.getColorScheme() === "dark";

export const bundle = {
    container: {
      borderBottomWidth: 1,
      borderBottomColor: brand.primary,
      borderBottomStyle: 'solid',
    },
};
  
export const remainingData = {
    container: {
      borderColor: brand.primary,
      borderStyle: 'solid',
      borderRadius: 4,
      borderWidth: 1,
      padding: 14,
    },
};
  
export const remainingDays = {
    container: {
      position: 'absolute',
      top: 0,
      left: 0,
      width: '25%',
    }
};
  
export const remainingDaysValue = {
    text: {
      color: '#969696',
      fontWeight: font.weightBold
    }
};
  
export const remainingDaysImage = {
    container: {
      alignItems: 'center',
    },
    image: {
      height: 30,
      marginTop: 4,
      marginBottom: 4,
      width: 30,
    }
};
  
export const tabContainer = {
    container: {
      marginLeft: -spacing.regular,
      marginRight: -spacing.regular,
    }
};
  
export const incidentText = {
    container: {
      flex: 1,
      alignContent: 'flex-start',
      color:'#000'
    }
};
  
export const incidentDevice = {
    text: {
      backgroundColor: '#ccc',
      borderRadius: 8,
      color: '#000',
      paddingLeft: 5,
      paddingBottom: 5,
      paddingRight: 5,
      paddingTop: 5,
      fontSize: font.sizeSmall,
      overflow: 'hidden'
    }
};

export const btnFullWidth = {
  container:{
      width:'100%'
  }
};

export const dropdownContainer ={
    container:{
        borderColor: input.borderColor,
        borderWidth: input.borderWidth,
        borderRadius: input.borderRadius,
        padding:1
    }
};

export const carousel ={
    container: {
        width: "100%",
        padding:10,
    },
    cardLayout: {
  
        slideItem: {
            width: 150,
            height: 150
        },
        inactiveSlideItem:{
            
        }
    },
    fullWidthLayout: {
        slideItem: {
            width: "100%",
            height: "100%"
        },
        inactiveSlideItem: {
            opacity: 1,
            scale: 1
        }
    }
};

/*-- bottom nav bar --*/
export const navigationStyle = {
    bottomBar: {
        container: {
            backgroundColor: darkMode ? contrast.lower : "white",
            borderTopWidth: darkMode ? 0: 1,
            ...Platform.select({
                ios: {
                },
                android: {

                },
            }),
        },
        label: {
            color: darkMode ? "rgba(255,255,255, 0.4)" : navigation.bottomBar.color,
        },
        selectedLabel: {
            color: darkMode ? "white" : navigation.bottomBar.selectedTextColor,
        },
        icon: {
            color: darkMode ? "rgba(255,255,255, 0.4)" : navigation.bottomBar.color,
        },
        selectedIcon: {
            color: darkMode ? "white" : navigation.bottomBar.selectedIconColor,
        },
    },
}