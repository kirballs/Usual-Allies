package kirballs.usualallies.client.renderer;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.client.model.KirbModel;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for Kirb entity.
 * Handles rendering the entity with animations.
 */
public class KirbRenderer extends GeoEntityRenderer<KirbEntity> {

    public KirbRenderer(EntityRendererProvider.Context context) {
        super(context, new KirbModel());
        // Shadow size - adjust based on entity size
        this.shadowRadius = 0.4f; // Slightly smaller than player shadow
    }

    @Override
    public ResourceLocation getTextureLocation(KirbEntity entity) {
        // Delegate to model for texture selection
        return super.getTextureLocation(entity);
    }

    @Override
    public void render(KirbEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // Scale adjustment if needed
        // Default scale is 1.0f
        // poseStack.scale(1.0f, 1.0f, 1.0f);
        
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
