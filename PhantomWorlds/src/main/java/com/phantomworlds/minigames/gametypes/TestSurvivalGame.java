package com.phantomworlds.minigames.gametypes;

import com.phantomworlds.minigames.*;
import com.phantomworlds.minigames.regions.MinigameRegionType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEST_SURVIVAL - Simple survival test minigame
 * Validates event system, timer system, and basic gameplay flow
 */
public class TestSurvivalGame implements GameType {
    
    private MinigameState state;
    private final Set<String> alivePlayers;
    private final Set<String> eliminatedPlayers;
    private final Map<String, Long> playerJoinTimes;
    
    private int gameTime;
    private int maxGameTime;
    private BukkitTask timerTask;
    private boolean gameEnded;
    
    public TestSurvivalGame() {
        this.state = MinigameState.WAITING;
        this.alivePlayers = ConcurrentHashMap.newKeySet();
        this.eliminatedPlayers = ConcurrentHashMap.newKeySet();
        this.playerJoinTimes = new ConcurrentHashMap<>();
        this.gameEnded = false;
    }
    
    @Override
    public String getId() {
        return "TEST_SURVIVAL";
    }
    
    @Override
    public String getDisplayName() {
        return "Test Survival";
    }
    
    @Override
    public MinigameCategory getCategory() {
        return MinigameCategory.PVP;
    }
    
    @Override
    public int getMinPlayers() {
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 10;
    }
    
    @Override
    public int getTeamCount() {
        return 1; // Free for all
    }
    
    @Override
    public boolean requiresRegions() {
        return true;
    }
    
    @Override
    public String[] getRequiredRegionTypes() {
        return new String[]{
            MinigameRegionType.LOBBY.getIdentifier(),
            MinigameRegionType.GAME.getIdentifier(),
            MinigameRegionType.TEAM_SPAWN.getIdentifier(),
            MinigameRegionType.SPECTATOR.getIdentifier()
        };
    }
    
    @Override
    public void initialize(MinigameConfig config) {
        this.maxGameTime = (Integer) config.getWinConditionValue("value");
        this.gameTime = maxGameTime;
        
        Bukkit.getLogger().info("Test Survival initialized with " + maxGameTime + " seconds game time");
    }
    
    @Override
    public void startGame() {
        state = MinigameState.PLAYING;
        gameEnded = false;
        gameTime = maxGameTime;
        
        // Give all players starting equipment
        giveStartingEquipment();
        
        // Start timer
        startGameTimer();
        
        // Send start message
        broadcastMessage("§aTest Survival started! Survive for " + maxGameTime + " seconds!");
        broadcastMessage("§eLast player standing wins!");
        
        Bukkit.getLogger().info("Test Survival game started with " + alivePlayers.size() + " players");
    }
    
