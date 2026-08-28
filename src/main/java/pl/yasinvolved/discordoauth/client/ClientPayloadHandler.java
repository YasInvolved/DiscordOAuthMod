package pl.yasinvolved.discordoauth.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.client.gui.screens.DiscordLinkScreen;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthAckPayloadC2S;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthRequestPayloadS2C;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthSuccessPayloadS2C;

public class ClientPayloadHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    public static void handleAuthRequest(final AuthRequestPayloadS2C payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MINECRAFT.setScreen(
                    new DiscordLinkScreen(payload.apiLoginUrl(), payload.challengeToken(), context)
            );
        });
    }

    public static void handleAuthSuccess(final AuthSuccessPayloadS2C payload, final IPayloadContext context) {
        LOGGER.info("Handshake succeeded. Sending acknowledged.");
        context.enqueueWork(() -> {
            if (MINECRAFT.screen instanceof DiscordLinkScreen linkScreen) {
                linkScreen.onAuthSuccess();
            }

            context.reply(new AuthAckPayloadC2S());
        });
    }
}
