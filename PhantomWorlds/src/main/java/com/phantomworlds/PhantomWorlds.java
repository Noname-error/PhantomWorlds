package com.phantomworlds;

import com.phantomworlds.commands.PartyCommand;
import com.phantomworlds.commands.TemplateCommand;
import com.phantomworlds.commands.InstanceCommand;
import com.phantomworlds.gui.GUIManager;
import com.phantomworlds.listeners.PlayerListener;
import com.phantomworlds.managers.*;
import com.phantomworlds.models.instances.InstanceState;
import com.phantomworlds.models.instances.InstanceType;
import com.phantomworlds.models.parties.PartyState;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Main plugin class for PhantomWorlds
 * Modular Paper plugin for instanced worlds, dungeons, and minigames
 */
public class PhantomWorlds extends JavaPlugin {
    
    private InstanceManager instanceManager;
    private TemplateManager templateManager;
    private PartyManager partyManager;
    private CleanupManager cleanupManager;
    private GUIManager guiManager;
    
    @Override
    public void onEnable() {
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register events
        registerEvents();
        
        // Save default config
        saveDefaultConfig();
        
        // Load data
        loadData();
        
        getLogger().info("PhantomWorlds enabled successfully!");
        getLogger().info("Loaded " + templateManager.getAllTemplates().size() + " templates");
        getLogger().info("Loaded " + partyManager.getAllParties().size() + " parties");
    }
    
    @Override
    public void onDisable() {
        // Save data
        saveData();
        
        // Shutdown managers
        shutdownManagers();
        
        getLogger().info("PhantomWorlds disabled successfully!");
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        // Initialize in dependency order
        templateManager = new TemplateManager();
        instanceManager = new InstanceManager(templateManager, null, this);
        partyManager = new PartyManager(instanceManager);
        guiManager = new GUIManager(this);
        
        // Update cleanup manager with proper dependencies
        cleanupManager = new CleanupManager(instanceManager, partyManager);
        
        getLogger().info("Managers initialized successfully");
    }
    
    /**
     * Register all commands
     */
    private void registerCommands() {
        getCommand("party").setExecutor(new PartyCommand(this, partyManager));
        getCommand("template").setExecutor(new TemplateCommand(this, templateManager));
        getCommand("instance").setExecutor(new InstanceCommand(this, instanceManager));
        
        getLogger().info("Commands registered successfully");
    }
    
    /**
     * Register all event listeners
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("Event listeners registered successfully");
    }
    
    /**
     * Load plugin data
     */
    private void loadData() {
        // Load templates
        templateManager.loadTemplates();
        
        // Load instance data
        instanceManager.loadInstanceData();
        
        getLogger().info("Data loaded successfully");
    }
    
    /**
     * Save plugin data
     */
    private void saveData() {
        // Save instance data
        instanceManager.saveInstanceData();
        
        getLogger().info("Data saved successfully");
    }
    
    /**
     * Shutdown all managers
     */
    private void shutdownManagers() {
        if (partyManager != null) {
            partyManager.shutdown();
        }
        
        if (instanceManager != null) {
            instanceManager.shutdown();
        }
        
        if (templateManager != null) {
            templateManager.shutdown();
        }
        
        if (cleanupManager != null) {
            cleanupManager.shutdown();
        }
        
        if (guiManager != null) {
            guiManager.shutdown();
        }
        
        getLogger().info("Managers shutdown successfully");
    }
    
    // Getters for managers
    
    public InstanceManager getInstanceManager() {
        return instanceManager;
    }
    
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
    
    public PartyManager getPartyManager() {
        return partyManager;
    }
    
    public CleanupManager getCleanupManager() {
        return cleanupManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    /**
     * Reload plugin configuration and data
     */
    public void reloadPlugin() {
        // Reload config
        reloadConfig();
        
        // Reload data
        loadData();
        
        getLogger().info("Plugin reloaded successfully");
    }
    
    /**
     * Get plugin statistics
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== PhantomWorlds Statistics ===\n");
        
        // Instance statistics
        stats.append("Instances: ").append(instanceManager.getAllInstances().size()).append("\n");
        Map<InstanceState, Integer> instanceStatsEnum = instanceManager.getInstanceStatistics();
        Map<String, Integer> instanceStats = new HashMap<>();
        for (Map.Entry<InstanceState, Integer> entry : instanceStatsEnum.entrySet()) {
            instanceStats.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : instanceStats.entrySet()) {
            stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Template statistics
        stats.append("Templates: ").append(templateManager.getAllTemplates().size()).append("\n");
        Map<InstanceType, Integer> templateStatsEnum = templateManager.getTemplateStatistics();
        Map<String, Integer> templateStats = new HashMap<>();
        for (Map.Entry<InstanceType, Integer> entry : templateStatsEnum.entrySet()) {
            templateStats.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : templateStats.entrySet()) {
            stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Party statistics
        stats.append("Parties: ").append(partyManager.getAllParties().size()).append("\n");
        Map<PartyState, Integer> partyStatsEnum = partyManager.getPartyStatistics();
        Map<String, Integer> partyStats = new HashMap<>();
        for (Map.Entry<PartyState, Integer> entry : partyStatsEnum.entrySet()) {
            partyStats.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : partyStats.entrySet()) {
            stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Cleanup statistics
        Map<String, Object> cleanupStats = cleanupManager.getCleanupStatistics();
        stats.append("Cleanup scheduled: ").append(cleanupStats.get("scheduled_cleanup_count")).append("\n");
        
        return stats.toString();
    }
}
