package kirballs.usualallies.projectile;

import kirballs.usualallies.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Air Bullet projectile - exhaled by Kirb after flying.
 * Short range projectile that disappears after one second.
 * Falls down to the floor.
 */
public class AirBulletProjectile extends ThrowableProjectile {

    // Maximum lifetime in ticks (1 second = 20 ticks)
    private static final int MAX_LIFETIME = 20;
    
    // Track age for despawning
    private int age = 0;

    public AirBulletProjectile(EntityType<? extends AirBulletProjectile> type, Level level) {
        super(type, level);
    }

    public AirBulletProjectile(EntityType<? extends AirBulletProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /**
     * Factory constructor for creating from Kirb's exhale.
     */
    public AirBulletProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.AIR_BULLET_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData() {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();
        
        age++;
        
        // Despawn after max lifetime
        if (age > MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        // Spawn puff particles while flying
        if (level().isClientSide) {
            // White cloud/puff particles
            level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + (random.nextDouble() - 0.5) * 0.2,
                    this.getY() + (random.nextDouble() - 0.5) * 0.2,
                    this.getZ() + (random.nextDouble() - 0.5) * 0.2,
                    0, -0.05, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!level().isClientSide) {
            // Spawn puff on impact
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.CLOUD,
                        this.getX(), this.getY(), this.getZ(),
                        (random.nextDouble() - 0.5) * 0.2,
                        0.1,
                        (random.nextDouble() - 0.5) * 0.2);
            }
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        // Higher gravity so it falls to floor
        return 0.08f;
    }
}
