package pl.yasinvolved.discordoauth.common.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.discordoauth.common.Discordoauth;

public record AuthSuccessPayloadS2C() implements CustomPacketPayload {
    public static final AuthSuccessPayloadS2C INSTANCE = new AuthSuccessPayloadS2C();
    public static final Type<AuthSuccessPayloadS2C> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID, "auth_success")
    );

    public static final StreamCodec<FriendlyByteBuf, AuthSuccessPayloadS2C> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
