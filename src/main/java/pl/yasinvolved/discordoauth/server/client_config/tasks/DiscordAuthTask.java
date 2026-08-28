package pl.yasinvolved.discordoauth.server.client_config.tasks;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthSuccessPayloadS2C;
import pl.yasinvolved.discordoauth.server.Config;
import pl.yasinvolved.discordoauth.server.webhook.WebhookManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DiscordAuthTask implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(
            String.format("%s:discord_auth", Discordoauth.MODID)
    );

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
        String secret = Config.WEBHOOK_SECRET.get();
        URI baseUrl = URI.create(Config.API_BASE_URL.get());
        URI playerUrl = baseUrl.resolve("player/").resolve(this.playerUuid.toString());
        URI initUrl = baseUrl.resolve("init");

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(3))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(playerUrl)
                        .header("Authorization", "Bearer " + secret)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ServerLifecycleHooks.getCurrentServer().execute(() -> {
                    if (response.statusCode() == 200) {
                        this.listener.finishCurrentTask(this.type());
                    } else if (response.statusCode() == 404) {
                        this.consumer.accept(new AuthRequestPayloadS2C(initUrl.toString(), this.challengeToken));
                        WebhookManager.addPendingLogin(this.playerUuid, this);
                    } else {
                        this.listener.disconnect(Component.literal("Auth server returned an error."));
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

    public void onWebhookSuccess() {
        if (this.consumer != null) {
            this.consumer.accept(new AuthSuccessPayloadS2C());
        }
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
