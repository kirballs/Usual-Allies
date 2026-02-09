package kirballs.usualallies.entity.kirb;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * AI Goal for Kirb's inhale attack behavior.
 * Kirby will initiate inhale when a target comes within 2-3 blocks.
 */
public class KirbInhaleGoal extends Goal {
    
    private final KirbEntity kirb;
    
    // Range at which Kirb will start inhaling (in blocks)
    private static final double INHALE_START_RANGE = 3.0; // 2-3 blocks
    
    // Minimum range before Kirb needs to get closer
    private static final double MIN_ATTACK_RANGE = 2.0;
    
    // Time in ticks without capture before repositioning
    private static final int REPOSITION_TIMEOUT = 100; // 5 seconds
    
    private int repositionTimer = 0;

    public KirbInhaleGoal(KirbEntity kirb) {
        this.kirb = kirb;
        // This goal controls movement and looking
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Can use if Kirb has a target and doesn't have anything captured
        LivingEntity target = kirb.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        // Don't inhale if already has something captured
        if (kirb.hasCapturedEntity()) {
            return false;
        }
        
        // Check if target can be inhaled
        if (!kirb.canInhale(target)) {
            return false;
        }
        
        // Check if target is within range
        double distance = kirb.distanceTo(target);
        return distance <= INHALE_START_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = kirb.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        // Stop if captured an entity
        if (kirb.hasCapturedEntity()) {
            return false;
        }
        
        return true;
    }

    @Override
    public void start() {
        repositionTimer = 0;
        kirb.setInhaling(true);
    }

    @Override
    public void stop() {
        kirb.setInhaling(false);
        repositionTimer = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = kirb.getTarget();
        if (target == null) {
            return;
        }
        
        // Look at target
        kirb.getLookControl().setLookAt(target, 30.0f, 30.0f);
        
        double distance = kirb.distanceTo(target);
        
        // If inhaling but target is too far, move closer
        if (distance > INHALE_START_RANGE) {
            kirb.setInhaling(false);
            kirb.getNavigation().moveTo(target, 1.2); // Move faster to catch up
            repositionTimer++;
        } else {
            // In range, start/continue inhaling
            kirb.getNavigation().stop();
            kirb.setInhaling(true);
            repositionTimer = 0;
        }
        
        // If we've been trying to inhale for too long without success, reposition
        if (kirb.isInhaling() && repositionTimer > REPOSITION_TIMEOUT) {
            kirb.setInhaling(false);
            // Move closer to target
            kirb.getNavigation().moveTo(target, 1.0);
            repositionTimer = 0;
        }
    }
}
