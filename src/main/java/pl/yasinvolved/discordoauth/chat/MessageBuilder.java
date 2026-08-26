package pl.yasinvolved.discordoauth.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MessageBuilder {
    private static final Component PREFIX = Component.empty()
        .append("[Discord OAuth] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

    public static final Component AUTH_SUCCESS_MESSAGE = MessageBuilder.formatTranslatable(
            Type.SUCCESS,
            "message.discordoauth.auth_success"
    );

    public static final Component AUTH_LINK_MESSAGE = Component.translatable("message.discordoauth.auth_link");

    public enum Type {
        INFO(ChatFormatting.GRAY),
        SUCCESS(ChatFormatting.GREEN),
        WARN(ChatFormatting.YELLOW),
        ERROR(ChatFormatting.RED);

        private final ChatFormatting defaultColor;

        Type(ChatFormatting color) {
            this.defaultColor = color;
        }

        public ChatFormatting getColor() {
            return defaultColor;
        }
    }

    public static MutableComponent format(Type type, Component content) {
        return Component.empty()
                .append(PREFIX)
                .append(content.copy().withStyle(style ->
                    style.getColor() == null ? style.withColor(type.getColor()) : style
                ));
    }

    public static MutableComponent format(Type type, String plainText) {
        return format(type, Component.literal(plainText));
    }

    public static MutableComponent formatTranslatable(Type type, String key, Object... args) {
        return format(type, Component.translatable(key, args));
    }

    public static MutableComponent makeUrl(Component component, String url) {
        return component.copy().withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
        );
    }
}
