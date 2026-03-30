package net.ibeanny.endmod.phantom;

import net.ibeanny.endmod.MinecraftEndExpansion;
import net.ibeanny.endmod.entity.EndExpansionEntities;
import net.ibeanny.endmod.entity.EndPhantomEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * End phantoms should feel like void predators instead of generic overworld stragglers.
 * Players can trigger them from islands or bridges, but the phantoms themselves still
 * only spawn out over real void space.
 */
@Mod.EventBusSubscriber(modid = MinecraftEndExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndVoidPhantomSpawns {

    private static final int HORIZ_SAMPLE = 12;
    private static final int DOWN_SCAN = 48;
    private static final int MIN_Y = 20;
    private static final int NEARBY_RADIUS = 72;
    private static final int SPAWN_SEARCH_STEPS = 8;
    private static final int CAP_PER_PLAYER = 6;
    private static final long TICK_INTERVAL = 60L;
    private static final float BASE_SPAWN_CHANCE = 0.34f;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Level level = event.level;
        if (!level.dimension().equals(Level.END)) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) return;
        if (level.getGameTime() % TICK_INTERVAL != 0L) return;

        for (Player player : level.players()) {
            if (player.isSpectator()) continue;

            BlockPos playerPos = player.blockPosition();
            if (!level.getBiome(playerPos).is(BiomeTags.IS_END)) continue;
            if (playerPos.getY() < MIN_Y) continue;

            AABB box = new AABB(
                    player.getX() - NEARBY_RADIUS, player.getY() - NEARBY_RADIUS, player.getZ() - NEARBY_RADIUS,
                    player.getX() + NEARBY_RADIUS, player.getY() + NEARBY_RADIUS, player.getZ() + NEARBY_RADIUS
            );
            int nearbyCount = level.getEntitiesOfClass(Phantom.class, box).size();
            if (nearbyCount >= CAP_PER_PLAYER) continue;

            float spawnChance = BASE_SPAWN_CHANCE;
            if (EndPhantomTargeting.isPhantomBait(player)) {
                spawnChance += 0.18f;
            }
            if (level.random.nextFloat() > spawnChance) continue;

            int toSpawn = 1 + (level.random.nextFloat() < 0.45f ? 1 : 0);
            if (EndPhantomTargeting.isPhantomBait(player) && level.random.nextFloat() < 0.40f) {
                toSpawn++;
            }

            for (int i = 0; i < toSpawn; i++) {
                EndPhantomEntity phantom = EndExpansionEntities.END_PHANTOM.get().create(level);
                if (phantom == null) break;

                BlockPos spawnPos = findVoidSpawnPos(level, player);
                if (spawnPos == null) continue;

                double x = spawnPos.getX() + 0.5D;
                double y = spawnPos.getY();
                double z = spawnPos.getZ() + 0.5D;

                phantom.moveTo(x, y, z, level.random.nextFloat() * 360f, 0f);
                phantom.setPhantomSize(1 + level.random.nextInt(3));
                EndPhantomData.markEndPhantom(phantom);
                if (!player.isCreative()) {
                    phantom.setTarget(player);
                }
                level.addFreshEntity(phantom);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EndPhantomEntity)) return;
        Level level = event.getLevel();
        if (!level.dimension().equals(Level.END)) return;

        BlockPos pos = event.getEntity().blockPosition();
        if (pos.getY() < MIN_Y) {
            event.setCanceled(true);
            return;
        }
        if (!level.getBiome(pos).is(BiomeTags.IS_END)) {
            event.setCanceled(true);
            return;
        }
        if (!isOverVoid(level, pos, HORIZ_SAMPLE, DOWN_SCAN)) {
            event.setCanceled(true);
        }
    }

    private static BlockPos findVoidSpawnPos(Level level, Player player) {
        for (int attempt = 0; attempt < SPAWN_SEARCH_STEPS; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double dist = 20 + level.random.nextInt(36);
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            double y = player.getY() + 14 + level.random.nextInt(16);

            BlockPos spawnPos = BlockPos.containing(x, y, z);
            if (spawnPos.getY() < MIN_Y) {
                continue;
            }
            if (!level.getBiome(spawnPos).is(BiomeTags.IS_END)) {
                continue;
            }
            if (!isOverVoid(level, spawnPos, 10, 32)) {
                continue;
            }
            return spawnPos;
        }

        return null;
    }

    private static boolean isOverVoid(LevelAccessor level, BlockPos pos, int sampleRadius, int down) {
        BlockPos[] samples = new BlockPos[] {
                pos,
                pos.offset(sampleRadius, 0, 0),
                pos.offset(-sampleRadius, 0, 0),
                pos.offset(0, 0, sampleRadius),
                pos.offset(0, 0, -sampleRadius)
        };

        for (BlockPos base : samples) {
            BlockPos cursor = base;
            for (int i = 0; i < down; i++) {
                cursor = cursor.below();
                if (!level.getBlockState(cursor).isAir()) {
                    return false;
                }
            }
        }

        return true;
    }
}
