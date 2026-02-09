package kirballs.usualallies;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry class for mod creative tabs.
 * The Usual Allies creative tab contains all mod items.
 */
public class ModCreativeTabs {
    // Deferred register for creative mode tabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UsualAllies.MOD_ID);

    // Usual Allies creative tab - contains all mod items
    // Tab icon uses the Kirb spawn egg
    // Tab name: "Usual Allies" (defined in translation key)
    public static final RegistryObject<CreativeModeTab> USUAL_ALLIES_TAB =
            CREATIVE_MODE_TABS.register("usual_allies_tab", () -> CreativeModeTab.builder()
                    // Tab icon - change this to use a different item as the icon
                    .icon(() -> new ItemStack(ModItems.KIRB_SPAWN_EGG.get()))
                    // Tab title - uses translation key "itemGroup.usualallies.usual_allies_tab"
                    .title(Component.translatable("itemGroup." + UsualAllies.MOD_ID + ".usual_allies_tab"))
                    // Items to display in the tab
                    .displayItems((parameters, output) -> {
                        // Add all mod items to the creative tab
                        output.accept(ModItems.KIRB_SPAWN_EGG.get());
                        output.accept(ModItems.FRIEND_HEART.get());
                        output.accept(ModItems.ONE_UP.get());
                        // Add more items here as they are created
                    })
                    .build());
}
