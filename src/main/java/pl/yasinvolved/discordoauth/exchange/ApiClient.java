package pl.yasinvolved.discordoauth.exchange;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.Config;
import pl.yasinvolved.discordoauth.crypto.HashedUuid;
import pl.yasinvolved.discordoauth.crypto.HashedUuidFactory;
import pl.yasinvolved.discordoauth.crypto.StateManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final URI BASE_URI = Config.getBaseUri();

    public static URI buildUri(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        return BASE_URI.resolve(endpoint);
    }

    public static String getVerificationLink(UUID playerUuid) {
        HashedUuid hashed = StateManager.createState(playerUuid);
        return buildUri("login?state=" + hashed).toString();
    }

    public static CompletableFuture<Boolean> checkVerificationStatus(UUID playerUuid) {
        HashedUuid hashedUuid = HashedUuidFactory.getInstance().derive(playerUuid);
        URI requestUri = buildUri("status/" + hashedUuid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                            return json.has("verified") && json.get("verified").getAsBoolean();
                        } catch (JsonSyntaxException | IllegalStateException e) {
                            LOGGER.error("Malformed JSON response from API: {}", e.getMessage());
                            return false;
                        }
                    }

                    return false;
                })
                .exceptionally(ex -> {
                    LOGGER.error("API check failed: {}", ex.getMessage());
                    return false;
                });
    }
}
