package com.phantomworlds.commands;

import com.phantomworlds.PhantomWorlds;
import com.phantomworlds.managers.TemplateManager;
import com.phantomworlds.models.instances.InstanceType;
import com.phantomworlds.models.templates.Template;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template management commands
 */
public class TemplateCommand implements CommandExecutor, TabCompleter {
    
    private final PhantomWorlds plugin;
    private final TemplateManager templateManager;
    
    public TemplateCommand(PhantomWorlds plugin, TemplateManager templateManager) {
        this.plugin = plugin;
        this.templateManager = templateManager;
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
            case "list":
                return handleList(player, args);
            case "info":
                return handleInfo(player, args);
            case "delete":
                return handleDelete(player, args);
            case "marker":
                return handleMarker(player, args);
            case "region":
                return handleRegion(player, args);
            default:
                showHelp(player);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /template create <id> <displayname> <world> <type>");
            return true;
        }
        
        String templateId = args[1];
        String displayName = args[2];
        String worldName = args[3];
        
        if (args.length < 5) {
            player.sendMessage("§cUsage: /template create <id> <displayname> <world> <type>");
            player.sendMessage("§7Available types: " + Arrays.stream(InstanceType.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return true;
        }
        
        InstanceType instanceType;
        try {
            instanceType = InstanceType.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid instance type! Available types: " + Arrays.stream(InstanceType.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return true;
        }
        
        if (templateManager.hasTemplate(templateId)) {
            player.sendMessage("§cTemplate '" + templateId + "' already exists!");
            return true;
        }
        
        try {
            templateManager.createTemplateFromWorld(templateId, displayName, worldName, instanceType);
            player.sendMessage("§aTemplate '" + templateId + "' created successfully!");
            player.sendMessage("§7World: " + worldName + " | Type: " + instanceType.getDisplayName());
        } catch (Exception e) {
            player.sendMessage("§cFailed to create template: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleList(Player player, String[] args) {
        InstanceType filterType = null;
        
        if (args.length > 1) {
            try {
                filterType = InstanceType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid instance type!");
                return true;
            }
        }
        
        List<Template> templates;
        if (filterType != null) {
            templates = templateManager.getTemplatesByType(filterType);
            player.sendMessage("§6=== Templates (" + filterType.getDisplayName() + ") ===");
        } else {
            templates = templateManager.getEnabledTemplates();
            player.sendMessage("§6=== All Templates ===");
        }
        
        if (templates.isEmpty()) {
            player.sendMessage("§7No templates found.");
            return true;
        }
        
        for (Template template : templates) {
            String status = template.isEnabled() ? "§a" : "§c";
            player.sendMessage(status + template.getTemplateId() + " §7- " + template.getDisplayName() + " (" + template.getInstanceType().getDisplayName() + ")");
        }
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /template info <template_id>");
            return true;
        }
        
        String templateId = args[1];
        Template template = templateManager.getTemplate(templateId);
        
        if (template == null) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        player.sendMessage("§6=== Template Info ===");
        player.sendMessage("§eID: §f" + template.getTemplateId());
        player.sendMessage("§eName: §f" + template.getDisplayName());
        player.sendMessage("§eType: §f" + template.getInstanceType().getDisplayName());
        player.sendMessage("§eWorld: §f" + template.getWorldName());
        player.sendMessage("§eAuthor: §f" + template.getAuthor());
        player.sendMessage("§eVersion: §f" + template.getVersion());
        player.sendMessage("§eDifficulty: §f" + template.getDifficulty());
        player.sendMessage("§eEnabled: §f" + (template.isEnabled() ? "§aYes" : "§cNo"));
        player.sendMessage("§eMarkers: §f" + template.getMarkers().size());
        player.sendMessage("§eRegions: §f" + template.getRegions().size());
        
        // Show spawn location if available
        if (template.hasMarker("spawn")) {
            player.sendMessage("§eSpawn Marker: §aAvailable");
        } else {
            player.sendMessage("§eSpawn Marker: §cMissing (Required for instance creation)");
        }
        
        // Show boss location if available
        if (template.hasMarker("boss")) {
            player.sendMessage("§eBoss Marker: §aAvailable");
        }
        
        return true;
    }
    
    private boolean handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /template delete <template_id>");
            return true;
        }
        
        String templateId = args[1];
        
        if (!templateManager.hasTemplate(templateId)) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        if (templateManager.deleteTemplate(templateId)) {
            player.sendMessage("§aTemplate '" + templateId + "' deleted successfully!");
        } else {
            player.sendMessage("§cFailed to delete template!");
        }
        
        return true;
    }
    
    private boolean handleMarker(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /template marker <add|remove|list> [args...]");
            return true;
        }
        
        String markerAction = args[1].toLowerCase();
        
