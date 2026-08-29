package pl.yasinvolved.discordoauth.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import pl.yasinvolved.discordoauth.common.Discordoauth;

@Mod(value = Discordoauth.MODID, dist = Dist.CLIENT)
// @EventBusSubscriber(modid = Discordoauth.MODID)
public class DiscordoauthClient {
    public DiscordoauthClient(IEventBus modEventBus, ModContainer modContainer) {

    }
}
