package com.phantomworlds.managers;

import com.phantomworlds.models.parties.Party;
import com.phantomworlds.models.parties.PartyInvite;
import com.phantomworlds.models.parties.PartyMember;
import com.phantomworlds.models.parties.PartyState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages party creation, membership, and operations
 */
public class PartyManager {
    
    private final Map<String, Party> parties;
    private final Map<UUID, String> playerToParty;
    private final Map<UUID, List<PartyInvite>> playerInvites;
    private final InstanceManager instanceManager;
    private int partyCounter;
    
    public PartyManager(InstanceManager instanceManager) {
        this.parties = new ConcurrentHashMap<>();
        this.playerToParty = new ConcurrentHashMap<>();
        this.playerInvites = new ConcurrentHashMap<>();
        this.instanceManager = instanceManager;
        this.partyCounter = 0;
    }
    
    /**
     * Create a new party
     */
    public Party createParty(UUID leaderId, String partyName, int maxMembers) {
        // Remove player from current party if any
        leaveParty(leaderId);
        
        String partyId = generatePartyId();
        Party party = new Party(partyId, leaderId, maxMembers);
        party.setDescription(partyName);
        
        parties.put(partyId, party);
        playerToParty.put(leaderId, partyId);
        
        return party;
    }
    
    /**
     * Get a party by ID
     */
    public Party getParty(String partyId) {
        return parties.get(partyId);
    }
    
    /**
     * Get party a player is currently in
     */
    public Party getPlayerParty(UUID playerId) {
        String partyId = playerToParty.get(playerId);
        return partyId != null ? parties.get(partyId) : null;
    }
    
    /**
     * Get all parties
     */
    public Collection<Party> getAllParties() {
        return Collections.unmodifiableCollection(parties.values());
    }
    
    /**
     * Get parties by state
     */
    public List<Party> getPartiesByState(PartyState state) {
        return parties.values().stream()
                .filter(party -> party.getState() == state)
                .collect(Collectors.toList());
    }
    
    /**
     * Get public parties
     */
    public List<Party> getPublicParties() {
        return parties.values().stream()
                .filter(Party::isPublicParty)
                .filter(party -> party.getState() == PartyState.FORMING)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a player is in a party
     */
    public boolean isInParty(UUID playerId) {
        return playerToParty.containsKey(playerId);
    }
    
    /**
     * Invite a player to a party
     */
    public boolean invitePlayer(String partyId, UUID inviterId, UUID targetId, String message) {
        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }
        
        // Check if inviter is leader
        if (!party.isLeader(inviterId)) {
            return false;
        }
        
        // Check if target is already in a party
        if (isInParty(targetId)) {
            return false;
        }
        
        // Check if party is full
        if (party.isFull()) {
            return false;
        }
        
        PartyInvite invite = new PartyInvite(partyId, targetId, inviterId);
        invite.setMessage(message);
        
        party.addInvite(invite);
        
        // Add to player invites
        playerInvites.computeIfAbsent(targetId, k -> new ArrayList<>()).add(invite);
        
        // Notify target player
        Player targetPlayer = Bukkit.getPlayer(targetId);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            Player inviterPlayer = Bukkit.getPlayer(inviterId);
            String inviterName = inviterPlayer != null ? inviterPlayer.getName() : "Unknown";
            targetPlayer.sendMessage("§eYou have been invited to join " + inviterName + "'s party!");
            if (message != null && !message.isEmpty()) {
                targetPlayer.sendMessage("§7Message: " + message);
            }
            targetPlayer.sendMessage("§7Use /party accept to join or /party decline to decline.");
        }
        
