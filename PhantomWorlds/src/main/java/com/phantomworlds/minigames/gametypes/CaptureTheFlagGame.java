package com.phantomworlds.minigames.gametypes;

import com.phantomworlds.minigames.*;
import com.phantomworlds.minigames.regions.MinigameRegionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capture the Flag game implementation
 */
public class CaptureTheFlagGame implements GameType {
    
    private MinigameState state;
    private final Map<String, String> playerTeams; // playerId -> teamId
    private final Map<String, ItemStack> teamFlags; // teamId -> flag
    private final Map<String, String> flagHolders; // teamId -> playerId
    private final Map<String, Integer> teamScores; // teamId -> score
    private final Set<String> eliminatedPlayers;
    
    private int winScore;
    private int teamCount;
    
    public CaptureTheFlagGame() {
        this.state = MinigameState.WAITING;
        this.playerTeams = new ConcurrentHashMap<>();
        this.teamFlags = new HashMap<>();
        this.flagHolders = new HashMap<>();
        this.teamScores = new HashMap<>();
        this.eliminatedPlayers = ConcurrentHashMap.newKeySet();
    }
    
    @Override
    public String getId() {
        return GameTypes.CAPTURE_THE_FLAG;
    }
    
    @Override
    public String getDisplayName() {
        return GameTypes.getDisplayName(GameTypes.CAPTURE_THE_FLAG);
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
        return teamCount;
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
            MinigameRegionType.OBJECTIVE.getIdentifier(),
            MinigameRegionType.SPECTATOR.getIdentifier()
        };
    }
    
    @Override
    public void initialize(MinigameConfig config) {
        this.teamCount = config.getTeamCount();
        this.winScore = (Integer) config.getWinConditionValue("value");
        
        // Initialize team scores
        for (int i = 1; i <= teamCount; i++) {
            String teamId = "team" + i;
            teamScores.put(teamId, 0);
            teamFlags.put(teamId, createFlag(i));
        }
        
        Bukkit.getLogger().info("Capture the Flag initialized with " + teamCount + " teams, win score: " + winScore);
    }
    
    private ItemStack createFlag(int teamNumber) {
        Material flagMaterial;
        switch (teamNumber) {
            case 1: flagMaterial = Material.RED_BANNER; break;
            case 2: flagMaterial = Material.BLUE_BANNER; break;
            case 3: flagMaterial = Material.GREEN_BANNER; break;
            case 4: flagMaterial = Material.YELLOW_BANNER; break;
            default: flagMaterial = Material.WHITE_BANNER; break;
        }
        
        ItemStack flag = new ItemStack(flagMaterial);
        // TODO: Add custom name and lore
        return flag;
    }
    
    @Override
    public void startGame() {
        state = MinigameState.PLAYING;
        
        // Assign players to teams
        assignPlayersToTeams();
        
        // Give flags to objective regions
        // This would be handled by region events
        
        Bukkit.getLogger().info("Capture the Flag game started!");
    }
    
    private void assignPlayersToTeams() {
        List<String> players = new ArrayList<>(playerTeams.keySet());
        Collections.shuffle(players);
        
        for (int i = 0; i < players.size(); i++) {
            String playerId = players.get(i);
            String teamId = "team" + ((i % teamCount) + 1);
            playerTeams.put(playerId, teamId);
        }
        
        Bukkit.getLogger().info("Assigned " + players.size() + " players to " + teamCount + " teams");
    }
    
    @Override
    public void endGame() {
        state = MinigameState.ENDED;
        
        // Return flags to base
        flagHolders.clear();
        
        Bukkit.getLogger().info("Capture the Flag game ended!");
    }
    
    @Override
    public void onPlayerJoin(String playerId) {
        playerTeams.put(playerId, "spectator"); // Will be assigned to team on start
        
        Player player = Bukkit.getPlayer(UUID.fromString(playerId));
        if (player != null) {
            player.sendMessage("§aYou joined Capture the Flag!");
        }
    }
    
    @Override
    public void onPlayerLeave(String playerId) {
        String teamId = playerTeams.remove(playerId);
        if (teamId != null && flagHolders.containsKey(teamId) && flagHolders.get(teamId).equals(playerId)) {
            // Player was holding flag, return it
            flagHolders.remove(teamId);
        }
        
        eliminatedPlayers.remove(playerId);
    }
    
    @Override
    public void onPlayerDeath(String playerId) {
        eliminatedPlayers.add(playerId);
        
        String teamId = playerTeams.get(playerId);
        if (teamId != null && flagHolders.containsKey(teamId) && flagHolders.get(teamId).equals(playerId)) {
            // Player was holding flag, drop it
            flagHolders.remove(teamId);
            
            Player player = Bukkit.getPlayer(UUID.fromString(playerId));
            if (player != null) {
                player.getInventory().remove(teamFlags.get(teamId));
            }
            
            Bukkit.getLogger().info("Player " + playerId + " died while holding flag!");
        }
    }
    
    @Override
    public void onRegionEnter(String playerId, String regionType) {
        if (state != MinigameState.PLAYING) return;
        
        String teamId = playerTeams.get(playerId);
        if (teamId == null || teamId.equals("spectator")) return;
        
        Player player = Bukkit.getPlayer(UUID.fromString(playerId));
        if (player == null) return;
        
        switch (regionType.toLowerCase()) {
            case "objective":
                handleObjectiveRegion(player, teamId);
                break;
            case "team_spawn":
                handleTeamSpawnRegion(player, teamId);
                break;
        }
    }
    
    private void handleObjectiveRegion(Player player, String teamId) {
        // Check if player is entering enemy objective (flag area)
        String enemyTeam = getEnemyTeam(teamId);
        if (enemyTeam == null) return;
        
        // Check if player is picking up enemy flag
        if (!flagHolders.containsKey(enemyTeam)) {
            // Give player the enemy flag
            ItemStack enemyFlag = teamFlags.get(enemyTeam);
            player.getInventory().addItem(enemyFlag);
            flagHolders.put(enemyTeam, player.getUniqueId().toString());
            
            player.sendMessage("§aYou picked up the enemy flag! Bring it back to your base!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§6" + player.getName() + " picked up the " + enemyTeam + " flag!");
            }
        }
    }
    
    private void handleTeamSpawnRegion(Player player, String teamId) {
        // Check if player is entering their own team spawn with enemy flag
        for (Map.Entry<String, String> entry : flagHolders.entrySet()) {
            String flagTeam = entry.getKey();
            String holderId = entry.getValue();
            
            if (holderId.equals(player.getUniqueId().toString()) && !flagTeam.equals(teamId)) {
                // Player scored!
                scorePoint(teamId);
                flagHolders.remove(flagTeam);
                
                // Remove flag from inventory
                player.getInventory().remove(teamFlags.get(flagTeam));
                
                player.sendMessage("§6You scored a point for your team!");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§6" + player.getName() + " scored for " + teamId + "!");
                }
                
                // Check win condition
                if (teamScores.get(teamId) >= winScore) {
                    // Game won!
                    for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§a" + teamId + " wins the game!");
                }
                    state = MinigameState.ENDING;
                }
                break;
            }
        }
    }
    
    private String getEnemyTeam(String teamId) {
        // Simple enemy team logic for 2 teams
        if (teamCount == 2) {
            return teamId.equals("team1") ? "team2" : "team1";
        }
        return null; // Multi-team logic would be more complex
    }
    
    private void scorePoint(String teamId) {
        teamScores.put(teamId, teamScores.get(teamId) + 1);
    }
    
    @Override
    public void onRegionLeave(String playerId, String regionType) {
        // Handle leaving objective regions if needed
    }
    
    @Override
    public void onTimerTick(int timeRemaining) {
        // Update scoreboard every 5 seconds
        if (timeRemaining % 5 == 0) {
            updateScoreboard();
        }
    }
    
    private void updateScoreboard() {
        // This would update the scoreboard for all players
        // Implementation depends on scoreboard system
    }
    
    @Override
    public boolean hasEnded() {
        return state == MinigameState.ENDED || state == MinigameState.ENDING;
    }
    
    @Override
    public String[] getWinners() {
        // Find team with highest score
        String winningTeam = null;
        int highestScore = -1;
        
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                winningTeam = entry.getKey();
            }
        }
        
        if (winningTeam == null) return new String[0];
        
        // Return all players on winning team
        final String finalWinningTeam = winningTeam;
        return playerTeams.entrySet().stream()
            .filter(entry -> entry.getValue().equals(finalWinningTeam))
            .map(Map.Entry::getKey)
            .toArray(String[]::new);
    }
    
    @Override
    public MinigameState getState() {
        return state;
    }
    
    @Override
    public String[] getScoreboardContent() {
        List<String> content = new ArrayList<>();
        content.add("§6§lCapture the Flag");
        content.add("§7");
        
        // Add team scores
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            content.add("§" + getTeamColor(entry.getKey()) + entry.getKey() + ": §f" + entry.getValue() + "/" + winScore);
        }
        
        content.add("§7");
        content.add("§7Players: " + playerTeams.size());
        
        return content.toArray(new String[0]);
    }
    
    private String getTeamColor(String teamId) {
        switch (teamId) {
            case "team1": return "c";
            case "team2": return "9";
            case "team3": return "a";
            case "team4": return "e";
            default: return "f";
        }
    }
}
