package kirballs.usualallies.entity.ai;

import kirballs.usualallies.util.AllyCommand;
import kirballs.usualallies.util.AllyManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

/**
 * AI Goal for allied mobs to follow their owner player.
 * Only active when the ally's command is set to FOLLOW.
 */
public class AllyFollowOwnerGoal extends Goal {

    private final Mob ally;
    private LivingEntity owner;
    
    // Distance at which the ally starts following
    private final double startFollowDistance;
    
    // Distance at which the ally stops following (close enough)
    private final double stopFollowDistance;
    
    // Speed multiplier when following
    private final double speedModifier;
    
    // Ticks since last path recalculation
    private int timeToRecalcPath;
    
    // Old water navigation settings
    private float oldWaterCost;

    /**
     * Creates a new follow owner goal for an allied mob.
     * 
     * @param ally The allied mob
     * @param speedModifier Speed when following (1.0 = normal speed)
     * @param startFollowDistance Distance at which to start following
     * @param stopFollowDistance Distance at which to stop (close enough)
     */
    public AllyFollowOwnerGoal(Mob ally, double speedModifier, double startFollowDistance, double stopFollowDistance) {
        this.ally = ally;
        this.speedModifier = speedModifier;
        this.startFollowDistance = startFollowDistance;
        this.stopFollowDistance = stopFollowDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Check if this mob is an ally
        if (!AllyManager.isAlly(ally)) {
            return false;
        }
        
        // Check if command is FOLLOW
        AllyCommand command = AllyManager.getCommand(ally);
        if (command != AllyCommand.FOLLOW) {
            return false;
        }
        
        // Find the owner
        UUID ownerUUID = AllyManager.getOwnerUUID(ally);
        if (ownerUUID == null) {
            return false;
        }
        
        // Try to get the owner entity
        Player player = ally.level().getPlayerByUUID(ownerUUID);
        if (player != null) {
            this.owner = player;
            
            // Check if far enough to start following
            double distance = ally.distanceToSqr(owner);
            return distance >= startFollowDistance * startFollowDistance;
        }
        
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if no longer an ally or command changed
        if (!AllyManager.isAlly(ally)) {
            return false;
        }
        
        AllyCommand command = AllyManager.getCommand(ally);
        if (command != AllyCommand.FOLLOW) {
            return false;
        }
        
        // Stop if owner is gone
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        
        // Stop if close enough
        double distance = ally.distanceToSqr(owner);
        return distance > stopFollowDistance * stopFollowDistance;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = ally.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        ally.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0.0f);
    }

    @Override
    public void stop() {
        this.owner = null;
        ally.getNavigation().stop();
        ally.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }
        
        // Look at owner
        ally.getLookControl().setLookAt(owner, 10.0f, ally.getMaxHeadXRot());
        
        // Recalculate path periodically
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = adjustedTickDelay(10);
            
            double distance = ally.distanceToSqr(owner);
            
            // Teleport if too far away (similar to tamed wolves)
            if (distance > 144.0) { // 12 blocks squared
                teleportToOwner();
            } else {
                ally.getNavigation().moveTo(owner, speedModifier);
            }
        }
    }
    
    /**
     * Teleports the ally near the owner if they're too far away.
     */
    private void teleportToOwner() {
        // Try to find a valid position near the owner
        for (int i = 0; i < 10; ++i) {
            int xOffset = randomIntInclusive(-3, 3);
            int yOffset = randomIntInclusive(-1, 1);
            int zOffset = randomIntInclusive(-3, 3);
            
            double x = owner.getX() + xOffset;
            double y = owner.getY() + yOffset;
            double z = owner.getZ() + zOffset;
            
            if (maybeTeleportTo(x, y, z)) {
                return;
            }
        }
    }
    
    /**
     * Attempts to teleport to the specified position.
     */
    private boolean maybeTeleportTo(double x, double y, double z) {
        if (Math.abs(x - owner.getX()) < 2.0 && Math.abs(z - owner.getZ()) < 2.0) {
            return false;
        }
        
        if (!canTeleportTo(new net.minecraft.core.BlockPos((int)x, (int)y, (int)z))) {
            return false;
        }
        
        ally.moveTo(x, y, z, ally.getYRot(), ally.getXRot());
        ally.getNavigation().stop();
        return true;
    }
    
    /**
     * Checks if the position is valid for teleportation.
     */
    private boolean canTeleportTo(net.minecraft.core.BlockPos pos) {
        net.minecraft.world.level.pathfinder.BlockPathTypes pathType = 
                net.minecraft.world.level.pathfinder.WalkNodeEvaluator.getBlockPathTypeStatic(
                        ally.level(), pos.mutable());
        
        if (pathType != net.minecraft.world.level.pathfinder.BlockPathTypes.WALKABLE) {
            return false;
        }
        
        net.minecraft.core.BlockPos blockPos = pos.subtract(ally.blockPosition());
        return ally.level().noCollision(ally, ally.getBoundingBox().move(blockPos));
    }
    
    private int randomIntInclusive(int min, int max) {
        return ally.getRandom().nextInt(max - min + 1) + min;
    }
}
