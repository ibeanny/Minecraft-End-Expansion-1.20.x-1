package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndPhantomEnhancements {

    private static final int STRIKE_COOLDOWN_TICKS = 60;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Phantom phantom)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.END)) return;
        if (!EndPhantomData.isEndPhantom(phantom)) return;

        CompoundTag data = phantom.getPersistentData();
        if (data.getBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG)) return;

        setBaseValue(phantom.getAttribute(Attributes.MAX_HEALTH), 28.0D);
        setBaseValue(phantom.getAttribute(Attributes.ATTACK_DAMAGE), 7.0D);
        setBaseValue(phantom.getAttribute(Attributes.FOLLOW_RANGE), 40.0D);
        setBaseValue(phantom.getAttribute(Attributes.MOVEMENT_SPEED), 0.42D);
        phantom.setHealth(phantom.getMaxHealth());
        data.putBoolean(EndPhantomData.ENHANCED_END_PHANTOM_TAG, true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Phantom phantom)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!phantom.level().dimension().equals(Level.END)) return;
        if (!EndPhantomData.isEnhancedEndPhantom(phantom)) return;

        CompoundTag data = phantom.getPersistentData();
        long now = phantom.level().getGameTime();
        long nextStrike = data.getLong(EndPhantomData.STRIKE_COOLDOWN_TAG);
        if (now < nextStrike) return;

        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));
        data.putLong(EndPhantomData.STRIKE_COOLDOWN_TAG, now + STRIKE_COOLDOWN_TICKS);
    }

    private static void setBaseValue(AttributeInstance attribute, double value) {
        if (attribute != null) {
            attribute.setBaseValue(value);
        }
    }

    public static boolean isEnhancedEndPhantom(Phantom phantom) {
        return EndPhantomData.isEnhancedEndPhantom(phantom);
    }
}
