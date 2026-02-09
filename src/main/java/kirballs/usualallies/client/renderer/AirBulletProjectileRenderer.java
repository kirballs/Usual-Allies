package kirballs.usualallies.client.renderer;

import kirballs.usualallies.client.model.AirBulletModel;
import kirballs.usualallies.projectile.AirBulletProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for Air Bullet projectile.
 * Renders the air puff as a 3D model with animations.
 * 
 * Required files:
 * - Model: assets/usualallies/geo/air_bullet.geo.json
 * - Texture: assets/usualallies/textures/entity/air_bullet.png
 * - Animation: assets/usualallies/animations/air_bullet.animation.json
 */
public class AirBulletProjectileRenderer extends GeoEntityRenderer<AirBulletProjectile> {

    public AirBulletProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, new AirBulletModel());
        // Shadow size - small for projectile
        this.shadowRadius = 0.15f;
    }

    @Override
    public ResourceLocation getTextureLocation(AirBulletProjectile entity) {
        return super.getTextureLocation(entity);
    }
}
