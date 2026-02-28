package kirballs.usualallies.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kirballs.usualallies.UsualAllies;
import kirballs.usualallies.entity.kirb.KirbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Render layer that draws the Kirb face texture as an overlay on top of the body.
 *
 * Face textures are 64x64 PNGs with data only at the face-bone UV area
 * (approximately [34,48] to [49,64]) and transparent everywhere else.
 * The body renderer handles all bones with the body texture; this layer
 * re-renders the full model with the state-appropriate face texture so only
 * the face area shows through.
 *
 * Face textures live in: assets/usualallies/textures/entity/kirb/face/
 *   face_o.png        - default (unfriended) / floating medium health
 *   face_happy.png    - befriended, high health
 *   face_mouthful.png - entity captured in mouth
 *   face_open.png     - inhaling / expelling star
 *   face_lowhp.png    - low health
 *   face_blowout.png  - blowing out air bullet after flight
 */
public class KirbFaceLayer extends GeoRenderLayer<KirbEntity> {

    private static final String FACE_BASE = "textures/entity/kirb/face/";

    // Face state constants (must match KirbEntity.FACE_STATE_* constants)
    public static final int FACE_O        = 0;
    public static final int FACE_HAPPY    = 1;
    public static final int FACE_MOUTHFUL = 2;
    public static final int FACE_OPEN     = 3;
    public static final int FACE_LOWHP    = 4;
    public static final int FACE_BLOWOUT  = 5;

    public KirbFaceLayer(GeoRenderer<KirbEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, KirbEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {

        ResourceLocation faceTex = getFaceTexture(animatable);
        RenderType faceRenderType = RenderType.entityTranslucentCull(faceTex);
        VertexConsumer faceBuffer = bufferSource.getBuffer(faceRenderType);

        // Re-render the full model with the face texture.
        // Since the face texture is transparent outside the face-bone UV region,
        // only the face plane will actually be visible.
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                faceRenderType, faceBuffer, partialTick, packedLight, packedOverlay,
                1f, 1f, 1f, 1f);
    }

    private ResourceLocation getFaceTexture(KirbEntity entity) {
        String name = switch (entity.getFaceState()) {
            case FACE_HAPPY    -> "face_happy";
            case FACE_MOUTHFUL -> "face_mouthful";
            case FACE_OPEN     -> "face_open";
            case FACE_LOWHP    -> "face_lowhp";
            case FACE_BLOWOUT  -> "face_blowout";
            default            -> "face_o";  // FACE_O (0) and any unfriended state
        };
        return new ResourceLocation(UsualAllies.MOD_ID, FACE_BASE + name + ".png");
    }
}
