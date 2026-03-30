package net.ibeanny.endmod.item;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EndExpansionItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MinecraftEndExpansion.MOD_ID);

    public static final RegistryObject<Item> VOID_MORSEL = ITEMS.register(
            "void_morsel",
            () -> new Item(new Item.Properties().stacksTo(16))
    );

    public static final RegistryObject<Item> PHANTOM_HARNESS = ITEMS.register(
            "phantom_harness",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    private EndExpansionItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
