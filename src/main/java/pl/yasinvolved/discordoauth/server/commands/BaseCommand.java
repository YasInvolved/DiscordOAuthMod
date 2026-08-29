package pl.yasinvolved.discordoauth.server.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public abstract class BaseCommand {
    private final String name;

    public BaseCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    protected abstract LiteralArgumentBuilder<CommandSourceStack> build(LiteralArgumentBuilder<CommandSourceStack> builder);

    private LiteralArgumentBuilder<CommandSourceStack> buildNode() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.name);
        builder = this.build(builder);

        return builder;
    }

    public LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        return parent.then(this.buildNode());
    }
}
