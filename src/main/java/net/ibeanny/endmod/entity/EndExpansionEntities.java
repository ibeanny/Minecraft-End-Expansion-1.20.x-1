package net.ibeanny.endmod.entity;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EndExpansionEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MinecraftEndExpansion.MOD_ID);

    public static final RegistryObject<EntityType<EndPhantomEntity>> END_PHANTOM = ENTITY_TYPES.register(
            "end_phantom",
            () -> EntityType.Builder.<EndPhantomEntity>of(EndPhantomEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.5F)
                    .clientTrackingRange(8)
                    .build("end_phantom")
    );

    private EndExpansionEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(END_PHANTOM.get(), Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0D)
                    .add(Attributes.FOLLOW_RANGE, 32.0D)
                    .add(Attributes.ATTACK_DAMAGE, 6.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)
                    .build());
        }
    }
}
