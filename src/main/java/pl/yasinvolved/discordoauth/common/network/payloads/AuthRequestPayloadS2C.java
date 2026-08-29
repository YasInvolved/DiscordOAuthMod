package pl.yasinvolved.discordoauth.common.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.discordoauth.common.Discordoauth;

public record AuthRequestPayloadS2C(String apiLoginUrl, String challengeToken) implements CustomPacketPayload {
    public static final Type<AuthRequestPayloadS2C> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID, "auth_request"));
    public static final StreamCodec<FriendlyByteBuf, AuthRequestPayloadS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AuthRequestPayloadS2C::apiLoginUrl,
            ByteBufCodecs.STRING_UTF8,
            AuthRequestPayloadS2C::challengeToken,
            AuthRequestPayloadS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
