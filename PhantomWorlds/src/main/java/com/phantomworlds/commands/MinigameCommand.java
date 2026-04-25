package com.phantomworlds.commands;

import com.phantomworlds.PhantomWorlds;
import com.phantomworlds.minigames.*;
import com.phantomworlds.minigames.regions.MinigameRegionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Minigame management commands
 */
public class MinigameCommand implements CommandExecutor, TabCompleter {
    
    private final PhantomWorlds plugin;
    private final MinigameManager minigameManager;
    
    public MinigameCommand(PhantomWorlds plugin, MinigameManager minigameManager) {
        this.plugin = plugin;
        this.minigameManager = minigameManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "list":
                return handleList(player, args);
            case "info":
                return handleInfo(player, args);
            case "stop":
                return handleStop(player, args);
            default:
                showHelp(player);
                return true;
        }
    }
    
    private boolean handleStart(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /minigame start <instanceId> <gameType>");
            return true;
        }
        
        String instanceId = args[1];
        String gameType = args[2].toUpperCase();
        
        // Check if instance exists and has minigame
        if (!plugin.getInstanceManager().hasInstance(instanceId)) {
            player.sendMessage("§cInstance '" + instanceId + "' not found!");
            return true;
        }
        
        // Check if minigame already active
        if (minigameManager.hasActiveMinigame(instanceId)) {
            player.sendMessage("§cInstance '" + instanceId + "' already has an active minigame!");
            return true;
        }
        
        // Load minigame config
        MinigameConfig config = loadMinigameConfig(gameType);
        if (config == null) {
            player.sendMessage("§cUnknown game type: " + gameType);
            return true;
        }
        
        // Start minigame
        if (minigameManager.startMinigame(instanceId, gameType, config)) {
            player.sendMessage("§aMinigame '" + gameType + "' started in instance '" + instanceId + "'!");
            
            // Register regions from template
            registerInstanceRegions(instanceId);
        } else {
            player.sendMessage("§cFailed to start minigame!");
        }
        
        return true;
    }
    
    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /minigame join <instanceId>");
            return true;
        }
        
        String instanceId = args[1];
        
        // Check if instance exists
        if (!plugin.getInstanceManager().hasInstance(instanceId)) {
            player.sendMessage("§cInstance '" + instanceId + "' not found!");
            return true;
        }
        
        // Check if minigame is active
        if (!minigameManager.hasActiveMinigame(instanceId)) {
            player.sendMessage("§cNo active minigame in instance '" + instanceId + "'!");
            return true;
        }
        
        // Check if player can join
        MinigameInstance minigame = minigameManager.getActiveMinigame(instanceId);
        if (minigame == null || !minigame.canJoin()) {
            player.sendMessage("§cCannot join minigame at this time!");
            return true;
        }
        
        // Add player to instance
        if (plugin.getInstanceManager().addPlayerToInstance(instanceId, player)) {
            minigameManager.onPlayerJoin(instanceId, player);
            player.sendMessage("§aYou joined the minigame in instance '" + instanceId + "'!");
        } else {
            player.sendMessage("§cFailed to join instance!");
        }
        
        return true;
    }
    
    private boolean handleLeave(Player player) {
        // Find player's current instance
        com.phantomworlds.models.instances.Instance instance = plugin.getInstanceManager().getPlayerInstance(player.getUniqueId());
        String instanceId = instance != null ? instance.getInstanceId() : null;
        
        if (instanceId == null) {
            player.sendMessage("§cYou are not in any instance!");
            return true;
        }
        
        // Check if minigame is active
        if (!minigameManager.hasActiveMinigame(instanceId)) {
            player.sendMessage("§cNo active minigame in your instance!");
            return true;
        }
        
        // Remove from minigame
        minigameManager.onPlayerLeave(instanceId, player);
        
        // Remove from instance
        if (plugin.getInstanceManager().removePlayerFromInstance(player.getUniqueId())) {
            player.sendMessage("§aYou left the minigame!");
            
            // Teleport to main world or lobby
            // This would teleport back to the main world
        } else {
            player.sendMessage("§cFailed to leave instance!");
        }
        
        return true;
    }
    
    private boolean handleList(Player player, String[] args) {
        MinigameCategory filterCategory = null;
        
        if (args.length > 1) {
            try {
                filterCategory = MinigameCategory.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid category! Available: " + 
                    Arrays.stream(MinigameCategory.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return true;
            }
        }
        
        player.sendMessage("§6=== Available Minigames ===");
        
        List<String> gameTypes = new ArrayList<>();
        if (filterCategory != null) {
            gameTypes.addAll(Arrays.asList(GameTypes.getGameTypesForCategory(filterCategory)));
            player.sendMessage("§7Category: " + filterCategory.getDisplayName());
        } else {
            for (MinigameCategory category : MinigameCategory.values()) {
                gameTypes.addAll(Arrays.asList(GameTypes.getGameTypesForCategory(category)));
            }
        }
        
        if (gameTypes.isEmpty()) {
            player.sendMessage("§7No minigames found.");
            return true;
        }
        
        for (String gameType : gameTypes) {
            MinigameCategory category = GameTypes.getCategory(gameType);
            String displayName = GameTypes.getDisplayName(gameType);
            player.sendMessage("§7- §e" + gameType + " §7(" + category.getDisplayName() + ") - " + displayName);
        }
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /minigame info <gameType>");
            return true;
        }
        
        String gameType = args[2].toUpperCase();
        MinigameConfig config = loadMinigameConfig(gameType);
        
        if (config == null) {
            player.sendMessage("§cUnknown game type: " + gameType);
            return true;
        }
        
        MinigameCategory category = config.getCategory();
        String displayName = GameTypes.getDisplayName(gameType);
        
        player.sendMessage("§6=== Minigame Info: " + displayName + " ===");
        player.sendMessage("§7Type: §e" + gameType);
        player.sendMessage("§7Category: §e" + category.getDisplayName());
        player.sendMessage("§7Max Players: §e" + config.getMaxPlayers());
        player.sendMessage("§7Teams: §e" + config.getTeamCount());
        
        // Show win condition
        Map<String, Object> winCondition = config.getWinCondition();
        if (winCondition != null) {
            player.sendMessage("§7Win Condition: §e" + winCondition.get("type") + " - " + winCondition.get("value"));
        }
        
        return true;
    }
    
    private boolean handleStop(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /minigame stop <instanceId>");
            return true;
        }
        
        String instanceId = args[1];
        
        // Check if minigame is active
        if (!minigameManager.hasActiveMinigame(instanceId)) {
            player.sendMessage("§cNo active minigame in instance '" + instanceId + "'!");
            return true;
        }
        
        // Stop minigame
        minigameManager.endMinigame(instanceId, "Stopped by admin");
        player.sendMessage("§aMinigame stopped in instance '" + instanceId + "'!");
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Minigame Commands ===");
        player.sendMessage("§7/minigame start <instanceId> <gameType> §f- Start a minigame");
        player.sendMessage("§7/minigame join <instanceId> §f- Join a minigame");
        player.sendMessage("§7/minigame leave §f- Leave current minigame");
        player.sendMessage("§7/minigame list [category] §f- List available minigames");
        player.sendMessage("§7/minigame info <gameType> §f- Get minigame information");
        player.sendMessage("§7/minigame stop <instanceId> §f- Stop a minigame (admin)");
        player.sendMessage("§7");
        player.sendMessage("§7Categories: " + Arrays.stream(MinigameCategory.values())
            .map(c -> "§e" + c.name()).collect(Collectors.joining("§7, ")));
    }
    
    private MinigameConfig loadMinigameConfig(String gameType) {
        try {
            // Load config from YAML file
            org.bukkit.configuration.file.YamlConfiguration config = 
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                    new java.io.File(plugin.getDataFolder(), "minigames/" + gameType.toLowerCase() + ".yml"));
            
            if (!config.contains("minigame")) {
                return null;
            }
            
            return new MinigameConfig(config.getConfigurationSection("minigame"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load minigame config for " + gameType + ": " + e.getMessage());
            return null;
        }
    }
    
    private void registerInstanceRegions(String instanceId) {
        // This would register regions from the template
        // Implementation depends on template system integration
        MinigameRegionManager regionManager = minigameManager.getRegionManager();
        
        // For now, we'll create a basic region map
        Map<com.phantomworlds.minigames.regions.MinigameRegionType, 
             com.phantomworlds.models.templates.TemplateRegion> regions = new HashMap<>();
        
        // This would be populated from template regions
        regionManager.registerInstanceRegions(instanceId, regions);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String[] subCommands = {"start", "join", "leave", "list", "info", "stop"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("start") || subCommand.equals("join") || subCommand.equals("stop")) {
                // Tab complete instance IDs
                // This would get actual instance IDs from InstanceManager
                completions.add("instance_id");
            } else if (subCommand.equals("info")) {
                // Tab complete game types
                for (String gameType : Arrays.asList(
                    GameTypes.CAPTURE_THE_FLAG, GameTypes.LAST_MAN_STANDING, 
                    GameTypes.KING_OF_THE_HILL, GameTypes.SPLEEF, GameTypes.TNT_RUN,
                    GameTypes.HOT_POTATO, GameTypes.LUCKY_BLOCK, GameTypes.MINI_BEDWARS)) {
                    if (gameType.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(gameType);
                    }
                }
            } else if (subCommand.equals("list")) {
                // Tab complete categories
                for (MinigameCategory category : MinigameCategory.values()) {
                    if (category.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(category.name());
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            // Tab complete game types for start command
            for (String gameType : Arrays.asList(
                GameTypes.CAPTURE_THE_FLAG, GameTypes.LAST_MAN_STANDING, 
                GameTypes.KING_OF_THE_HILL, GameTypes.SPLEEF, GameTypes.TNT_RUN,
                GameTypes.HOT_POTATO, GameTypes.LUCKY_BLOCK, GameTypes.MINI_BEDWARS)) {
                if (gameType.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(gameType);
                }
            }
        }
        
        return completions;
    }
}