    private void giveStartingEquipment() {
        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        
        for (String playerId : alivePlayers) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerId));
            if (player != null && player.isOnline()) {
                // Clear inventory and give starting sword
                player.getInventory().clear();
                player.getInventory().addItem(sword);
                player.sendMessage("§aYou received a stone sword! Good luck!");
            }
        }
    }
    
    private void startGameTimer() {
        // Cancel existing timer if any
        if (timerTask != null) {
            timerTask.cancel();
        }
        
        AtomicInteger timeRemaining = new AtomicInteger(gameTime);
        
        timerTask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("PhantomWorlds"), () -> {
            if (gameEnded) {
                timerTask.cancel();
                return;
            }
            
            int time = timeRemaining.decrementAndGet();
            gameTime = time;
            
            // Trigger timer tick event
            onTimerTick(maxGameTime - time);
            
            // Check for time warnings
            if (time == 10) {
                broadcastMessage("§e10 seconds remaining!");
            } else if (time == 5) {
                broadcastMessage("§c5 seconds remaining!");
            } else if (time == 3) {
                broadcastMessage("§c3 seconds remaining!");
            } else if (time == 2) {
                broadcastMessage("§c2 seconds remaining!");
            } else if (time == 1) {
                broadcastMessage("§c1 second remaining!");
            } else if (time <= 0) {
                // Time's up - end game
                endGame("TIME_UP");
                timerTask.cancel();
            }
        }, 0L, 20L); // Every second
    }
    
    @Override
    public void endGame() {
        gameEnded = true;
        state = MinigameState.ENDED;
        
        // Cancel timer
        if (timerTask != null) {
            timerTask.cancel();
        }
        
        Bukkit.getLogger().info("Test Survival game ended");
    }
    
    private void endGame(String reason) {
        if (gameEnded) return;
        
        gameEnded = true;
        state = MinigameState.ENDING;
        
        // Cancel timer
        if (timerTask != null) {
            timerTask.cancel();
        }
        
        // Determine winner(s)
        String[] winners = getWinners();
        
        // Announce result
        if (winners.length > 0) {
            if (winners.length == 1) {
                Player winner = Bukkit.getPlayer(UUID.fromString(winners[0]));
                String winnerName = winner != null ? winner.getName() : winners[0];
                broadcastMessage("§6§l" + winnerName + " wins the Test Survival!");
            } else {
                broadcastMessage("§6§lDraw! " + winners.length + " players survived!");
            }
        } else {
            broadcastMessage("§c§lNo winners! Everyone was eliminated!");
        }
        
        // End the game officially
        endGame();
        
        Bukkit.getLogger().info("Test Survival ended with reason: " + reason + ", winners: " + winners.length);
    }
    
    @Override
    public void onPlayerJoin(String playerId) {
        alivePlayers.add(playerId);
        playerJoinTimes.put(playerId, System.currentTimeMillis());
        
        Player player = Bukkit.getPlayer(UUID.fromString(playerId));
        if (player != null) {
            player.sendMessage("§aYou joined Test Survival!");
            player.sendMessage("§eSurvive as long as you can!");
        }
    }
    
    @Override
    public void onPlayerLeave(String playerId) {
        alivePlayers.remove(playerId);
        eliminatedPlayers.remove(playerId);
        playerJoinTimes.remove(playerId);
        
        // Check win condition after player leaves
        checkWinCondition();
    }
    
    @Override
    public void onPlayerDeath(String playerId) {
        if (!alivePlayers.contains(playerId)) return; // Already eliminated
        
        // Mark as eliminated
        alivePlayers.remove(playerId);
        eliminatedPlayers.add(playerId);
        
        Player player = Bukkit.getPlayer(UUID.fromString(playerId));
        if (player != null) {
            player.sendMessage("§cYou have been eliminated!");
            
            // Move to spectator (clear inventory, keep spectator mode)
            player.getInventory().clear();
            // Note: Actual spectator mode would be handled by the main system
        }
        
        // Broadcast elimination
        Player deadPlayer = Bukkit.getPlayer(UUID.fromString(playerId));
        String playerName = deadPlayer != null ? deadPlayer.getName() : "Unknown";
        broadcastMessage("§c" + playerName + " has been eliminated! " + alivePlayers.size() + " players remaining!");
        
        // Check win condition
        checkWinCondition();
        
        Bukkit.getLogger().info("Player " + playerId + " eliminated in Test Survival");
    }
    
    @Override
    public void onRegionEnter(String playerId, String regionType) {
        // Handle region events if needed
        // For Test Survival, we mainly use spawn regions
    }
    
    @Override
    public void onRegionLeave(String playerId, String regionType) {
        // Handle region events if needed
    }
    
    @Override
    public void onTimerTick(int timeElapsed) {
        // Update scoreboard every 5 seconds
        if (timeElapsed % 5 == 0) {
            updateScoreboard();
        }
        
        // Check win condition periodically
        if (timeElapsed % 2 == 0) {
            checkWinCondition();
        }
    }
    
    private void checkWinCondition() {
        if (gameEnded) return;
        
        // Check if only one player remains
        if (alivePlayers.size() <= 1) {
            endGame("LAST_PLAYER_STANDING");
        }
    }
    
    private void updateScoreboard() {
        // This would update the scoreboard for all players
        // Implementation depends on scoreboard system
        for (String playerId : alivePlayers) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerId));
            if (player != null && player.isOnline()) {
                // Update player's scoreboard with current game info
                // Note: Actual scoreboard update would be handled by the main system
            }
        }
    }
    
    private void broadcastMessage(String message) {
        // Send message to all players in the game
        Set<String> allPlayers = new HashSet<>(alivePlayers);
        allPlayers.addAll(eliminatedPlayers);
        
        for (String playerId : allPlayers) {
            Player player = Bukkit.getPlayer(UUID.fromString(playerId));
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }
    
    @Override
    public boolean hasEnded() {
        return gameEnded || state == MinigameState.ENDED || state == MinigameState.ENDING;
    }
    
    @Override
    public String[] getWinners() {
        if (alivePlayers.size() == 1) {
            return alivePlayers.toArray(new String[0]);
        } else if (gameTime <= 0 && alivePlayers.size() > 0) {
            // Time's up - all remaining players are winners (draw)
            return alivePlayers.toArray(new String[0]);
        }
        return new String[0];
    }
    
    @Override
    public MinigameState getState() {
        return state;
    }
    
    @Override
    public String[] getScoreboardContent() {
        List<String> content = new ArrayList<>();
        content.add("§c§lTest Survival");
        content.add("§7");
        content.add("§7Alive: §f" + alivePlayers.size());
        content.add("§7Time: §f" + gameTime + "s");
        content.add("§7");
        
        if (alivePlayers.size() <= 3) {
            content.add("§7Players:");
            for (String playerId : alivePlayers) {
                Player player = Bukkit.getPlayer(UUID.fromString(playerId));
                String name = player != null ? player.getName() : "Unknown";
                content.add("§f- " + name);
            }
        }
        
        return content.toArray(new String[0]);
    }
    
    /**
     * Get game statistics
     */
    public Map<String, Object> getGameStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("alivePlayers", alivePlayers.size());
        stats.put("eliminatedPlayers", eliminatedPlayers.size());
        stats.put("gameTime", gameTime);
        stats.put("maxGameTime", maxGameTime);
        stats.put("state", state.name());
        return stats;
    }
    
    /**
     * Get player statistics
     */
    public Map<String, Object> getPlayerStats(String playerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("isAlive", alivePlayers.contains(playerId));
        stats.put("isEliminated", eliminatedPlayers.contains(playerId));
        stats.put("joinTime", playerJoinTimes.getOrDefault(playerId, 0L));
        stats.put("survivalTime", alivePlayers.contains(playerId) ? 
            (System.currentTimeMillis() - playerJoinTimes.getOrDefault(playerId, 0L)) / 1000 : 0);
        return stats;
    }
}
