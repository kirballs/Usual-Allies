package kirballs.usualallies.entity.ai;

import kirballs.usualallies.util.AllyCommand;
import kirballs.usualallies.util.AllyManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * AI Goal for allied mobs to stay in place.
 * Only active when the ally's command is set to STAY.
 */
public class AllyStayGoal extends Goal {

    private final Mob ally;

    /**
     * Creates a new stay goal for an allied mob.
     * 
     * @param ally The allied mob
     */
    public AllyStayGoal(Mob ally) {
        this.ally = ally;
        // This goal blocks movement
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // Check if this mob is an ally
        if (!AllyManager.isAlly(ally)) {
            return false;
        }
        
        // Check if command is STAY
        AllyCommand command = AllyManager.getCommand(ally);
        return command == AllyCommand.STAY;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        // Stop any current navigation
        ally.getNavigation().stop();
    }

    @Override
    public void tick() {
        // Keep stopping movement
        ally.getNavigation().stop();
        // Reset movement
        ally.setDeltaMovement(ally.getDeltaMovement().multiply(0, 1, 0));
    }
}
