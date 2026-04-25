package com.phantomworlds.gui;

import com.phantomworlds.PhantomWorlds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages GUI menus for PhantomWorlds
 */
public class GUIManager {
    
    private final Map<UUID, GUI> openGUIs;
    
    public GUIManager(PhantomWorlds plugin) {
        this.openGUIs = new HashMap<>();
    }
    
    /**
     * Open a GUI for a player
     */
    public void openGUI(Player player, GUI gui) {
        // Close any existing GUI
        closeGUI(player);
        
        // Open new GUI
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * Close a player's GUI
     */
    public void closeGUI(Player player) {
        GUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.close(player);
        }
    }
    
    /**
     * Check if a player has an open GUI
     */
    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Get a player's current GUI
     */
    public GUI getCurrentGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * Handle inventory click
     */
    public boolean handleInventoryClick(Player player, int slot, ItemStack item) {
        GUI gui = openGUIs.get(player.getUniqueId());
        if (gui != null) {
            return gui.handleClick(player, slot, item);
        }
        return false;
    }
    
    /**
     * Handle inventory close
     */
    public void handleInventoryClose(Player player) {
        GUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.onClose(player);
        }
    }
    
    /**
     * Shutdown GUI manager
     */
    public void shutdown() {
        // Close all open GUIs
        for (UUID playerId : openGUIs.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                closeGUI(player);
            }
        }
        
        openGUIs.clear();
    }
}
