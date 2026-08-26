package pl.yasinvolved.discordoauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import pl.yasinvolved.discordoauth.authvoid.PlayerManager;
import pl.yasinvolved.discordoauth.chat.MessageBuilder;

public class AuthCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("oauth")
                        .requires(source -> source.hasPermission(3))
                        .then(buildForceAuthCommand())
        );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildForceAuthCommand() {
        return Commands.literal("force")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(AuthSuggestions.UNAUTHENTICATED_PLAYERS)
                        .executes(AuthCommands::handleForce)
                );
    }

    private static int handleForce(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayer();
        if (sender != null && !sender.hasPermissions(3))
            return 0;

        String playerName = StringArgumentType.getString(ctx, "player");
        ServerPlayer releasedPlayer = sender.getServer().getPlayerList().getPlayerByName(playerName);
        if (releasedPlayer == null)
            return 0;

        PlayerManager.releaseFromVoid(releasedPlayer);
        ctx.getSource().sendSystemMessage(
                MessageBuilder.formatTranslatable(
                        MessageBuilder.Type.WARN,
                        "message.discordoauth.force_release_sender"
                )
        );

        releasedPlayer.sendSystemMessage(
                MessageBuilder.formatTranslatable(
                        MessageBuilder.Type.WARN,
                        "message.discordoauth.force_release_receiver"
                )
        );

        return 1;
    }
}
