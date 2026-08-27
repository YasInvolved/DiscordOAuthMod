package pl.yasinvolved.discordoauth.common.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.discordoauth.common.Discordoauth;

public record AuthAckPayloadC2S() implements CustomPacketPayload {
    public static final AuthAckPayloadC2S INSTANCE = new AuthAckPayloadC2S();

    public static final Type<AuthAckPayloadC2S> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID,"auth_ack")
    );

    public static final StreamCodec<FriendlyByteBuf, AuthAckPayloadC2S> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
