package pl.yasinvolved.discordoauth.server.client_config.tasks;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRefusedPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthSuccessPayloadS2C;
import pl.yasinvolved.discordoauth.server.Config;
import pl.yasinvolved.discordoauth.server.api.ApiClient;
import pl.yasinvolved.discordoauth.server.api.ApiResponse;
import pl.yasinvolved.discordoauth.server.webhook.WebhookManager;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DiscordAuthTask implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(
            String.format("%s:discord_auth", Discordoauth.MODID)
    );

    private static final URI BASE_URI = Config.getBaseUri();
    private static final URI INIT_URI = BASE_URI.resolve("init");

    private final ServerConfigurationPacketListener listener;
    private final String challengeToken;
    private final UUID playerUuid;
    private Consumer<CustomPacketPayload> consumer;

    public DiscordAuthTask(ServerConfigurationPacketListener listener, UUID playerUuid, String challengeToken) {
        this.listener = listener;
        this.playerUuid = playerUuid;
        this.challengeToken = challengeToken;
    }

    @Override
    public void run(Consumer<CustomPacketPayload> consumer) {
        this.consumer = consumer;

        CompletableFuture.runAsync(() -> {
            try {
                boolean initialCheckStatus = initialCheck(Config.SERVER_CHECK.get());

                ServerLifecycleHooks.getCurrentServer().execute(() -> {
                    if (initialCheckStatus) {
                        this.consumer.accept(new AuthSuccessPayloadS2C());
                    } else {
                        this.consumer.accept(new AuthRequestPayloadS2C(INIT_URI.toString(), this.challengeToken));
                        WebhookManager.addPendingLogin(this.playerUuid, this);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                ServerLifecycleHooks.getCurrentServer().execute(() -> {
                    this.listener.disconnect(Component.literal("Failed to reach the authentication server."));
                });
            }
        });
    }

    private boolean serverCheck(ApiClient client) {
        ApiResponse response = client.getMemberInfo(this.playerUuid, Config.SERVER_CHECK_ID.get());
        return response.statusCode() == 200;
    }

    private boolean initialCheck(boolean serverCheckEnabled) {
        ApiClient client = new ApiClient();
        ApiResponse userResponse = client.getUser(this.playerUuid);

        boolean serverCheckSuccess = true;
        if (serverCheckEnabled) {
            serverCheckSuccess = serverCheck(client);
        }
        return userResponse.statusCode() == 200 && serverCheckSuccess;
    }

    public void onWebhookSuccess() {
        if (this.consumer != null) {
            boolean serverCheckSuccess = true;
            if (Config.SERVER_CHECK.get()) {
                serverCheckSuccess = serverCheck(new ApiClient());
            }

            if (serverCheckSuccess) {
                ServerLifecycleHooks.getCurrentServer().execute(() -> this.consumer.accept(new AuthSuccessPayloadS2C()));
            } else {
                ServerLifecycleHooks.getCurrentServer().execute(() -> this.consumer.accept(new AuthRefusedPayloadS2C("Permission denied.")));
            }
        }
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
