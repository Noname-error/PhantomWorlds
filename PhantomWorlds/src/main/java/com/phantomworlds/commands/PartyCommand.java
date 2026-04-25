package com.phantomworlds.commands;

import com.phantomworlds.PhantomWorlds;
import com.phantomworlds.managers.PartyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Party management commands
 */
public class PartyCommand implements CommandExecutor, TabCompleter {
    
    private final PhantomWorlds plugin;
    private final PartyManager partyManager;
    
    public PartyCommand(PhantomWorlds plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
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
            case "invite":
                return handleInvite(player, args);
            case "accept":
                return handleAccept(player);
            case "decline":
                return handleDecline(player);
            case "leave":
                return handleLeave(player);
            case "kick":
                return handleKick(player, args);
            case "list":
                return handleList(player);
            case "ready":
                return handleReady(player, args);
            case "start":
                return handleStart(player, args);
            case "info":
                return handleInfo(player);
            default:
                showHelp(player);
                return true;
        }
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cYou are already in a party!");
            return true;
        }
        
        String partyName = args.length > 1 ? String.join(" ", args).substring(7) : player.getName() + "'s Party";
        int maxMembers = 8; // Default max members
        
        partyManager.createParty(player.getUniqueId(), partyName, maxMembers);
        player.sendMessage("§aParty '" + partyName + "' created successfully!");
        return true;
    }
    
    private boolean handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party invite <player>");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage("§cYou cannot invite yourself!");
            return true;
        }
        
        String partyId = partyManager.getPlayerParty(player.getUniqueId()).getPartyId();
        String message = args.length > 2 ? String.join(" ", args).substring(14) : "";
        
        if (partyManager.invitePlayer(partyId, player.getUniqueId(), target.getUniqueId(), message)) {
            player.sendMessage("§aInvitation sent to " + target.getName() + "!");
        } else {
            player.sendMessage("§cFailed to send invitation!");
        }
        
        return true;
    }
    
    private boolean handleAccept(Player player) {
        if (partyManager.acceptInvite(player.getUniqueId())) {
            player.sendMessage("§aYou joined the party!");
        } else {
            player.sendMessage("§cNo pending invitations!");
        }
        return true;
    }
    
    private boolean handleDecline(Player player) {
        if (partyManager.declineInvite(player.getUniqueId())) {
            player.sendMessage("§aYou declined the invitation!");
        } else {
            player.sendMessage("§cNo pending invitations!");
        }
        return true;
    }
    
    private boolean handleLeave(Player player) {
        if (partyManager.leaveParty(player.getUniqueId())) {
            player.sendMessage("§aYou left the party!");
        } else {
            player.sendMessage("§cYou are not in a party!");
        }
        return true;
    }
    
    private boolean handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party kick <player>");
            return true;
        }
        
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return true;
        }
        
        String partyId = partyManager.getPlayerParty(player.getUniqueId()).getPartyId();
        
        if (partyManager.kickPlayer(partyId, player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage("§a" + target.getName() + " has been kicked from the party!");
        } else {
            player.sendMessage("§cFailed to kick player!");
        }
        
        return true;
    }
    
    private boolean handleList(Player player) {
        player.sendMessage("§6=== Public Parties ===");
        
        var publicParties = partyManager.getPublicParties();
        if (publicParties.isEmpty()) {
            player.sendMessage("§7No public parties available.");
            return true;
        }
        
        for (var party : publicParties) {
            player.sendMessage("§e" + party.getDescription() + " §7(" + party.getMemberCount() + "/" + party.getMaxMembers() + ")");
        }
        
        return true;
    }
    
    private boolean handleReady(Player player, String[] args) {
        boolean ready = args.length > 1 && Boolean.parseBoolean(args[1]);
        
        if (partyManager.setPlayerReady(player.getUniqueId(), ready)) {
            player.sendMessage(ready ? "§aYou are now ready!" : "§eYou are no longer ready!");
        } else {
            player.sendMessage("§cYou are not in a party!");
        }
        
        return true;
    }
    
    private boolean handleStart(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party start <template>");
            return true;
        }
        
        String templateId = args[1];
        String partyId = partyManager.getPlayerParty(player.getUniqueId()).getPartyId();
        
        if (partyManager.startInstance(partyId, templateId)) {
            player.sendMessage("§aStarting instance with template: " + templateId);
        } else {
            player.sendMessage("§cFailed to start instance!");
        }
        
        return true;
    }
    
    private boolean handleInfo(Player player) {
        var party = partyManager.getPlayerParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return true;
        }
        
        player.sendMessage("§6=== Party Info ===");
        player.sendMessage("§eName: §f" + party.getDescription());
        player.sendMessage("§eLeader: §f" + party.getLeaderId());
        player.sendMessage("§eMembers: §f" + party.getMemberCount() + "/" + party.getMaxMembers());
        player.sendMessage("§eState: §f" + party.getState());
        player.sendMessage("§eReady: §f" + party.getReadyMemberCount() + "/" + party.getMemberCount());
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Party Commands ===");
        player.sendMessage("§e/party create [name] §7- Create a new party");
        player.sendMessage("§e/party invite <player> [message] §7- Invite a player");
        player.sendMessage("§e/party accept §7- Accept invitation");
        player.sendMessage("§e/party decline §7- Decline invitation");
        player.sendMessage("§e/party leave §7- Leave current party");
        player.sendMessage("§e/party kick <player> §7- Kick a player (leader only)");
        player.sendMessage("§e/party list §7- List public parties");
        player.sendMessage("§e/party ready [true/false] §7- Set ready status");
        player.sendMessage("§e/party start <template> §7- Start instance (leader only)");
        player.sendMessage("§e/party info §7- Show party info");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String[] subCommands = {"create", "invite", "accept", "decline", "leave", "kick", "list", "ready", "start", "info"};
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "invite":
                case "kick":
                    // Tab complete player names
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                    break;
                case "ready":
                    completions.add("true");
                    completions.add("false");
                    break;
                case "start":
                    // Tab complete template names (would need template manager)
                    break;
            }
        }
        
        return completions;
    }
}
