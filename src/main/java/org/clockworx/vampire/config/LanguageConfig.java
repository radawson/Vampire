package org.clockworx.vampire.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.clockworx.vampire.VampirePlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageConfig {
    private final VampirePlugin plugin;
    private FileConfiguration langConfig;
    private File langFile;
    private String language;
    private final Map<String, String> messages;
    
    public LanguageConfig(VampirePlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }
    
    public void loadLanguage(String language) {
        this.language = language;
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        
        // Create languages directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists()) {
            langDir.mkdir();
        }
        
        // Load default language file if it doesn't exist
        File defaultLangFile = new File(langDir, "en.yml");
        if (!defaultLangFile.exists()) {
            plugin.saveResource("languages/en.yml", false);
        }
        
        // Load the requested language file
        langFile = new File(langDir, language + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + language + ".yml not found, using English");
            langFile = defaultLangFile;
            this.language = "en";
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        loadMessages();
        
        plugin.getLogger().info("Language file loaded: " + this.language);
    }
    
    private void loadMessages() {
        messages.clear();
        
        // Load all messages from the configuration
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isString(key)) {
                messages.put(key, langConfig.getString(key));
            }
        }
        
        // Load default messages if some are missing
        loadDefaultMessages();
    }
    
    private void loadDefaultMessages() {
        // General messages
        setDefaultMessage("general.prefix", "&8[&cVampire&8]");
        setDefaultMessage("general.reload", "&aPlugin reloaded successfully!");
        setDefaultMessage("general.no-permission", "&cYou don't have permission to do that!");
        
        // Player messages
        setDefaultMessage("player.not-found", "&cPlayer not found!");
        setDefaultMessage("player.offline", "&cPlayer is offline!");
        
        // Vampire messages
        setDefaultMessage("vampire.status", "&7Vampire Status: &c%status%");
        setDefaultMessage("vampire.blood", "&7Blood Level: &c%blood%");
        setDefaultMessage("vampire.infected", "&7Infected: &c%infected%");
        setDefaultMessage("vampire.night-vision", "&7Night Vision: &c%enabled%");
        setDefaultMessage("vampire.sun-damage", "&7Sun Damage: &c%enabled%");
        
        // Infection messages
        setDefaultMessage("infection.start", "&cYou have been infected!");
        setDefaultMessage("infection.end", "&aYou have been cured of infection!");
        setDefaultMessage("infection.spread", "&c%player% has been infected!");
        
        // Blood messages
        setDefaultMessage("blood.low", "&cYour blood level is low!");
        setDefaultMessage("blood.regen", "&aYou regenerated %amount% blood");
        setDefaultMessage("blood.offer", "&aYou offered %amount% blood to %player%");
        setDefaultMessage("blood.receive", "&aYou received %amount% blood from %player%");
        
        // Altar messages
        setDefaultMessage("altar.dark.create", "&8Dark Altar created!");
        setDefaultMessage("altar.dark.destroy", "&8Dark Altar destroyed!");
        setDefaultMessage("altar.light.create", "&fLight Altar created!");
        setDefaultMessage("altar.light.destroy", "&fLight Altar destroyed!");
        setDefaultMessage("altar.cure", "&aYou have been cured at the Light Altar!");
        setDefaultMessage("altar.infect", "&cYou have been infected at the Dark Altar!");
        
        // Command messages
        setDefaultMessage("command.help", "&7=== &cVampire Commands &7===");
        setDefaultMessage("command.help.help", "&7/vampire help &8- &fShow help");
        setDefaultMessage("command.help.version", "&7/vampire version &8- &fShow version");
        setDefaultMessage("command.help.show", "&7/vampire show [player] &8- &fShow vampire status");
        setDefaultMessage("command.help.set", "&7/vampire set <type> <player> [value] &8- &fSet vampire properties");
        setDefaultMessage("command.help.offer", "&7/vampire offer <player> <amount> &8- &fOffer blood to a player");
        setDefaultMessage("command.help.reload", "&7/vampire reload &8- &fReload the plugin");
    }

    public boolean reload() {
        loadLanguage(language);
        return true;    
    }
    
    private void setDefaultMessage(String key, String defaultValue) {
        if (!messages.containsKey(key)) {
            messages.put(key, defaultValue);
        }
    }
    
    public void saveLanguage() {
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save language file to " + langFile, e);
        }
    }
    
    public String getMessage(String key) {
        return getMessage(key, new String[0]);
    }
    
    public String getMessage(String key, String... args) {
        String message = messages.getOrDefault(key, "Missing message: " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                message = message.replace("%" + (i + 1) + "%", args[i]);
            }
        }
        
        return message;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
        loadLanguage(language);
    }
    
    public Map<String, String> getMessages() {
        return new HashMap<>(messages);
    }
    
    public void setMessage(String key, String value) {
        messages.put(key, value);
        langConfig.set(key, value);
    }
} 