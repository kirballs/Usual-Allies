package kirballs.usualallies.util;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for player interactions with allied mobs.
 * Handles command cycling (follow/stay/wander/patrol) for generic allies
 * that were befriended via Friend Heart.
 */
@Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID)
public class AllyInteractionHandler {

    /**
     * Handles right-click interactions with allied mobs.
     * Allows cycling through commands when the player right-clicks with an empty hand.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        
        // Only handle on server side
        if (player.level().isClientSide) {
            return;
        }
        
        // Check if target is a living entity
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        
        // Skip if this is a KirbEntity (handled by its own mobInteract)
        if (target instanceof KirbEntity) {
            return;
        }
        
        // Check if the target is an ally of this player
        if (!AllyManager.isAllyOf(player, target)) {
            return;
        }
        
        // Check if player has empty main hand
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }
        
        // Cycle the command
        AllyCommand newCommand = AllyManager.cycleCommand(target);
        
        // Apply command effects if it's a Mob
        if (target instanceof Mob mob) {
            applyCommandToMob(mob, newCommand);
        }
        
        // Send message to player
        String entityName = target.getDisplayName().getString();
        player.displayClientMessage(
                Component.translatable("entity.usualallies.ally.command." + newCommand.name().toLowerCase(), entityName),
                true); // true = action bar message
        
        // Mark the event as handled
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
    
    /**
     * Applies the command effects to a mob's AI.
     */
    private static void applyCommandToMob(Mob mob, AllyCommand command) {
        switch (command) {
            case STAY:
                // Stop navigation and make the mob sit/stay
                mob.getNavigation().stop();
                break;
            case FOLLOW:
                // Enable normal following behavior (handled by AllyFollowOwnerGoal)
                break;
            case WANDER:
                // Enable wandering behavior (uses vanilla wandering)
                break;
            case PATROL:
                // Set patrol center to current position
                // Store in persistent data for the patrol goal to use
                mob.getPersistentData().putIntArray("PatrolCenter", 
                        new int[]{mob.blockPosition().getX(), 
                                  mob.blockPosition().getY(), 
                                  mob.blockPosition().getZ()});
                break;
        }
    }
}
