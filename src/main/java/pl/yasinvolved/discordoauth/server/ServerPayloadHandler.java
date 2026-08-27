package pl.yasinvolved.discordoauth.server;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthAckPayloadC2S;

public class ServerPayloadHandler {
    public static void handleAuthAck(final AuthAckPayloadC2S payload, final IPayloadContext context) {
        context.enqueueWork(() -> {

        });
    }
}
