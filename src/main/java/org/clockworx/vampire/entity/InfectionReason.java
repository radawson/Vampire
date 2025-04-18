package org.clockworx.vampire.entity;

/**
 * Represents the different reasons why a player might become infected with vampirism.
 * Each reason includes information about whether it's noticeable to the player,
 * whether it involves another player, and appropriate message formatting.
 */
public enum InfectionReason {
    ALTAR(true, false, "altar", 
        "<i>You infected yourself using an <h>altar<i>.", 
        "<i>%1$s was infected using an <h>altar<i>."),
        
    COMBAT_MISTAKE(false, true, "combat mistake", 
        "<h>%2$s <i>infected you during combat by mistake.", 
        "<h>%2$s <i>infected %1$s during combat by mistake."),
        
    COMBAT_INTENDED(false, true, "combat intended", 
        "<h>%2$s <i>infected you during combat on purpose.", 
        "<h>%2$s <i>infected %1$s during combat on purpose."),
        
    TRADE(false, true, "offer", 
        "<i>You were infected from drinking <h>%2$ss <i>blood.", 
        "<i>%1$s was infected from drinking <h>%2$ss <i>blood."),
        
    FLASK(true, false, "blood flask", 
        "<i>You were infected by a <h>blood flask<i>.", 
        "<i>%1$s was infected by a <h>blood flask<i>."),
        
    OPERATOR(true, false, "evil powers", 
        "<i>You were infected by <h>evil powers<i>.", 
        "<i>%1$s was infected by <h>evil powers<i>."),
        
    UNKNOWN(true, false, "unknown", 
        "<i>You were infected for <h>unknown <i>reasons.", 
        "<i>%1$s was infected for <h>unknown <i>reasons.");

    private final boolean noticeable;
    private final boolean maker;
    private final String shortname;
    private final String selfDesc;
    private final String otherDesc;

    /**
     * Creates a new infection reason.
     * 
     * @param noticeable Whether the victim notices this infection
     * @param maker Whether another player caused this infection
     * @param shortname Short identifier for this reason
     * @param selfDesc Description when showing to the infected player
     * @param otherDesc Description when showing to other players
     */
    InfectionReason(boolean noticeable, boolean maker, String shortname, 
                    String selfDesc, String otherDesc) {
        this.noticeable = noticeable;
        this.maker = maker;
        this.shortname = shortname;
        this.selfDesc = selfDesc;
        this.otherDesc = otherDesc;
    }

    /**
     * Gets whether this infection is noticeable to the victim.
     * 
     * @return true if noticeable
     */
    public boolean isNoticeable() {
        return noticeable;
    }

    /**
     * Gets whether another player caused this infection.
     * 
     * @return true if another player was involved
     */
    public boolean isMaker() {
        return maker;
    }

    /**
     * Gets the short identifier for this reason.
     * 
     * @return the shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * Gets the formatted description for the infected player.
     * 
     * @param player The infected player
     * @param maker The player who caused the infection (if any)
     * @return formatted description
     */
    public String getSelfDescription(VampirePlayer player, VampirePlayer maker) {
        return String.format(selfDesc, player.getName(), maker != null ? maker.getName() : "");
    }

    /**
     * Gets the formatted description for other players.
     * 
     * @param player The infected player
     * @param maker The player who caused the infection (if any)
     * @return formatted description
     */
    public String getOtherDescription(VampirePlayer player, VampirePlayer maker) {
        return String.format(otherDesc, player.getName(), maker != null ? maker.getName() : "");
    }
} 