        return true;
    }
    
    /**
     * Accept a party invitation
     */
    public boolean acceptInvite(UUID playerId) {
        List<PartyInvite> invites = playerInvites.get(playerId);
        if (invites == null || invites.isEmpty()) {
            return false;
        }
        
        // Get the most recent invite
        PartyInvite invite = invites.get(invites.size() - 1);
        Party party = parties.get(invite.getPartyId());
        
        if (party == null || party.isFull()) {
            return false;
        }
        
        // Remove from current party if any
        leaveParty(playerId);
        
        // Add to new party
        if (party.addMember(playerId, "Member")) {
            playerToParty.put(playerId, invite.getPartyId());
            invite.accept();
            
            // Remove all invites for this player
            clearPlayerInvites(playerId);
            
            // Notify party members
            Player player = Bukkit.getPlayer(playerId);
            String playerName = player != null ? player.getName() : "Unknown";
            
            party.getMembers().keySet().forEach(memberId -> {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("§a" + playerName + " has joined the party!");
                }
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Decline a party invitation
     */
    public boolean declineInvite(UUID playerId) {
        List<PartyInvite> invites = playerInvites.get(playerId);
        if (invites == null || invites.isEmpty()) {
            return false;
        }
        
        PartyInvite invite = invites.get(invites.size() - 1);
        Party party = parties.get(invite.getPartyId());
        
        invite.decline();
        
        // Remove from party invites
        party.removeInvite(playerId);
        
        // Clear player invites
        clearPlayerInvites(playerId);
        
        // Notify inviter
        if (party != null) {
            Player inviter = Bukkit.getPlayer(invite.getInviterId());
            Player target = Bukkit.getPlayer(playerId);
            String targetName = target != null ? target.getName() : "Unknown";
            
            if (inviter != null && inviter.isOnline()) {
                inviter.sendMessage("§c" + targetName + " declined your party invitation.");
            }
        }
        
        return true;
    }
    
    /**
     * Leave a party
     */
    public boolean leaveParty(UUID playerId) {
        String partyId = playerToParty.get(playerId);
        if (partyId == null) {
            return false;
        }
        
        Party party = parties.get(partyId);
        if (party == null) {
            playerToParty.remove(playerId);
            return false;
        }
        
        // Remove from party
        party.removeMember(playerId);
        playerToParty.remove(playerId);
        
        // Notify remaining members
        Player player = Bukkit.getPlayer(playerId);
        String playerName = player != null ? player.getName() : "Unknown";
        
        party.getMembers().keySet().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c" + playerName + " has left the party.");
            }
        });
        
        // Disband party if empty
        if (party.getMemberCount() == 0) {
            disbandParty(partyId);
        }
        
        return true;
    }
    
    /**
     * Kick a player from a party
     */
    public boolean kickPlayer(String partyId, UUID leaderId, UUID targetId) {
        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }
        
        // Check if leader
        if (!party.isLeader(leaderId)) {
            return false;
        }
        
        // Cannot kick yourself
        if (leaderId.equals(targetId)) {
            return false;
        }
        
        if (!party.isMember(targetId)) {
            return false;
        }
        
        // Remove from party
        party.removeMember(targetId);
        playerToParty.remove(targetId);
        
        // Notify kicked player
        Player target = Bukkit.getPlayer(targetId);
        if (target != null && target.isOnline()) {
            target.sendMessage("§cYou have been kicked from the party.");
        }
        
        // Notify remaining members
        String targetName = target != null ? target.getName() : "Unknown";
        party.getMembers().keySet().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c" + targetName + " has been kicked from the party.");
            }
        });
        
        return true;
    }
    
    /**
     * Transfer leadership to another member
     */
    public boolean transferLeadership(String partyId, UUID currentLeaderId, UUID newLeaderId) {
        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }
        
        // Check if current leader
        if (!party.isLeader(currentLeaderId)) {
            return false;
        }
        
        // Check if new leader is member
        if (!party.isMember(newLeaderId)) {
            return false;
        }
        
        // Update roles
        PartyMember oldLeader = party.getMember(currentLeaderId);
        PartyMember newLeader = party.getMember(newLeaderId);
        
        if (oldLeader != null) {
            oldLeader.setRole("Member");
        }
        
        if (newLeader != null) {
            newLeader.promoteToLeader();
        }
        
        // Note: This would require updating the leaderId field in Party class
        // For now, this is a placeholder for future implementation
        
        // Notify party
        Player newLeaderPlayer = Bukkit.getPlayer(newLeaderId);
        String newLeaderName = newLeaderPlayer != null ? newLeaderPlayer.getName() : "Unknown";
        
        party.getMembers().keySet().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§6" + newLeaderName + " is now the party leader!");
            }
        });
        
        return true;
    }
    
    /**
     * Start an instance with the party
     */
    public boolean startInstance(String partyId, String templateId) {
        Party party = parties.get(partyId);
        if (party == null) {
            return false;
        }
        
        // Check if party can start instance
        if (!party.canStartInstance()) {
            return false;
        }
        
        // Create instance
        try {
            instanceManager.createInstance(templateId, party.getType())
                    .thenAccept(instance -> {
                        // Add all party members to instance
                        party.getMembers().keySet().forEach(memberId -> {
                            Player player = Bukkit.getPlayer(memberId);
                            if (player != null && player.isOnline()) {
                                instanceManager.addPlayerToInstance(instance.getInstanceId(), player);
                            }
                        });
                        
                        // Update party state
                        party.setCurrentInstanceId(instance.getInstanceId());
                        party.setState(PartyState.IN_INSTANCE);
                        
                        // Start instance
                        instanceManager.startInstance(instance.getInstanceId());
                    })
                    .exceptionally(throwable -> {
                        Bukkit.getLogger().warning("Failed to create instance for party " + partyId + ": " + throwable.getMessage());
                        return null;
                    });
            
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error starting instance for party " + partyId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Disband a party
     */
    public boolean disbandParty(String partyId) {
        Party party = parties.remove(partyId);
        if (party == null) {
            return false;
        }
        
        // Remove all players from party
        new ArrayList<>(party.getMembers().keySet()).forEach(memberId -> {
            playerToParty.remove(memberId);
            clearPlayerInvites(memberId);
        });
        
        // Leave instance if in one
        if (party.isInInstance()) {
            instanceManager.endInstance(party.getCurrentInstanceId());
        }
        
        return true;
    }
    
    /**
     * Set player ready status
     */
    public boolean setPlayerReady(UUID playerId, boolean ready) {
        Party party = getPlayerParty(playerId);
        if (party == null) {
            return false;
        }
        
        PartyMember member = party.getMember(playerId);
        if (member != null) {
            member.setReady(ready);
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear all invites for a player
     */
    public void clearPlayerInvites(UUID playerId) {
        List<PartyInvite> invites = playerInvites.remove(playerId);
        if (invites != null) {
            invites.forEach(invite -> {
                Party party = parties.get(invite.getPartyId());
                if (party != null) {
                    party.removeInvite(playerId);
                }
            });
        }
    }
    
    /**
     * Get player's pending invites
     */
    public List<PartyInvite> getPlayerInvites(UUID playerId) {
        return playerInvites.getOrDefault(playerId, Collections.emptyList());
    }
    
    /**
     * Cleanup inactive parties
     */
    public int cleanupInactiveParties(long inactiveThresholdMs, long inviteExpireMs) {
        int removed = 0;
        
        List<String> partiesToRemove = new ArrayList<>();
        
        for (Party party : parties.values()) {
            // Remove inactive members
            party.removeInactiveMembers(inactiveThresholdMs);
            
            // Remove expired invites
            party.removeExpiredInvites(inviteExpireMs);
            
            // Disband empty parties
            if (party.getMemberCount() == 0) {
                partiesToRemove.add(party.getPartyId());
            }
        }
        
        // Remove empty parties
        for (String partyId : partiesToRemove) {
            disbandParty(partyId);
            removed++;
        }
        
        return removed;
    }
    
    /**
     * Generate unique party ID
     */
    private String generatePartyId() {
        return "party_" + (++partyCounter) + "_" + System.currentTimeMillis();
    }
    
    /**
     * Get party statistics
     */
    public Map<PartyState, Integer> getPartyStatistics() {
        Map<PartyState, Integer> stats = new EnumMap<>(PartyState.class);
        
        for (Party party : parties.values()) {
            stats.merge(party.getState(), 1, (existing, one) -> existing + one);
        }
        
        return stats;
    }
    
    /**
     * Shutdown cleanup
     */
    public void shutdown() {
        // Disband all parties
        new ArrayList<>(parties.keySet()).forEach(this::disbandParty);
        
        // Clear mappings
        playerToParty.clear();
        playerInvites.clear();
    }
}
