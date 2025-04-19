package org.clockworx.vampire.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.clockworx.vampire.VampirePlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VampireConfig {
    private final VampirePlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    // Database settings
    private String databaseType;
    private Map<String, Object> databaseConfig;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    
    // General settings
    private boolean debug;
    private String language;
    private boolean saveOnQuit;
    private boolean autoSave;
    private int autoSaveInterval;
    
    // Vampire settings
    private double maxBlood;
    private double bloodRegenRate;
    private boolean nightVision;
    private boolean sunDamage;
    private double sunDamageAmount;
    private boolean canInfect;
    private double infectionChance;
    private int infectionDuration;
    private boolean canOfferBlood;
    private double maxBloodOffer;
    private double bloodlustThreshold;
    private double bloodlustBloodDecrease;
    private int taskDelay;
    private double infectionRate;
    private double sunlightDamage;
    private double bloodDecreaseRate;
    private double lowBloodThreshold;
    
    // Altar settings
    private boolean altarsEnabled;
    private int altarSearchRadius;
    private double altarMinRatio;
    private Map<String, Object> darkAltarConfig;
    private Map<String, Object> lightAltarConfig;
    
    // Block opacity settings
    private Map<Material, Double> blockOpacity;
    private double opacityPerArmorPiece;
    
    // Night vision settings
    private boolean nightVisionEnabled;
    private int nightVisionLevel;
    
    // Bloodlust settings
    private int bloodlustDuration;
    private double bloodlustDamageBoost;
    private double bloodlustSpeedBoost;
    private int bloodlustCooldown;
    
    public VampireConfig(VampirePlugin plugin) {
        this.plugin = plugin;
        this.config = null;
        this.databaseType = "sqlite";
        this.databaseConfig = new HashMap<>();
        this.darkAltarConfig = new HashMap<>();
        this.lightAltarConfig = new HashMap<>();
        this.blockOpacity = new HashMap<>();
        loadConfig();
    }
    
    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load database settings
        loadDatabaseSettings();
        
        // Load general settings
        loadGeneralSettings();
        
        // Load vampire settings
        loadVampireSettings();
        
        // Load night vision settings
        loadNightVisionSettings();
        
        // Load bloodlust settings
        loadBloodlustSettings();
        
        // Load altar settings
        loadAltarSettings();
        
        // Load block opacity settings
        loadBlockOpacitySettings();
        
        plugin.getLogger().info("Configuration loaded successfully");
    }
    
    private void loadDefaults() {
        config.addDefault("vampire.effects.vampire.type", "INCREASE_DAMAGE");
        config.addDefault("vampire.effects.vampire.duration", 100000);
        config.addDefault("vampire.effects.vampire.amplifier", 1);
        
        config.addDefault("vampire.effects.bloodlust.type", "SPEED");
        config.addDefault("vampire.effects.bloodlust.duration", 100000);
        config.addDefault("vampire.effects.bloodlust.amplifier", 2);
        
        config.addDefault("vampire.effects.nightvision.type", "NIGHT_VISION");
        config.addDefault("vampire.effects.nightvision.duration", 100000);
        config.addDefault("vampire.effects.nightvision.amplifier", 0);
        
        config.addDefault("vampire.effects.infected.type", "CONFUSION");
        config.addDefault("vampire.effects.infected.duration", 100000);
        config.addDefault("vampire.effects.infected.amplifier", 0);
        
        config.addDefault("vampire.effects.human.type", "DAMAGE_RESISTANCE");
        config.addDefault("vampire.effects.human.duration", 100000);
        config.addDefault("vampire.effects.human.amplifier", 0);
        
        config.addDefault("vampire.combat.damageFactor.withBloodlust", 2.0);
        config.addDefault("vampire.combat.damageFactor.withoutBloodlust", 1.5);
        config.addDefault("vampire.combat.infectRisk.withIntent", 0.3);
        config.addDefault("vampire.combat.infectRisk.withoutIntent", 0.1);
        
        config.addDefault("vampire.shriek.cooldown", 30000);
        config.addDefault("vampire.shriek.sound", "ENTITY_ENDER_DRAGON_GROWL");
        
        config.addDefault("vampire.trade.offerTolerance", 30000);
        config.addDefault("vampire.trade.maxDistance", 5.0);
        
        config.addDefault("vampire.truce.breakTime", 300000);
        
        config.addDefault("debug", false);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    private void loadDatabaseSettings() {
        ConfigurationSection dbSection = config.getConfigurationSection("database");
        if (dbSection == null) {
            plugin.getLogger().warning("Database section not found in config.yml, using defaults");
            databaseType = "sqlite";
            databaseUrl = "jdbc:sqlite:plugins/Vampire/database.db";
            databaseUser = "";
            databasePassword = "";
            return;
        }
        
        databaseType = dbSection.getString("type", "sqlite");
        databaseUrl = dbSection.getString("url", "jdbc:sqlite:plugins/Vampire/database.db");
        databaseUser = dbSection.getString("user", "");
        databasePassword = dbSection.getString("password", "");
        
        // Load config
        databaseConfig = dbSection.getValues(false);
    }
    
    private void loadGeneralSettings() {
        ConfigurationSection generalSection = config.getConfigurationSection("general");
        if (generalSection == null) {
            plugin.getLogger().warning("General section not found in config.yml, using defaults");
            debug = false;
            language = "en";
            saveOnQuit = true;
            autoSave = true;
            autoSaveInterval = 5;
            return;
        }
        
        debug = generalSection.getBoolean("debug", false);
        language = generalSection.getString("language", "en");
        saveOnQuit = generalSection.getBoolean("save-on-quit", true);
        autoSave = generalSection.getBoolean("auto-save", true);
        autoSaveInterval = generalSection.getInt("auto-save-interval", 5);
    }
    
    private void loadVampireSettings() {
        ConfigurationSection vampireSection = config.getConfigurationSection("vampire");
        if (vampireSection == null) {
            plugin.getLogger().warning("Vampire section not found in config.yml, using defaults");
            setDefaultVampireSettings();
            return;
        }
        
        // Load and validate settings
        maxBlood = validatePositiveDouble(vampireSection, "max-blood", 20.0);
        bloodRegenRate = validatePositiveDouble(vampireSection, "blood-regen-rate", 0.1);
        nightVision = vampireSection.getBoolean("night-vision", true);
        sunDamage = vampireSection.getBoolean("sun-damage", true);
        sunDamageAmount = validatePositiveDouble(vampireSection, "sun-damage-amount", 1.0);
        canInfect = vampireSection.getBoolean("can-infect", true);
        infectionChance = validatePercentage(vampireSection, "infection-chance", 0.25);
        infectionDuration = validatePositiveInteger(vampireSection, "infection-duration", 60);
        canOfferBlood = vampireSection.getBoolean("can-offer-blood", true);
        maxBloodOffer = validatePositiveDouble(vampireSection, "max-blood-offer", 5.0);
        bloodlustThreshold = validatePositiveDouble(vampireSection, "bloodlust-threshold", 15.0);
        bloodlustBloodDecrease = validatePositiveDouble(vampireSection, "bloodlust-blood-decrease", 0.1);
        taskDelay = validatePositiveInteger(vampireSection, "task-delay", 20);
        infectionRate = validatePositiveDouble(vampireSection, "infection-rate", 0.1);
        sunlightDamage = validatePositiveDouble(vampireSection, "sunlight-damage", 1.0);
        bloodDecreaseRate = validatePositiveDouble(vampireSection, "blood-decrease-rate", 0.05);
        lowBloodThreshold = validatePositiveDouble(vampireSection, "low-blood-threshold", 2.0);
    }

    public boolean reload() {
        loadConfig();
        return true;    
    }
    
    private void setDefaultVampireSettings() {
        maxBlood = 20.0;
        bloodRegenRate = 0.1;
        nightVision = true;
        sunDamage = true;
        sunDamageAmount = 1.0;
        canInfect = true;
        infectionChance = 0.25;
        infectionDuration = 60;
        canOfferBlood = true;
        maxBloodOffer = 5.0;
        bloodlustThreshold = 15.0;
        bloodlustBloodDecrease = 0.1;
        taskDelay = 20;
        infectionRate = 0.1;
        sunlightDamage = 1.0;
    }
    
    private double validatePositiveDouble(ConfigurationSection section, String path, double defaultValue) {
        double value = section.getDouble(path, defaultValue);
        if (value <= 0) {
            plugin.getLogger().warning("Invalid value for " + path + ": " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
    
    private double validatePercentage(ConfigurationSection section, String path, double defaultValue) {
        double value = section.getDouble(path, defaultValue);
        if (value < 0 || value > 1) {
            plugin.getLogger().warning("Invalid percentage for " + path + ": " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
    
    private int validatePositiveInteger(ConfigurationSection section, String path, int defaultValue) {
        int value = section.getInt(path, defaultValue);
        if (value <= 0) {
            plugin.getLogger().warning("Invalid value for " + path + ": " + value + ". Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
    }
    
    private void loadAltarSettings() {
        ConfigurationSection altarSection = config.getConfigurationSection("altar");
        if (altarSection == null) {
            plugin.getLogger().warning("Altar section not found in config.yml, using defaults");
            altarsEnabled = true;
            altarSearchRadius = 3;
            altarMinRatio = 0.5;
            darkAltarConfig = new HashMap<>();
            lightAltarConfig = new HashMap<>();
            return;
        }
        
        altarsEnabled = altarSection.getBoolean("enabled", true);
        altarSearchRadius = altarSection.getInt("search-radius", 3);
        altarMinRatio = altarSection.getDouble("min-ratio", 0.5);
        
        // Load dark altar config
        ConfigurationSection darkSection = altarSection.getConfigurationSection("dark");
        if (darkSection != null) {
            darkAltarConfig = darkSection.getValues(true);
        }
        
        // Load light altar config
        ConfigurationSection lightSection = altarSection.getConfigurationSection("light");
        if (lightSection != null) {
            lightAltarConfig = lightSection.getValues(true);
        }
    }
    
    private void loadBlockOpacitySettings() {
        ConfigurationSection opacitySection = config.getConfigurationSection("block-opacity");
        if (opacitySection == null) {
            plugin.getLogger().warning("Block opacity section not found in config.yml, using defaults");
            setDefaultBlockOpacitySettings();
            return;
        }
        
        opacityPerArmorPiece = opacitySection.getDouble("opacity-per-armor-piece", 0.1);
        
        ConfigurationSection blocksSection = opacitySection.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String materialName : blocksSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    double opacity = blocksSection.getDouble(materialName);
                    blockOpacity.put(material, opacity);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in block opacity config: " + materialName);
                }
            }
        }
    }
    
    private void setDefaultBlockOpacitySettings() {
        opacityPerArmorPiece = 0.1;
        blockOpacity.put(Material.GLASS, 0.3);
        blockOpacity.put(Material.GLASS_PANE, 0.3);
        blockOpacity.put(Material.ICE, 0.5);
        blockOpacity.put(Material.WATER, 0.7);
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }
    
    // Getters for database settings
    public String getDatabaseType() {
        return databaseType;
    }
    
    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    public String getDatabaseUser() {
        return databaseUser;
    }
    
    public String getDatabasePassword() {
        return databasePassword;
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public Map<String, Object> getDatabaseConfig() {
        return databaseConfig;
    }
    
    // Getters for general settings
    public boolean isDebug() {
        return debug;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public boolean isSaveOnQuit() {
        return saveOnQuit;
    }
    
    public boolean isAutoSave() {
        return autoSave;
    }
    
    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }
    
    // Getters for vampire settings
    public double getMaxBlood() {
        return maxBlood;
    }
    
    public double getBloodRegenRate() {
        return bloodRegenRate;
    }
    
    public boolean isNightVision() {
        return nightVision;
    }
    
    public boolean isSunDamage() {
        return sunDamage;
    }
    
    public double getSunDamageAmount() {
        return sunDamageAmount;
    }
    
    public boolean isCanInfect() {
        return canInfect;
    }
    
    public double getInfectionChance() {
        return infectionChance;
    }
    
    public int getInfectionDuration() {
        return infectionDuration;
    }
    
    public boolean isCanOfferBlood() {
        return canOfferBlood;
    }
    
    public double getMaxBloodOffer() {
        return maxBloodOffer;
    }
    
    public double getBloodlustThreshold() {
        return bloodlustThreshold;
    }
    
    public double getBloodlustBloodDecrease() {
        return bloodlustBloodDecrease;
    }
    
    public int getTaskDelay() {
        return taskDelay;
    }
    
    public double getInfectionRate() {
        return infectionRate;
    }
    
    public double getSunlightDamage() {
        return sunlightDamage;
    }
    
    // Getters for altar settings
    public boolean isAltarsEnabled() {
        return altarsEnabled;
    }
    
    public int getAltarSearchRadius() {
        return altarSearchRadius;
    }
    
    public double getAltarMinRatio() {
        return altarMinRatio;
    }
    
    public Map<String, Object> getDarkAltarConfig() {
        return darkAltarConfig;
    }
    
    public Map<String, Object> getLightAltarConfig() {
        return lightAltarConfig;
    }
    
    // Getters for block opacity settings
    public double getBlockOpacity(Material material) {
        return blockOpacity.getOrDefault(material, 1.0);
    }
    
    public double getOpacityPerArmorPiece() {
        return opacityPerArmorPiece;
    }
    
    // Helper method to get Material from string
    public Material getMaterial(String materialName) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName);
            return Material.STONE; // Default material
        }
    }
    
    private PotionEffect createEffect(String path) {
        String effectName = config.getString(path + ".type").toLowerCase();
        PotionEffectType type = PotionEffectType.getByKey(org.bukkit.NamespacedKey.minecraft(effectName));
        int duration = config.getInt(path + ".duration");
        int amplifier = config.getInt(path + ".amplifier");
        return new PotionEffect(type, duration, amplifier);
    }
    
    public PotionEffect getVampireEffect() {
        return createEffect("vampire.effects.vampire");
    }
    
    public PotionEffect getBloodlustEffect() {
        return createEffect("vampire.effects.bloodlust");
    }
    
    public PotionEffect getNightVisionEffect() {
        return createEffect("vampire.effects.nightvision");
    }
    
    public PotionEffect getInfectedEffect() {
        return createEffect("vampire.effects.infected");
    }
    
    public PotionEffect getHumanEffect() {
        return createEffect("vampire.effects.human");
    }
    
    public double getCombatDamageFactorWithBloodlust() {
        return config.getDouble("vampire.combat.damageFactor.withBloodlust");
    }
    
    public double getCombatDamageFactorWithoutBloodlust() {
        return config.getDouble("vampire.combat.damageFactor.withoutBloodlust");
    }
    
    public double getCombatInfectRiskWithIntent() {
        return config.getDouble("vampire.combat.infectRisk.withIntent");
    }
    
    public double getCombatInfectRiskWithoutIntent() {
        return config.getDouble("vampire.combat.infectRisk.withoutIntent");
    }
    
    public long getShriekCooldown() {
        return config.getLong("vampire.shriek.cooldown");
    }
    
    public Sound getShriekSound() {
        return Sound.valueOf(config.getString("vampire.shriek.sound"));
    }
    
    public long getTradeOfferTolerance() {
        return config.getLong("vampire.trade.offerTolerance");
    }
    
    public double getTradeOfferMaxDistance() {
        return config.getDouble("vampire.trade.maxDistance");
    }
    
    public long getTruceBreakTime() {
        return config.getLong("vampire.truce.breakTime");
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug");
    }

    private void loadNightVisionSettings() {
        ConfigurationSection nightVisionSection = config.getConfigurationSection("vampire.night-vision");
        if (nightVisionSection != null) {
            nightVisionEnabled = nightVisionSection.getBoolean("enabled", true);
            nightVisionLevel = nightVisionSection.getInt("level", 1);
        }
    }

    private void loadBloodlustSettings() {
        ConfigurationSection bloodlustSection = config.getConfigurationSection("vampire.bloodlust");
        if (bloodlustSection != null) {
            bloodlustDuration = bloodlustSection.getInt("duration", 300);
            bloodlustDamageBoost = bloodlustSection.getDouble("damage-boost", 1.5);
            bloodlustSpeedBoost = bloodlustSection.getDouble("speed-boost", 1.2);
            bloodlustCooldown = bloodlustSection.getInt("cooldown", 600);
        }
    }

    // Getters for night vision settings
    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    public int getNightVisionLevel() {
        return nightVisionLevel;
    }

    // Getters for bloodlust settings
    public int getBloodlustDuration() {
        return bloodlustDuration;
    }

    public double getBloodlustDamageBoost() {
        return bloodlustDamageBoost;
    }

    public double getBloodlustSpeedBoost() {
        return bloodlustSpeedBoost;
    }

    public int getBloodlustCooldown() {
        return bloodlustCooldown;
    }

    // Getters for vampire settings
    public double getBloodDecreaseRate() {
        return bloodDecreaseRate;
    }

    public double getLowBloodThreshold() {
        return lowBloodThreshold;
    }
} 