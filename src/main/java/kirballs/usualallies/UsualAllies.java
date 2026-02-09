package kirballs.usualallies;

import kirballs.usualallies.client.renderer.KirbRenderer;
import kirballs.usualallies.client.renderer.FriendHeartProjectileRenderer;
import kirballs.usualallies.client.renderer.StarProjectileRenderer;
import kirballs.usualallies.client.renderer.AirBulletProjectileRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The mod ID - must match the mod_id in gradle.properties and mods.toml
@Mod(UsualAllies.MOD_ID)
public class UsualAllies {
    // The mod ID constant - change this if you want a different mod ID
    public static final String MOD_ID = "usualallies";
    
    // Logger for debugging
    private static final Logger LOGGER = LoggerFactory.getLogger(UsualAllies.class);

    public UsualAllies() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all registry classes
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Register common setup event
        modEventBus.addListener(this::commonSetup);

        // Register to the Forge event bus for game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Usual Allies mod initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Usual Allies common setup complete!");
    }

    // Client-side setup events
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Register entity renderers
            EntityRenderers.register(ModEntities.KIRB.get(), KirbRenderer::new);
            EntityRenderers.register(ModEntities.FRIEND_HEART_PROJECTILE.get(), FriendHeartProjectileRenderer::new);
            EntityRenderers.register(ModEntities.STAR_PROJECTILE.get(), StarProjectileRenderer::new);
            EntityRenderers.register(ModEntities.AIR_BULLET_PROJECTILE.get(), AirBulletProjectileRenderer::new);
            
            LOGGER.info("Usual Allies client setup complete!");
        }
        
        private static final Logger LOGGER = LoggerFactory.getLogger(ClientModEvents.class);
    }
}
