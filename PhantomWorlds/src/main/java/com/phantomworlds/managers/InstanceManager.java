package com.phantomworlds.managers;

import com.phantomworlds.PhantomWorlds;
import com.phantomworlds.models.instances.Instance;
import com.phantomworlds.models.instances.InstanceState;
import com.phantomworlds.models.instances.InstanceType;
import com.phantomworlds.models.templates.Template;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all instance creation, lifecycle, and cleanup
 */
public class InstanceManager {
    
    private final Map<String, Instance> instances;
    private final Map<UUID, String> playerToInstance;
    private final TemplateManager templateManager;
    private final CleanupManager cleanupManager;
    private final PhantomWorlds plugin;
    private int instanceCounter;
    
    public InstanceManager(TemplateManager templateManager, CleanupManager cleanupManager, PhantomWorlds plugin) {
        this.instances = new ConcurrentHashMap<>();
        this.playerToInstance = new ConcurrentHashMap<>();
        this.templateManager = templateManager;
        this.cleanupManager = cleanupManager;
        this.plugin = plugin;
        this.instanceCounter = 0;
    }
    
    /**
     * Create a new instance from a template
     */
    public CompletableFuture<Instance> createInstance(String templateId, InstanceType instanceType) {
        return CompletableFuture.supplyAsync(() -> {
            // Validate template first
            Template template = templateManager.getTemplate(templateId);
            if (template == null) {
                Bukkit.getLogger().warning("Template not found: " + templateId);
                throw new IllegalArgumentException("Invalid template: " + templateId + " (template does not exist)");
            }
            
            if (!template.isValid()) {
                Bukkit.getLogger().warning("Template is invalid: " + templateId);
                throw new IllegalArgumentException("Invalid template: " + templateId + " (template is invalid)");
            }
            
            if (template.getInstanceType() != instanceType) {
                Bukkit.getLogger().warning("Template type mismatch for template: " + templateId + 
                    " (template type: " + template.getInstanceType() + ", requested type: " + instanceType + ")");
                throw new IllegalArgumentException("Template type mismatch for template: " + templateId + 
                    " (template type: " + template.getInstanceType() + ", requested type: " + instanceType + ")");
            }
            
            Bukkit.getLogger().info("Creating instance from template: " + templateId + 
                " (instance type: " + instanceType + ", template type: " + template.getInstanceType() + ")");
            Bukkit.getLogger().info("Template details - ID: " + template.getId() + 
                ", Name: " + template.getDisplayName() + 
                ", World: " + template.getWorldName() + 
                ", Valid: " + template.isValid() + 
                ", Type: " + template.getInstanceType() + ")");
            
            String instanceId = generateInstanceId();
            String worldName = "instance_" + instanceId;
            
            // Create world synchronously using Bukkit scheduler
            CompletableFuture<Instance> instanceFuture = new CompletableFuture<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World world = Bukkit.createWorld(new WorldCreator(worldName)
                            .copy(new WorldCreator(template.getWorldName()))
                            .environment(World.Environment.NORMAL));
                    
                    if (world == null) {
                        instanceFuture.completeExceptionally(new RuntimeException("Failed to create world: " + worldName));
                        return;
                    }
                    
                    Instance instance = new Instance(instanceId, templateId, world, instanceType, template.getInstanceType().getMaxPlayers());
                    instances.put(instanceId, instance);
                    instanceFuture.complete(instance);
                } catch (Exception e) {
                    instanceFuture.completeExceptionally(e);
                }
            });
            
