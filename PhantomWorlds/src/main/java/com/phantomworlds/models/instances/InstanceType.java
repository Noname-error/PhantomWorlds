package com.phantomworlds.models.instances;

/**
 * Defines the rules and behavior for different types of instances
 */
public enum InstanceType {
    /**
     * Dungeon instance - single party, limited players, boss fights
     */
    DUNGEON("Dungeon", 1, 4, 3600000, true),
    
    /**
     * Minigame instance - multiple parties, competitive
     */
    MINIGAME("Minigame", 2, 16, 1800000, false),
    
    /**
     * Survival instance - open world, persistent
     */
    SURVIVAL("Survival", 1, 8, 7200000, false),
    
    /**
     * Creative instance - building mode, no restrictions
     */
    CREATIVE("Creative", 1, 12, -1, false),
    
    /**
     * PvP instance - battle arena, team based
     */
    PVP("PvP Arena", 2, 20, 900000, true);
    
    private final String displayName;
    private final int minParties;
    private final int maxPlayers;
    private final long durationMs;
    private final boolean requiresParty;
    
    InstanceType(String displayName, int minParties, int maxPlayers, long durationMs, boolean requiresParty) {
        this.displayName = displayName;
        this.minParties = minParties;
        this.maxPlayers = maxPlayers;
        this.durationMs = durationMs;
        this.requiresParty = requiresParty;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getMinParties() {
        return minParties;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public boolean requiresParty() {
        return requiresParty;
    }
    
    public boolean hasTimeLimit() {
        return durationMs > 0;
    }
    
    /**
     * Get remaining time in milliseconds
     * @param startTime the start time of the instance
     * @return remaining time in milliseconds, or -1 if no time limit
     */
    public long getRemainingTime(long startTime) {
        if (!hasTimeLimit()) {
            return -1;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, durationMs - elapsed);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
