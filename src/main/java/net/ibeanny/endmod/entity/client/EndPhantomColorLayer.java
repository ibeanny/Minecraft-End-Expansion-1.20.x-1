package net.ibeanny.endmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ibeanny.endmod.MinecraftEndExpansion;
import net.ibeanny.endmod.entity.EndPhantomEntity;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.DyeColor;

public class EndPhantomColorLayer extends RenderLayer<Phantom, PhantomModel<Phantom>> {

    private static final ResourceLocation COLOR_MASK =
            new ResourceLocation(MinecraftEndExpansion.MOD_ID, "textures/entity/phantom/phantom_color_mask.png");

    public EndPhantomColorLayer(RenderLayerParent<Phantom, PhantomModel<Phantom>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Phantom phantom,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        DyeColor color = phantom instanceof EndPhantomEntity endPhantom ? endPhantom.getColor().orElse(null) : null;
        if (color == null) {
            return;
        }

        float[] rgb = color.getTextureDiffuseColors();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(COLOR_MASK));
        this.getParentModel().renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                rgb[0],
                rgb[1],
                rgb[2],
                0.72F
        );
    }
}
