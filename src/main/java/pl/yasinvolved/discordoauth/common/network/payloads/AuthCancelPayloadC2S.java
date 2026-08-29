package pl.yasinvolved.discordoauth.common.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.discordoauth.common.Discordoauth;

public record AuthCancelPayloadC2S() implements CustomPacketPayload {
    public static final AuthCancelPayloadC2S INSTANCE = new AuthCancelPayloadC2S();

    public static final Type<AuthCancelPayloadC2S> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID, "auth_cancel")
    );

    public static final StreamCodec<FriendlyByteBuf, AuthCancelPayloadC2S> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
