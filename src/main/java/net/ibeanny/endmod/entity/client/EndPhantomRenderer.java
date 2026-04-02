package net.ibeanny.endmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ibeanny.endmod.MinecraftEndExpansion;
import net.ibeanny.endmod.entity.client.EndPhantomColorLayer;
import net.ibeanny.endmod.phantom.EndPhantomEyesLayer;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

public class EndPhantomRenderer extends MobRenderer<Phantom, PhantomModel<Phantom>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MinecraftEndExpansion.MOD_ID, "textures/entity/phantom/phantom_skin.png");

    public EndPhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel<>(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
        this.addLayer(new EndPhantomColorLayer(this));
        this.addLayer(new EndPhantomHarnessLayer(this));
        this.addLayer(new EndPhantomEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Phantom phantom) {
        return TEXTURE;
    }

    @Override
    protected void scale(Phantom phantom, PoseStack poseStack, float partialTickTime) {
        int size = phantom.getPhantomSize();
        float scale = 1.0F + 0.15F * (float) size;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, 1.3125F, 0.1875F);
    }
}
