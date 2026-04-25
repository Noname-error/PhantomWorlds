package com.phantomworlds.minigames;

import org.bukkit.configuration.ConfigurationSection;
import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for a minigame
 */
public class MinigameConfig {
    
    private String type;
    private MinigameCategory category;
    private int maxPlayers;
    private int teamCount;
    private Map<String, Object> winCondition;
    private Map<String, Object> regions;
    private Map<String, Object> spawns;
    private Map<String, Object> modifiers;
    private Map<String, Object> eventTriggers;
    
    public MinigameConfig(ConfigurationSection section) {
        loadFromConfig(section);
    }
    
    private void loadFromConfig(ConfigurationSection section) {
        this.type = section.getString("type");
        this.category = MinigameCategory.valueOf(section.getString("category"));
        this.maxPlayers = section.getInt("maxPlayers", 10);
        this.teamCount = section.getInt("teams", 2);
        
        // Load win condition
        if (section.contains("winCondition")) {
            this.winCondition = new HashMap<>();
            ConfigurationSection winSection = section.getConfigurationSection("winCondition");
            for (String key : winSection.getKeys(false)) {
                this.winCondition.put(key, winSection.get(key));
            }
        }
        
        // Load regions
        if (section.contains("regions")) {
            this.regions = new HashMap<>();
            ConfigurationSection regionsSection = section.getConfigurationSection("regions");
            for (String key : regionsSection.getKeys(false)) {
                this.regions.put(key, regionsSection.get(key));
            }
        }
        
        // Load spawns
        if (section.contains("spawns")) {
            this.spawns = new HashMap<>();
            ConfigurationSection spawnsSection = section.getConfigurationSection("spawns");
            for (String key : spawnsSection.getKeys(false)) {
                this.spawns.put(key, spawnsSection.get(key));
            }
        }
        
        // Load modifiers
        if (section.contains("modifiers")) {
            this.modifiers = new HashMap<>();
            ConfigurationSection modifiersSection = section.getConfigurationSection("modifiers");
            for (String key : modifiersSection.getKeys(false)) {
                this.modifiers.put(key, modifiersSection.get(key));
            }
        }
        
        // Load event triggers
        if (section.contains("eventTriggers")) {
            this.eventTriggers = new HashMap<>();
            ConfigurationSection eventsSection = section.getConfigurationSection("eventTriggers");
            for (String key : eventsSection.getKeys(false)) {
                this.eventTriggers.put(key, eventsSection.get(key));
            }
        }
    }
    
    // Getters
    public String getType() { return type; }
    public MinigameCategory getCategory() { return category; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getTeamCount() { return teamCount; }
    public Map<String, Object> getWinCondition() { return winCondition; }
    public Map<String, Object> getRegions() { return regions; }
    public Map<String, Object> getSpawns() { return spawns; }
    public Map<String, Object> getModifiers() { return modifiers; }
    public Map<String, Object> getEventTriggers() { return eventTriggers; }
    
    public Object getWinConditionValue(String key) {
        return winCondition != null ? winCondition.get(key) : null;
    }
    
    public Object getRegionConfig(String regionType) {
        return regions != null ? regions.get(regionType) : null;
    }
    
    public Object getSpawnConfig(String spawnType) {
        return spawns != null ? spawns.get(spawnType) : null;
    }
    
    public Object getModifier(String modifierName) {
        return modifiers != null ? modifiers.get(modifierName) : null;
    }
    
    public Object getEventTrigger(String eventName) {
        return eventTriggers != null ? eventTriggers.get(eventName) : null;
    }
}
