package kirballs.usualallies;

import kirballs.usualallies.client.particle.InhaleParticle;
import kirballs.usualallies.client.renderer.AirBulletProjectileRenderer;
import kirballs.usualallies.client.renderer.FriendHeartProjectileRenderer;
import kirballs.usualallies.client.renderer.KirbRenderer;
import kirballs.usualallies.client.renderer.StarProjectileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(UsualAllies.MOD_ID)
public class UsualAllies {

    public static final String MOD_ID = "usualallies";
    private static final Logger LOGGER = LoggerFactory.getLogger(UsualAllies.class);

    public UsualAllies() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Usual Allies mod initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Usual Allies common setup complete!");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.KIRB.get(), KirbRenderer::new);
            EntityRenderers.register(ModEntities.FRIEND_HEART_PROJECTILE.get(), FriendHeartProjectileRenderer::new);
            EntityRenderers.register(ModEntities.STAR_PROJECTILE.get(), StarProjectileRenderer::new);
            EntityRenderers.register(ModEntities.AIR_BULLET_PROJECTILE.get(), AirBulletProjectileRenderer::new);
            LOGGER.info("Usual Allies client setup complete!");
        }

        // Register inhale-particle providers so the engine knows which sprite to use
        @SubscribeEvent
        public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.AIR_BIG.get(),    InhaleParticle.BigProvider::new);
            event.registerSpriteSet(ModParticles.AIR_MEDIUM.get(), InhaleParticle.MediumProvider::new);
            event.registerSpriteSet(ModParticles.AIR_SMALL.get(),  InhaleParticle.SmallProvider::new);
        }

        private static final Logger LOGGER = LoggerFactory.getLogger(ClientModEvents.class);
    }
}

