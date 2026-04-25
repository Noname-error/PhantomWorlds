package com.phantomworlds.minigames;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an active minigame instance
 */
public class MinigameInstance {
    
    private final String instanceId;
    private final String gameType;
    private final GameType game;
    private MinigameState state;
    private final Set<UUID> players;
    private final long startTime;
    private long endTime;
    
    public MinigameInstance(String instanceId, String gameType, GameType game) {
        this.instanceId = instanceId;
        this.gameType = gameType;
        this.game = game;
        this.state = MinigameState.WAITING;
        this.players = new HashSet<>();
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
    }
    
    // Getters and setters
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public GameType getGame() {
        return game;
    }
    
    public MinigameState getState() {
        return state;
    }
    
    public void setState(MinigameState state) {
        this.state = state;
    }
    
    public Set<UUID> getPlayers() {
        return new HashSet<>(players);
    }
    
    public boolean addPlayer(UUID playerId) {
        return players.add(playerId);
    }
    
    public boolean removePlayer(UUID playerId) {
        return players.remove(playerId);
    }
    
    public boolean hasPlayer(UUID playerId) {
        return players.contains(playerId);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public long getDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Check if the game is active (not ended)
     */
    public boolean isActive() {
        return state != MinigameState.ENDED && state != MinigameState.ENDING;
    }
    
    /**
     * Check if players can join
     */
    public boolean canJoin() {
        return state == MinigameState.WAITING || state == MinigameState.LOBBY;
    }
    
    @Override
    public String toString() {
        return "MinigameInstance{" +
                "instanceId='" + instanceId + '\'' +
                ", gameType='" + gameType + '\'' +
                ", state=" + state +
                ", playerCount=" + players.size() +
                ", duration=" + getDuration() + "ms" +
                '}';
    }
}
