package com.phantomworlds.models.templates;

import com.phantomworlds.models.instances.InstanceType;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a world template that can be used to create instances
 */
public class Template {
    
    private final String templateId;
    private String displayName;
    private final String description;
    private final String worldName;
    private final InstanceType instanceType;
    private final long creationTime;
    private final Map<String, TemplateMarker> markers;
    private final Map<String, TemplateRegion> regions;
    private final List<String> requiredPlugins;
    private final Map<String, Object> metadata;
    
    private boolean enabled;
    private String author;
    private String version;
    private int difficulty;
    
    public Template(String templateId, String displayName, String worldName, InstanceType instanceType) {
        this.templateId = templateId;
        this.displayName = displayName;
        this.description = "";
        this.worldName = worldName;
        this.instanceType = instanceType;
        this.creationTime = System.currentTimeMillis();
        this.markers = new ConcurrentHashMap<>();
        this.regions = new ConcurrentHashMap<>();
        this.requiredPlugins = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.enabled = true;
        this.author = "Unknown";
        this.version = "1.0.0";
        this.difficulty = 1;
    }
    
    public String getTemplateId() {
        return templateId;
    }
    
    public String getId() {
        return templateId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public InstanceType getInstanceType() {
        return instanceType;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(10, difficulty));
    }
    
    public Map<String, TemplateMarker> getMarkers() {
        return Collections.unmodifiableMap(markers);
    }
    
    public TemplateMarker getMarker(String markerId) {
        return markers.get(markerId);
    }
    
    public void addMarker(TemplateMarker marker) {
        markers.put(marker.getMarkerId(), marker);
    }
    
    public void removeMarker(String markerId) {
        markers.remove(markerId);
    }
    
    public boolean hasMarker(String markerId) {
        return markers.containsKey(markerId);
    }
    
    public Map<String, TemplateRegion> getRegions() {
        return Collections.unmodifiableMap(regions);
    }
    
    public TemplateRegion getRegion(String regionId) {
        return regions.get(regionId);
    }
    
    public void addRegion(TemplateRegion region) {
        regions.put(region.getRegionId(), region);
    }
    
    public void removeRegion(String regionId) {
        regions.remove(regionId);
    }
    
    public boolean hasRegion(String regionId) {
        return regions.containsKey(regionId);
    }
    
    public List<String> getRequiredPlugins() {
        return new ArrayList<>(requiredPlugins);
    }
    
    public void addRequiredPlugin(String plugin) {
        if (!requiredPlugins.contains(plugin)) {
            requiredPlugins.add(plugin);
        }
    }
    
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Get the spawn location for this template
     */
    public Location getSpawnLocation(World world) {
        TemplateMarker spawnMarker = getMarker("spawn");
        if (spawnMarker != null) {
            return spawnMarker.getLocation(world);
        }
        return null;
    }
    
    /**
     * Get the boss location for this template
     */
    public Location getBossLocation(World world) {
        TemplateMarker bossMarker = getMarker("boss");
        if (bossMarker != null) {
            return bossMarker.getLocation(world);
        }
        return null;
    }
    
    /**
     * Check if a location is within any region
     */
    public TemplateRegion getRegionAt(Location location) {
        for (TemplateRegion region : regions.values()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }
    
    /**
     * Validate template integrity
     */
    public boolean isValid() {
        return templateId != null && !templateId.isEmpty() &&
               displayName != null && !displayName.isEmpty() &&
               worldName != null && !worldName.isEmpty() &&
               instanceType != null &&
               enabled; // Template must be enabled
    }
    
    @Override
    public String toString() {
        return "Template{" +
                "templateId='" + templateId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", instanceType=" + instanceType +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                ", enabled=" + enabled +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(templateId, template.templateId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(templateId);
    }
}
