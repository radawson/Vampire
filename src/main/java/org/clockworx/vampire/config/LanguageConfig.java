package org.clockworx.vampire.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Manages the loading and retrieval of localized messages from language YAML files.
 * Ensures that the correct language file is loaded based on the main configuration,
 * provides default English messages if a file is missing or incomplete, and checks
 * for version mismatches between the language file and the plugin.
 */
public class LanguageConfig {
    private final VampirePlugin plugin;
    private FileConfiguration langConfig;
    private File langFile;
    private String currentLangCode; // Stores the actual language code loaded (e.g., "en")
    private final Map<String, String> messages;
    
    /**
     * Initializes the LanguageConfig.
     * 
     * @param plugin The main VampirePlugin instance.
     */
    public LanguageConfig(VampirePlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }

    /**
     * Loads the specified language file (e.g., "en", "es").
     * Ensures the default "en.yml" exists and falls back to it if the specified language is not found.
     * Also performs a version check between the loaded language file and the plugin version.
     * 
     * @param langCode The language code (e.g., "en", "es") to load.
     */
    public void loadLanguage(String langCode) {
        this.currentLangCode = langCode; // Store the requested language
        
        File langDir = setupLanguagesFolder();
        if (langDir == null) return; // Error occurred during setup
        
        // Ensure default English file exists
        File defaultLangFile = new File(langDir, "en.yml");
        
        if (!defaultLangFile.exists()) {
            // Attempt to save from JAR resources
            try {
                plugin.saveResource("languages/en.yml", false);
                
                // Verify the file was actually created
                if (defaultLangFile.exists()) {
                    plugin.getLogger().info("Created default language file en.yml from JAR resource.");
                } else {
                    // File was not created - this could indicate a file system issue
                    plugin.getLogger().log(Level.SEVERE, "*********************************************************************");
                    plugin.getLogger().log(Level.SEVERE, "Failed to create en.yml: File was not created after resource extraction.");
                    plugin.getLogger().log(Level.SEVERE, "Target location: " + defaultLangFile.getAbsolutePath());
                    plugin.getLogger().log(Level.SEVERE, "Please check file permissions and available disk space.");
                    plugin.getLogger().log(Level.SEVERE, "Language configuration may not work correctly without this file.");
                    plugin.getLogger().log(Level.SEVERE, "*********************************************************************");
                    // Don't throw - allow fallback behavior, but log the error
                }
            } catch (Exception e) {
                // Resource not found in JAR or other error
                plugin.getLogger().log(Level.SEVERE, "*********************************************************************");
                plugin.getLogger().log(Level.SEVERE, "Failed to create en.yml: Resource not found in plugin JAR or extraction failed.");
                plugin.getLogger().log(Level.SEVERE, "Expected location: plugins/Vampire/languages/en.yml");
                plugin.getLogger().log(Level.SEVERE, "Please ensure the plugin JAR contains languages/en.yml in src/main/resources/");
                plugin.getLogger().log(Level.SEVERE, "Language configuration may not work correctly without this file.");
                plugin.getLogger().log(Level.SEVERE, "*********************************************************************");
                // Don't throw - allow fallback behavior, but log the error
            }
        }
        
        // Attempt to load the requested language file
        langFile = new File(langDir, currentLangCode + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + currentLangCode + ".yml' not found. Falling back to 'en.yml'.");
            langFile = defaultLangFile;
            // Ensure the fallback file actually exists before trying to load
            if (!langFile.exists()) {
                 plugin.getLogger().severe("Fallback language file 'en.yml' also does not exist! Cannot load language configuration.");
                 return; // Cannot proceed without a language file
            }
            this.currentLangCode = "en"; // Update code to reflect fallback
        }
        
        // Load the YAML configuration from the selected file
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // --- Language File Version Check ---
        String loadedLangVersion = "error"; // Default to error string
        if (langConfig != null) { // Check if langConfig loaded successfully
             loadedLangVersion = langConfig.getString("version", "0.0.0"); // Read version from the file
        } else {
             plugin.getLogger().severe("Cannot check language version because langConfig failed to load.");
        }
        String pluginVersion = plugin.getPluginMeta().getVersion();
        
        if (!pluginVersion.equals(loadedLangVersion)) {
            plugin.getLogger().log(Level.WARNING, "*********************************************************************");
            plugin.getLogger().log(Level.WARNING, "Your language file ('" + langFile.getName() + "') version does not match the plugin version!");
            // Display the actual value read (or the error marker)
            plugin.getLogger().log(Level.WARNING, "Language File Version: " + loadedLangVersion + ", Plugin Version: " + pluginVersion);
            plugin.getLogger().log(Level.WARNING, "Messages might be missing or incorrect. Consider backing up your file,");
            plugin.getLogger().log(Level.WARNING, "deleting it, and letting the plugin regenerate it, then merge your changes.");
            plugin.getLogger().log(Level.WARNING, "*********************************************************************");
            // We still load the file, but warn the user.
        }
        
        // Load messages from the file into memory (check langConfig again)
        if (langConfig != null) {
            loadMessagesFromConfig();
        } else {
             plugin.getLogger().warning("Skipping loading messages from user file as it failed to load.");
        }
        
