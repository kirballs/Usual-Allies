package kirballs.usualallies.entity.kirb;

import kirballs.usualallies.UsualAllies;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Server-side event handler for Kirb's carry/throw mechanic and the
 * player-escape-from-mouth mechanic.
 *
 * Carry/throw:
 *   Right-clicking empty space with empty main hand while Kirb is in
 *   CARRY_HELD state launches Kirb as a projectile.
 *
 * Player escape:
 *   While a player is captured inside Kirb's mouth, any of the following
 *   inputs count as one "mash" toward the 10-15 needed to break free:
 *     - Sprint / sneak toggle  (tracked inside KirbEntity.trackPlayerEscape)
 *     - Left-click (punch air OR attack an entity)
 *     - Right-click empty space
 */
@Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KirbCarryEvents {

    // -------------------------------------------------------------------------
    // Throw mechanic
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.getMainHandItem().isEmpty()) return;

        // Priority 1: throw held Kirb
        KirbEntity held = findHeldKirb(player);
        if (held != null) {
            held.throwKirb(player);
            event.setCanceled(true);
            return;
        }

        // Priority 2: count as escape input if player is captured
        KirbEntity captor = findCaptorKirb(player);
        if (captor != null) {
            captor.onCapturedPlayerAction();
        }
    }

    // -------------------------------------------------------------------------
    // Escape mash – left-click (punch air)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        KirbEntity captor = findCaptorKirb(player);
        if (captor != null) {
            captor.onCapturedPlayerAction();
        }
    }

    // -------------------------------------------------------------------------
    // Escape mash – attack (punch entity)
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // We want the case where a player swings and the attack source is the player
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        KirbEntity captor = findCaptorKirb(player);
        if (captor != null) {
            captor.onCapturedPlayerAction();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Finds a {@link KirbEntity} in CARRY_HELD state belonging to {@code player}. */
    private static KirbEntity findHeldKirb(Player player) {
        AABB area = player.getBoundingBox().inflate(1.5);
        List<KirbEntity> kirbs = player.level().getEntitiesOfClass(KirbEntity.class, area,
                k -> k.getCarryState() == KirbEntity.CARRY_HELD
                        && k.getHoldingPlayerId() == player.getId());
        return kirbs.isEmpty() ? null : kirbs.get(0);
    }

    /**
     * Finds a {@link KirbEntity} that currently has {@code player} as its
     * captured entity.  The player's position is forced to match Kirb's each
     * tick, so a small search radius is sufficient.
     */
    private static KirbEntity findCaptorKirb(Player player) {
        AABB area = player.getBoundingBox().inflate(3.0);
        List<KirbEntity> kirbs = player.level().getEntitiesOfClass(KirbEntity.class, area,
                k -> k.getCapturedEntity() == player);
        return kirbs.isEmpty() ? null : kirbs.get(0);
    }
}
