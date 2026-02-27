package kirballs.usualallies;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for all mod particle types.
 * Particle textures go in: assets/usualallies/textures/particle/
 * Particle sprite definitions go in: assets/usualallies/particles/
 */
public class ModParticles {

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, UsualAllies.MOD_ID);

    // Large air puff - inhale suction particle (big variant)
    // Texture: assets/usualallies/textures/particle/air_big.png
    public static final RegistryObject<SimpleParticleType> AIR_BIG =
            PARTICLE_TYPES.register("air_big", () -> new SimpleParticleType(false));

    // Medium air puff - inhale suction particle (medium variant)
    // Texture: assets/usualallies/textures/particle/air_medium.png
    public static final RegistryObject<SimpleParticleType> AIR_MEDIUM =
            PARTICLE_TYPES.register("air_medium", () -> new SimpleParticleType(false));

    // Small air puff - inhale suction particle (small variant)
    // Texture: assets/usualallies/textures/particle/air_small.png
    public static final RegistryObject<SimpleParticleType> AIR_SMALL =
            PARTICLE_TYPES.register("air_small", () -> new SimpleParticleType(false));
}
