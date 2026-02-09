package kirballs.usualallies.projectile;

import kirballs.usualallies.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Star projectile - shot by Kirb when spitting out captured entities.
 * Deals 3 hearts of damage with knockback.
 * The star spins as it flies (handled in renderer).
 */
public class StarProjectile extends ThrowableProjectile {

    // Damage dealt by star in half-hearts (6 = 3 hearts)
    private static final float STAR_DAMAGE = 6.0f;
    
    // Knockback strength
    private static final float KNOCKBACK_STRENGTH = 1.5f;
    
    // Maximum lifetime in ticks (prevents infinite flying)
    private static final int MAX_LIFETIME = 60; // 3 seconds
    
    // Track age for despawning
    private int age = 0;

    public StarProjectile(EntityType<? extends StarProjectile> type, Level level) {
        super(type, level);
    }

    public StarProjectile(EntityType<? extends StarProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /**
     * Factory constructor for creating from Kirb's spit.
     */
    public StarProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.STAR_PROJECTILE.get(), shooter, level);
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
        
        // Spawn sparkle particles while flying
        if (level().isClientSide) {
            // Yellow/gold sparkle particles
            level().addParticle(ParticleTypes.CRIT,
                    this.getX() + (random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!level().isClientSide) {
            // Spawn impact particles
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.CRIT,
                        this.getX(), this.getY(), this.getZ(),
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5,
                        (random.nextDouble() - 0.5) * 0.5);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) return;
        
        Entity target = result.getEntity();
        
        if (target instanceof LivingEntity livingTarget) {
            // Don't damage the shooter
            if (target == getOwner()) {
                return;
            }
            
            // Deal damage
            DamageSource source = level().damageSources().thrown(this, getOwner());
            livingTarget.hurt(source, STAR_DAMAGE);
            
            // Apply knockback
            if (getOwner() != null) {
                double dx = target.getX() - getOwner().getX();
                double dz = target.getZ() - getOwner().getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > 0) {
                    livingTarget.knockback(KNOCKBACK_STRENGTH, -dx / distance, -dz / distance);
                }
            }
        }
        
        super.onHitEntity(result);
    }

    @Override
    protected float getGravity() {
        // Very low gravity for star projectile
        return 0.01f;
    }
    
    /**
     * Returns the current rotation angle for the spinning effect.
     * Used by the renderer for visual rotation.
     */
    public float getSpinAngle(float partialTick) {
        // Spin based on age, completing one rotation every ~15 ticks
        return ((age + partialTick) * 24.0f) % 360.0f;
    }
}
