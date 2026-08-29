package pl.yasinvolved.discordoauth.server.webhook;

import com.mojang.logging.LogUtils;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.server.client_config.tasks.DiscordAuthTask;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class WebhookManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentHashMap<UUID, DiscordAuthTask> PENDING_LOGINS = new ConcurrentHashMap<>();
    private static HttpServer server;

    public static void addPendingLogin(UUID playerId, DiscordAuthTask task) {
        PENDING_LOGINS.put(playerId, task);
    }

    public static DiscordAuthTask removePendingLogin(UUID playerId) {
        return PENDING_LOGINS.remove(playerId);
    }

    public static void startServer(String addr, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(addr, port), 0);
            server.createContext("/webhook", new WebhookHandler());
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            LOGGER.info("Minecraft Webhook Server listening on port {}", port);
        } catch (Exception e) {
            LOGGER.error("Failed to start Minecraft Webhook Server", e);
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }
}
