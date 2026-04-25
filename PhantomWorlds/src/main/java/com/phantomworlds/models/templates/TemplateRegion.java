package com.phantomworlds.models.templates;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a region within a template
 */
public class TemplateRegion {
    
    private final String regionId;
    private final String regionType;
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;
    private final Map<String, Object> properties;
    
    public TemplateRegion(String regionId, String regionType, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.regionId = regionId;
        this.regionType = regionType;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        this.properties = new HashMap<>();
    }
    
    public String getRegionId() {
        return regionId;
    }
    
    public String getRegionType() {
        return regionType;
    }
    
    public double getMinX() {
        return minX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMinZ() {
        return minZ;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public double getMaxZ() {
        return maxZ;
    }
    
    public double getWidth() {
        return maxX - minX;
    }
    
    public double getHeight() {
        return maxY - minY;
    }
    
    public double getDepth() {
        return maxZ - minZ;
    }
    
    /**
     * Check if a location is within this region
     */
    public boolean contains(Location location) {
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    /**
     * Check if coordinates are within this region
     */
    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    /**
     * Get the center location of this region
     */
    public Location getCenter(World world) {
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;
        return new Location(world, centerX, centerY, centerZ);
    }
    
    /**
     * Create a region from two corner locations
     */
    public static TemplateRegion fromLocations(String regionId, String regionType, Location loc1, Location loc2) {
        return new TemplateRegion(
                regionId,
                regionType,
                Math.min(loc1.getX(), loc2.getX()),
                Math.min(loc1.getY(), loc2.getY()),
                Math.min(loc1.getZ(), loc2.getZ()),
                Math.max(loc1.getX(), loc2.getX()),
                Math.max(loc1.getY(), loc2.getY()),
                Math.max(loc1.getZ(), loc2.getZ())
        );
    }
    
    /**
     * Create a cuboid region centered on a location
     */
    public static TemplateRegion createCuboid(String regionId, String regionType, Location center, double radiusX, double radiusY, double radiusZ) {
        return new TemplateRegion(
                regionId,
                regionType,
                center.getX() - radiusX,
                center.getY() - radiusY,
                center.getZ() - radiusZ,
                center.getX() + radiusX,
                center.getY() + radiusY,
                center.getZ() + radiusZ
        );
    }
    
    /**
     * Create a cube region centered on a location
     */
    public static TemplateRegion createCube(String regionId, String regionType, Location center, double radius) {
        return createCuboid(regionId, regionType, center, radius, radius, radius);
    }
    
    /**
     * Create common region types
     */
    public static TemplateRegion createSpawnZone(Location center, double radius) {
        return createCube("spawn_zone", Types.SPAWN_ZONE, center, radius);
    }
    
    public static TemplateRegion createBossArena(Location center, double radiusX, double radiusY, double radiusZ) {
        return createCuboid("boss_arena", Types.BOSS_ARENA, center, radiusX, radiusY, radiusZ);
    }
    
    public static TemplateRegion createSafeZone(Location center, double radius) {
        return createCube("safe_zone", Types.SAFE_ZONE, center, radius);
    }
    
    public static TemplateRegion createDangerZone(Location center, double radius) {
        return createCube("danger_zone", Types.DANGER_ZONE, center, radius);
    }
    
    public static TemplateRegion createPuzzleZone(String regionId, Location center, double radius) {
        return createCube(regionId, Types.PUZZLE_ZONE, center, radius);
    }
    
    public static TemplateRegion createPvPZone(Location center, double radius) {
        return createCube("pvp_zone", Types.PVP_ZONE, center, radius);
    }
    
    /**
     * Region property management
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
    
    /**
     * Get the volume of this region
     */
    public double getVolume() {
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateRegion that = (TemplateRegion) o;
        return Objects.equals(regionId, that.regionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(regionId);
    }
    
    @Override
    public String toString() {
        return "TemplateRegion{" +
                "regionId='" + regionId + '\'' +
                ", regionType='" + regionType + '\'' +
                ", volume=" + getVolume() +
                '}';
    }
    
    /**
     * Common region type constants
     */
    public static class Types {
        public static final String SPAWN_ZONE = "spawn_zone";
        public static final String BOSS_ARENA = "boss_arena";
        public static final String SAFE_ZONE = "safe_zone";
        public static final String DANGER_ZONE = "danger_zone";
        public static final String PUZZLE_ZONE = "puzzle_zone";
        public static final String PVP_ZONE = "pvp_zone";
        public static final String OBJECTIVE_ZONE = "objective_zone";
        public static final String RESTRICTED_ZONE = "restricted_zone";
        public static final String TRIGGER_ZONE = "trigger_zone";
        public static final String CHECKPOINT_ZONE = "checkpoint_zone";
        public static final String TREASURE_ZONE = "treasure_zone";
    }
}
