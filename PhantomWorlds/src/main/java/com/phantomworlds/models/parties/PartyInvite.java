package com.phantomworlds.models.parties;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an invitation to join a party
 */
public class PartyInvite {
    
    private final String partyId;
    private final UUID playerId;
    private final UUID inviterId;
    private final long creationTime;
    private String message;
    private boolean accepted;
    private boolean declined;
    
    public PartyInvite(String partyId, UUID playerId, UUID inviterId) {
        this.partyId = partyId;
        this.playerId = playerId;
        this.inviterId = inviterId;
        this.creationTime = System.currentTimeMillis();
        this.message = "";
        this.accepted = false;
        this.declined = false;
    }
    
    public String getPartyId() {
        return partyId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public UUID getInviterId() {
        return inviterId;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isAccepted() {
        return accepted;
    }
    
    public boolean isDeclined() {
        return declined;
    }
    
    public boolean isPending() {
        return !accepted && !declined;
    }
    
    public void accept() {
        this.accepted = true;
        this.declined = false;
    }
    
    public void decline() {
        this.declined = true;
        this.accepted = false;
    }
    
    /**
     * Check if invite has expired (older than specified time)
     */
    public boolean isExpired(long expireThresholdMs) {
        return (System.currentTimeMillis() - creationTime) > expireThresholdMs;
    }
    
    /**
     * Get invite age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - creationTime;
    }
    
    @Override
    public String toString() {
        return "PartyInvite{" +
                "partyId='" + partyId + '\'' +
                ", playerId=" + playerId +
                ", inviterId=" + inviterId +
                ", status=" + (accepted ? "accepted" : declined ? "declined" : "pending") +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyInvite that = (PartyInvite) o;
        return Objects.equals(partyId, that.partyId) && 
               Objects.equals(playerId, that.playerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partyId, playerId);
    }
}
