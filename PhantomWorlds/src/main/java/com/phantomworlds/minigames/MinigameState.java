package com.phantomworlds.minigames;

/**
 * States of a minigame
 */
public enum MinigameState {
    WAITING("Waiting for players"),
    LOBBY("In lobby"),
    COUNTDOWN("Game starting"),
    PLAYING("Game in progress"),
    ENDING("Game ending"),
    ENDED("Game ended");
    
    private final String displayName;
    
    MinigameState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
