package pl.yasinvolved.discordoauth.server.api;

import pl.yasinvolved.discordoauth.server.Config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class ApiClient {
    private static final URI API_BASE_URI = Config.getBaseUri();
    private static final String API_SECRET = Config.WEBHOOK_SECRET.get();

    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public ApiResponse getUser(UUID minecraftUuid) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(API_BASE_URI.resolve("player/" + minecraftUuid))
                .header("Authorization", "Bearer " + API_SECRET)
                .GET()
                .build();

        try {
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200 && !res.body().isEmpty()) {
                return ApiResponse.fromJsonString(res.statusCode(), res.body());
            }

            return new ApiResponse(res.statusCode(), null);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Failed to make a request.", e);
        }
    }

    public ApiResponse getMemberInfo(UUID minecraftUuid, String discordServerId) {
        URI reqUri = API_BASE_URI.resolve("player/" + minecraftUuid.toString() + "/").resolve(discordServerId);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(reqUri)
                .header("Authorization", "Bearer " + API_SECRET)
                .GET()
                .build();

        try {
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200 && !res.body().isEmpty()) {
                return ApiResponse.fromJsonString(res.statusCode(), res.body());
            }

            return new ApiResponse(res.statusCode(), null);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Failed to make a request", e);
        }
    }

    public ApiResponse logoutPlayer(UUID minecraftUuid) {
        URI reqUri = API_BASE_URI.resolve("player/" + minecraftUuid.toString() + "/unlink");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(reqUri)
                .header("Authorization", "Bearer " + API_SECRET)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(res.statusCode(), null);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Failed to make a request", e);
        }
    }
}
