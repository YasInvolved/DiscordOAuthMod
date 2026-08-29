package pl.yasinvolved.discordoauth.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import pl.yasinvolved.discordoauth.common.Discordoauth;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Discordoauth.MODID)
public class CommandRegistrar {
    private static final List<BaseCommand> COMMANDS = new ArrayList<>();
    private static final UnlinkCommand UNLINK = register(new UnlinkCommand());

    private static <T extends BaseCommand> T register(T command) {
        COMMANDS.add(command);
        return command;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("dc")
                .requires(source -> source.hasPermission(0));

        for (BaseCommand command : COMMANDS) {
            rootCommand = command.register(rootCommand);
        }

        dispatcher.register(rootCommand);
    }
}
