package pl.yasinvolved.discordoauth.client.gui.screens;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.client.gui.components.StatusString;
import pl.yasinvolved.discordoauth.common.network.payloads.AuthCancelPayloadC2S;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DiscordLinkScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft = Minecraft.getInstance();
    private final String challengeToken;
    private final String apiUrl;
    private final IPayloadContext networkContext;

    private String authUrl = null;

    private Button openBrowserButton;
    private Button cancelButton;

    private final StatusString statusMessage;
    private boolean isError = false;

    public DiscordLinkScreen(String apiUrl, String challengeToken, IPayloadContext context) {
        super(Component.translatable("discordoauth.link_screen.title"));
        this.statusMessage = StatusString.ofTranslatable("discordoauth.link_screen.status.waiting_for_link", StatusString.Status.NEUTRAL);
        this.apiUrl = apiUrl;
        this.challengeToken = challengeToken;
        this.networkContext = context;
    }

    public void onAuthSuccess() {
        statusMessage.updateTranslatable("discordoauth.link_screen.status.success", StatusString.Status.SUCCESS);

        if (this.openBrowserButton != null) {
            this.openBrowserButton.active = false;
        }

        if (this.cancelButton != null) {
            this.cancelButton.active = false;
        }
    }

    public void onAuthRefused(String reason) {
        statusMessage.updateTranslatable("discordoauth.link_screen.status.refused", StatusString.Status.ERROR);

        if (this.openBrowserButton != null) {
            this.openBrowserButton.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.openBrowserButton = Button.builder(
                Component.translatable("discordoauth.link_screen.button.open_discord"),
                button -> {
                    if (this.authUrl != null) {
                        Util.getPlatform().openUri(URI.create(this.authUrl));
                        statusMessage.updateTranslatable("discordoauth.link_screen.status.waiting_for_browser", StatusString.Status.NEUTRAL);
                    }
                })
                .bounds(centerX - 100, centerY + 10, 200, 20)
                .build();

        this.openBrowserButton.active = false;
        this.addRenderableWidget(this.openBrowserButton);

        this.cancelButton = Button.builder(
                    Component.translatable("discordoauth.link_screen.button.cancel"),
                    this::cancel
                )
                .bounds(centerX - 100, centerY + 35, 200, 20)
                .build();

        this.addRenderableWidget(cancelButton);
        if (this.authUrl == null && !this.isError) {
            fetchAuthUrl();
        }
    }

    private void fetchAuthUrl() {
        UUID playerUuid = this.minecraft.getUser().getProfileId();

        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                JsonObject json = new JsonObject();
                json.addProperty("minecraft_uuid", playerUuid.toString());
                json.addProperty("challenge_token", this.challengeToken);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(this.apiUrl))
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                    String receivedUrl = responseJson.get("auth_url").getAsString();

                    this.minecraft.execute(() -> {
                        this.authUrl = receivedUrl;
                        statusMessage.updateTranslatable("discordoauth.link_screen.status.link_ready", StatusString.Status.SUCCESS);

                        if (this.openBrowserButton != null) {
                            this.openBrowserButton.active = true;
                        }
                    });
                } else {
                    throw new RuntimeException("API HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                this.minecraft.execute(() -> {
                    this.isError = true;
                    statusMessage.updateTranslatable("discordoauth.link_screen.status.api_conn_failure", StatusString.Status.ERROR);
                    LOGGER.error("Failed to connect to auth server.", e);
                });
            }
        });
    }

    private void cancel(Button _button) {
        this.networkContext.reply(new AuthCancelPayloadC2S());

        Screen parentMenu = new JoinMultiplayerScreen(new TitleScreen());
        this.minecraft.setScreen(new DisconnectedScreen(
                parentMenu,
                Component.translatable("disconnect.disconnected"),
                Component.translatable("discordoauth.disconnect_screen.reason")
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, centerX, centerY - 60, 0xFFFFFF);

        graphics.drawCenteredString(
                this.font,
                Component.translatable("discordoauth.link_screen.subtitle"),
                centerX,
                centerY - 45,
                0xAAAAAA
        );


        statusMessage.render(graphics, this.font, centerX, centerY - 20);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
