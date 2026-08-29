package pl.yasinvolved.discordoauth.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthAckPayloadC2S;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthCancelPayloadC2S;
import pl.yasinvolved.discordoauth.server.api.ApiClient;
import pl.yasinvolved.discordoauth.server.client_config.tasks.DiscordAuthTask;
import pl.yasinvolved.discordoauth.server.mixin.ServerCommonPacketListenerImplInvoker;

import java.util.concurrent.CompletableFuture;

public class ServerPayloadHandler {
    public static void handleAuthAck(final AuthAckPayloadC2S payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            context.finishCurrentTask(DiscordAuthTask.TYPE);
        });
    }

    public static void handleAuthCancel(final AuthCancelPayloadC2S payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.listener() instanceof ServerCommonPacketListenerImplInvoker invoker) {
                GameProfile profile = invoker.invokePlayerProfile();

                CompletableFuture.runAsync(() -> {
                    ApiClient client = new ApiClient();
                    client.logoutPlayer(profile.getId());
                });
            }

            context.disconnect(Component.literal("Authentication cancelled."));
        });
    }
}
