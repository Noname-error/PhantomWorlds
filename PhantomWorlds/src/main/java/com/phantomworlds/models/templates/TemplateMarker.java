package com.phantomworlds.models.templates;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a marker location within a template
 */
public class TemplateMarker {
    
    private final String markerId;
    private final String markerType;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final Map<String, Object> properties;
    
    public TemplateMarker(String markerId, String markerType, double x, double y, double z, float yaw, float pitch) {
        this.markerId = markerId;
        this.markerType = markerType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.properties = new HashMap<>();
    }
    
    public String getMarkerId() {
        return markerId;
    }
    
    public String getMarkerType() {
        return markerType;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public float getYaw() {
        return yaw;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    /**
     * Get the Bukkit Location for this marker
     */
    public Location getLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Create a marker from a Bukkit Location
     */
    public static TemplateMarker fromLocation(String markerId, String markerType, Location location) {
        return new TemplateMarker(
                markerId,
                markerType,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }
    
    /**
     * Create common marker types
     */
    public static TemplateMarker createSpawn(Location location) {
        return fromLocation(Types.SPAWN, Types.SPAWN, location);
    }
    
    public static TemplateMarker createBoss(Location location) {
        return fromLocation(Types.BOSS, Types.BOSS, location);
    }
    
    public static TemplateMarker createExit(Location location) {
        return fromLocation(Types.EXIT, Types.EXIT, location);
    }
    
    public static TemplateMarker createCheckpoint(String checkpointId, Location location) {
        return fromLocation(checkpointId, Types.CHECKPOINT, location);
    }
    
    public static TemplateMarker createTreasure(String treasureId, Location location) {
        return fromLocation(treasureId, Types.TREASURE, location);
    }
    
    public static TemplateMarker createTrigger(String triggerId, Location location) {
        return fromLocation(triggerId, Types.TRIGGER, location);
    }
    
    public static TemplateMarker createTeleport(String teleportId, Location location, String destination) {
        TemplateMarker marker = fromLocation(teleportId, Types.TELEPORT, location);
        marker.setProperty("destination", destination);
        return marker;
    }
    
    public static TemplateMarker createNPC(String npcId, Location location, String npcType) {
        TemplateMarker marker = fromLocation(npcId, Types.NPC, location);
        marker.setProperty("npc_type", npcType);
        return marker;
    }
    
    public static TemplateMarker createChest(String chestId, Location location, String chestType) {
        TemplateMarker marker = fromLocation(chestId, Types.CHEST, location);
        marker.setProperty("chest_type", chestType);
        return marker;
    }
    
    /**
     * Marker property management
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateMarker that = (TemplateMarker) o;
        return Objects.equals(markerId, that.markerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(markerId);
    }
    
    @Override
    public String toString() {
        return "TemplateMarker{" +
                "markerId='" + markerId + '\'' +
                ", markerType='" + markerType + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
    
    /**
     * Common marker type constants
     */
    public static class Types {
        public static final String SPAWN = "spawn";
        public static final String EXIT = "exit";
        public static final String BOSS = "boss";
        public static final String CHECKPOINT = "checkpoint";
        public static final String TREASURE = "treasure";
        public static final String TRIGGER = "trigger";
        public static final String TELEPORT = "teleport";
        public static final String NPC = "npc";
        public static final String CHEST = "chest";
        public static final String SPAWN_POINT = "spawn_point";
        public static final String SAFE_ZONE = "safe_zone";
        public static final String DANGER_ZONE = "danger_zone";
        public static final String PUZZLE = "puzzle";
        public static final String OBJECTIVE = "objective";
    }
}
