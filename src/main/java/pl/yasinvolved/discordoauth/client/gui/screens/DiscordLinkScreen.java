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

    private Component statusMessage = Component.literal("Requesting authorization link...");
    private int statusColor = 0xFFFF55;
    private boolean isError = false;

    public DiscordLinkScreen(String apiUrl, String challengeToken, IPayloadContext context) {
        super(Component.literal("Discord Account Verification"));
        this.apiUrl = apiUrl;
        System.out.println(apiUrl);
        this.challengeToken = challengeToken;
        this.networkContext = context;
    }

    public void onAuthSuccess() {
        updateStatus("Authentication successful! Loading world...", 0x55FF55);

        if (this.openBrowserButton != null) {
            this.openBrowserButton.active = false;
        }

        if (this.cancelButton != null) {
            this.cancelButton.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.openBrowserButton = Button.builder(
                Component.literal("Open Discord"),
                button -> {
                    if (this.authUrl != null) {
                        Util.getPlatform().openUri(URI.create(this.authUrl));
                        updateStatus("Waiting for you to authorize in the browser...", 0x55FFFF);
                    }
                })
                .bounds(centerX - 100, centerY + 10, 200, 20)
                .build();

        this.openBrowserButton.active = false;
        this.addRenderableWidget(this.openBrowserButton);

        this.cancelButton = Button.builder(
                    Component.literal("Disconnect"),
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
        if (this.minecraft.getUser() == null) {
            updateStatus("Error: Could not determine local player profile.", 0xFF5555);
            this.isError = true;
            return;
        }

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
                        updateStatus("Ready! Click below to link your account:", 0x55FF55);

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
                    updateStatus("Failed to connect to auth server: " + e.getMessage(), 0xFF5555);
                    LOGGER.error("Failed to connect to auth server.", e);
                });
            }
        });
    }

    private void updateStatus(String message, int color) {
        this.statusMessage = Component.literal(message);
        this.statusColor = color;
    }

    private void cancel(Button _button) {
        this.networkContext.disconnect(Component.literal("Discord verification cancelled."));

        Screen parentMenu = new JoinMultiplayerScreen(new TitleScreen());
        this.minecraft.setScreen(new DisconnectedScreen(
                parentMenu,
                Component.literal("Disconnected"),
                Component.literal("Discord verification cancelled.")
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
                Component.literal("You must link your Discord account to play on this server."),
                centerX,
                centerY - 45,
                0xAAAAAA
        );

        graphics.drawCenteredString(this.font, this.statusMessage, centerX, centerY - 20, this.statusColor);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
