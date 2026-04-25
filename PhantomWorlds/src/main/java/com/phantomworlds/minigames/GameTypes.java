package com.phantomworlds.minigames;

/**
 * Registry of all available game types
 */
public class GameTypes {
    
    // PvP Games
    public static final String CAPTURE_THE_FLAG = "CAPTURE_THE_FLAG";
    public static final String LAST_MAN_STANDING = "LAST_MAN_STANDING";
    public static final String KING_OF_THE_HILL = "KING_OF_THE_HILL";
    public static final String TEST_SURVIVAL = "TEST_SURVIVAL";
    
    // Skill Games
    public static final String SPLEEF = "SPLEEF";
    public static final String TNT_RUN = "TNT_RUN";
    
    // Fun Games
    public static final String HOT_POTATO = "HOT_POTATO";
    public static final String LUCKY_BLOCK = "LUCKY_BLOCK";
    
    // Strategy Games
    public static final String MINI_BEDWARS = "MINI_BEDWARS";
    
    /**
     * Get the category for a game type
     */
    public static MinigameCategory getCategory(String gameType) {
        switch (gameType) {
            case CAPTURE_THE_FLAG:
            case LAST_MAN_STANDING:
            case KING_OF_THE_HILL:
            case TEST_SURVIVAL:
                return MinigameCategory.PVP;
                
            case SPLEEF:
            case TNT_RUN:
                return MinigameCategory.SKILL;
                
            case HOT_POTATO:
            case LUCKY_BLOCK:
                return MinigameCategory.FUN;
                
            case MINI_BEDWARS:
                return MinigameCategory.STRATEGY;
                
            default:
                return MinigameCategory.FUN;
        }
    }
    
    /**
     * Get all game types for a category
     */
    public static String[] getGameTypesForCategory(MinigameCategory category) {
        switch (category) {
            case PVP:
                return new String[]{CAPTURE_THE_FLAG, LAST_MAN_STANDING, KING_OF_THE_HILL, TEST_SURVIVAL};
            case SKILL:
                return new String[]{SPLEEF, TNT_RUN};
            case FUN:
                return new String[]{HOT_POTATO, LUCKY_BLOCK};
            case STRATEGY:
                return new String[]{MINI_BEDWARS};
            default:
                return new String[]{};
        }
    }
    
    /**
     * Get display name for a game type
     */
    public static String getDisplayName(String gameType) {
        switch (gameType) {
            case CAPTURE_THE_FLAG:
                return "Capture the Flag";
            case LAST_MAN_STANDING:
                return "Last Man Standing";
            case KING_OF_THE_HILL:
                return "King of the Hill";
            case TEST_SURVIVAL:
                return "Test Survival";
            case SPLEEF:
                return "Spleef";
            case TNT_RUN:
                return "TNT Run";
            case HOT_POTATO:
                return "Hot Potato";
            case LUCKY_BLOCK:
                return "Lucky Blocks";
            case MINI_BEDWARS:
                return "Mini BedWars";
            default:
                return gameType;
        }
    }
}
