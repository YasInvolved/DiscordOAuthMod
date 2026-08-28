package pl.yasinvolved.discordoauth.server.network;

import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthAckPayloadC2S;
import pl.yasinvolved.discordoauth.server.client_config.tasks.DiscordAuthTask;

public class ServerPayloadHandler {
    public static void handleAuthAck(final AuthAckPayloadC2S payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.finishCurrentTask(DiscordAuthTask.TYPE);
        });
    }
}
