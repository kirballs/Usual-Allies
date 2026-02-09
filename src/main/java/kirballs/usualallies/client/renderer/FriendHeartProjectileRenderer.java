package kirballs.usualallies.client.renderer;

import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.projectile.FriendHeartProjectile;
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
 * Renderer for Friend Heart projectile.
 * Renders a heart-shaped texture that floats through the air.
 */
public class FriendHeartProjectileRenderer extends EntityRenderer<FriendHeartProjectile> {

    // Texture location for friend heart projectile
    // Place texture at: assets/usualallies/textures/entity/friend_heart.png
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(UsualAllies.MOD_ID, "textures/entity/friend_heart.png");

    public FriendHeartProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FriendHeartProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Scale the heart
        float scale = 0.5f; // Adjust size as needed
        poseStack.scale(scale, scale, scale);

        // Make the heart always face the camera (billboard)
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        
        // Rotate to face correctly
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        // Render the heart quad
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        // Heart vertices (quad)
        float minU = 0.0f;
        float maxU = 1.0f;
        float minV = 0.0f;
        float maxV = 1.0f;
        float halfSize = 0.5f;

        // Draw quad facing camera
        vertexConsumer.vertex(matrix4f, -halfSize, -halfSize, 0.0f)
                .color(255, 255, 255, 255)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, -halfSize, 0.0f)
                .color(255, 255, 255, 255)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, halfSize, halfSize, 0.0f)
                .color(255, 255, 255, 255)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
        vertexConsumer.vertex(matrix4f, -halfSize, halfSize, 0.0f)
                .color(255, 255, 255, 255)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();

        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FriendHeartProjectile entity) {
        return TEXTURE;
    }
}
