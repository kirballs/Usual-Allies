package kirballs.usualallies;

import kirballs.usualallies.item.FriendHeartItem;
import kirballs.usualallies.item.OneUpItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for all mod items.
 * Add new items here by creating new RegistryObject entries.
 */
public class ModItems {
    // Deferred register for items
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UsualAllies.MOD_ID);

    // Kirb spawn egg
    // Primary and secondary colors for the spawn egg (in hex format converted to int)
    // Change these colors to customize the spawn egg appearance
    public static final RegistryObject<Item> KIRB_SPAWN_EGG =
            ITEMS.register("kirb_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.KIRB,
                    0xFFB7C5, // Primary color (pink) - Kirby's body color
                    0xFF69B4, // Secondary color (hot pink) - Kirby's feet color
                    new Item.Properties()));

    // Friend Heart item - used to befriend hostile mobs
    // Can be thrown like a projectile to make mobs friendly
    public static final RegistryObject<Item> FRIEND_HEART =
            ITEMS.register("friend_heart", () -> new FriendHeartItem(new Item.Properties()
                    .stacksTo(16))); // Max stack size - adjust as needed

    // 1-Up item - gives Kirb an extra life when fed to him
    public static final RegistryObject<Item> ONE_UP =
            ITEMS.register("one_up", () -> new OneUpItem(new Item.Properties()
                    .stacksTo(64))); // Max stack size for 1-Up items
}
