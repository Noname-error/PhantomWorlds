package com.phantomworlds.models.instances;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player within an instance
 */
public class InstancePlayer {
    
    private final UUID playerId;
    private final String playerName;
    private final long joinTime;
    private long lastActivityTime;
    private int deaths;
    private int kills;
    private double damageDealt;
    private double damageTaken;
    private boolean isReady;
    private boolean isOnline;
    
    public InstancePlayer(UUID playerId, String playerName, long joinTime) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.joinTime = joinTime;
        this.lastActivityTime = joinTime;
        this.deaths = 0;
        this.kills = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
        this.isReady = false;
        this.isOnline = true;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public long getJoinTime() {
        return joinTime;
    }
    
    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void updateActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void incrementDeaths() {
        this.deaths++;
        updateActivity();
    }
    
    public int getKills() {
        return kills;
    }
    
    public void incrementKills() {
        this.kills++;
        updateActivity();
    }
    
    public double getDamageDealt() {
        return damageDealt;
    }
    
    public void addDamageDealt(double damage) {
        this.damageDealt += damage;
        updateActivity();
    }
    
    public double getDamageTaken() {
        return damageTaken;
    }
    
    public void addDamageTaken(double damage) {
        this.damageTaken += damage;
        updateActivity();
    }
    
    public boolean isReady() {
        return isReady;
    }
    
    public void setReady(boolean ready) {
        this.isReady = ready;
        updateActivity();
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        this.isOnline = online;
        updateActivity();
    }
    
    public long getPlayTime() {
        return System.currentTimeMillis() - joinTime;
    }
    
    public double getKillDeathRatio() {
        if (deaths == 0) {
            return kills > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }
        return (double) kills / deaths;
    }
    
    @Override
    public String toString() {
        return "InstancePlayer{" +
                "playerName='" + playerName + '\'' +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", ready=" + isReady +
                ", online=" + isOnline +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstancePlayer that = (InstancePlayer) o;
        return Objects.equals(playerId, that.playerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
}
