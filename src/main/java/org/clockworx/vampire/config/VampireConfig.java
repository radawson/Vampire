package org.clockworx.vampire.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.clockworx.vampire.VampirePlugin;
import org.bukkit.entity.EntityType;
import org.clockworx.vampire.util.VampireMessages;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
    
    // *** New Config Fields ***
    /** Bonus to max blood per vampire level. */
    private double maxBloodBonusPerLevel;
    /** Cooldown in seconds after taking damage before blood regeneration resumes. */
    private int regenDamageCooldownSeconds;
    /** Food level below which blood regeneration is penalized. */
    private int regenHungerThreshold;
    /** Multiplier applied to blood regeneration when below hunger threshold. */
    private double regenHungerMultiplier;
    /** Food level above which blood regeneration is boosted. */
    private int regenSatiatedThreshold;
    /** Multiplier applied to blood regeneration when above satiated threshold. */
    private double regenSatiatedMultiplier;
    /** Multiplier applied to blood regeneration when sleeping. */
    private double regenRestingMultiplier;
    /** Cost in blood points to attempt infecting another entity. */
    private double infectBloodCost;
    /** Amount of infection applied per infecting attack. */
    private double infectInfectionAmount;
    /** Whether blood regeneration is disabled in direct sunlight. */
    private boolean regenDisableInSunlight;
    // *** End New Config Fields ***
    
    // *** New Gift Offer Config Fields ***
    /** Whether the Offer Gift mechanic is enabled. */
    private boolean giftEnabled;
    /** Maximum distance in blocks between players for offering/accepting the gift. */
    private double giftMaxDistance;
    /** Cost in blood for the offering vampire. */
    private double giftBloodCost;
    /** Time in seconds the gift offer remains valid. */
    private int giftOfferToleranceSeconds;
    // *** End New Gift Offer Config Fields ***
    
    // *** New Blood Source Config Fields ***
    /** Map storing blood gain per hit for specific EntityTypes. */
    private Map<EntityType, Double> bloodGainPerHit;
    /** Damage dealt to player when vampire gains blood from them. */
    private double playerHealthCostOnHit;
    // *** End New Blood Source Config Fields ***
    
    // *** New Item Config Fields ***
    private double bloodVialInfectionAmount;
    private double holyWaterVampireDamage;
    private double holyWaterUndeadDamage;
    private double holyWaterInfectionCureAmount;
    // *** End New Item Config Fields ***
    
    // Altar settings
    private boolean altarsEnabled;
    private int altarSearchRadius;
    private double altarMinRatio;
    private Map<String, Object> darkAltarConfig;
    private Map<String, Object> lightAltarConfig;
    
    // --- Sunlight Interaction Settings ---
    /** Opacity contribution of various blocks to block sunlight. */
    private Map<Material, Double> blockOpacity;
    /** Base opacity value provided by different armor materials (e.g., LEATHER, IRON). */
    private Map<String, Double> armorBaseMaterialOpacities;
    /** Multiplier based on armor type/slot (e.g., HELMET, CHESTPLATE). */
    private Map<String, Double> armorTypeWeights;
    /** Base damage per second from sunlight at full irradiation (1.0). */
    private double sunlightBaseDamage;
    // --- End Sunlight Interaction Settings ---
    
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
        this.armorBaseMaterialOpacities = new HashMap<>();
        this.armorTypeWeights = new HashMap<>();
        this.bloodGainPerHit = new HashMap<>();
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
        
        // Load Gift Offer settings
        loadGiftSettings();
        
        // Load Blood Source settings (New)
        loadBloodSourceSettings();
        
        // Load Item settings (New)
        loadItemSettings();
        
        // Load night vision settings
        loadNightVisionSettings();
        
        // Load bloodlust settings
        loadBloodlustSettings();
        
        // Load altar settings
        loadAltarSettings();
        
        // Load sunlight settings (block opacity and armor)
        loadSunlightSettings();
        
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
            // Also set defaults for new settings if section is missing
            setDefaultNewSettings(); 
            return;
        }
        
        // Load existing settings
        maxBlood = validatePositiveDouble(vampireSection, "blood.max-blood", 20.0);
        bloodRegenRate = validatePositiveDouble(vampireSection, "blood.regen-rate", 0.1);
        nightVision = vampireSection.getBoolean("night-vision.enabled", true);
        sunDamage = vampireSection.getBoolean("daylight.enabled", true);
        sunDamageAmount = validatePositiveDouble(vampireSection, "daylight.damage-amount", 1.0);
        canInfect = vampireSection.getBoolean("infection.can-infect", true);
        infectionChance = validatePercentage(vampireSection, "infection.chance", 0.25); // Might be redundant now?
        infectionDuration = validatePositiveInteger(vampireSection, "infection.duration-seconds", 60); // Cure time?
        canOfferBlood = vampireSection.getBoolean("trade.can-offer", true);
        maxBloodOffer = validatePositiveDouble(vampireSection, "trade.max-offer", 5.0);
        bloodlustThreshold = validatePositiveDouble(vampireSection, "bloodlust.threshold", 15.0);
        bloodlustBloodDecrease = validatePositiveDouble(vampireSection, "bloodlust.blood-decrease-rate", 0.1);
        taskDelay = validatePositiveInteger(vampireSection, "task-delay-ticks", 20);
        infectionRate = validatePositiveDouble(vampireSection, "infection.rate-per-second", 0.005); // e.g. 1 / (60*3) for 3 mins
        sunlightDamage = validatePositiveDouble(vampireSection, "daylight.damage-per-second", 1.0);
        bloodDecreaseRate = validatePositiveDouble(vampireSection, "blood.decrease-rate-per-second", 0.01); // Passive drain
        lowBloodThreshold = validatePositiveDouble(vampireSection, "blood.low-threshold", 5.0);

        // Load new settings from the same section
        loadNewSettings(vampireSection); 
    }

    /** Loads the newly added configuration settings. */
    private void loadNewSettings(ConfigurationSection section) {
        // Ensure section is not null before proceeding
        if (section == null) {
             plugin.getLogger().warning("Cannot load new settings: Vampire config section not found.");
             setDefaultNewSettings();
             return;
        }
        maxBloodBonusPerLevel = validatePositiveDouble(section, "level.max-blood-bonus-per-level", 1.0);
        regenDamageCooldownSeconds = validatePositiveInteger(section, "blood.regen-damage-cooldown-seconds", 5);
        regenHungerThreshold = validateIntegerRange(section, "blood.regen-hunger-threshold", 6, 0, 20);
        regenHungerMultiplier = validatePercentage(section, "blood.regen-hunger-multiplier", 0.25);
        regenSatiatedThreshold = validateIntegerRange(section, "blood.regen-satiated-threshold", 18, 0, 20);
        regenSatiatedMultiplier = validatePositiveDouble(section, "blood.regen-satiated-multiplier", 1.1);
        regenRestingMultiplier = validatePositiveDouble(section, "blood.regen-resting-multiplier", 2.0);
        infectBloodCost = validatePositiveDouble(section, "combat.infect-blood-cost", 0.5);
        infectInfectionAmount = validatePercentage(section, "combat.infect-infection-amount", 0.05);
        regenDisableInSunlight = section.getBoolean("blood.regen-disable-in-sunlight", true);
    }

    /** Sets default values for the newly added settings. */
    private void setDefaultNewSettings() {
        maxBloodBonusPerLevel = 1.0;
        regenDamageCooldownSeconds = 5;
        regenHungerThreshold = 6;
        regenHungerMultiplier = 0.25;
        regenSatiatedThreshold = 18;
        regenSatiatedMultiplier = 1.1;
        regenRestingMultiplier = 2.0;
        infectBloodCost = 0.5;
        infectInfectionAmount = 0.05;
        regenDisableInSunlight = true;
    }

    // Helper for integer range validation
    private int validateIntegerRange(ConfigurationSection section, String path, int defaultValue, int min, int max) {
        int value = section.getInt(path, defaultValue);
        if (value < min || value > max) {
            plugin.getLogger().warning("Invalid value for " + path + ": " + value + ". Must be between "+min+" and "+max+". Using default: " + defaultValue);
            return defaultValue;
        }
        return value;
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
    
    /** Loads settings related to sunlight interaction: block opacity and armor protection. */
    private void loadSunlightSettings() {
        blockOpacity.clear();
        armorBaseMaterialOpacities.clear();
        armorTypeWeights.clear();

        // Get the main sunlight section
        ConfigurationSection sunlightSection = config.getConfigurationSection("sunlight");
        if (sunlightSection == null) {
            plugin.getLogger().warning("Sunlight section not found in config.yml. Using default values for block and armor opacity.");
            setDefaultSunlightSettings(); // Call a method to set defaults if the section is missing
            return;
        }

        // Load Base Damage
        sunlightBaseDamage = validatePositiveDouble(sunlightSection, "base_damage", 1.0);

        // Load Block Opacity
        ConfigurationSection blockSection = sunlightSection.getConfigurationSection("block_opacity");
        if (blockSection != null) {
            for (String key : blockSection.getKeys(false)) {
                try {
                    Material material = Material.matchMaterial(key.toUpperCase());
                    if (material != null && material.isBlock()) {
                        double opacity = blockSection.getDouble(key);
                        if (opacity < 0.0 || opacity > 1.0) {
                            plugin.getLogger().warning("Invalid opacity value for block " + key + ": " + opacity + ". Clamping to [0, 1].");
                            opacity = Math.max(0.0, Math.min(1.0, opacity));
                        }
                        blockOpacity.put(material, opacity);
                    } else {
                        plugin.getLogger().warning("Invalid or non-block material specified in block_opacity: " + key);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material specified in block_opacity: " + key);
                }
            }
        } else {
            plugin.getLogger().warning("sunlight.block_opacity subsection not found. Using default block opacities.");
            setDefaultBlockOpacities();
        }

        // Load Armor Material Opacities
        ConfigurationSection materialSection = sunlightSection.getConfigurationSection("armor_base_material_opacities");
        if (materialSection != null) {
            for (String key : materialSection.getKeys(false)) {
                double opacity = materialSection.getDouble(key);
                if (opacity < 0.0) { // Allow opacity > 1.0 if desired, but not negative
                   plugin.getLogger().warning("Invalid negative opacity value for armor material " + key + ": " + opacity + ". Setting to 0.");
                   opacity = 0.0;
                }
                armorBaseMaterialOpacities.put(key.toUpperCase(), opacity); // Store key as uppercase
            }
        } else {
             plugin.getLogger().warning("sunlight.armor_base_material_opacities subsection not found. Using default material opacities.");
             setDefaultArmorMaterialOpacities();
        }

        // Load Armor Type Weights
        ConfigurationSection typeSection = sunlightSection.getConfigurationSection("armor_type_weights");
        if (typeSection != null) {
            for (String key : typeSection.getKeys(false)) {
                 double weight = typeSection.getDouble(key);
                 if (weight < 0.0) {
                    plugin.getLogger().warning("Invalid negative weight value for armor type " + key + ": " + weight + ". Setting to 0.");
                    weight = 0.0;
                 }
                 armorTypeWeights.put(key.toUpperCase(), weight); // Store key as uppercase
            }
        } else {
            plugin.getLogger().warning("sunlight.armor_type_weights subsection not found. Using default type weights.");
            setDefaultArmorTypeWeights();
        }
    }
    
    // --- Helper methods to set default sunlight values --- 
    private void setDefaultSunlightSettings() {
        sunlightBaseDamage = 1.0; // Default base damage
        setDefaultBlockOpacities();
        setDefaultArmorMaterialOpacities();
        setDefaultArmorTypeWeights();
    }

    private void setDefaultBlockOpacities() {
        blockOpacity.clear();
        // Add essential defaults
        blockOpacity.put(Material.STONE, 0.9);
        blockOpacity.put(Material.DIRT, 0.8);
        blockOpacity.put(Material.GLASS, 0.1);
        blockOpacity.put(Material.WATER, 0.3); 
        // Add more defaults if needed
    }

    private void setDefaultArmorMaterialOpacities() {
        armorBaseMaterialOpacities.clear();
        armorBaseMaterialOpacities.put("LEATHER", 0.15);
        armorBaseMaterialOpacities.put("CHAINMAIL", 0.25);
        armorBaseMaterialOpacities.put("IRON", 0.35);
        armorBaseMaterialOpacities.put("GOLD", 0.20);
        armorBaseMaterialOpacities.put("DIAMOND", 0.45);
        armorBaseMaterialOpacities.put("NETHERITE", 0.60);
        armorBaseMaterialOpacities.put("TURTLE", 0.30);
        armorBaseMaterialOpacities.put("ELYTRA", 0.05); // Elytra has low opacity
    }

    private void setDefaultArmorTypeWeights() {
        armorTypeWeights.clear();
        armorTypeWeights.put("HELMET", 0.25);
        armorTypeWeights.put("CHESTPLATE", 0.40);
        armorTypeWeights.put("LEGGINGS", 0.25);
        armorTypeWeights.put("BOOTS", 0.10);
        armorTypeWeights.put("ELYTRA", 0.40); // Elytra uses the chestplate slot weight
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
        // Default to 1.0 (fully opaque) if material not explicitly defined
        return blockOpacity.getOrDefault(material, 1.0); 
    }
    
    /**
     * Gets the configured map of base opacity values for different armor materials.
     * Keys are uppercase material names (e.g., "IRON", "LEATHER").
     * Values are the base opacity contribution (>= 0.0).
     * @return The map of armor base material opacities.
     */
    public Map<String, Double> getArmorBaseMaterialOpacities() {
        return Collections.unmodifiableMap(armorBaseMaterialOpacities);
    }

    /**
     * Gets the configured map of weights for different armor types/slots.
     * Keys are uppercase armor types (e.g., "HELMET", "CHESTPLATE").
     * Values are the weight multipliers (>= 0.0).
     * @return The map of armor type weights.
     */
    public Map<String, Double> getArmorTypeWeights() {
        return Collections.unmodifiableMap(armorTypeWeights);
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
        return config.getLong("vampire.shriek.cooldown_seconds", 60) * 1000L; // Convert seconds to ms
    }
    
    /**
     * Gets the sound key string to play when a player uses the shriek ability.
     * Defaults to "minecraft:entity.ghast.scream".
     * @return The sound key string.
     */
    public String getShriekSoundKey() {
        // Read the key string directly, remove Sound.valueOf
        return config.getString("vampire.shriek.sound_key", "minecraft:entity.ghast.scream");
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

    // --- Getters for New Settings --- 

    /**
     * Gets the bonus to maximum blood capacity granted per vampire level.
     * @return Bonus max blood per level.
     */
    public double getMaxBloodBonusPerLevel() {
        return maxBloodBonusPerLevel;
    }

    /**
     * Gets the cooldown in seconds after taking damage before blood regeneration resumes.
     * @return Damage cooldown in seconds.
     */
    public int getRegenDamageCooldownSeconds() {
        return regenDamageCooldownSeconds;
    }

    /**
     * Gets the food level (0-20) below which blood regeneration rate is penalized.
     * @return Hunger threshold for regeneration penalty.
     */
    public int getRegenHungerThreshold() {
        return regenHungerThreshold;
    }

    /**
     * Gets the multiplier (e.g., 0.25 for 25%) applied to blood regeneration when below the hunger threshold.
     * @return Hunger regeneration multiplier.
     */
    public double getRegenHungerMultiplier() {
        return regenHungerMultiplier;
    }

    /**
     * Gets the food level (0-20) at or above which blood regeneration rate is boosted.
     * @return Satiated threshold for regeneration boost.
     */
    public int getRegenSatiatedThreshold() {
        return regenSatiatedThreshold;
    }

    /**
     * Gets the multiplier (e.g., 1.1 for 110%) applied to blood regeneration when at or above the satiated threshold.
     * @return Satiated regeneration multiplier.
     */
    public double getRegenSatiatedMultiplier() {
        return regenSatiatedMultiplier;
    }

    /**
     * Gets the multiplier (e.g., 2.0 for 200%) applied to blood regeneration when the player is sleeping.
     * @return Resting regeneration multiplier.
     */
    public double getRegenRestingMultiplier() {
        return regenRestingMultiplier;
    }

    /**
     * Gets the amount of blood consumed when a vampire successfully hits an entity with infection intent enabled.
     * @return Blood cost per infecting attack.
     */
    public double getInfectBloodCost() {
        return infectBloodCost;
    }

    /**
     * Gets the amount of infection (0.0 to 1.0) applied per successful infecting attack.
     * @return Infection amount per infecting attack.
     */
    public double getInfectInfectionAmount() {
        return infectInfectionAmount;
    }

    /**
     * Gets whether blood regeneration should be disabled when the player is exposed to direct sunlight.
     * Checks config path: vampire.blood.regen-disable-in-sunlight
     * @return True if regeneration is disabled in sunlight, false otherwise.
     */
    public boolean getRegenDisableInSunlight() {
        return regenDisableInSunlight;
    }

    /** Loads the gift offer configuration settings. */
    private void loadGiftSettings() {
        ConfigurationSection giftSection = config.getConfigurationSection("vampire.gift");
        if (giftSection == null) {
            plugin.getLogger().warning("Gift section (vampire.gift) not found in config.yml, using defaults.");
            setDefaultGiftSettings();
            return;
        }
        giftEnabled = giftSection.getBoolean("enabled", true);
        giftMaxDistance = validatePositiveDouble(giftSection, "max-distance", 5.0);
        giftBloodCost = validatePositiveDouble(giftSection, "blood-cost", 5.0);
        giftOfferToleranceSeconds = validatePositiveInteger(giftSection, "offer-tolerance-seconds", 60);
    }

    /** Sets default values for the gift offer settings. */
    private void setDefaultGiftSettings() {
        giftEnabled = true;
        giftMaxDistance = 5.0;
        giftBloodCost = 5.0;
        giftOfferToleranceSeconds = 60;
    }

    // --- Getters for Gift Offer Settings --- 

    /**
     * Checks if the Offer Gift mechanic is enabled.
     * @return True if enabled, false otherwise.
     */
    public boolean isGiftEnabled() {
        return giftEnabled;
    }

    /**
     * Gets the maximum distance allowed between players for offering or accepting the Dark Gift.
     * @return Maximum distance in blocks.
     */
    public double getGiftMaxDistance() {
        return giftMaxDistance;
    }

    /**
     * Gets the amount of blood the offering vampire must pay to turn the target.
     * @return Blood cost for the offerer.
     */
    public double getGiftBloodCost() {
        return giftBloodCost;
    }

    /**
     * Gets the time in seconds an offer for the Dark Gift remains valid before expiring.
     * @return Offer tolerance in seconds.
     */
    public int getGiftOfferToleranceSeconds() {
        return giftOfferToleranceSeconds;
    }

    /** Loads the blood source configuration settings. */
    private void loadBloodSourceSettings() {
        ConfigurationSection bsSection = config.getConfigurationSection("vampire.blood_sources");
        if (bsSection == null) {
            plugin.getLogger().warning("Blood sources section (vampire.blood_sources) not found in config.yml, using defaults.");
            setDefaultBloodSourceSettings();
            return;
        }

        // Clear previous values before loading new ones
        bloodGainPerHit.clear();
        ConfigurationSection gainSection = bsSection.getConfigurationSection("gain_per_hit");
        if (gainSection != null) {
            for (String entityTypeName : gainSection.getKeys(false)) {
                try {
                    EntityType type = EntityType.valueOf(entityTypeName.toUpperCase());
                    double amount = gainSection.getDouble(entityTypeName, 0.0);
                    if (amount < 0) {
                        plugin.getLogger().warning("Invalid negative blood gain value for " + entityTypeName + ", setting to 0.");
                        amount = 0.0;
                    }
                    bloodGainPerHit.put(type, amount);
                    VampireMessages.debug("Loaded blood source: " + type + " -> " + amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid entity type in blood sources config: " + entityTypeName);
                }
            }
        }

        playerHealthCostOnHit = bsSection.getDouble("player_health_cost_on_hit", 0.0);
        if (playerHealthCostOnHit < 0) {
            plugin.getLogger().warning("Invalid negative player health cost: " + playerHealthCostOnHit + ", setting to 0.");
            playerHealthCostOnHit = 0.0;
        }
         VampireMessages.debug("Loaded player health cost on hit: " + playerHealthCostOnHit);
    }

    /** Sets default values for the blood source settings. */
    private void setDefaultBloodSourceSettings() {
        bloodGainPerHit = new HashMap<>(); // Ensure it's a new map if defaults are set
        // Add a few key defaults programmatically - config.yml provides the full list
        bloodGainPerHit.put(EntityType.PLAYER, 0.5);
        bloodGainPerHit.put(EntityType.VILLAGER, 1.0);
        bloodGainPerHit.put(EntityType.COW, 0.8);
        bloodGainPerHit.put(EntityType.PIG, 0.7);
        bloodGainPerHit.put(EntityType.CHICKEN, 0.3);
        bloodGainPerHit.put(EntityType.ZOMBIE, 0.0);
        bloodGainPerHit.put(EntityType.SKELETON, 0.0);
        playerHealthCostOnHit = 0.0;
    }

    // --- Getters for Blood Source Settings (New) --- 

    /**
     * Gets the amount of blood gained per hit for a specific entity type.
     * Returns 0.0 if the entity type is not configured or invalid.
     *
     * @param type The EntityType.
     * @return The amount of blood gained, or 0.0.
     */
    public double getBloodGain(EntityType type) {
        return bloodGainPerHit.getOrDefault(type, 0.0);
    }

    /**
     * Gets the amount of health damage dealt to a player when a vampire
     * successfully gains blood from hitting them.
     *
     * @return The health cost (damage amount).
     */
    public double getPlayerHealthCostOnHit() {
        return playerHealthCostOnHit;
    }

    /** Loads the item configuration settings. */
    private void loadItemSettings() {
        ConfigurationSection itemSection = config.getConfigurationSection("items");
        if (itemSection == null) {
            plugin.getLogger().warning("Items section (items) not found in config.yml, using defaults.");
            setDefaultItemSettings();
            return;
        }

        ConfigurationSection bvSection = itemSection.getConfigurationSection("blood_vial");
        if (bvSection != null) {
            bloodVialInfectionAmount = validatePercentage(bvSection, "infection_amount", 0.15);
        } else {
            bloodVialInfectionAmount = 0.15; // Default if subsection missing
        }

        ConfigurationSection hwSection = itemSection.getConfigurationSection("holy_water");
        if (hwSection != null) {
            holyWaterVampireDamage = validatePositiveDouble(hwSection, "vampire_damage", 4.0);
            holyWaterUndeadDamage = validatePositiveDouble(hwSection, "undead_damage", 6.0);
            holyWaterInfectionCureAmount = validatePercentage(hwSection, "infection_cure_amount", 0.25);
        } else {
             // Defaults if subsection missing
             holyWaterVampireDamage = 4.0;
             holyWaterUndeadDamage = 6.0;
             holyWaterInfectionCureAmount = 0.25;
        }
        // Format the debug string before passing it
        String debugMsg = String.format("Loaded item settings: BV Infect=%.2f, HW VampDmg=%.1f, HW UndeadDmg=%.1f, HW Cure=%.2f", 
            bloodVialInfectionAmount, holyWaterVampireDamage, holyWaterUndeadDamage, holyWaterInfectionCureAmount);
        VampireMessages.debug(debugMsg);
    }

    /** Sets default values for the item settings. */
    private void setDefaultItemSettings() {
         bloodVialInfectionAmount = 0.15;
         holyWaterVampireDamage = 4.0;
         holyWaterUndeadDamage = 6.0;
         holyWaterInfectionCureAmount = 0.25;
    }

    // --- Getters for Item Settings (New) --- 

    public double getBloodVialInfectionAmount() {
        return bloodVialInfectionAmount;
    }

    public double getHolyWaterVampireDamage() {
        return holyWaterVampireDamage;
    }

    public double getHolyWaterUndeadDamage() {
        return holyWaterUndeadDamage;
    }

    public double getHolyWaterInfectionCureAmount() {
        return holyWaterInfectionCureAmount;
    }

    // --- Getter for Sunlight Base Damage --- 
    public double getSunlightBaseDamage() {
        return sunlightBaseDamage;
    }

    /**
     * Gets the sound key string for successful Dark Altar activation.
     * Defaults to "minecraft:entity.wither.spawn".
     * @return The sound key string.
     */
    public String getAltarDarkSuccessSoundKey() {
        return config.getString("altar.dark.success_sound_key", "minecraft:entity.wither.spawn");
    }

    /**
     * Gets the sound key string for successful Light Altar activation.
     * Defaults to "minecraft:entity.player.levelup".
     * @return The sound key string.
     */
    public String getAltarLightSuccessSoundKey() {
        return config.getString("altar.light.success_sound_key", "minecraft:entity.player.levelup");
    }

    /**
     * Gets the sound key string for failed altar rituals (movement, etc.).
     * Defaults to "minecraft:entity.enderman.teleport".
     * @return The sound key string.
     */
    public String getAltarFailSoundKey() {
        return config.getString("altar.fail_sound_key", "minecraft:entity.enderman.teleport");
    }

    public double getCombatInfectionChance() {
        return config.getDouble("vampire.combat.infectRisk.withIntent", 0.3);
    }
} 