        // Load default messages from the JAR to fill in missing keys
        loadDefaultMessagesFromJar();
        
        // Initialize VampireMessages utility class AFTER messages are loaded
        VampireMessages.init(plugin); 

        plugin.getLogger().info("Language file loaded: " + currentLangCode + ".yml (Plugin v" + pluginVersion + ")");
    }
    
    /**
     * Sets up the languages folder and returns the directory File object.
     *
     * @return The File object for the languages directory, or null if creation failed.
     */
    private File setupLanguagesFolder() {
        File dataFolder = plugin.getDataFolder();
        
        // Ensure data folder exists
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                plugin.getLogger().severe("Could not create plugin data folder!");
                return null;
            }
        }
        
        File langDir = new File(dataFolder, "languages");
        if (!langDir.exists()) {
            if (!langDir.mkdirs()) {
                plugin.getLogger().severe("Could not create languages folder!");
                return null;
            }
        }
        return langDir;
    }

    /**
     * Loads messages from the currently loaded langConfig (the user's file) into the memory map.
     * Clears existing messages first.
     */
    private void loadMessagesFromConfig() {
        messages.clear();
        // This method is now only called if langConfig is not null
        
        // Load all messages found in the user's language file
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isConfigurationSection(key)) continue; // Skip sections
            // Check if it's a direct string or representable as one
            if (langConfig.get(key) != null) { 
                // Let Bukkit handle converting simple types to String if possible
                String value = langConfig.getString(key);
                if (value != null && !key.equals("version")) { // Exclude the version key itself
                    messages.put(key, value);
                }
            }
        }
         plugin.getLogger().info("Loaded " + messages.size() + " messages from " + langFile.getName());
    }

    /**
     * Loads the default messages from the en.yml file packaged within the plugin JAR.
     * Only adds messages to the map if they are not already present (preserving user overrides).
     */
    private void loadDefaultMessagesFromJar() {
        // Load the default en.yml from the JAR
        Reader defConfigStream = null;
        int defaultMessagesLoaded = 0;
        try {
            defConfigStream = new InputStreamReader(plugin.getResource("languages/en.yml"), StandardCharsets.UTF_8);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                // Iterate through default keys and add missing ones
                for (String key : defConfig.getKeys(true)) {
                     if (defConfig.isConfigurationSection(key)) continue; // Skip sections
                     // Check if it's a direct string or representable as one in default config
                     if (defConfig.get(key) != null) {
                        String defaultValue = defConfig.getString(key);
                         if (defaultValue != null && !key.equals("version")) {
                            // Add to map only if the key doesn't already exist from user's file
                            if (messages.putIfAbsent(key, defaultValue) == null) {
                                defaultMessagesLoaded++;
                            };
                        }
                    }
                }
            } else {
                 plugin.getLogger().warning("Could not find default languages/en.yml within the JAR.");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default language file from JAR!", e);
        } finally {
            if (defConfigStream != null) {
                try {
                    defConfigStream.close();
                } catch (IOException e) {
                    // Ignore closing error
                }
            }
        }
        plugin.getLogger().info("Loaded " + defaultMessagesLoaded + " default messages from JAR (for missing keys).");
    }

    /**
     * Reloads the current language file.
     * 
     * @return True if reload was successful, false otherwise.
     */
    public boolean reload() {
        if (currentLangCode == null) {
             plugin.getLogger().warning("Cannot reload language config, no language was initially loaded.");
             return false;
        }
        plugin.getLogger().info("Reloading language file: " + currentLangCode + ".yml");
        loadLanguage(currentLangCode); // Reload the same language
        VampireMessages.reloadMessages(); // Trigger reload in VampireMessages
        return langConfig != null; // Return true if loading succeeded
    }
    
    /**
     * Saves the current in-memory messages back to the language file.
     * Primarily used if messages are modified programmatically (e.g., in-game editor).
     * Avoids saving default messages that weren't in the original user file.
     */
    public void saveLanguage() {
        if (langFile == null || langConfig == null) {
             plugin.getLogger().warning("Cannot save language file, not properly loaded.");
             return;
        }
        // Create a new config to save, only including keys that were in the original file
        // or keys that are currently in our message map (which includes defaults for missing keys).
        // This logic might need refinement depending on desired save behavior.
        FileConfiguration configToSave = new YamlConfiguration();
        configToSave.set("version", plugin.getPluginMeta().getVersion()); // Always save current plugin version
        
        // Add all currently loaded messages to the save config
        messages.forEach(configToSave::set);

        try {
            configToSave.save(langFile);
             plugin.getLogger().info("Saved language file: " + langFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save language file to " + langFile, e);
        }
    }

    /**
     * Gets the currently loaded language code (e.g., "en").
     * 
     * @return The active language code.
     */
    public String getLanguageCode() {
        return currentLangCode;
    }
    
    /**
     * Retrieves all loaded messages.
     * 
     * @return A map containing all key-value message pairs.
     */
    public Map<String, String> getMessages() {
        return new HashMap<>(messages); // Return a copy to prevent external modification
    }
} 