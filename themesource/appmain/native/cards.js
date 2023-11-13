import { Appearance } from "react-native";
import { background, contrast, spacing, brand } from "../../../theme/native/custom-variables";

export const darkMode = Appearance.getColorScheme() === "dark";

export const billContainer = {
    container: {
        overflow: 'visible'
    },
    listItem: {
        borderLeftWidth: 5,
        borderLeftColor: darkMode ? contrast.higher : brand.primary,
        borderLeftStyle: 'solid',
        backgroundColor: background.primary,
        borderRightWidth: 0,
        padding: 0,
        marginBottom: spacing.smaller,
        width: '100%',
        elevation: 1.5,
        shadowColor: contrast.higher,
        shadowOpacity: 0.12,
        shadowRadius: 10,
        shadowOffset: {
            width: 0,
            height: 0,
        },

    }
};

export const billCard = {
    container:{
        padding:spacing.regular,
        height: 60,
    }
};

export const cardShadow = {
    container: {
        elevation: 1.5,
        shadowColor: contrast.higher,
        shadowOpacity: 0.12,
        shadowRadius: 10,
        shadowOffset: {
            width: 0,
            height: 0,
        },
    },
};
  
export const cardBtn = {
    container: {
      borderWidth: 0,
      borderLeftWidth: 5,
      borderLeftStyle: 'solid',
      borderBottomLeftRadius: 0,
      borderBottomRightRadius: 4,
      borderTopLeftRadius: 0,
      borderTopRightRadius: 4,
      color: darkMode ? '#fff' : '#000',
      paddingTop:20,
      paddingBottom:20,
      height:'auto',
      justifyContent: 'flex-start',
      flexDirection: 'row',
      marginBottom: spacing.smaller,
      ...cardShadow.container,
      backgroundColor: background.primary,
      borderColor: darkMode ? contrast.high : brand.primary,
    },
    icon: {
        color: darkMode ? '#fff' : '#000',
    },
    caption: {
        color: darkMode ? '#fff' : '#000',
        fontSize:16
    },
};

export const serviceCard = {
    container: {
      alignItems: 'center',
      backgroundColor: darkMode ? contrast.low : background.primary,
      borderWidth: 1,
      borderColor: darkMode ? contrast.high : contrast.low,
      borderRadius: 4,
      flexDirection: 'column',
      height: 100,
      justifyContent: 'center',
      paddingLeft: spacing.smallest,
      paddingRight: spacing.smallest,
      paddingTop: spacing.regular,
      paddingBottom: spacing.regular,
      position: 'relative',
      width: '100%',
      ...cardShadow.container,
    },
};
  
export const svgBtn = {
    container: {
        backgroundColor: "transparent",
        borderWidth: 0,
        borderRadius: 0,
        width: 50,
        paddingVertical: 0,
        paddingHorizontal: 0,
        marginBottom: spacing.small,
    },
    icon: {
        color: darkMode ? "white" : "black",
        size: 50,
    },
};
  
export const sensorcardShadow = {
    container: {
        elevation: 1.5,
        shadowColor: contrast.higher,
        shadowOpacity: 0.7,
        shadowRadius: 6,
        shadowOffset: {
            width: 0,
            height: 2,
        },
    },
};

export const sensorCard = {
    container: {
        alignItems: 'flex-start',
        backgroundColor: darkMode ? contrast.low : background.primary,
        borderRadius: 4,
        flexDirection: 'column',
        justifyContent: 'flex-end',
        height: 150,
        width: 150,
        opacity: .8,
        paddingLeft: spacing.small,
        paddingRight: spacing.smallest,
        paddingTop: spacing.small,
        paddingBottom: spacing.small,
        ...sensorcardShadow.container,
    }
};
  
export const sensorCardData = {
    text: {
        fontSize: 30,
        lineHeight: 32,
        fontWeight: 'bold',
        color: brand.success
    }
};
  
export const sensorCardText = {
    text: {
        fontSize: 16,
        lineHeight: 18,
    }
};
  
export const sensorCardBtn = {
    container: {
        position: 'absolute',
        top: 10,
        right: 10,
        backgroundColor:'transparent',
        borderWidth: 0,
        borderRadius: 0,
        width: 36,
        paddingVertical: 0,
        paddingHorizontal: 0,
    },
    icon: {
        color: darkMode ? "white" : "black",
        size: 36,
    }
};

export const roomcardShadow = {
    container: {
        elevation: 1.5,
        shadowColor: contrast.higher,
        shadowOpacity: 0.7,
        shadowRadius: 6,
        shadowOffset: {
            width: 0,
            height: 2,
        },
    },
};

export const roomCard = {
    container: {
        alignItems: 'flex-start',
        width: 150,
        borderRadius: 4,
        flexDirection: 'column',
        justifyContent: 'flex-end',
        height: 130,
        width: '100%',
        backgroundColor: 'rgba(0,0,0,0.9)',
        paddingLeft: spacing.small,
        paddingRight: spacing.smallest,
        paddingTop: spacing.small,
        paddingBottom: spacing.small,
        ...roomcardShadow.container,
    }
};
  
export const roomCardData = {
    text: {
      fontSize: 30,
      lineHeight: 32,
      fontWeight: 'bold',
      color: '#FFF'
    }
};
  
export const roomCardText = {
    text: {
      fontSize: 16,
      lineHeight: 18,
      color: '#fff'
    }
};
  
export const roomCardImage = {
    container: {
      position: 'absolute',
      top: 10,
      right: 10,
    },
    image: {
      height: 36,
      opacity: .2,
      width: 36,
    }
};

export const roomImg = {
    container: {
        position: 'absolute',
        top: 0,
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: -1,
        resizeMode: 'cover',
        opacity:0.5,
        borderRadius: 4,
        
    },
    
    image: {
        flex: 1,
        resizeMode: "cover",
        borderRadius: 4,
        width:150

    },
};

export const incidentContainer = {
    container: {
        overflow: 'visible'
    },
    listItem: {
        borderLeftWidth: 5,
        borderLeftColor: darkMode ? contrast.higher : brand.primary,
        borderLeftStyle: 'solid',
        backgroundColor: background.primary,
        borderRightWidth: 0,
        padding: 0,
        marginBottom: spacing.smaller,
        width: '100%',
        elevation: 1.5,
        shadowColor: contrast.higher,
        shadowOpacity: 0.12,
        shadowRadius: 10,
        shadowOffset: {
            width: 0,
            height: 0,
        },

    }
};

export const incidentCard = {
    container:{
        height: 60,
        width: '100%',
    }
};

export const swipeBtn = {
    container:{
        height:'100%',
        borderRadius:0,
        margin:0,
        width:'50%'
    }
}