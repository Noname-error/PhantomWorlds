package com.phantomworlds.minigames;

import com.phantomworlds.minigames.events.*;
import com.phantomworlds.minigames.regions.MinigameRegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main manager for all minigame instances
 */
public class MinigameManager {
    
    private final Map<String, MinigameInstance> activeMinigames;
    private final MinigameRegionManager regionManager;
    private final Map<String, GameType> gameTypeRegistry;
    private final Map<String, BukkitTask> countdownTasks;
    private final Map<String, BukkitTask> gameTimerTasks;
    
    public MinigameManager() {
        this.activeMinigames = new ConcurrentHashMap<>();
        this.regionManager = new MinigameRegionManager();
        this.gameTypeRegistry = new HashMap<>();
        this.countdownTasks = new ConcurrentHashMap<>();
        this.gameTimerTasks = new ConcurrentHashMap<>();
        
        // Register game types
        registerGameTypes();
    }
    
    private void registerGameTypes() {
        // Game types will be registered here
        // For now, we'll create a basic registry
    }
    
    /**
     * Register a game type
     */
    public void registerGameType(GameType gameType) {
        gameTypeRegistry.put(gameType.getId(), gameType);
    }
    
    /**
     * Start a minigame in an instance
     */
    public boolean startMinigame(String instanceId, String gameType, MinigameConfig config) {
        if (activeMinigames.containsKey(instanceId)) {
            Bukkit.getLogger().warning("Instance " + instanceId + " already has an active minigame");
            return false;
        }
        
        GameType game = gameTypeRegistry.get(gameType);
        if (game == null) {
            Bukkit.getLogger().warning("Unknown game type: " + gameType);
            return false;
        }
        
        // Initialize game with config
        game.initialize(config);
        
        // Create minigame instance
        MinigameInstance minigame = new MinigameInstance(instanceId, gameType, game);
        activeMinigames.put(instanceId, minigame);
        
        // Start with lobby phase
        startLobbyPhase(instanceId);
        
        return true;
    }
    
    /**
     * Start lobby phase for a minigame
     */
    private void startLobbyPhase(String instanceId) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        minigame.setState(MinigameState.LOBBY);
        
        // Start countdown timer
        AtomicInteger countdown = new AtomicInteger(30); // 30 seconds lobby time
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PhantomWorlds"), () -> {
            int time = countdown.decrementAndGet();
            
