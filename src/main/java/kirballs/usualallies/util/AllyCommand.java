package kirballs.usualallies.util;

/**
 * Enum representing the different command states for allied mobs.
 * Players can cycle through these commands when interacting with their allies.
 */
public enum AllyCommand {
    /**
     * Follow the player around.
     * Ally will stay close to the player and follow when they move.
     */
    FOLLOW,
    
    /**
     * Stay in place.
     * Ally will remain at their current position and not move.
     */
    STAY,
    
    /**
     * Wander around freely.
     * Ally will walk around aimlessly, not following the player.
     */
    WANDER,
    
    /**
     * Patrol an area.
     * Ally will wander around their current location, staying within a certain radius.
     * They will avoid walls and attack hostile mobs that approach.
     */
    PATROL
}
