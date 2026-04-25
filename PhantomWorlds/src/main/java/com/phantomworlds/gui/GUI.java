package com.phantomworlds.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Base GUI class for PhantomWorlds menus
 */
public abstract class GUI {
    
    protected final String title;
    protected final int size;
    protected Inventory inventory;
    
    public GUI(String title, int size) {
        this.title = title;
        this.size = size;
    }
    
    /**
     * Open the GUI for a player
     */
    public void open(Player player) {
        inventory = createInventory();
        player.openInventory(inventory);
    }
    
    /**
     * Close the GUI
     */
    public void close(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }
    
    /**
     * Handle inventory click
     */
    public abstract boolean handleClick(Player player, int slot, ItemStack item);
    
    /**
     * Handle GUI close
     */
    public abstract void onClose(Player player);
    
    /**
     * Create the inventory
     */
    protected abstract Inventory createInventory();
    
    /**
     * Get the GUI title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the GUI size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Check if a slot is valid
     */
    public boolean isValidSlot(int slot) {
        return slot >= 0 && slot < size;
    }
    
    /**
     * Update the inventory
     */
    public void update() {
        if (inventory != null) {
            // Rebuild inventory contents
            rebuildInventory();
        }
    }
    
    /**
     * Rebuild inventory contents
     */
    protected abstract void rebuildInventory();
}
