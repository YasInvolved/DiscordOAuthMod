package pl.yasinvolved.discordoauth.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.yasinvolved.discordoauth.client.gui.screens.DiscordLinkScreen;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthSuccessPayloadS2C;

public class ClientPayloadHandler {
    public static void handleAuthRequest(final AuthRequestPayloadS2C payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(
                    new DiscordLinkScreen(payload.apiLoginUrl(), payload.challengeToken(), context)
            );
        });
    }

    public static void handleAuthSuccess(final AuthSuccessPayloadS2C payload, final IPayloadContext context) {
        context.enqueueWork(() -> {

        });
    }
}
