package pl.yasinvolved.discordoauth.common.network;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.client.ClientPayloadHandler;
import pl.yasinvolved.discordoauth.common.Discordoauth;
import pl.yasinvolved.discordoauth.common.network.payloads.*;
import pl.yasinvolved.discordoauth.server.network.ServerPayloadHandler;

@EventBusSubscriber(modid = Discordoauth.MODID)
public class PayloadRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        LOGGER.info("Registering configuration packets...");
        final PayloadRegistrar registrar = event.registrar(Discordoauth.MODID).versioned("1.0.0");

        registrar.configurationToClient(
                AuthRequestPayloadS2C.TYPE,
                AuthRequestPayloadS2C.CODEC,
                (payload, context) -> ClientPayloadHandler.handleAuthRequest(payload, context)
        );

        registrar.configurationToServer(
                AuthAckPayloadC2S.TYPE,
                AuthAckPayloadC2S.CODEC,
                (payload, context) -> ServerPayloadHandler.handleAuthAck(payload, context)
        );

        registrar.configurationToClient(
                AuthSuccessPayloadS2C.TYPE,
                AuthSuccessPayloadS2C.CODEC,
                (payload, context) -> ClientPayloadHandler.handleAuthSuccess(payload, context)
        );

        registrar.configurationToClient(
                AuthRefusedPayloadS2C.TYPE,
                AuthRefusedPayloadS2C.CODEC,
                (payload, context) -> ClientPayloadHandler.handleAuthRefused(payload, context)
        );

        registrar.configurationToServer(
                AuthCancelPayloadC2S.TYPE,
                AuthCancelPayloadC2S.CODEC,
                (payload, context) -> ServerPayloadHandler.handleAuthCancel(payload, context)
        );

        LOGGER.info("Configuration packets registered successfully!");
    }
}
