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
            plugin.saveResource("languages/en.yml", false);
        }
        
        // Attempt to load the requested language file
        langFile = new File(langDir, currentLangCode + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + currentLangCode + ".yml' not found. Falling back to 'en.yml'.");
            langFile = defaultLangFile;
            this.currentLangCode = "en"; // Update code to reflect fallback
        }
        
        // Load the YAML configuration from the selected file
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // --- Language File Version Check ---
        String loadedLangVersion = langConfig.getString("version", "0.0.0"); // Read version from the file
        String pluginVersion = plugin.getPluginMeta().getVersion();
        
        if (!pluginVersion.equals(loadedLangVersion)) {
            plugin.getLogger().log(Level.WARNING, "*********************************************************************");
            plugin.getLogger().log(Level.WARNING, "Your language file ('" + langFile.getName() + "') version does not match the plugin version!");
            plugin.getLogger().log(Level.WARNING, "Language File Version: " + loadedLangVersion + ", Plugin Version: " + pluginVersion);
            plugin.getLogger().log(Level.WARNING, "Messages might be missing or incorrect. Consider backing up your file,");
            plugin.getLogger().log(Level.WARNING, "deleting it, and letting the plugin regenerate it, then merge your changes.");
            plugin.getLogger().log(Level.WARNING, "*********************************************************************");
            // We still load the file, but warn the user.
        }
        
        // Load messages from the file into memory
        loadMessagesFromConfig();
        
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
        if (langConfig == null) return;
        
        // Load all messages found in the user's language file
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isString(key) && !key.equals("version")) { // Exclude the version key itself
                messages.put(key, langConfig.getString(key));
            }
        }
    }

    /**
     * Loads the default messages from the en.yml file packaged within the plugin JAR.
     * Only adds messages to the map if they are not already present (preserving user overrides).
     */
    private void loadDefaultMessagesFromJar() {
        // Load the default en.yml from the JAR
        Reader defConfigStream = null;
        try {
            defConfigStream = new InputStreamReader(plugin.getResource("languages/en.yml"), StandardCharsets.UTF_8);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                // Iterate through default keys and add missing ones
                for (String key : defConfig.getKeys(true)) {
                     if (defConfig.isString(key) && !key.equals("version")) {
                        // Add to map only if the key doesn't already exist from user's file
                        messages.putIfAbsent(key, defConfig.getString(key));
                    }
                }
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
        loadLanguage(currentLangCode); // Reload the same language
        VampireMessages.reloadMessages(); // Trigger reload in VampireMessages
        return true;    
    }
    
    /**
     * Saves the current in-memory messages back to the language file.
     * Primarily used if messages are modified programmatically.
     */
    public void saveLanguage() {
        if (langFile == null || langConfig == null) {
             plugin.getLogger().warning("Cannot save language file, not properly loaded.");
             return;
        }
        // Update langConfig object with current messages before saving
        messages.forEach((key, value) -> langConfig.set(key, value));
        // Set the version string explicitly
        langConfig.set("version", plugin.getPluginMeta().getVersion());
        try {
            langConfig.save(langFile);
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