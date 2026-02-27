package kirballs.usualallies.client;

import kirballs.usualallies.ModParticles;
import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handler.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Spawn inhale-suction particles for all currently-inhaling Kirb entities.</li>
 * </ul>
 * </p>
 *
 * <p>Particle rate: ~130 BPM ≈ 1 particle every 9 ticks.
 * We cycle through the three size variants and add slight random scatter
 * so the effect looks organic rather than mechanical.</p>
 */
@Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    // Ticks between successive particle spawns (130 BPM ≈ 9.2 ticks per beat).
    private static final int PARTICLE_INTERVAL = 9;

    // Cyclic counter used to rotate through the three variants evenly.
    private static int variantCycle = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof KirbEntity kirb)) continue;
            if (!kirb.isInhaling()) continue;

            // Only spawn on every PARTICLE_INTERVAL tick to hit ~130 BPM
            long tick = mc.level.getGameTime();
            // Stagger each Kirb using its UUID hash so multiple nearby Kirbs
            // don't all fire particles on the same tick
            if (((tick + (kirb.getUUID().hashCode() & 0x7FFFFFFF)) % PARTICLE_INTERVAL) != 0) continue;

            spawnInhaleParticles(mc, kirb);
        }
    }

    /**
     * Spawns one randomly-sized suction particle ~2-3 blocks in front of {@code kirb}.
     * The particle's initial velocity is aimed straight at Kirb's centre so it
     * appears to be sucked into the mouth.
     */
    private static void spawnInhaleParticles(Minecraft mc, KirbEntity kirb) {
        Vec3 look = kirb.getLookAngle();

        // Random distance between 2 and 3 blocks in front of Kirb
        double dist = 2.0 + kirb.getRandom().nextDouble();

        // Spread: ±0.4 blocks perpendicular to look direction so particles
        // fan out into a loose cloud rather than a thin line
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 up     = new Vec3(0, 1, 0);
        double spreadH = (kirb.getRandom().nextDouble() - 0.5) * 0.8;
        double spreadV = (kirb.getRandom().nextDouble() - 0.5) * 0.5;

        Vec3 spawnPos = kirb.position()
                .add(0, kirb.getBbHeight() * 0.5, 0)   // eye-ish height
                .add(look.scale(dist))
                .add(right.scale(spreadH))
                .add(up.scale(spreadV));

        // Target: Kirb's centre (mouth area)
        Vec3 target = kirb.position().add(0, kirb.getBbHeight() * 0.5, 0);
        Vec3 toKirb = target.subtract(spawnPos);
        double travelDist = toKirb.length();

        // Speed so the particle reaches Kirb in ~12 ticks
        double speed = travelDist / 12.0;
        Vec3 vel = toKirb.normalize().scale(speed);

        // Pick particle type by cycling through the three variants
        SimpleParticleType type = switch (variantCycle % 3) {
            case 0  -> ModParticles.AIR_BIG.get();
            case 1  -> ModParticles.AIR_MEDIUM.get();
            default -> ModParticles.AIR_SMALL.get();
        };
        variantCycle++;

        mc.level.addParticle(type,
                spawnPos.x, spawnPos.y, spawnPos.z,
                vel.x, vel.y, vel.z);
    }

    // -------------------------------------------------------------------------
    // Black-out screen while the local player is captured inside Kirb's mouth
    // -------------------------------------------------------------------------

    /**
     * Fires after the 3-D world is rendered but before the HUD, so the black
     * fill covers the world view while the hotbar, health bar, etc. remain
     * visible on top – identical to the behaviour when suffocating inside a block.
     */
    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int localId = mc.player.getId();
        boolean captured = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof KirbEntity kirb
                    && kirb.getCapturedEntityId() == localId) {
                captured = true;
                break;
            }
        }
        if (!captured) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        // Solid black – matches the opaque feel of suffocating in a block
        graphics.fill(0, 0, w, h, 0xFF000000);
    }
}
