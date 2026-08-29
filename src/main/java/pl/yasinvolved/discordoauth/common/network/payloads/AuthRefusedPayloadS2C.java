package pl.yasinvolved.discordoauth.common.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pl.yasinvolved.discordoauth.common.Discordoauth;

public record AuthRefusedPayloadS2C(String reason) implements CustomPacketPayload {
    public static final Type<AuthRefusedPayloadS2C> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Discordoauth.MODID, "auth_refused")
    );

    public static final StreamCodec<FriendlyByteBuf, AuthRefusedPayloadS2C> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AuthRefusedPayloadS2C::reason,
            AuthRefusedPayloadS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
