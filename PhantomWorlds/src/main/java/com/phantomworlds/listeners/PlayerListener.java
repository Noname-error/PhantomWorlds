package com.phantomworlds.listeners;

import com.phantomworlds.PhantomWorlds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Player event listeners for PhantomWorlds
 */
public class PlayerListener implements Listener {
    
    private final PhantomWorlds plugin;
    
    public PlayerListener(PhantomWorlds plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove player from party
        plugin.getPartyManager().leaveParty(player.getUniqueId());
        
        // Remove player from instance
        plugin.getInstanceManager().removePlayerFromInstance(player.getUniqueId());
        
        // Clear player invites
        plugin.getPartyManager().clearPlayerInvites(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        var instance = plugin.getInstanceManager().getPlayerInstance(player.getUniqueId());
        if (instance != null) {
            var instancePlayer = instance.getInstancePlayer(player.getUniqueId());
            if (instancePlayer != null) {
                instancePlayer.incrementDeaths();
            }
        }
        
        var party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());
        if (party != null) {
            var partyMember = party.getMember(player.getUniqueId());
            if (partyMember != null) {
                partyMember.incrementDeaths();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Check if teleporting out of instance
        var instance = plugin.getInstanceManager().getPlayerInstance(player.getUniqueId());
        if (instance != null) {
            // If teleporting to different world, remove from instance
            if (!event.getTo().getWorld().equals(instance.getWorld())) {
                plugin.getInstanceManager().removePlayerFromInstance(player.getUniqueId());
                player.sendMessage("§eYou left the instance by teleporting to another world!");
            }
        }
    }
}
