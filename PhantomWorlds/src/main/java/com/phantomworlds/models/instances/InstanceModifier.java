package com.phantomworlds.models.instances;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a gameplay modifier applied to an instance
 */
public class InstanceModifier {
    
    private final String type;
    private final String displayName;
    private final Map<String, Object> properties;
    private boolean active;
    private long startTime;
    
    public InstanceModifier(String type, String displayName) {
        this.type = type;
        this.displayName = displayName;
        this.properties = new HashMap<>();
        this.active = false;
        this.startTime = 0;
    }
    
    public String getType() {
        return type;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            this.startTime = System.currentTimeMillis();
        } else {
            this.startTime = 0;
        }
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getActiveDuration() {
        if (startTime > 0 && active) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    /**
     * Common modifier types and their default properties
     */
    public static class Types {
        public static final String HEALTH_MULTIPLIER = "health_multiplier";
        public static final String DAMAGE_MULTIPLIER = "damage_multiplier";
        public static final String SPEED_MULTIPLIER = "speed_multiplier";
        public static final String EXPERIENCE_MULTIPLIER = "experience_multiplier";
        public static final String DROP_MULTIPLIER = "drop_multiplier";
        public static final String NO_REGENERATION = "no_regeneration";
        public static final String INSTANT_KILL = "instant_kill";
        public static final String TIME_LIMIT = "time_limit";
        public static final String DIFFICULTY_SCALING = "difficulty_scaling";
    }
    
    /**
     * Create common modifier instances
     */
    public static InstanceModifier createHealthMultiplier(double multiplier) {
        InstanceModifier modifier = new InstanceModifier(Types.HEALTH_MULTIPLIER, "Health Multiplier");
        modifier.setProperty("multiplier", multiplier);
        return modifier;
    }
    
    public static InstanceModifier createDamageMultiplier(double multiplier) {
        InstanceModifier modifier = new InstanceModifier(Types.DAMAGE_MULTIPLIER, "Damage Multiplier");
        modifier.setProperty("multiplier", multiplier);
        return modifier;
    }
    
    public static InstanceModifier createSpeedMultiplier(double multiplier) {
        InstanceModifier modifier = new InstanceModifier(Types.SPEED_MULTIPLIER, "Speed Multiplier");
        modifier.setProperty("multiplier", multiplier);
        return modifier;
    }
    
    public static InstanceModifier createExperienceMultiplier(double multiplier) {
        InstanceModifier modifier = new InstanceModifier(Types.EXPERIENCE_MULTIPLIER, "Experience Multiplier");
        modifier.setProperty("multiplier", multiplier);
        return modifier;
    }
    
    public static InstanceModifier createDropMultiplier(double multiplier) {
        InstanceModifier modifier = new InstanceModifier(Types.DROP_MULTIPLIER, "Drop Multiplier");
        modifier.setProperty("multiplier", multiplier);
        return modifier;
    }
    
    public static InstanceModifier createNoRegeneration() {
        InstanceModifier modifier = new InstanceModifier(Types.NO_REGENERATION, "No Regeneration");
        modifier.setProperty("enabled", true);
        return modifier;
    }
    
    public static InstanceModifier createInstantKill() {
        InstanceModifier modifier = new InstanceModifier(Types.INSTANT_KILL, "Instant Kill");
        modifier.setProperty("enabled", true);
        return modifier;
    }
    
    public static InstanceModifier createTimeLimit(long timeLimitMs) {
        InstanceModifier modifier = new InstanceModifier(Types.TIME_LIMIT, "Time Limit");
        modifier.setProperty("time_limit_ms", timeLimitMs);
        return modifier;
    }
    
    public static InstanceModifier createDifficultyScaling(double difficulty) {
        InstanceModifier modifier = new InstanceModifier(Types.DIFFICULTY_SCALING, "Difficulty Scaling");
        modifier.setProperty("difficulty", difficulty);
        return modifier;
    }
    
    @Override
    public String toString() {
        return "InstanceModifier{" +
                "type='" + type + '\'' +
                ", displayName='" + displayName + '\'' +
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceModifier that = (InstanceModifier) o;
        return Objects.equals(type, that.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
