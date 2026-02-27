package kirballs.usualallies.entity.kirb;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.network.EmptyClickPacket;
import kirballs.usualallies.network.ModNetwork;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/**
 * Event handler for Kirb's carry/throw mechanic and the
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
public class KirbCarryEvents {

    // -------------------------------------------------------------------------
    // Client-side forwarding for empty-click events
    // -------------------------------------------------------------------------

    @Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) return;
            ModNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new EmptyClickPacket(true));
        }

        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) return;
            ModNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(), new EmptyClickPacket(false));
        }
    }

    // -------------------------------------------------------------------------
    // Server-side handlers
    // -------------------------------------------------------------------------

    public static void handleEmptyClickServer(Player player, boolean rightClick) {
        if (player.level().isClientSide) return;

        if (rightClick) {
            if (!player.getMainHandItem().isEmpty()) return;

            // Priority 1: throw held Kirb
            KirbEntity held = findHeldKirb(player);
            if (held != null) {
                held.throwKirb(player);
                return;
            }
        }

        // Priority 2: count as escape input if player is captured
        KirbEntity captor = findCaptorKirb(player);
        if (captor != null) {
            captor.onCapturedPlayerAction();
        }
    }

    // -------------------------------------------------------------------------
    // Escape mash – attack (punch entity)
    // -------------------------------------------------------------------------

    @Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {

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
