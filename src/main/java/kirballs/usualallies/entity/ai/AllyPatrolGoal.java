package kirballs.usualallies.entity.ai;

import kirballs.usualallies.util.AllyCommand;
import kirballs.usualallies.util.AllyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AI Goal for allied mobs to patrol around a center point.
 * Only active when the ally's command is set to PATROL.
 * The mob will wander within a radius and avoid walls.
 */
public class AllyPatrolGoal extends Goal {

    private final Mob ally;
    
    // Patrol radius in blocks
    private final double patrolRadius;
    
    // Speed when patrolling
    private final double speedModifier;
    
    // Center of patrol area (stored in persistent data)
    private BlockPos patrolCenter;
    
    // Current target position
    private Vec3 targetPos;
    
    // Ticks until next move
    private int cooldown;
    
    // NBT key for storing patrol center
    private static final String PATROL_CENTER_TAG = "PatrolCenter";

    /**
     * Creates a new patrol goal for an allied mob.
     * 
     * @param ally The allied mob
     * @param speedModifier Speed when patrolling (1.0 = normal speed)
     * @param patrolRadius Maximum distance from patrol center
     */
    public AllyPatrolGoal(Mob ally, double speedModifier, double patrolRadius) {
        this.ally = ally;
        this.speedModifier = speedModifier;
        this.patrolRadius = patrolRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Check if this mob is an ally
        if (!AllyManager.isAlly(ally)) {
            return false;
        }
        
        // Check if command is PATROL
        AllyCommand command = AllyManager.getCommand(ally);
        if (command != AllyCommand.PATROL) {
            return false;
        }
        
        // Get or set patrol center
        if (patrolCenter == null) {
            loadPatrolCenter();
            if (patrolCenter == null) {
                // Set current position as patrol center
                patrolCenter = ally.blockPosition();
                savePatrolCenter();
            }
        }
        
        return cooldown-- <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if no longer an ally or command changed
        if (!AllyManager.isAlly(ally)) {
            return false;
        }
        
        AllyCommand command = AllyManager.getCommand(ally);
        if (command != AllyCommand.PATROL) {
            return false;
        }
        
        // Continue until we reach the target
        return !ally.getNavigation().isDone();
    }

    @Override
    public void start() {
        // Find a random position within patrol radius
        targetPos = findPatrolTarget();
        if (targetPos != null) {
            ally.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speedModifier);
        }
        
        // Set cooldown for next patrol move (5-10 seconds)
        cooldown = ally.getRandom().nextInt(100) + 100;
    }

    @Override
    public void stop() {
        ally.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Look in the direction of movement
        if (targetPos != null) {
            ally.getLookControl().setLookAt(targetPos.x, targetPos.y + ally.getEyeHeight(), targetPos.z, 10.0f, ally.getMaxHeadXRot());
        }
    }
    
    /**
     * Finds a valid patrol target within the patrol radius.
     */
    private Vec3 findPatrolTarget() {
        for (int attempts = 0; attempts < 10; attempts++) {
            // Random offset within patrol radius
            double xOffset = (ally.getRandom().nextDouble() * 2 - 1) * patrolRadius;
            double zOffset = (ally.getRandom().nextDouble() * 2 - 1) * patrolRadius;
            
            double targetX = patrolCenter.getX() + 0.5 + xOffset;
            double targetZ = patrolCenter.getZ() + 0.5 + zOffset;
            double targetY = patrolCenter.getY();
            
            BlockPos targetBlockPos = new BlockPos((int)targetX, (int)targetY, (int)targetZ);
            
            // Find valid Y position
            targetBlockPos = findValidYPosition(targetBlockPos);
            if (targetBlockPos == null) {
                continue;
            }
            
            // Check if this position is walkable
            if (isPositionValid(targetBlockPos)) {
                return new Vec3(targetBlockPos.getX() + 0.5, targetBlockPos.getY(), targetBlockPos.getZ() + 0.5);
            }
        }
        
        return null;
    }
    
    /**
     * Finds a valid Y position (ground level) for the given X/Z coordinates.
     */
    private BlockPos findValidYPosition(BlockPos pos) {
        // Search up and down for valid ground
        for (int yOffset = -3; yOffset <= 3; yOffset++) {
            BlockPos checkPos = pos.offset(0, yOffset, 0);
            BlockPathTypes pathType = WalkNodeEvaluator.getBlockPathTypeStatic(
                    ally.level(), checkPos.mutable());
            
            if (pathType == BlockPathTypes.WALKABLE) {
                return checkPos;
            }
        }
        return null;
    }
    
    /**
     * Checks if a position is valid for the mob to move to.
     */
    private boolean isPositionValid(BlockPos pos) {
        BlockPathTypes pathType = WalkNodeEvaluator.getBlockPathTypeStatic(
                ally.level(), pos.mutable());
        
        if (pathType != BlockPathTypes.WALKABLE) {
            return false;
        }
        
        // Check if there's room for the mob
        return ally.level().noCollision(ally, ally.getBoundingBox().move(
                pos.getX() - ally.getX(),
                pos.getY() - ally.getY(),
                pos.getZ() - ally.getZ()));
    }
    
    /**
     * Loads the patrol center from persistent data.
     */
    private void loadPatrolCenter() {
        if (ally.getPersistentData().contains(PATROL_CENTER_TAG)) {
            int[] coords = ally.getPersistentData().getIntArray(PATROL_CENTER_TAG);
            if (coords.length == 3) {
                patrolCenter = new BlockPos(coords[0], coords[1], coords[2]);
            }
        }
    }
    
    /**
     * Saves the patrol center to persistent data.
     */
    private void savePatrolCenter() {
        if (patrolCenter != null) {
            ally.getPersistentData().putIntArray(PATROL_CENTER_TAG, 
                    new int[]{patrolCenter.getX(), patrolCenter.getY(), patrolCenter.getZ()});
        }
    }
    
    /**
     * Sets a new patrol center for this mob.
     */
    public void setPatrolCenter(BlockPos center) {
        this.patrolCenter = center;
        savePatrolCenter();
    }
}