            if (time <= 0) {
                // Start the game
                startGame(instanceId);
                countdownTasks.remove(instanceId).cancel();
            } else if (time <= 10 || time % 10 == 0) {
                // Announce countdown
                broadcastToInstance(instanceId, "§eGame starting in " + time + " seconds!");
            }
        }, 0L, 20L);
        
        countdownTasks.put(instanceId, task);
    }
    
    /**
     * Start the actual game
     */
    private void startGame(String instanceId) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        minigame.setState(MinigameState.PLAYING);
        minigame.getGame().startGame();
        
        // Fire start event
        Bukkit.getPluginManager().callEvent(new MinigameStartEvent(instanceId, minigame.getGameType()));
        
        // Start game timer
        startGameTimer(instanceId);
        
        broadcastToInstance(instanceId, "§aGame started! Good luck!");
    }
    
    /**
     * Start game timer for scorekeeping and win conditions
     */
    private void startGameTimer(String instanceId) {
        AtomicInteger gameTime = new AtomicInteger(0);
        
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PhantomWorlds"), () -> {
            int time = gameTime.incrementAndGet();
            
            MinigameInstance minigame = activeMinigames.get(instanceId);
            if (minigame == null) {
                gameTimerTasks.remove(instanceId).cancel();
                return;
            }
            
            // Update game type with timer tick
            minigame.getGame().onTimerTick(time);
            
            // Check if game has ended
            if (minigame.getGame().hasEnded()) {
                endMinigame(instanceId, "Game completed");
                gameTimerTasks.remove(instanceId).cancel();
            }
        }, 0L, 20L);
        
        gameTimerTasks.put(instanceId, task);
    }
    
    /**
     * End a minigame
     */
    public void endMinigame(String instanceId, String reason) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        minigame.setState(MinigameState.ENDING);
        minigame.getGame().endGame();
        
        // Get winners
        String[] winners = minigame.getGame().getWinners();
        
        // Fire end event
        Bukkit.getPluginManager().callEvent(new MinigameEndEvent(instanceId, minigame.getGameType(), 
            Arrays.asList(winners), reason));
        
        // Announce results
        if (winners.length > 0) {
            broadcastToInstance(instanceId, "§6Game ended! Winners: " + String.join(", ", winners));
        } else {
            broadcastToInstance(instanceId, "§6Game ended! No winners.");
        }
        
        // Cleanup
        cleanupMinigame(instanceId);
    }
    
    /**
     * Clean up a minigame instance
     */
    private void cleanupMinigame(String instanceId) {
        // Cancel tasks
        if (countdownTasks.containsKey(instanceId)) {
            countdownTasks.remove(instanceId).cancel();
        }
        if (gameTimerTasks.containsKey(instanceId)) {
            gameTimerTasks.remove(instanceId).cancel();
        }
        
        // Clean up regions
        regionManager.cleanupInstance(instanceId);
        
        // Remove from active games
        activeMinigames.remove(instanceId);
    }
    
    /**
     * Handle player joining a minigame instance
     */
    public void onPlayerJoin(String instanceId, Player player) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        minigame.getGame().onPlayerJoin(player.getUniqueId().toString());
        
        // Teleport to lobby region
        Location lobbySpawn = regionManager.getSpawnLocation(instanceId, "lobby", player.getWorld());
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
    }
    
    /**
     * Handle player leaving a minigame instance
     */
    public void onPlayerLeave(String instanceId, Player player) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        minigame.getGame().onPlayerLeave(player.getUniqueId().toString());
        regionManager.removePlayer(instanceId, player.getUniqueId().toString());
    }
    
    /**
     * Handle player death in a minigame
     */
    public void onPlayerDeath(String instanceId, Player player, Player killer) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        String killerId = killer != null ? killer.getUniqueId().toString() : null;
        minigame.getGame().onPlayerDeath(player.getUniqueId().toString());
        
        // Fire death event
        Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(instanceId, minigame.getGameType(), player, killerId));
    }
    
    /**
     * Handle player movement for region events
     */
    public void onPlayerMove(String instanceId, Player player, org.bukkit.Location location) {
        MinigameInstance minigame = activeMinigames.get(instanceId);
        if (minigame == null) return;
        
        // Handle region events
        regionManager.handlePlayerMove(instanceId, player.getUniqueId().toString(), location);
        
        // Get current region and notify game type
        com.phantomworlds.minigames.regions.MinigameRegionType regionType = 
            regionManager.getRegionAt(instanceId, location);
        
        if (regionType != null) {
            minigame.getGame().onRegionEnter(player.getUniqueId().toString(), regionType.name());
        }
    }
    
    /**
     * Broadcast message to all players in an instance
     */
    private void broadcastToInstance(String instanceId, String message) {
        // This would integrate with the existing InstanceManager
        // For now, we'll just log it
        Bukkit.getLogger().info("[Minigame " + instanceId + "] " + message);
    }
    
    /**
     * Get active minigame for an instance
     */
    public MinigameInstance getActiveMinigame(String instanceId) {
        return activeMinigames.get(instanceId);
    }
    
    /**
     * Get region manager
     */
    public MinigameRegionManager getRegionManager() {
        return regionManager;
    }
    
    /**
     * Check if an instance has an active minigame
     */
    public boolean hasActiveMinigame(String instanceId) {
        return activeMinigames.containsKey(instanceId);
    }
    
    /**
     * Get all active minigames
     */
    public Collection<MinigameInstance> getActiveMinigames() {
        return activeMinigames.values();
    }
}
