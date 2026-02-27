package kirballs.usualallies.client.renderer;

import kirballs.usualallies.client.model.KirbModel;
import kirballs.usualallies.client.renderer.layer.KirbFaceLayer;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for Kirb entity.
 * Handles rendering the entity with animations.
 * A {@link KirbFaceLayer} is added on top to apply the state-appropriate face texture.
 */
public class KirbRenderer extends GeoEntityRenderer<KirbEntity> {

    public KirbRenderer(EntityRendererProvider.Context context) {
        super(context, new KirbModel());
        this.shadowRadius = 0.4f;
        // Register face overlay as an additional render layer
        this.addRenderLayer(new KirbFaceLayer(this));
    }
}

