package pl.yasinvolved.discordoauth.server;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.server.client_config.tasks.DiscordAuthTask;
import pl.yasinvolved.discordoauth.server.crypto.TokenGenerator;
import pl.yasinvolved.discordoauth.server.webhook.WebhookManager;

import java.lang.reflect.Field;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(value = Discordoauth.MODID, dist = Dist.DEDICATED_SERVER)
@EventBusSubscriber(modid = Discordoauth.MODID)
public class DiscordoauthServer {
    // Define mod id in a common place for everything to reference
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DiscordoauthServer(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    @SubscribeEvent()
    public static void onServerStarting(final ServerStartingEvent event) {
        WebhookManager.startServer(Config.WEBHOOK_PORT.get());
    }

    @SubscribeEvent()
    public static void onServerStopping(final ServerStoppingEvent event) {
        WebhookManager.stopServer();
    }

    public static GameProfile getProfileFromListener(ServerConfigurationPacketListener listener) {
        try {
            Class<?> clazz = listener.getClass();
            Field field = null;

            while (clazz != null && field == null) {
                try {
                    field = clazz.getDeclaredField("gameProfile");
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (field != null) {
                field.setAccessible(true);
                return (GameProfile) field.get(listener);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to extract GameProfile from Configuration Listener", e);
        }

        return null;
    }

    @SubscribeEvent
    public static void onRegisterConfiguationTasks(final RegisterConfigurationTasksEvent event) {
        LOGGER.info("Registering configuration task...");
        ServerConfigurationPacketListener listener = event.getListener();
        GameProfile profile = getProfileFromListener(listener);

        if (profile != null) {
            String challengeToken = TokenGenerator.generateChallengeToken();

            DiscordAuthTask task = new DiscordAuthTask(
                    listener,
                    profile.getId(),
                    challengeToken
            );

            event.register(task);
            LOGGER.info("Task registered successfully!");
            return;
        }

        LOGGER.error("Failed to register configuration task.");
    }
}
