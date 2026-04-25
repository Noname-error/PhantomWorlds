package com.phantomworlds.minigames;

/**
 * Base interface for all game types
 */
public interface GameType {
    
    /**
     * Get the unique identifier for this game type
     */
    String getId();
    
    /**
     * Get the display name for this game type
     */
    String getDisplayName();
    
    /**
     * Get the category this game type belongs to
     */
    MinigameCategory getCategory();
    
    /**
     * Get the minimum number of players required
     */
    int getMinPlayers();
    
    /**
     * Get the maximum number of players allowed
     */
    int getMaxPlayers();
    
    /**
     * Get the number of teams this game type supports (0 for no teams)
     */
    int getTeamCount();
    
    /**
     * Check if this game type requires specific regions
     */
    boolean requiresRegions();
    
    /**
     * Get the required region types for this game
     */
    String[] getRequiredRegionTypes();
    
    /**
     * Initialize the game with configuration
     */
    void initialize(MinigameConfig config);
    
    /**
     * Start the game
     */
    void startGame();
    
    /**
     * End the game
     */
    void endGame();
    
    /**
     * Handle player joining the game
     */
    void onPlayerJoin(String playerId);
    
    /**
     * Handle player leaving the game
     */
    void onPlayerLeave(String playerId);
    
    /**
     * Handle player death
     */
    void onPlayerDeath(String playerId);
    
    /**
     * Handle region events
     */
    void onRegionEnter(String playerId, String regionType);
    void onRegionLeave(String playerId, String regionType);
    
    /**
     * Handle timer tick (every second)
     */
    void onTimerTick(int timeRemaining);
    
    /**
     * Check if the game has ended
     */
    boolean hasEnded();
    
    /**
     * Get the winner(s) of the game
     */
    String[] getWinners();
    
    /**
     * Get current game state
     */
    MinigameState getState();
    
    /**
     * Get scoreboard content for this game
     */
    String[] getScoreboardContent();
}
