package pl.yasinvolved.discordoauth.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StatusString {
    public enum Status {
        NEUTRAL(0xAAAAAA),
        SUCCESS(0x55FF55),
        ERROR(0xFF5555);

        private final int color;

        Status(int hex) {
            this.color = hex;
        }

        public int getColor() { return color; }
    }

    private MutableComponent component;
    private Status status;

    private StatusString(MutableComponent comp, Status status) {
        this.component = comp;
        this.status = status;
    }

    public static StatusString ofTranslatable(String key, Status status) {
        return new StatusString(Component.translatable(key), status);
    }

    public static StatusString ofText(String text, Status status) {
        return new StatusString(Component.literal(text), status);
    }

    public void updateTranslatable(String key, Status status) {
        this.component = Component.translatable(key);
        this.status = status;
    }

    public void updateText(String text, Status status) {
        this.component = Component.literal(text);
        this.status = status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void render(GuiGraphics graphics, Font font, int posX, int posY) {
        graphics.drawCenteredString(font, this.component, posX, posY, this.status.getColor());
    }
}
