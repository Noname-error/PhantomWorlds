package com.phantomworlds.commands;

import com.phantomworlds.PhantomWorlds;
import com.phantomworlds.managers.InstanceManager;
import com.phantomworlds.models.instances.Instance;
import com.phantomworlds.models.instances.InstanceState;
import com.phantomworlds.models.instances.InstanceType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

/**
 * Instance management commands
 */
public class InstanceCommand implements CommandExecutor, TabCompleter {
    
    private final PhantomWorlds plugin;
    private final InstanceManager instanceManager;
    
    public InstanceCommand(PhantomWorlds plugin, InstanceManager instanceManager) {
        this.plugin = plugin;
        this.instanceManager = instanceManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "list":
                return handleList(player, args);
            case "info":
                return handleInfo(player, args);
            case "end":
                return handleEnd(player, args);
            case "teleport":
                return handleTeleport(player, args);
            default:
                showHelp(player);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /instance create <template_id> [instance_type]");
            return true;
        }
        
        String templateId = args[1];
        InstanceType instanceType = InstanceType.DUNGEON; // Default type
        
        if (args.length > 2) {
            try {
                instanceType = InstanceType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid instance type! Available types: " + Arrays.stream(InstanceType.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return true;
            }
        }
        
        if (instanceManager.getPlayerInstance(player.getUniqueId()) != null) {
            player.sendMessage("§cYou are already in an instance!");
            return true;
        }
        
        player.sendMessage("§eCreating instance from template '" + templateId + "'...");
        
        // Check if template exists
        if (!plugin.getTemplateManager().hasTemplate(templateId)) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        try {
            instanceManager.createInstance(templateId, instanceType)
                    .thenAccept(instance -> {
                        if (instance != null) {
                            if (instanceManager.addPlayerToInstance(instance.getInstanceId(), player)) {
                                player.sendMessage("§aInstance created successfully!");
                                player.sendMessage("§7Instance ID: " + instance.getInstanceId());
                                player.sendMessage("§7Type: " + instance.getType().getDisplayName());
                                player.sendMessage("§7Max Players: " + instance.getMaxPlayers());
                            } else {
                                player.sendMessage("§cFailed to join the created instance!");
                            }
                        } else {
                            player.sendMessage("§cFailed to create instance - null returned!");
                        }
                    })
                    .exceptionally(throwable -> {
                        player.sendMessage("§cFailed to create instance: " + throwable.getMessage());
                        Bukkit.getLogger().severe("Instance creation failed: " + throwable.getMessage());
                        throwable.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            player.sendMessage("§cFailed to create instance: " + e.getMessage());
            Bukkit.getLogger().severe("Instance creation exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /instance join <instance_id>");
            return true;
        }
        
        String instanceId = args[1];
        
        if (!instanceManager.hasInstance(instanceId)) {
            player.sendMessage("§cInstance '" + instanceId + "' not found!");
            return true;
        }
        
        Instance instance = instanceManager.getInstance(instanceId);
        
        if (instance.isFull()) {
            player.sendMessage("§cInstance is full!");
            return true;
        }
        
        if (instance.getState() != InstanceState.LOBBY) {
            player.sendMessage("§cCannot join instance - it's not in lobby state!");
            return true;
        }
        
        if (instanceManager.addPlayerToInstance(instanceId, player)) {
            player.sendMessage("§aJoined instance '" + instanceId + "'!");
            player.sendMessage("§7Players: " + instance.getPlayerCount() + "/" + instance.getMaxPlayers());
        } else {
            player.sendMessage("§cFailed to join instance!");
        }
        
        return true;
    }
    
    private boolean handleLeave(Player player) {
        UUID playerId = player.getUniqueId();
        Instance currentInstance = instanceManager.getPlayerInstance(playerId);
        
        if (currentInstance == null) {
            player.sendMessage("§cYou are not in an instance!");
            return true;
        }
        
        if (instanceManager.removePlayerFromInstance(playerId)) {
            player.sendMessage("§aYou left the instance!");
            
            // Teleport back to main world
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        } else {
            player.sendMessage("§cFailed to leave instance!");
        }
        
        return true;
    }
    
    private boolean handleList(Player player, String[] args) {
        InstanceState filterState = null;
        
        if (args.length > 1) {
            try {
                filterState = InstanceState.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid instance state!");
                return true;
            }
        }
        
        List<Instance> instances;
        if (filterState != null) {
            instances = instanceManager.getInstancesByState(filterState);
            player.sendMessage("§6=== Instances (" + filterState + ") ===");
        } else {
            instances = new ArrayList<>(instanceManager.getAllInstances());
            player.sendMessage("§6=== All Instances ===");
        }
        
        if (instances.isEmpty()) {
            player.sendMessage("§7No instances found.");
            return true;
        }
        
        for (Instance instance : instances) {
            String stateColor = getStateColor(instance.getState());
            player.sendMessage(stateColor + instance.getInstanceId() + " §7- " + 
                             instance.getType().getDisplayName() + " (" + 
                             instance.getPlayerCount() + "/" + instance.getMaxPlayers() + ")");
        }
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        Instance instance;
        
        if (args.length > 1) {
            // Info about specific instance
            String instanceId = args[1];
            instance = instanceManager.getInstance(instanceId);
            
            if (instance == null) {
                player.sendMessage("§cInstance '" + instanceId + "' not found!");
                return true;
            }
        } else {
            // Info about current instance
            instance = instanceManager.getPlayerInstance(player.getUniqueId());
            
            if (instance == null) {
                player.sendMessage("§cYou are not in an instance!");
                return true;
            }
        }
        
        player.sendMessage("§6=== Instance Info ===");
        player.sendMessage("§eID: §f" + instance.getInstanceId());
        player.sendMessage("§eTemplate: §f" + instance.getTemplateId());
        player.sendMessage("§eType: §f" + instance.getType().getDisplayName());
        player.sendMessage("§eState: §f" + getStateColor(instance.getState()) + instance.getState());
        player.sendMessage("§ePlayers: §f" + instance.getPlayerCount() + "/" + instance.getMaxPlayers());
        player.sendMessage("§eCreated: §f" + formatTime(instance.getCreationTime()));
        
        if (instance.getStartTime() > 0) {
            player.sendMessage("§eStarted: §f" + formatTime(instance.getStartTime()));
            player.sendMessage("§eDuration: §f" + formatDuration(instance.getDuration()));
        }
        
        if (instance.getCurrentRegion() != null) {
            player.sendMessage("§eCurrent Region: §f" + instance.getCurrentRegion());
        }
        
        if (!instance.getModifiers().isEmpty()) {
            player.sendMessage("§eModifiers: §f" + instance.getModifiers().size());
        }
        
        return true;
    }
    
    private boolean handleEnd(Player player, String[] args) {
        Instance instance;
        
        if (args.length > 1) {
            // End specific instance (admin only)
            if (!player.hasPermission("phantomworlds.instance.admin")) {
                player.sendMessage("§cYou don't have permission to end other instances!");
                return true;
            }
            
            String instanceId = args[1];
            instance = instanceManager.getInstance(instanceId);
            
            if (instance == null) {
                player.sendMessage("§cInstance '" + instanceId + "' not found!");
                return true;
            }
        } else {
            // End current instance
            instance = instanceManager.getPlayerInstance(player.getUniqueId());
            
            if (instance == null) {
                player.sendMessage("§cYou are not in an instance!");
                return true;
            }
            
            // Check if player is leader or has admin permission
            // This would need party system integration
        }
        
        if (instanceManager.endInstance(instance.getInstanceId())) {
            player.sendMessage("§aInstance '" + instance.getInstanceId() + "' ended!");
        } else {
            player.sendMessage("§cFailed to end instance!");
        }
        
        return true;
    }
    
    private boolean handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("phantomworlds.instance.admin")) {
            player.sendMessage("§cYou don't have permission to teleport to instances!");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /instance teleport <instance_id>");
            return true;
        }
        
