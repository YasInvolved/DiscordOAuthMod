package pl.yasinvolved.discordoauth.server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;

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
        String apiUrl = "http://localhost:3000/api/auth/init";
        consumer.accept(new AuthRequestPayloadS2C(this.challengeToken, apiUrl));
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
