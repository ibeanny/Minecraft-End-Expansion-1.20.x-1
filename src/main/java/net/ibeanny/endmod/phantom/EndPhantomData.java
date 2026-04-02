package net.ibeanny.endmod.phantom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.monster.Phantom;

import java.util.Optional;

public final class EndPhantomData {

    public static final String END_PHANTOM_TAG = "endexpansionmod.end_phantom";
    public static final String ENHANCED_END_PHANTOM_TAG = "endexpansionmod.end_buffed";
    public static final String TAMED_TAG = "endexpansionmod.tamed";
    public static final String OWNER_TAG = "endexpansionmod.owner";
    public static final String HARNESS_TAG = "endexpansionmod.harnessed";
    public static final String STRIKE_COOLDOWN_TAG = "endexpansionmod.strike_cooldown";
    public static final String AMBIENT_SOUND_COOLDOWN_TAG = "endexpansionmod.ambient_sound_cooldown";
    public static final String COLOR_TAG = "endexpansionmod.color";
    public static final String OWNER_ANCHOR_X_TAG = "endexpansionmod.owner_anchor_x";
    public static final String OWNER_ANCHOR_Y_TAG = "endexpansionmod.owner_anchor_y";
    public static final String OWNER_ANCHOR_Z_TAG = "endexpansionmod.owner_anchor_z";

    private EndPhantomData() {
    }

    public static CompoundTag data(Phantom phantom) {
        return phantom.getPersistentData();
    }

    public static boolean isEndPhantom(Phantom phantom) {
        return data(phantom).getBoolean(END_PHANTOM_TAG);
    }

    public static void markEndPhantom(Phantom phantom) {
        data(phantom).putBoolean(END_PHANTOM_TAG, true);
    }

    public static boolean isEnhancedEndPhantom(Phantom phantom) {
        return data(phantom).getBoolean(ENHANCED_END_PHANTOM_TAG);
    }

    public static Optional<DyeColor> getColor(Phantom phantom) {
        CompoundTag data = data(phantom);
        if (!data.contains(COLOR_TAG)) {
            return Optional.empty();
        }
        DyeColor color = DyeColor.byName(data.getString(COLOR_TAG), null);
        return Optional.ofNullable(color);
    }

    public static void setColor(Phantom phantom, DyeColor color) {
        data(phantom).putString(COLOR_TAG, color.getName());
    }
}
