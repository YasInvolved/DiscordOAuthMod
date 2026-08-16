package pl.yasinvolved.discordoauth.authvoid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import pl.yasinvolved.discordoauth.Discordoauth;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private record SavedLocation(
            ResourceKey<Level> dimension,
            double x, double y, double z,
            float xRot, float yRot
    ) {}

    private static final ResourceKey<Level> AUTHVOID_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID, "authvoid")
    );

    private static final ConcurrentHashMap<UUID, SavedLocation> AUTHVOID_PLAYERS = new ConcurrentHashMap<>();

    public static boolean isUnverified(ServerPlayer player) {
        return AUTHVOID_PLAYERS.containsKey(player.getUUID());
    }

    public static void clearTracking(ServerPlayer player) {
        AUTHVOID_PLAYERS.remove(player.getUUID());
    }

    private static SavedLocation getDefaultLocation(ServerPlayer player) {
        BlockPos pos = player.getRespawnPosition();
        ResourceKey<Level> dimension = player.getRespawnDimension();

        if (pos == null) {
            pos = player.getServer().getLevel(dimension).getSharedSpawnPos();
        }

        return new SavedLocation(
                dimension,
                pos.getX(), pos.getY(), pos.getZ(),
                0, 0
        );
    }

    public static void putInVoid(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ServerLevel currentLevel = player.serverLevel();
        ServerLevel target = player.getServer().getLevel(AUTHVOID_KEY);

        if (!currentLevel.dimension().equals(AUTHVOID_KEY)) {
            AUTHVOID_PLAYERS.put(uuid, new SavedLocation(
                    currentLevel.dimension(),
                    player.getX(), player.getY(), player.getZ(),
                    player.getXRot(), player.getYRot()
            ));
            player.teleportTo(target, 0.5, 100.0, 0.5, 0f, 0f);
        } else {
            AUTHVOID_PLAYERS.putIfAbsent(uuid, getDefaultLocation(player));
        }

        player.setDeltaMovement(0, 0, 0);

        player.getAbilities().mayBuild = false;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();

        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, MobEffectInstance.INFINITE_DURATION, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, false));
    }

    public static void releaseFromVoid(ServerPlayer player) {
        UUID uuid = player.getUUID();
        SavedLocation loc = AUTHVOID_PLAYERS.get(uuid);

        if (loc == null) {
            BlockPos defaultSpawn = player.getRespawnPosition();
            loc = new SavedLocation(
                    player.getRespawnDimension(),
                    defaultSpawn.getX(), defaultSpawn.getY(), defaultSpawn.getZ(),
                    0, 0
            );
        }

        player.removeEffect(MobEffects.LEVITATION);
        player.removeEffect(MobEffects.INVISIBILITY);

        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().flying = false;
            player.getAbilities().mayBuild = true;
            player.onUpdateAbilities();
        }

        ServerLevel target = player.getServer().getLevel(loc.dimension());
        if (target == null) target = player.getServer().overworld();

        player.teleportTo(target, loc.x(), loc.y(), loc.z(), loc.yRot(), loc.xRot());

        player.sendSystemMessage(
                Component.literal("Account verified! Welcome to the server.")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
        );
    }
}
