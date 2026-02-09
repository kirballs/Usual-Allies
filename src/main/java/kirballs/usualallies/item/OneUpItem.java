package kirballs.usualallies.item;

import net.minecraft.world.item.Item;

/**
 * 1-Up item - used to give Kirb an extra life.
 * 
 * Usage:
 * - Right-click on a tamed Kirb to give them an extra life
 * - Kirb can have more than 3 lives if fed multiple 1-Ups
 * - Currently only available in creative mode tab
 */
public class OneUpItem extends Item {

    public OneUpItem(Properties properties) {
        super(properties);
    }

    // The actual feeding logic is handled in KirbEntity.mobInteract()
    // This item just needs to exist and be identifiable
}
