package pl.yasinvolved.discordoauth.exchange;

import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.authvoid.PlayerManager;
import pl.yasinvolved.discordoauth.crypto.HashedUuid;
import pl.yasinvolved.discordoauth.crypto.StateManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;

public class CallbackServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer mcServer;
    private HttpServer server;

    public CallbackServer(MinecraftServer mcServer) { this.mcServer = mcServer; }

    public boolean init() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/verify", this::handleVerify);

            server.setExecutor(Executors.newSingleThreadExecutor());
            server.start();
            LOGGER.info("Callback listener started successfully on port 8080");
        } catch (Exception e) {
            final String message = "Failed to initialize callback server:\n" + e;
            LOGGER.error(message);
            return false;
        }

        return true;
    }

    private void handleVerify(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try (InputStream body = exchange.getRequestBody()) {
                String hashedStr = new String(body.readAllBytes()).trim();
                HashedUuid hashed = HashedUuid.fromHex(hashedStr);
                UUID playerId = StateManager.close(hashed);
                ServerPlayer player = mcServer.getPlayerList().getPlayer(playerId);
                if (player == null) {
                    LOGGER.info("A player left the server during verification process.");
                    return;
                }

                this.mcServer.execute(() -> PlayerManager.releaseFromVoid(player));

                String response = "OK";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid request body", e);
                exchange.sendResponseHeaders(400, -1);
            }
        } catch (IOException e) {
            LOGGER.error("I/O exception during callback processing: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error in callback handler: ", e);
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {

            }
        } finally {
            exchange.close();
        }
    }
}
