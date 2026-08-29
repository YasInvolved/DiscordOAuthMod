package pl.yasinvolved.discordoauth.server.webhook;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.server.Config;
import pl.yasinvolved.discordoauth.server.client_config.tasks.DiscordAuthTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class WebhookHandler implements HttpHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(406, -1);
                exchange.close();
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.equals("Bearer " + Config.WEBHOOK_SECRET.get())) {
                LOGGER.error("Invalid auth header: {}", authHeader);
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            JsonObject payload = JsonParser.parseReader(reader).getAsJsonObject();

            if (payload.has("minecraft_uuid") && payload.has("event")) {
                String event = payload.get("event").getAsString();
                if (event.equals("link_complete")) {
                    UUID playerUuid = UUID.fromString(payload.get("minecraft_uuid").getAsString());
                    DiscordAuthTask task = WebhookManager.removePendingLogin(playerUuid);

                    if (task != null) {
                        task.onWebhookSuccess();
                        exchange.sendResponseHeaders(200, -1);
                        return;
                    } else {
                        exchange.sendResponseHeaders(500, -1);
                    }
                }
            }

            exchange.sendResponseHeaders(400, -1);
        } catch (Exception e) {
            LOGGER.error("Failed to process webhook signal from API.", e);
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        } finally {
            exchange.close();
        }
    }
}
