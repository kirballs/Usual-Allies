package kirballs.usualallies;

import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for entity-related mod events.
 * Registers entity attributes for custom entities.
 */
@Mod.EventBusSubscriber(modid = UsualAllies.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEvents {

    /**
     * Registers entity attributes.
     * This is required for all living entities to function properly.
     */
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Register Kirb attributes
        event.put(ModEntities.KIRB.get(), KirbEntity.createAttributes().build());
    }
}
