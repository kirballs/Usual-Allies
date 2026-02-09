package kirballs.usualallies.client.model;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Kirb entity.
 * References the geometry JSON, texture, and animation files.
 */
public class KirbModel extends GeoModel<KirbEntity> {

    // Path to the geometry model file
    // Location: assets/usualallies/geo/kirb.geo.json
    private static final ResourceLocation MODEL = new ResourceLocation(UsualAllies.MOD_ID, "geo/kirb.geo.json");
    
    // Path to the animation file
    // Location: assets/usualallies/animations/kirb.animation.json
    private static final ResourceLocation ANIMATION = new ResourceLocation(UsualAllies.MOD_ID, "animations/kirb.animation.json");

    @Override
    public ResourceLocation getModelResource(KirbEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KirbEntity entity) {
        // Determine texture based on Kirb's state
        String texturePath = getTexturePathForEntity(entity);
        return new ResourceLocation(UsualAllies.MOD_ID, texturePath);
    }

    @Override
    public ResourceLocation getAnimationResource(KirbEntity entity) {
        return ANIMATION;
    }
    
    /**
     * Determines the correct texture path based on Kirb's current state.
     * Textures should be placed in: assets/usualallies/textures/entity/kirb/
     * 
     * Texture files needed:
     * - kirb_body.png (or kirb_body_[color].png for dyed versions)
     * - kirb_face_default.png (spawned, not befriended)
     * - kirb_face_full.png (befriended, full health)
     * - kirb_face_medium.png (befriended, medium health)
     * - kirb_face_low.png (befriended, low health)
     * 
     * The body and face are separate layers combined in the final texture.
     */
    private String getTexturePathForEntity(KirbEntity entity) {
        // Base path for textures
        String basePath = "textures/entity/kirb/";
        
        // Get color suffix if dyed (-1 = default pink, no suffix)
        int color = entity.getColor();
        String colorSuffix = "";
        if (color >= 0) {
            // Use dye color names: white, orange, magenta, light_blue, yellow, lime, pink, gray,
            // light_gray, cyan, purple, blue, brown, green, red, black
            String[] colorNames = {"white", "orange", "magenta", "light_blue", "yellow", "lime",
                    "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
            if (color < colorNames.length) {
                colorSuffix = "_" + colorNames[color];
            }
        }
        
        // Main texture path - body texture includes color variant
        // For now, return the main body texture
        // Face overlay is handled separately if your model supports it
        return basePath + "kirb" + colorSuffix + ".png";
    }
}
