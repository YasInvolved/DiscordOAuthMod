package pl.yasinvolved.discordoauth.server.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import pl.yasinvolved.discordoauth.server.api.ApiClient;
import pl.yasinvolved.discordoauth.server.api.ApiResponse;

import java.util.concurrent.CompletableFuture;

public class UnlinkCommand extends BaseCommand {
    public UnlinkCommand() {
        super("unlink");
    }

    private int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        source.sendSuccess(() -> Component.translatable("discordoauth.chat.unlink_started"), false);

        CompletableFuture.runAsync(() -> {
            ApiClient client = new ApiClient();
            ApiResponse response = client.logoutPlayer(player.getUUID());

            player.server.execute(() -> {
                if (response.statusCode() == 200) {
                    player.connection.disconnect(Component.translatable("discordoauth.chat.unlink_success"));
                } else {
                    player.connection.disconnect(Component.translatable("discordoauth.chat.unlink_failure"));
                }
            });
        });

        return 1;
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.executes(this::execute);
    }
}
