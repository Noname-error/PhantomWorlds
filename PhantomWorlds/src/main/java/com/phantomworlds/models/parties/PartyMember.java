package com.phantomworlds.models.parties;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a member of a party
 */
public class PartyMember {
    
    private final UUID playerId;
    private String role;
    private final long joinTime;
    private long lastActivityTime;
    private boolean ready;
    private boolean online;
    private int deaths;
    private int kills;
    private double damageDealt;
    private double damageTaken;
    
    public PartyMember(UUID playerId, String role, long joinTime) {
        this.playerId = playerId;
        this.role = role;
        this.joinTime = joinTime;
        this.lastActivityTime = joinTime;
        this.ready = false;
        this.online = true;
        this.deaths = 0;
        this.kills = 0;
        this.damageDealt = 0;
        this.damageTaken = 0;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
        updateActivity();
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
    
    public boolean isReady() {
        return ready;
    }
    
    public void setReady(boolean ready) {
        this.ready = ready;
        updateActivity();
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
        updateActivity();
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
    
    public long getPlayTime() {
        return System.currentTimeMillis() - joinTime;
    }
    
    public double getKillDeathRatio() {
        if (deaths == 0) {
            return kills > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }
        return (double) kills / deaths;
    }
    
    public boolean isLeader() {
        return "Leader".equalsIgnoreCase(role);
    }
    
    public void promoteToLeader() {
        this.role = "Leader";
        updateActivity();
    }
    
    /**
     * Check if member has been inactive for specified time
     */
    public boolean isInactive(long thresholdMs) {
        if (online) {
            return false;
        }
        return (System.currentTimeMillis() - lastActivityTime) > thresholdMs;
    }
    
    @Override
    public String toString() {
        return "PartyMember{" +
                "playerId=" + playerId +
                ", role='" + role + '\'' +
                ", ready=" + ready +
                ", online=" + online +
                ", kills=" + kills +
                ", deaths=" + deaths +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyMember that = (PartyMember) o;
        return Objects.equals(playerId, that.playerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }
}
