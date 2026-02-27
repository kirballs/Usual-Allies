package kirballs.usualallies.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Client-side particle for Kirb's inhale suction effect.
 * Spawned in front of Kirb and flies straight toward him (velocity is preset at spawn).
 * Three visual variants share this class: air_big, air_medium, air_small.
 */
public class InhaleParticle extends TextureSheetParticle {

    protected InhaleParticle(ClientLevel level, double x, double y, double z,
                              double xSpeed, double ySpeed, double zSpeed,
                              SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        // Pick a random sprite from the set
        this.pickSprite(sprites);
        // Lifetime: long enough to reach Kirb (~2.5 blocks at 0.22/tick ≈ 12 ticks)
        this.lifetime = 14;
        this.gravity = 0.0f;
        this.hasPhysics = false;
        // No drag so velocity stays constant
        this.friction = 1.0f;
        // Slight alpha fade at the end
        this.alpha = 0.85f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Fade out in the last 4 ticks
        if (this.age > this.lifetime - 4) {
            this.alpha = Math.max(0f, this.alpha - 0.22f);
        }

        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    // ---- Provider factories for each variant ----

    /** Provider for the large air puff variant. */
    public record BigProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            InhaleParticle p = new InhaleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            p.setSize(0.18f, 0.18f);
            return p;
        }
    }

    /** Provider for the medium air puff variant. */
    public record MediumProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            InhaleParticle p = new InhaleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            p.setSize(0.12f, 0.12f);
            return p;
        }
    }

    /** Provider for the small air puff variant. */
    public record SmallProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            InhaleParticle p = new InhaleParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
            p.setSize(0.07f, 0.07f);
            return p;
        }
    }
}
