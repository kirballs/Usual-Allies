package kirballs.usualallies.client.model;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Kirb entity.
 * References the geometry JSON, texture, and animation files.
 *
 * Textures are split into two layers:
 *   Body  – textures/entity/kirb/body/body_(color).png
 *           Default (pink) uses body_default.png.
 *   Face overlay – rendered by KirbFaceLayer as a second pass
 *                  using a state-appropriate face texture.
 */
public class KirbModel extends GeoModel<KirbEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(UsualAllies.MOD_ID, "geo/kirb.geo.json");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(UsualAllies.MOD_ID, "animations/kirb.animation.json");

    // Dye color names ordered by DyeColor ordinal
    private static final String[] COLOR_NAMES = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue",
            "brown", "green", "red", "black"
    };

    @Override
    public ResourceLocation getModelResource(KirbEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KirbEntity entity) {
        return new ResourceLocation(UsualAllies.MOD_ID, getBodyTexturePath(entity));
    }

    @Override
    public ResourceLocation getAnimationResource(KirbEntity entity) {
        return ANIMATION;
    }

    /**
     * Returns the body texture path for {@code entity}.
     * Format: {@code textures/entity/kirb/body/body_(color).png}.
     * Default pink uses {@code body_default.png}.
     */
    private String getBodyTexturePath(KirbEntity entity) {
        int color = entity.getColor();
        String colorName = "default";
        if (color >= 0 && color < COLOR_NAMES.length) {
            colorName = COLOR_NAMES[color];
        }
        return "textures/entity/kirb/body/body_" + colorName + ".png";
    }
}

