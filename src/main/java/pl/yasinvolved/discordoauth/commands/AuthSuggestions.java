package pl.yasinvolved.discordoauth.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import pl.yasinvolved.discordoauth.authvoid.PlayerManager;

public class AuthSuggestions {
    public static final SuggestionProvider<CommandSourceStack> UNAUTHENTICATED_PLAYERS = (ctx, builder) -> {
        CommandSourceStack source = ctx.getSource();

        if (source.isPlayer())
        {
            return SharedSuggestionProvider.suggest(PlayerManager.getUnverifiedPlayerNames(), builder);
        }

        return SharedSuggestionProvider.suggest(new String[0], builder);
    };
}
