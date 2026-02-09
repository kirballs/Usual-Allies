package kirballs.usualallies.client.model;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.projectile.AirBulletProjectile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Air Bullet projectile.
 * 
 * Required files:
 * - Geometry: assets/usualallies/geo/air_bullet.geo.json
 * - Texture: assets/usualallies/textures/entity/air_bullet.png
 * - Animation: assets/usualallies/animations/air_bullet.animation.json
 */
public class AirBulletModel extends GeoModel<AirBulletProjectile> {

    // Path to the geometry model file
    // Location: assets/usualallies/geo/air_bullet.geo.json
    private static final ResourceLocation MODEL = 
            new ResourceLocation(UsualAllies.MOD_ID, "geo/air_bullet.geo.json");
    
    // Path to the texture file
    // Location: assets/usualallies/textures/entity/air_bullet.png
    private static final ResourceLocation TEXTURE = 
            new ResourceLocation(UsualAllies.MOD_ID, "textures/entity/air_bullet.png");
    
    // Path to the animation file
    // Location: assets/usualallies/animations/air_bullet.animation.json
    private static final ResourceLocation ANIMATION = 
            new ResourceLocation(UsualAllies.MOD_ID, "animations/air_bullet.animation.json");

    @Override
    public ResourceLocation getModelResource(AirBulletProjectile entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AirBulletProjectile entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AirBulletProjectile entity) {
        return ANIMATION;
    }
}
