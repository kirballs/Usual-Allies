package kirballs.usualallies.projectile;

import kirballs.usualallies.ModEntities;
import kirballs.usualallies.ModSounds;
import kirballs.usualallies.util.AllyManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Friend Heart projectile - thrown to befriend hostile mobs.
 * On impact, makes the target mob neutral/friendly to the player.
 */
public class FriendHeartProjectile extends ThrowableProjectile {

    // Maximum travel distance in blocks before despawning (4 blocks)
    private static final double MAX_DISTANCE = 4.0;
    
    // Starting position for distance tracking
    private double startX, startY, startZ;
    private boolean hasStartPos = false;

    public FriendHeartProjectile(EntityType<? extends FriendHeartProjectile> type, Level level) {
        super(type, level);
    }

    public FriendHeartProjectile(EntityType<? extends FriendHeartProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /**
     * Factory constructor for creating from item use.
     */
    public FriendHeartProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.FRIEND_HEART_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData() {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();
        
        // Track starting position
        if (!hasStartPos) {
            startX = this.getX();
            startY = this.getY();
            startZ = this.getZ();
            hasStartPos = true;
        }
        
        // Calculate distance traveled
        double dx = this.getX() - startX;
        double dy = this.getY() - startY;
        double dz = this.getZ() - startZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Despawn if exceeded max distance
        if (distance > MAX_DISTANCE) {
            this.discard();
            return;
        }
        
        // Spawn heart particles while flying
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.HEART,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) return;
        
        if (result.getEntity() instanceof LivingEntity target && !(target instanceof Player)) {
            // Get the player who threw this
            if (getOwner() instanceof Player player) {
                // Befriend the mob
                befriendMob(target, player);
            }
        }
        
        super.onHitEntity(result);
    }

    /**
     * Makes a mob friendly to the player.
     * Creates heart particle burst effect and plays sound.
     */
    private void befriendMob(LivingEntity target, Player player) {
        // Add to ally manager
        AllyManager.addAlly(player, target);
        
        // Play friend heart hit sound
        level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.FRIEND_HEART_HIT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Spawn heart burst particles
        if (level() instanceof ServerLevel serverLevel) {
            // Create burst of hearts in different directions
            for (int i = 0; i < 8; i++) { // 8 heart particles in a burst
                double angle = (2 * Math.PI / 8) * i;
                double xOffset = Math.cos(angle) * 0.5;
                double zOffset = Math.sin(angle) * 0.5;
                
                serverLevel.sendParticles(ParticleTypes.HEART,
                        target.getX() + xOffset,
                        target.getY() + target.getBbHeight() / 2,
                        target.getZ() + zOffset,
                        1, // count
                        xOffset * 0.5, 0.2, zOffset * 0.5, // offset/spread
                        0.1); // speed
            }
        }
        
        // If target is a Mob, clear its target
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    @Override
    protected float getGravity() {
        // Slight gravity for arc trajectory
        return 0.03f; // Lower than normal projectiles for floaty feel
    }
}
