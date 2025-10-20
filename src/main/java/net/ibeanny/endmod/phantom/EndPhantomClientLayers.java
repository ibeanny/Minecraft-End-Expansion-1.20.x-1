package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EndPhantomClientLayers {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer<? extends Phantom> r = event.getRenderer(EntityType.PHANTOM);
        if (r instanceof PhantomRenderer phantomRenderer) {
            phantomRenderer.addLayer(new EndPhantomEyesLayer(phantomRenderer));
        }
    }
}
