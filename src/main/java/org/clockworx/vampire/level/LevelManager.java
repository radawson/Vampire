package org.clockworx.vampire.level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Manages the loading and accessing of vampire level configurations from levels.yml.
 */
public class LevelManager {

    private final VampirePlugin plugin;
    private final Map<Integer, VampireLevel> levelDataMap;
    private FileConfiguration levelConfig = null;
    private File levelConfigFile = null;

    public LevelManager(VampirePlugin plugin) {
        this.plugin = plugin;
        this.levelDataMap = new HashMap<>();
    }

    /**
     * Loads the level configurations from the levels.yml file.
     * Creates the file with defaults if it doesn't exist.
     * Parses the YAML structure and populates the levelDataMap.
     */
    public void loadLevels() {
        levelConfigFile = new File(plugin.getDataFolder(), "levels.yml");
        if (!levelConfigFile.exists()) {
            plugin.saveResource("levels.yml", false); // Copy default from JAR
            plugin.getLogger().info("Created default levels.yml. Please configure vampire levels.");
        }

        levelConfig = YamlConfiguration.loadConfiguration(levelConfigFile);

        // Look for defaults in the jar
        try (InputStream defConfigStream = plugin.getResource("levels.yml")) {
            if (defConfigStream != null) {
                levelConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
             plugin.getLogger().log(Level.SEVERE, "Could not load default levels.yml from jar", e);
        }

        levelDataMap.clear(); // Clear previous data before loading
        ConfigurationSection levelsSection = levelConfig.getConfigurationSection("levels");

        if (levelsSection == null) {
            plugin.getLogger().severe("Could not find 'levels' section in levels.yml!");
            return;
        }

        for (String levelKey : levelsSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(levelKey);
                ConfigurationSection section = levelsSection.getConfigurationSection(levelKey);
                if (section == null) {
                    plugin.getLogger().warning("Invalid configuration section for level: " + levelKey);
                    continue;
                }

                // Read values with defaults
                String description = section.getString("description", "Level " + level);
                double maxBlood = section.getDouble("max_blood", 20.0);
                double bloodRegenRate = section.getDouble("blood_regen_rate", 1.0);
                double sunModifier = section.getDouble("sun_modifier", 1.0);
                double speedBoost = section.getDouble("speed_boost", 1.0);
                boolean canUseShriek = section.getBoolean("can_use_shriek", true);
                double shriekCooldownModifier = section.getDouble("shriek_cooldown_modifier", 1.0);

                // Create and store the VampireLevel object
                VampireLevel levelData = new VampireLevel(
                    level,
                    description,
                    maxBlood,
                    bloodRegenRate,
                    sunModifier,
                    speedBoost,
                    canUseShriek,
                    shriekCooldownModifier
                    // Add other fields here...
                );
                levelDataMap.put(level, levelData);
                VampireMessages.debug("Loaded level " + level + ": " + levelData);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid level key in levels.yml: " + levelKey + ". Must be an integer.");
            } catch (Exception e) {
                 plugin.getLogger().log(Level.WARNING, "Error loading configuration for level: " + levelKey, e);
            }
        }

        plugin.getLogger().info("Loaded " + levelDataMap.size() + " vampire levels from levels.yml.");
        if (levelDataMap.isEmpty()) {
            plugin.getLogger().warning("No vampire levels were loaded. Check levels.yml format.");
        }
    }

    /**
     * Retrieves the VampireLevel data for a specific level.
     *
     * @param level The integer level number.
     * @return The {@link VampireLevel} object for the given level, or the default level data if not found.
     */
    public VampireLevel getLevelData(int level) {
        // Return specific level data or default if not found/map is empty
        return levelDataMap.getOrDefault(level, VampireLevel.defaultLevel());
    }

    /**
     * Reloads the level configurations from the levels.yml file.
     */
    public void reloadLevels() {
         plugin.getLogger().info("Reloading vampire levels from levels.yml...");
         loadLevels(); // Re-run the loading logic
    }
} 