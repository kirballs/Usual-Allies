package kirballs.usualallies.client.renderer;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.projectile.StarProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renderer for Star projectile.
 * Renders a spinning star texture.
 */
public class StarProjectileRenderer extends EntityRenderer<StarProjectile> {

    // Texture location for star projectile
    // Place texture at: assets/usualallies/textures/entity/star_projectile.png
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(UsualAllies.MOD_ID, "textures/entity/star_projectile.png");

    public StarProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StarProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Scale the star
        float scale = 0.75f; // Adjust size as needed
        poseStack.scale(scale, scale, scale);

        // Make the star always face the camera (billboard)
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        
        // Apply spinning rotation
        float spinAngle = entity.getSpinAngle(partialTick);
        poseStack.mulPose(Axis.ZP.rotationDegrees(spinAngle));
        
        // Rotate to face correctly
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        // Render the star quad
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        // Star vertices (quad)
        float minU = 0.0f;
        float maxU = 1.0f;
        float minV = 0.0f;
        float maxV = 1.0f;
        float halfSize = 0.5f;

        // Draw quad facing camera with yellow tint
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, 0.0f)
                .color(255, 255, 200, 255) // Slightly yellow tint
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, 0.0f)
                .color(255, 255, 200, 255)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, 0.0f)
                .color(255, 255, 200, 255)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, 0.0f)
                .color(255, 255, 200, 255)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();

        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(StarProjectile entity) {
        return TEXTURE;
    }
}
