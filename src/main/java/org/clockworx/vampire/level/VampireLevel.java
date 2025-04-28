package org.clockworx.vampire.level;

/**
 * Represents the configuration for a specific vampire level.
 * Holds stats and abilities associated with that level, loaded from levels.yml.
 */
public record VampireLevel(
    int level,
    String description,
    double maxBlood,
    double bloodRegenRate, // Multiplier for base regen
    double sunModifier,    // Multiplier for sun damage (lower is better)
    double speedBoost,     // Multiplier for base speed
    boolean canUseShriek,
    double shriekCooldownModifier, // Multiplier for base cooldown (lower is faster)
    double fallDamageReduction // Add fall damage reduction field
    // Add more fields here as needed, matching levels.yml keys
) {
    /**
     * Provides a default level configuration, typically for level 0 or when data is missing.
     *
     * @return A default VampireLevel instance.
     */
    public static VampireLevel defaultLevel() {
        return new VampireLevel(0, "Default", 20.0, 1.0, 1.0, 1.0, true, 1.0, 0.0); // Sensible defaults
    }
}
