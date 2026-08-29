package pl.yasinvolved.discordoauth.server.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonPacketListenerImplInvoker {
    @Invoker("playerProfile")
    GameProfile invokePlayerProfile();
}
