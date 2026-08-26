package pl.yasinvolved.discordoauth;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.authvoid.PlayerManager;
import pl.yasinvolved.discordoauth.chat.MessageBuilder;
import pl.yasinvolved.discordoauth.crypto.SecretLoader;
import pl.yasinvolved.discordoauth.exchange.ApiClient;
import pl.yasinvolved.discordoauth.exchange.CallbackServer;

import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Discordoauth.MODID)
@EventBusSubscriber(modid = Discordoauth.MODID)
public class Discordoauth {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "discordoauth";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    private static CallbackServer callbackServer;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Discordoauth(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private static void sendAuthLink(ServerPlayer player) {
        String authUrl = ApiClient.getVerificationLink(player.getUUID());
        Component hyperlink = MessageBuilder.makeUrl(MessageBuilder.AUTH_LINK_MESSAGE, authUrl);
        player.sendSystemMessage(MessageBuilder.format(MessageBuilder.Type.INFO, hyperlink));
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer mcServer = event.getServer();
        SecretLoader.load();
        callbackServer = new CallbackServer(mcServer);
        callbackServer.init();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        event.getServer().levelKeys().forEach(key -> LOGGER.info("Loaded level: {}", key.location()));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerUuid = player.getUUID();
        if (Config.WHITELISTED_UUIDS.get().contains(playerUuid.toString())) {
            LOGGER.info("Player {} ({}) is whitelisted. Skipping authentication.", playerUuid, player.getDisplayName().getString());
            PlayerManager.releaseFromVoid(player);
            return;
        }

        PlayerManager.putInVoid(player);

        ApiClient.checkVerificationStatus(playerUuid).thenAccept(isVerified -> {
            player.getServer().execute(() -> {
                if (isVerified) {
                    PlayerManager.releaseFromVoid(player);
                    player.sendSystemMessage(MessageBuilder.AUTH_SUCCESS_MESSAGE);
                } else {
                    sendAuthLink(player);
                }
            });
        });
    }
}
