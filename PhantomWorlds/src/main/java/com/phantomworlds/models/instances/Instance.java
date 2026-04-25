package com.phantomworlds.models.instances;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a temporary world instance created from a template
 */
public class Instance {
    
    private final String instanceId;
    private final String templateId;
    private final World world;
    private final InstanceType type;
    private final int maxPlayers;
    private final long creationTime;
    private final Map<UUID, InstancePlayer> players;
    private final List<InstanceModifier> modifiers;
    
    private InstanceState state;
    private long startTime;
    private long endTime;
    private String currentRegion;
    
    public Instance(String instanceId, String templateId, World world, InstanceType type, int maxPlayers) {
        this.instanceId = instanceId;
        this.templateId = templateId;
        this.world = world;
        this.type = type;
        this.maxPlayers = maxPlayers;
        this.creationTime = System.currentTimeMillis();
        this.players = new ConcurrentHashMap<>();
        this.modifiers = new ArrayList<>();
        this.state = InstanceState.LOBBY;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getTemplateId() {
        return templateId;
    }
    
    public World getWorld() {
        return world;
    }
    
    public InstanceType getType() {
        return type;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public InstanceState getState() {
        return state;
    }
    
    public void setState(InstanceState state) {
        this.state = state;
        if (state == InstanceState.RUNNING) {
            this.startTime = System.currentTimeMillis();
        } else if (state == InstanceState.ENDING) {
            this.endTime = System.currentTimeMillis();
        }
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public String getCurrentRegion() {
        return currentRegion;
    }
    
    public void setCurrentRegion(String currentRegion) {
        this.currentRegion = currentRegion;
    }
    
    public Map<UUID, InstancePlayer> getPlayers() {
        return Collections.unmodifiableMap(players);
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    public boolean addPlayer(Player player) {
        if (isFull() || players.containsKey(player.getUniqueId())) {
            return false;
        }
        
        InstancePlayer instancePlayer = new InstancePlayer(player.getUniqueId(), player.getName(), System.currentTimeMillis());
        players.put(player.getUniqueId(), instancePlayer);
        return true;
    }
    
    public boolean removePlayer(UUID playerId) {
        InstancePlayer removed = players.remove(playerId);
        return removed != null;
    }
    
    public boolean hasPlayer(UUID playerId) {
        return players.containsKey(playerId);
    }
    
    public InstancePlayer getInstancePlayer(UUID playerId) {
        return players.get(playerId);
    }
    
    public List<InstanceModifier> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }
    
    public void addModifier(InstanceModifier modifier) {
        modifiers.add(modifier);
    }
    
    public void removeModifier(InstanceModifier modifier) {
        modifiers.remove(modifier);
    }
    
    public boolean hasModifier(String modifierType) {
        return modifiers.stream().anyMatch(m -> m.getType().equals(modifierType));
    }
    
    public InstanceModifier getModifier(String modifierType) {
        return modifiers.stream()
                .filter(m -> m.getType().equals(modifierType))
                .findFirst()
                .orElse(null);
    }
    
    public long getDuration() {
        if (startTime > 0 && endTime > 0) {
            return endTime - startTime;
        } else if (startTime > 0) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    public boolean isExpired(long timeoutMs) {
        if (state == InstanceState.ENDING) {
            return System.currentTimeMillis() - endTime > timeoutMs;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Instance{" +
                "instanceId='" + instanceId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", state=" + state +
                ", players=" + players.size() + "/" + maxPlayers +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instance instance = (Instance) o;
        return Objects.equals(instanceId, instance.instanceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }
}