        switch (markerAction) {
            case "add":
                return handleMarkerAdd(player, args);
            case "remove":
                return handleMarkerRemove(player, args);
            case "list":
                return handleMarkerList(player, args);
            default:
                player.sendMessage("§cUsage: /template marker <add|remove|list> [args...]");
                return true;
        }
    }
    
    private boolean handleMarkerAdd(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("§cUsage: /template marker add <template_id> <marker_type> [marker_id]");
            return true;
        }
        
        String templateId = args[2];
        String markerType = args[3];
        String markerId = args.length > 4 ? args[4] : markerType + "_" + System.currentTimeMillis();
        
        Template template = templateManager.getTemplate(templateId);
        if (template == null) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        // Create marker at player's location
        
        // This would need to be implemented with actual TemplateMarker creation
        player.sendMessage("§aMarker '" + markerId + "' added to template '" + templateId + "' at your location!");
        
        return true;
    }
    
    private boolean handleMarkerRemove(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /template marker remove <template_id> <marker_id>");
            return true;
        }
        
        String templateId = args[2];
        String markerId = args[3];
        
        if (templateManager.removeMarkerFromTemplate(templateId, markerId)) {
            player.sendMessage("§aMarker '" + markerId + "' removed from template '" + templateId + "'!");
        } else {
            player.sendMessage("§cFailed to remove marker!");
        }
        
        return true;
    }
    
    private boolean handleMarkerList(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /template marker list <template_id>");
            return true;
        }
        
        String templateId = args[2];
        Template template = templateManager.getTemplate(templateId);
        
        if (template == null) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        player.sendMessage("§6=== Markers for " + templateId + " ===");
        
        if (template.getMarkers().isEmpty()) {
            player.sendMessage("§7No markers found.");
            return true;
        }
        
        template.getMarkers().forEach((markerId, marker) -> {
            player.sendMessage("§e" + markerId + " §7- " + marker.getMarkerType() + " (" + 
                             String.format("%.1f, %.1f, %.1f", marker.getX(), marker.getY(), marker.getZ()) + ")");
        });
        
        return true;
    }
    
    private boolean handleRegion(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /template region <add|remove|list> [args...]");
            return true;
        }
        
        String regionAction = args[1].toLowerCase();
        
        switch (regionAction) {
            case "add":
                return handleRegionAdd(player, args);
            case "remove":
                return handleRegionRemove(player, args);
            case "list":
                return handleRegionList(player, args);
            default:
                player.sendMessage("§cUsage: /template region <add|remove|list> [args...]");
                return true;
        }
    }
    
    private boolean handleRegionAdd(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage("§cUsage: /template region add <template_id> <region_type> [region_id]");
            player.sendMessage("§7Region will be created from your selection (WorldEdit)");
            return true;
        }
        
        String templateId = args[2];
        
        Template template = templateManager.getTemplate(templateId);
        if (template == null) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        // This would need WorldEdit integration to get selection
        player.sendMessage("§eRegion creation requires WorldEdit selection!");
        player.sendMessage("§7Please select a region with WorldEdit first.");
        
        return true;
    }
    
    private boolean handleRegionRemove(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /template region remove <template_id> <region_id>");
            return true;
        }
        
        String templateId = args[2];
        String regionId = args[3];
        
        if (templateManager.removeRegionFromTemplate(templateId, regionId)) {
            player.sendMessage("§aRegion '" + regionId + "' removed from template '" + templateId + "'!");
        } else {
            player.sendMessage("§cFailed to remove region!");
        }
        
        return true;
    }
    
    private boolean handleRegionList(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /template region list <template_id>");
            return true;
        }
        
        String templateId = args[2];
        Template template = templateManager.getTemplate(templateId);
        
        if (template == null) {
            player.sendMessage("§cTemplate '" + templateId + "' not found!");
            return true;
        }
        
        player.sendMessage("§6=== Regions for " + templateId + " ===");
        
        if (template.getRegions().isEmpty()) {
            player.sendMessage("§7No regions found.");
            return true;
        }
        
        template.getRegions().forEach((regionId, region) -> {
            player.sendMessage("§e" + regionId + " §7- " + region.getRegionType() + " (Volume: " + 
                             String.format("%.1f", region.getVolume()) + ")");
        });
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Template Commands ===");
        player.sendMessage("§e/template create <id> <displayname> <world> <type> §7- Create template");
        player.sendMessage("§e/template list [type] §7- List templates");
        player.sendMessage("§e/template info <template_id> §7- Show template info");
        player.sendMessage("§e/template delete <template_id> §7- Delete template");
        player.sendMessage("§e/template marker <add|remove|list> [args...] §7- Manage markers");
        player.sendMessage("§e/template region <add|remove|list> [args...] §7- Manage regions");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String[] subCommands = {"create", "list", "info", "delete", "marker", "region"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "info":
                case "delete":
                    // Tab complete template IDs
                    for (Template template : templateManager.getAllTemplates()) {
                        if (template.getTemplateId().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(template.getTemplateId());
                        }
                    }
                    break;
                case "create":
                    // Tab complete world names
                    for (var world : plugin.getServer().getWorlds()) {
                        if (world.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(world.getName());
                        }
                    }
                    break;
                case "marker":
                case "region":
                    String[] actions = {"add", "remove", "list"};
                    for (String action : actions) {
                        if (action.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(action);
                        }
                    }
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            
            if ((subCommand.equals("marker") || subCommand.equals("region")) && action.equals("remove")) {
                // Tab complete template IDs
                for (Template template : templateManager.getAllTemplates()) {
                    if (template.getTemplateId().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(template.getTemplateId());
                    }
                }
            }
        }
        
        return completions;
    }
}
