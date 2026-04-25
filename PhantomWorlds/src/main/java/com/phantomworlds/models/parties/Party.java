package com.phantomworlds.models.parties;

import com.phantomworlds.models.instances.InstanceType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a party of players for instance gameplay
 */
public class Party {
    
    private final String partyId;
    private final UUID leaderId;
    private final long creationTime;
    private final Map<UUID, PartyMember> members;
    private final List<PartyInvite> invites;
    private String currentInstanceId;
    private PartyState state;
    private int maxMembers;
    private boolean publicParty;
    private String description;
    
    public Party(String partyId, UUID leaderId, int maxMembers) {
        this.partyId = partyId;
        this.leaderId = leaderId;
        this.maxMembers = maxMembers;
        this.creationTime = System.currentTimeMillis();
        this.members = new ConcurrentHashMap<>();
        this.invites = new ArrayList<>();
        this.state = PartyState.FORMING;
        this.publicParty = false;
        this.description = "";
        
        // Add leader as first member
        addMember(leaderId, "Leader");
    }
    
    public String getPartyId() {
        return partyId;
    }
    
    public UUID getLeaderId() {
        return leaderId;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public PartyState getState() {
        return state;
    }
    
    public InstanceType getType() {
        // Default to DUNGEON type for now
        return InstanceType.DUNGEON;
    }
    
    public void setState(PartyState state) {
        this.state = state;
    }
    
    public int getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(int maxMembers) {
        this.maxMembers = Math.max(2, Math.min(20, maxMembers));
    }
    
    public boolean isPublicParty() {
        return publicParty;
    }
    
    public void setPublicParty(boolean publicParty) {
        this.publicParty = publicParty;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCurrentInstanceId() {
        return currentInstanceId;
    }
    
    public void setCurrentInstanceId(String currentInstanceId) {
        this.currentInstanceId = currentInstanceId;
    }
    
    public boolean isInInstance() {
        return currentInstanceId != null && !currentInstanceId.isEmpty();
    }
    
    public Map<UUID, PartyMember> getMembers() {
        return Collections.unmodifiableMap(members);
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    public boolean isFull() {
        return members.size() >= maxMembers;
    }
    
    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }
    
    public PartyMember getMember(UUID playerId) {
        return members.get(playerId);
    }
    
    public boolean isLeader(UUID playerId) {
        return leaderId.equals(playerId);
    }
    
    public boolean addMember(UUID playerId, String role) {
        if (isFull() || isMember(playerId)) {
            return false;
        }
        
        PartyMember member = new PartyMember(playerId, role, System.currentTimeMillis());
        members.put(playerId, member);
        return true;
    }
    
    public boolean removeMember(UUID playerId) {
        PartyMember removed = members.remove(playerId);
        
        // If leader leaves, promote next member
        if (removed != null && leaderId.equals(playerId) && !members.isEmpty()) {
            UUID newLeader = members.keySet().iterator().next();
            PartyMember newLeaderMember = members.get(newLeader);
            newLeaderMember.setRole("Leader");
            // Note: This would require updating the leaderId field
            // For now, this is a placeholder for future implementation
        }
        
        return removed != null;
    }
    
    public List<PartyInvite> getInvites() {
        return Collections.unmodifiableList(invites);
    }
    
    public PartyInvite getInvite(UUID playerId) {
        return invites.stream()
                .filter(invite -> invite.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }
    
    public boolean hasInvite(UUID playerId) {
        return getInvite(playerId) != null;
    }
    
    public void addInvite(PartyInvite invite) {
        // Remove existing invite for this player
        invites.removeIf(i -> i.getPlayerId().equals(invite.getPlayerId()));
        invites.add(invite);
    }
    
    public boolean removeInvite(UUID playerId) {
        return invites.removeIf(invite -> invite.getPlayerId().equals(playerId));
    }
    
    public void clearInvites() {
        invites.clear();
    }
    
    /**
     * Check if all members are ready
     */
    public boolean areAllMembersReady() {
        return members.values().stream().allMatch(PartyMember::isReady);
    }
    
    /**
     * Get ready member count
     */
    public int getReadyMemberCount() {
        return (int) members.values().stream().filter(PartyMember::isReady).count();
    }
    
    /**
     * Set all members ready status
     */
    public void setAllMembersReady(boolean ready) {
        members.values().forEach(member -> member.setReady(ready));
    }
    
    /**
     * Get online member count
     */
    public int getOnlineMemberCount() {
        return (int) members.values().stream().filter(PartyMember::isOnline).count();
    }
    
    /**
     * Check if party can start an instance
     */
    public boolean canStartInstance() {
        return state == PartyState.FORMING &&
               getOnlineMemberCount() >= 2 &&
               areAllMembersReady() &&
               !isInInstance();
    }
    
    /**
     * Get party age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - creationTime;
    }
    
    /**
     * Remove inactive members (offline for more than specified time)
     */
    public int removeInactiveMembers(long inactiveThresholdMs) {
        long currentTime = System.currentTimeMillis();
        List<UUID> inactiveMembers = members.values().stream()
                .filter(member -> !member.isOnline() && 
                                (currentTime - member.getLastActivityTime()) > inactiveThresholdMs)
                .map(PartyMember::getPlayerId)
                .toList();
        
        inactiveMembers.forEach(this::removeMember);
        return inactiveMembers.size();
    }
    
    /**
     * Remove expired invites (older than specified time)
     */
    public int removeExpiredInvites(long expireThresholdMs) {
        long currentTime = System.currentTimeMillis();
        List<PartyInvite> expiredInvites = invites.stream()
                .filter(invite -> (currentTime - invite.getCreationTime()) > expireThresholdMs)
                .toList();
        
        expiredInvites.forEach(invite -> removeInvite(invite.getPlayerId()));
        return expiredInvites.size();
    }
    
    @Override
    public String toString() {
        return "Party{" +
                "partyId='" + partyId + '\'' +
                ", leaderId=" + leaderId +
                ", state=" + state +
                ", members=" + members.size() + "/" + maxMembers +
                ", public=" + publicParty +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(partyId, party.partyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partyId);
    }
}