        String instanceId = args[1];
        Instance instance = instanceManager.getInstance(instanceId);
        
        if (instance == null) {
            player.sendMessage("§cInstance '" + instanceId + "' not found!");
            return true;
        }
        
        // Teleport to instance spawn
        com.phantomworlds.models.templates.Template template = plugin.getTemplateManager().getTemplate(instance.getTemplateId());
        if (template != null) {
            org.bukkit.Location spawnLocation = template.getSpawnLocation(instance.getWorld());
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.sendMessage("§aTeleported to instance '" + instanceId + "'!");
            } else {
                player.sendMessage("§cFailed to teleport - no spawn location found!");
            }
        } else {
            player.sendMessage("§cFailed to teleport - template not found!");
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Instance Commands ===");
        player.sendMessage("§e/instance create <template_id> [type] §7- Create new instance");
        player.sendMessage("§e/instance join <instance_id> §7- Join existing instance");
        player.sendMessage("§e/instance leave §7- Leave current instance");
        player.sendMessage("§e/instance list [state] §7- List instances");
        player.sendMessage("§e/instance info [instance_id] §7- Show instance info");
        player.sendMessage("§e/instance end [instance_id] §7- End instance");
        player.sendMessage("§e/instance teleport <instance_id> §7- Teleport to instance (admin)");
    }
    
    private String getStateColor(InstanceState state) {
        switch (state) {
            case LOBBY: return "§a";
            case RUNNING: return "§e";
            case ENDING: return "§c";
            case COMPLETED: return "§2";
            case FAILED: return "§4";
            default: return "§7";
        }
    }
    
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
    
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // alias parameter is not used but required by interface
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String[] subCommands = {"create", "join", "leave", "list", "info", "end", "teleport"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "create":
                    // Tab complete template IDs
                    for (com.phantomworlds.models.templates.Template template : plugin.getTemplateManager().getAllTemplates()) {
                        if (template.getTemplateId().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(template.getTemplateId());
                        }
                    }
                    break;
                case "join":
                case "info":
                case "end":
                case "teleport":
                    // Tab complete instance IDs
                    for (var instance : instanceManager.getAllInstances()) {
                        if (instance.getInstanceId().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(instance.getInstanceId());
                        }
                    }
                    break;
                case "list":
                    // Tab complete instance states
                    for (InstanceState state : InstanceState.values()) {
                        String stateName = state.name();
                        if (stateName.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(stateName);
                        }
                    }
                    break;
            }
        }
        
        return completions;
    }
}
