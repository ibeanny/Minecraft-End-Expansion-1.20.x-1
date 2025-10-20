package net.ibeanny.endmod.phantom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.client.model.PhantomModel;

public class EndPhantomEyesLayer extends RenderLayer<Phantom, PhantomModel<Phantom>> {

    // resources/assets/endexpansionmod/textures/entity/phantom/phantom_end_eyes.png
    private static final ResourceLocation EYES_TEX =
            new ResourceLocation(MinecraftEndExpansion.MOD_ID, "textures/entity/phantom/phantom_skin.png");

    public EndPhantomEyesLayer(RenderLayerParent<Phantom, PhantomModel<Phantom>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose,
                       MultiBufferSource buffer,
                       int packedLight,
                       Phantom phantom,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        // Only render glow in the End
        if (phantom.level().dimension() != Level.END)
            return;

        VertexConsumer vc = buffer.getBuffer(RenderType.eyes(EYES_TEX));
        // packedLight set to full-bright by eyes() anyway, overlay off
        this.getParentModel().renderToBuffer(pose, vc, 0xF000F0, OverlayTexture.NO_OVERLAY,
                1f, 1f, 1f, 1f);
    }
}