            try {
                return instanceFuture.get(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance within timeout", e);
            }
        });
    }
    
    /**
     * Get an instance by ID
     */
    public Instance getInstance(String instanceId) {
        return instances.get(instanceId);
    }
    
    /**
     * Get all instances
     */
    public Collection<Instance> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }
    
    /**
     * Get instances by state
     */
    public List<Instance> getInstancesByState(InstanceState state) {
        return instances.values().stream()
                .filter(instance -> instance.getState() == state)
                .toList();
    }
    
    /**
     * Get instances by type
     */
    public List<Instance> getInstancesByType(InstanceType type) {
        return instances.values().stream()
                .filter(instance -> instance.getType() == type)
                .toList();
    }
    
    /**
     * Get instance a player is currently in
     */
    public Instance getPlayerInstance(UUID playerId) {
        String instanceId = playerToInstance.get(playerId);
        return instanceId != null ? instances.get(instanceId) : null;
    }
    
    /**
     * Add a player to an instance
     */
    public boolean addPlayerToInstance(String instanceId, Player player) {
        Instance instance = instances.get(instanceId);
        if (instance == null) {
            return false;
        }
        
        // Remove player from current instance if any
        removePlayerFromInstance(player.getUniqueId());
        
        if (instance.addPlayer(player)) {
            playerToInstance.put(player.getUniqueId(), instanceId);
            
            // Teleport player to spawn location synchronously
            Bukkit.getScheduler().runTask(plugin, () -> {
                Template template = templateManager.getTemplate(instance.getTemplateId());
                if (template != null) {
                    Location spawnLocation = template.getSpawnLocation(instance.getWorld());
                    if (spawnLocation != null) {
                        player.teleport(spawnLocation);
                        Bukkit.getLogger().info("Teleported player " + player.getName() + 
                            " to instance " + instanceId + " spawn location");
                    } else {
                        Bukkit.getLogger().warning("No spawn location found for template: " + 
                            instance.getTemplateId());
                    }
                } else {
                    Bukkit.getLogger().warning("Template not found for instance: " + 
                        instance.getTemplateId());
                }
            });
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove a player from an instance
     */
    public boolean removePlayerFromInstance(UUID playerId) {
        String instanceId = playerToInstance.get(playerId);
        if (instanceId == null) {
            return false;
        }
        
        Instance instance = instances.get(instanceId);
        if (instance != null) {
            instance.removePlayer(playerId);
        }
        
        playerToInstance.remove(playerId);
        
        // If instance is empty, schedule cleanup
        if (instance != null && instance.getPlayerCount() == 0) {
            scheduleInstanceCleanup(instanceId);
        }
        
        return true;
    }
    
    /**
     * Start an instance
     */
    public boolean startInstance(String instanceId) {
        Instance instance = instances.get(instanceId);
        if (instance == null || instance.getState() != InstanceState.LOBBY) {
            return false;
        }
        
        instance.setState(InstanceState.RUNNING);
        return true;
    }
    
    /**
     * End an instance
     */
    public boolean endInstance(String instanceId) {
        Instance instance = instances.get(instanceId);
        if (instance == null) {
            return false;
        }
        
        instance.setState(InstanceState.ENDING);
        
        // Remove all players
        new ArrayList<>(instance.getPlayers().keySet()).forEach(this::removePlayerFromInstance);
        
        // Schedule cleanup
        scheduleInstanceCleanup(instanceId);
        
        return true;
    }
    
    /**
     * Delete an instance immediately
     */
    public boolean deleteInstance(String instanceId) {
        Instance instance = instances.remove(instanceId);
        if (instance == null) {
            return false;
        }
        
        // Remove all players
        new ArrayList<>(instance.getPlayers().keySet()).forEach(this::removePlayerFromInstance);
        
        // Delete world
        World world = instance.getWorld();
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            
            // Delete world folder
            cleanupManager.deleteWorldFolder(world.getName());
        }
        
        return true;
    }
    
    /**
     * Check if an instance exists
     */
    public boolean hasInstance(String instanceId) {
        return instances.containsKey(instanceId);
    }
    
    /**
     * Get instance statistics
     */
    public Map<InstanceState, Integer> getInstanceStatistics() {
        Map<InstanceState, Integer> stats = new EnumMap<>(InstanceState.class);
        
        for (Instance instance : instances.values()) {
            stats.merge(instance.getState(), 1, (existing, one) -> existing + one);
        }
        
        return stats;
    }
    
    /**
     * Get total player count across all instances
     */
    public int getTotalPlayerCount() {
        return instances.values().stream()
                .mapToInt(Instance::getPlayerCount)
                .sum();
    }
    
    /**
     * Find instances that need cleanup
     */
    public List<String> findInstancesNeedingCleanup(long timeoutMs) {
        return instances.values().stream()
                .filter(instance -> instance.isExpired(timeoutMs))
                .map(Instance::getInstanceId)
                .toList();
    }
    
    /**
     * Cleanup expired instances
     */
    public int cleanupExpiredInstances(long timeoutMs) {
        List<String> expiredInstances = findInstancesNeedingCleanup(timeoutMs);
        expiredInstances.forEach(this::deleteInstance);
        return expiredInstances.size();
    }
    
    /**
     * Generate unique instance ID
     */
    private String generateInstanceId() {
        return "inst_" + (++instanceCounter) + "_" + System.currentTimeMillis();
    }
    
    /**
     * Schedule instance cleanup
     */
    private void scheduleInstanceCleanup(String instanceId) {
        if (cleanupManager != null) {
            cleanupManager.scheduleInstanceCleanup(instanceId);
        } else {
            Bukkit.getLogger().warning("cleanupManager is null, cannot schedule instance cleanup for: " + instanceId);
        }
    }
    
    /**
     * Save instance data
     */
    public void saveInstanceData() {
        // Implementation for saving instance data to storage
        // This would be expanded with database support
    }
    
    /**
     * Load instance data
     */
    public void loadInstanceData() {
        // Implementation for loading instance data from storage
        // This would be expanded with database support
    }
    
    /**
     * Shutdown cleanup
     */
    public void shutdown() {
        // End all running instances
        instances.keySet().forEach(this::endInstance);
        
        // Wait a moment for cleanup
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Force delete remaining instances
        new ArrayList<>(instances.keySet()).forEach(this::deleteInstance);
    }
}
