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

public class EndPhantomHarnessLayer extends RenderLayer<Phantom, PhantomModel<Phantom>> {

    private static final ResourceLocation HARNESS_TEX =
            new ResourceLocation(MinecraftEndExpansion.MOD_ID, "textures/entity/phantom/phantom_harness.png");

    public EndPhantomHarnessLayer(RenderLayerParent<Phantom, PhantomModel<Phantom>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Phantom phantom,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!(phantom instanceof EndPhantomEntity endPhantom) || !endPhantom.isHarnessed()) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(HARNESS_TEX));
        this.getParentModel().renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
