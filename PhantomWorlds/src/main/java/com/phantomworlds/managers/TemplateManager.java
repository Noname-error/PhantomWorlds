package com.phantomworlds.managers;

import com.phantomworlds.models.instances.InstanceType;
import com.phantomworlds.models.templates.Template;
import com.phantomworlds.models.templates.TemplateMarker;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages template loading, saving, and operations
 */
public class TemplateManager {
    
    private final Map<String, Template> templates;
    private final File templatesFolder;
    private final File worldTemplatesFolder;
    
    public TemplateManager() {
        this.templates = new ConcurrentHashMap<>();
        this.templatesFolder = new File("plugins/PhantomWorlds/templates");
        this.worldTemplatesFolder = new File("plugins/PhantomWorlds/world_templates");
        
        createFolders();
        loadTemplates();
    }
    
    /**
     * Create necessary folders
     */
    private void createFolders() {
        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
        }
        if (!worldTemplatesFolder.exists()) {
            worldTemplatesFolder.mkdirs();
        }
    }
    
    /**
     * Load all templates from disk
     */
    public void loadTemplates() {
        templates.clear();
        
        // Load template configurations
        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
        }
        File[] templateFiles = templatesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (templateFiles != null) {
            for (File file : templateFiles) {
                try {
                    Template template = loadTemplateFromFile(file);
                    if (template != null) {
                        templates.put(template.getTemplateId(), template);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Failed to load template from " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        
        // Load world templates
        loadWorldTemplates();
        
        Bukkit.getLogger().info("Loaded " + templates.size() + " templates");
    }
    
    /**
     * Load templates from world folders
     */
    private void loadWorldTemplates() {
        if (!worldTemplatesFolder.exists()) {
            worldTemplatesFolder.mkdirs();
        }
        File[] worldFolders = worldTemplatesFolder.listFiles(File::isDirectory);
        if (worldFolders != null) {
            for (File worldFolder : worldFolders) {
                try {
                    Template template = loadTemplateFromWorldFolder(worldFolder);
                    if (template != null) {
                        templates.put(template.getTemplateId(), template);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Failed to load world template from " + worldFolder.getName() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Create a new template from an existing world
     */
    public Template createTemplateFromWorld(String templateId, String displayName, String worldName, InstanceType instanceType) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldName);
        }
        
        Template template = new Template(templateId, displayName, worldName, instanceType);
        
        // Create default spawn marker at world spawn
        TemplateMarker spawnMarker = TemplateMarker.createSpawn(world.getSpawnLocation());
        template.addMarker(spawnMarker);
        
        // Save template
        saveTemplate(template);
        
        // Copy world to templates folder
        copyWorldToTemplate(worldName, templateId);
        
        templates.put(templateId, template);
        return template;
    }
    
    /**
     * Save a template to disk
     */
    public boolean saveTemplate(Template template) {
        try {
            // Save template configuration
            // This would use a YAML configuration library
            // For now, this is a placeholder
            
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to save template " + template.getTemplateId() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a template
     */
    public boolean deleteTemplate(String templateId) {
        Template template = templates.remove(templateId);
        if (template == null) {
            return false;
        }
        
        // Delete template directory
        File templateDir = new File(templatesFolder, templateId);
        if (templateDir.exists()) {
            deleteDirectory(templateDir);
        }
        
        // Remove from world templates
        File worldDir = new File(worldTemplatesFolder, templateId);
        if (worldDir.exists()) {
            deleteDirectory(worldDir);
        }
        
        return true;
    }
    
    /**
     * Delete directory recursively
     */
    private void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }
        
        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to delete directory: " + directory.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Load template from file
     */
    private Template loadTemplateFromFile(File file) {
        try {
            // Simple template loading from filename
            String fileName = file.getName().replace(".yml", "");
            String templateId = fileName;
            String displayName = fileName.replace("_", " ");
            String worldName = fileName;
            InstanceType instanceType = InstanceType.DUNGEON; // Default type
            
            // Try to determine instance type from filename
            if (fileName.toLowerCase().contains("pvp") || fileName.toLowerCase().contains("arena")) {
                instanceType = InstanceType.PVP;
            } else if (fileName.toLowerCase().contains("survival")) {
                instanceType = InstanceType.SURVIVAL;
            } else if (fileName.toLowerCase().contains("creative")) {
                instanceType = InstanceType.CREATIVE;
            } else if (fileName.toLowerCase().contains("minigame")) {
                instanceType = InstanceType.MINIGAME;
            }
            
            Template template = new Template(templateId, displayName, worldName, instanceType);
            template.setAuthor("System");
            
            return template;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to load template from " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Load template from world folder
     */
    private Template loadTemplateFromWorldFolder(File worldFolder) {
        try {
            // Simple template loading from world folder name
            String folderName = worldFolder.getName();
            String templateId = folderName;
            String displayName = folderName.replace("_", " ");
            String worldName = folderName;
            InstanceType instanceType = InstanceType.DUNGEON; // Default type
            
            // Try to determine instance type from folder name
            if (folderName.toLowerCase().contains("pvp") || folderName.toLowerCase().contains("arena")) {
                instanceType = InstanceType.PVP;
            } else if (folderName.toLowerCase().contains("survival")) {
                instanceType = InstanceType.SURVIVAL;
            } else if (folderName.toLowerCase().contains("creative")) {
                instanceType = InstanceType.CREATIVE;
            } else if (folderName.toLowerCase().contains("minigame")) {
                instanceType = InstanceType.MINIGAME;
            }
            
            Template template = new Template(templateId, displayName, worldName, instanceType);
            template.setAuthor("World Folder");
            
            // Check if world folder exists and has required files
            File levelDat = new File(worldFolder, "level.dat");
            if (levelDat.exists()) {
                template.setEnabled(true);
            } else {
                template.setEnabled(false);
                Bukkit.getLogger().warning("World folder " + folderName + " missing level.dat, template disabled");
            }
            
            return template;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to load world template from " + worldFolder.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Copy world to template folder
     */
    private void copyWorldToTemplate(String worldName, String templateId) {
        Path sourcePath = Paths.get(worldName);
        Path targetPath = worldTemplatesFolder.toPath().resolve(templateId);
        
        try {
            if (Files.exists(targetPath)) {
                deleteDirectory(targetPath.toFile());
            }
            
            // Retry mechanism for session.lock issues
            int maxRetries = 3;
            boolean success = false;
            for (int retry = 0; retry < maxRetries && !success; retry++) {
                try {
                    Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
                            Files.createDirectories(targetDir);
                            return FileVisitResult.CONTINUE;
                        }
                        
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            String fileName = file.getFileName().toString();
                            
                            // Skip all Minecraft lock files and temporary files
                            if (fileName.equals("session.lock") || 
                                fileName.equals("uid.dat") ||
                                fileName.equals("uid.dat_old") ||
                                fileName.endsWith(".tmp") ||
                                fileName.startsWith("tmp_") ||
                                fileName.equals("playerdata.lock") ||
                                fileName.equals("region.lock")) {
                                Bukkit.getLogger().info("Skipping locked/temporary file: " + fileName);
                                return FileVisitResult.CONTINUE;
                            }
                            
                            Path targetFile = targetPath.resolve(sourcePath.relativize(file));
                            try {
                                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                // If file is locked, log and continue
                                if (e.getMessage().contains("gesperrt") || e.getMessage().contains("locked") || e.getMessage().contains("access denied")) {
                                    Bukkit.getLogger().warning("Skipping locked file: " + fileName + " - " + e.getMessage());
                                    return FileVisitResult.CONTINUE;
                                }
                                throw e;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    success = true;
                    break;
                } catch (IOException e) {
                    Bukkit.getLogger().warning("Retry " + (retry + 1) + " failed to copy world " + worldName + ": " + e.getMessage());
                    if (retry == maxRetries - 1) {
                        Bukkit.getLogger().severe("Failed to copy world " + worldName + " after " + maxRetries + " retries: " + e.getMessage());
                    }
                }
            }
            
            if (!success) {
                Bukkit.getLogger().warning("Failed to copy world " + worldName + " to template " + templateId + " after " + maxRetries + " retries");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to copy world " + worldName + " to template " + templateId + ": " + e.getMessage());
        }
    }
    
    /**
     * Get all templates
     */
    public Collection<Template> getAllTemplates() {
        return templates.values();
    }
    
    /**
     * Get template by ID
     */
    public Template getTemplate(String templateId) {
        return templates.get(templateId);
    }
    
    /**
     * Check if template exists
     */
    public boolean hasTemplate(String templateId) {
        return templates.containsKey(templateId);
    }
    
    /**
     * Get template statistics
     */
    public Map<InstanceType, Integer> getTemplateStatistics() {
        Map<InstanceType, Integer> stats = new EnumMap<>(InstanceType.class);
        
        for (Template template : templates.values()) {
            stats.merge(template.getInstanceType(), 1, (existing, one) -> existing + one);
        }
        
        return stats;
    }
    
    /**
     * Reload all templates
     */
    public void reloadTemplates() {
        loadTemplates();
    }
    
    /**
     * Shutdown the template manager
     */
    public void shutdown() {
        templates.clear();
        Bukkit.getLogger().info("TemplateManager shutdown successfully");
    }
    
    /**
     * Get templates by type
     */
    public List<Template> getTemplatesByType(InstanceType type) {
        return templates.values().stream()
                .filter(template -> template.getInstanceType() == type)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get enabled templates
     */
    public List<Template> getEnabledTemplates() {
        return new ArrayList<>(templates.values());
    }
    
    /**
     * Remove marker from template
     */
    public boolean removeMarkerFromTemplate(String templateId, String markerId) {
        Template template = templates.get(templateId);
        if (template != null) {
            template.removeMarker(markerId);
            return true;
        }
        return false;
    }
    
    /**
     * Remove region from template
     */
    public boolean removeRegionFromTemplate(String templateId, String regionId) {
        Template template = templates.get(templateId);
        if (template != null) {
            template.removeRegion(regionId);
            return true;
        }
        return false;
    }
}