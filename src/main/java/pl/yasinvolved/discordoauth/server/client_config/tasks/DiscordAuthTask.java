package pl.yasinvolved.discordoauth.server.client_config.tasks;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthSuccessPayloadS2C;
import pl.yasinvolved.discordoauth.server.Config;
import pl.yasinvolved.discordoauth.server.webhook.WebhookManager;

import java.net.URI;
import java.util.UUID;
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
        String apiUrl = URI.create(Config.API_BASE_URL.get()).resolve("init").toString();
        consumer.accept(new AuthRequestPayloadS2C(apiUrl, this.challengeToken));
        WebhookManager.addPendingLogin(this.playerUuid, this);
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
