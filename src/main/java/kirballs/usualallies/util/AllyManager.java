package kirballs.usualallies.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * Manages the ally system for befriended mobs.
 * Tracks which mobs are allied to which players.
 * 
 * Features:
 * - Befriended mobs follow the player
 * - Befriended mobs fight for the player
 * - Befriended mobs are not targeted by entities hostile to their species
 * - Commands: follow, stay, wander, patrol
 */
@Mod.EventBusSubscriber
public class AllyManager {

    // Map of player UUIDs to their allied mob UUIDs
    private static final Map<UUID, Set<UUID>> PLAYER_ALLIES = new HashMap<>();
    
    // Map of ally UUIDs to their current command
    private static final Map<UUID, AllyCommand> ALLY_COMMANDS = new HashMap<>();
    
    // NBT key for ally data on mobs
    public static final String ALLY_TAG = "UsualAlliesData";
    public static final String OWNER_UUID_TAG = "OwnerUUID";
    public static final String COMMAND_TAG = "Command";

    /**
     * Adds a mob as an ally to a player.
     * 
     * @param player The player who will own this ally
     * @param entity The mob to befriend
     */
    public static void addAlly(Player player, LivingEntity entity) {
        UUID playerUUID = player.getUUID();
        UUID entityUUID = entity.getUUID();
        
        // Add to tracking maps
        PLAYER_ALLIES.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(entityUUID);
        ALLY_COMMANDS.put(entityUUID, AllyCommand.FOLLOW);
        
        // Store ally data on the entity for persistence
        CompoundTag entityData = entity.getPersistentData();
        CompoundTag allyData = new CompoundTag();
        allyData.putUUID(OWNER_UUID_TAG, playerUUID);
        allyData.putInt(COMMAND_TAG, AllyCommand.FOLLOW.ordinal());
        entityData.put(ALLY_TAG, allyData);
        
        // Modify mob AI if it's a Mob
        if (entity instanceof Mob mob) {
            // Clear current target
            mob.setTarget(null);
            
            // Add ally AI goals
            addAllyAIGoals(mob);
        }
    }
    
    /**
     * Adds ally-specific AI goals to a befriended mob.
     * These goals handle follow, stay, patrol, and wander behaviors.
     * 
     * @param mob The mob to add goals to
     */
    private static void addAllyAIGoals(Mob mob) {
        // Add stay goal with high priority (blocks other goals when active)
        mob.goalSelector.addGoal(1, new kirballs.usualallies.entity.ai.AllyStayGoal(mob));
        
        // Add follow owner goal
        mob.goalSelector.addGoal(2, new kirballs.usualallies.entity.ai.AllyFollowOwnerGoal(mob, 1.0, 10.0, 2.0));
        
        // Add patrol goal
        mob.goalSelector.addGoal(3, new kirballs.usualallies.entity.ai.AllyPatrolGoal(mob, 0.8, 8.0));
    }

    /**
     * Removes a mob from the ally system.
     * 
     * @param entity The mob to remove
     */
    public static void removeAlly(LivingEntity entity) {
        UUID entityUUID = entity.getUUID();
        
        // Find and remove from player's ally list
        UUID ownerUUID = getOwnerUUID(entity);
        if (ownerUUID != null) {
            Set<UUID> allies = PLAYER_ALLIES.get(ownerUUID);
            if (allies != null) {
                allies.remove(entityUUID);
            }
        }
        
        // Remove from command map
        ALLY_COMMANDS.remove(entityUUID);
        
        // Remove ally data from entity
        entity.getPersistentData().remove(ALLY_TAG);
    }

    /**
     * Checks if an entity is an ally of any player.
     * 
     * @param entity The entity to check
     * @return True if the entity is someone's ally
     */
    public static boolean isAlly(LivingEntity entity) {
        return entity.getPersistentData().contains(ALLY_TAG);
    }

