package com.phantomworlds.managers;

import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages cleanup of expired instances, worlds, and resources
 */
public class CleanupManager {
    
    private final Map<String, Long> scheduledCleanup;
    private final ScheduledExecutorService scheduler;
    private final InstanceManager instanceManager;
    private final PartyManager partyManager;
    private final long cleanupIntervalMs;
    private final long instanceTimeoutMs;
    private final long partyInactiveMs;
    private final long inviteExpireMs;
    
    public CleanupManager(InstanceManager instanceManager, PartyManager partyManager) {
        this.scheduledCleanup = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.instanceManager = instanceManager;
        this.partyManager = partyManager;
        
        // Default cleanup intervals (can be configured)
        this.cleanupIntervalMs = 60000; // 1 minute
        this.instanceTimeoutMs = 300000; // 5 minutes
        this.partyInactiveMs = 600000; // 10 minutes
        this.inviteExpireMs = 300000; // 5 minutes
        
        startCleanupTask();
    }
    
    /**
     * Start periodic cleanup task
     */
    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(this::performCleanup, 
                cleanupIntervalMs, cleanupIntervalMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Schedule instance cleanup
     */
    public void scheduleInstanceCleanup(String instanceId) {
        if (instanceId == null || instanceId.trim().isEmpty()) {
            Bukkit.getLogger().warning("Cannot schedule cleanup for null or empty instance ID");
            return;
        }
        
        if (scheduledCleanup == null) {
            Bukkit.getLogger().warning("scheduledCleanup map is null, cannot schedule instance cleanup for: " + instanceId);
            return;
        }
        
        try {
            scheduledCleanup.put(instanceId, System.currentTimeMillis() + instanceTimeoutMs);
            Bukkit.getLogger().info("Scheduled cleanup for instance: " + instanceId + " (timeout: " + instanceTimeoutMs + "ms)");
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to schedule cleanup for instance: " + instanceId + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cancel scheduled cleanup
     */
    public void cancelInstanceCleanup(String instanceId) {
        if (instanceId == null || instanceId.trim().isEmpty()) {
            Bukkit.getLogger().warning("Cannot cancel cleanup for null or empty instance ID");
            return;
        }
        
        if (scheduledCleanup == null) {
            Bukkit.getLogger().warning("scheduledCleanup map is null, cannot cancel instance cleanup for: " + instanceId);
            return;
        }
        
        try {
            boolean removed = scheduledCleanup.remove(instanceId) != null;
            if (removed) {
                Bukkit.getLogger().info("Cancelled cleanup for instance: " + instanceId);
            } else {
                Bukkit.getLogger().info("No cleanup scheduled for instance: " + instanceId + " (nothing to cancel)");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to cancel cleanup for instance: " + instanceId + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Perform cleanup operations
     */
    private void performCleanup() {
        long currentTime = System.currentTimeMillis();
        
        try {
            // Cleanup expired instances
            cleanupExpiredInstances(currentTime);
            
            // Cleanup inactive parties
            cleanupInactiveParties();
            
            // Cleanup orphaned worlds
            cleanupOrphanedWorlds();
            
            // Cleanup temporary files
            cleanupTempFiles();
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error during cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup expired instances
     */
    private void cleanupExpiredInstances(long currentTime) {
        List<String> expiredInstances = new ArrayList<>();
        
        // Check scheduled cleanup
        for (Map.Entry<String, Long> entry : scheduledCleanup.entrySet()) {
            if (currentTime >= entry.getValue()) {
                expiredInstances.add(entry.getKey());
            }
        }
        
        // Check instance manager for expired instances
        expiredInstances.addAll(instanceManager.findInstancesNeedingCleanup(instanceTimeoutMs));
        
        // Delete expired instances
        int deleted = 0;
        for (String instanceId : expiredInstances) {
            if (instanceManager.deleteInstance(instanceId)) {
                scheduledCleanup.remove(instanceId);
                deleted++;
            }
        }
        
        if (deleted > 0) {
            Bukkit.getLogger().info("Cleaned up " + deleted + " expired instances");
        }
    }
    
    /**
     * Cleanup inactive parties
     */
    private void cleanupInactiveParties() {
        int cleaned = partyManager.cleanupInactiveParties(partyInactiveMs, inviteExpireMs);
        
        if (cleaned > 0) {
            Bukkit.getLogger().info("Cleaned up " + cleaned + " inactive parties/invites");
        }
    }
    
    /**
     * Cleanup orphaned worlds
     */
    private void cleanupOrphanedWorlds() {
        File worldContainer = Bukkit.getWorldContainer();
        String[] worldNames = Bukkit.getWorlds().stream()
                .map(world -> world.getName())
                .toArray(String[]::new);
        Set<String> activeWorlds = new HashSet<>(Arrays.asList(worldNames));
        
        int deleted = 0;
        File[] worldFolders = worldContainer.listFiles(File::isDirectory);
        
        if (worldFolders != null) {
            for (File worldFolder : worldFolders) {
                String worldName = worldFolder.getName();
                
                // Skip non-instance worlds
                if (!worldName.startsWith("instance_")) {
                    continue;
                }
                
                // Skip active worlds
                if (activeWorlds.contains(worldName)) {
                    continue;
                }
                
                // Check if world folder is old enough to delete
                long folderAge = System.currentTimeMillis() - worldFolder.lastModified();
                if (folderAge > instanceTimeoutMs) {
                    if (deleteWorldFolder(worldName)) {
                        deleted++;
                    }
                }
            }
        }
        
        if (deleted > 0) {
            Bukkit.getLogger().info("Cleaned up " + deleted + " orphaned world folders");
        }
    }
    
    /**
     * Cleanup temporary files
     */
    private void cleanupTempFiles() {
        File tempFolder = new File("plugins/PhantomWorlds/temp");
        if (!tempFolder.exists()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        int deleted = 0;
        
        File[] tempFiles = tempFolder.listFiles();
        if (tempFiles != null) {
            for (File file : tempFiles) {
                long fileAge = currentTime - file.lastModified();
                if (fileAge > instanceTimeoutMs) {
                    if (file.delete()) {
                        deleted++;
                    }
                }
            }
        }
        
        if (deleted > 0) {
            Bukkit.getLogger().info("Cleaned up " + deleted + " temporary files");
        }
    }
    
    /**
     * Delete world folder
     */
    public boolean deleteWorldFolder(String worldName) {
        File worldContainer = Bukkit.getWorldContainer();
        File worldFolder = new File(worldContainer, worldName);
        
        if (!worldFolder.exists()) {
            return true;
        }
        
        // Unload world if loaded
        if (Bukkit.getWorld(worldName) != null) {
            Bukkit.unloadWorld(worldName, false);
        }
        
        return deleteDirectory(worldFolder);
    }
    
    /**
     * Delete directory recursively
     */
    private boolean deleteDirectory(File directory) {
        if (!directory.exists()) {
            return true;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        
        return directory.delete();
    }
    
    /**
     * Force cleanup all instances
     */
    public int forceCleanupAllInstances() {
        Collection<String> allInstanceIds = instanceManager.getAllInstances().stream()
                .map(instance -> instance.getInstanceId())
                .toList();
        
        int deleted = 0;
        for (String instanceId : allInstanceIds) {
            if (instanceManager.deleteInstance(instanceId)) {
                scheduledCleanup.remove(instanceId);
                deleted++;
            }
        }
        
        return deleted;
    }
    
    /**
     * Get cleanup statistics
     */
    public Map<String, Object> getCleanupStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("scheduled_cleanup_count", scheduledCleanup.size());
        stats.put("cleanup_interval_ms", cleanupIntervalMs);
        stats.put("instance_timeout_ms", instanceTimeoutMs);
        stats.put("party_inactive_ms", partyInactiveMs);
        stats.put("invite_expire_ms", inviteExpireMs);
        
        // Count expired scheduled cleanups
        long currentTime = System.currentTimeMillis();
        long expiredScheduled = scheduledCleanup.values().stream()
                .mapToLong(time -> currentTime >= time ? 1 : 0)
                .sum();
        
        stats.put("expired_scheduled_count", expiredScheduled);
        
        return stats;
    }
    
    /**
     * Check if cleanup is scheduled for instance
     */
    public boolean isCleanupScheduled(String instanceId) {
        return scheduledCleanup.containsKey(instanceId);
    }
    
    /**
     * Get scheduled cleanup time for instance
     */
    public Long getScheduledCleanupTime(String instanceId) {
        return scheduledCleanup.get(instanceId);
    }
    
    /**
     * Get all scheduled cleanup times
     */
    public Map<String, Long> getAllScheduledCleanup() {
        return new HashMap<>(scheduledCleanup);
    }
    
    /**
     * Perform immediate cleanup
     */
    public void performImmediateCleanup() {
        performCleanup();
    }
    
    /**
     * Set cleanup intervals
     */
    public void setCleanupIntervals(long cleanupIntervalMs, long instanceTimeoutMs, 
                                   long partyInactiveMs, long inviteExpireMs) {
        // Note: This would require restarting the cleanup task
        // For now, this is a placeholder for future implementation
    }
    
    /**
     * Check cleanup health
     */
    public Map<String, Boolean> checkCleanupHealth() {
        Map<String, Boolean> health = new HashMap<>();
        
        // Check scheduler
        health.put("scheduler_active", !scheduler.isShutdown());
        
        // Check instance manager
        health.put("instance_manager_available", instanceManager != null);
        
        // Check party manager
        health.put("party_manager_available", partyManager != null);
        
        // Check for too many scheduled cleanups
        health.put("scheduled_cleanup_healthy", scheduledCleanup.size() < 100);
        
        return health;
    }
    
    /**
     * Shutdown cleanup manager
     */
    public void shutdown() {
        // Perform final cleanup
        performImmediateCleanup();
        
        // Shutdown scheduler
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Clear scheduled cleanup
        scheduledCleanup.clear();
        
        Bukkit.getLogger().info("Cleanup manager shutdown complete");
    }
}