    /**
     * Checks if an entity is an ally of a specific player.
     * 
     * @param player The player to check
     * @param entity The entity to check
     * @return True if the entity is allied to this player
     */
    public static boolean isAllyOf(Player player, LivingEntity entity) {
        UUID ownerUUID = getOwnerUUID(entity);
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    /**
     * Gets the owner UUID of an allied mob.
     * 
     * @param entity The ally to check
     * @return The owner's UUID, or null if not an ally
     */
    public static UUID getOwnerUUID(LivingEntity entity) {
        CompoundTag entityData = entity.getPersistentData();
        if (entityData.contains(ALLY_TAG)) {
            CompoundTag allyData = entityData.getCompound(ALLY_TAG);
            if (allyData.hasUUID(OWNER_UUID_TAG)) {
                return allyData.getUUID(OWNER_UUID_TAG);
            }
        }
        return null;
    }

    /**
     * Gets the current command for an ally.
     * 
     * @param entity The ally
     * @return The current command, or FOLLOW if not found
     */
    public static AllyCommand getCommand(LivingEntity entity) {
        UUID entityUUID = entity.getUUID();
        
        // Check memory first
        if (ALLY_COMMANDS.containsKey(entityUUID)) {
            return ALLY_COMMANDS.get(entityUUID);
        }
        
        // Check persistent data
        CompoundTag entityData = entity.getPersistentData();
        if (entityData.contains(ALLY_TAG)) {
            CompoundTag allyData = entityData.getCompound(ALLY_TAG);
            int commandOrdinal = allyData.getInt(COMMAND_TAG);
            AllyCommand command = AllyCommand.values()[commandOrdinal];
            ALLY_COMMANDS.put(entityUUID, command);
            return command;
        }
        
        return AllyCommand.FOLLOW;
    }

    /**
     * Sets the command for an ally.
     * 
     * @param entity The ally
     * @param command The new command
     */
    public static void setCommand(LivingEntity entity, AllyCommand command) {
        UUID entityUUID = entity.getUUID();
        ALLY_COMMANDS.put(entityUUID, command);
        
        // Update persistent data
        CompoundTag entityData = entity.getPersistentData();
        if (entityData.contains(ALLY_TAG)) {
            CompoundTag allyData = entityData.getCompound(ALLY_TAG);
            allyData.putInt(COMMAND_TAG, command.ordinal());
        }
    }

    /**
     * Cycles to the next command for an ally.
     * 
     * @param entity The ally
     * @return The new command
     */
    public static AllyCommand cycleCommand(LivingEntity entity) {
        AllyCommand current = getCommand(entity);
        AllyCommand next = AllyCommand.values()[(current.ordinal() + 1) % AllyCommand.values().length];
        setCommand(entity, next);
        return next;
    }

    /**
     * Gets all allies of a player.
     * 
     * @param player The player
     * @return Set of ally UUIDs
     */
    public static Set<UUID> getAllies(Player player) {
        return PLAYER_ALLIES.getOrDefault(player.getUUID(), Collections.emptySet());
    }

    /**
     * Checks if two entities should be friendly to each other.
     * (Both are allies of the same player)
     * 
     * @param entity1 First entity
     * @param entity2 Second entity
     * @return True if they should not fight each other
     */
    public static boolean areFriendly(LivingEntity entity1, LivingEntity entity2) {
        UUID owner1 = getOwnerUUID(entity1);
        UUID owner2 = getOwnerUUID(entity2);
        
        if (owner1 == null || owner2 == null) {
            return false;
        }
        
        return owner1.equals(owner2);
    }

    /**
     * Event handler to prevent allies from being targeted by their former enemies.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.level().isClientSide) {
            return;
        }
        
        // If this is an allied mob, check if anything is incorrectly targeting it
        if (isAlly(entity) && entity instanceof Mob allyMob) {
            // Nothing specific to do here right now
            // More complex ally behavior can be added
        }
    }
}
