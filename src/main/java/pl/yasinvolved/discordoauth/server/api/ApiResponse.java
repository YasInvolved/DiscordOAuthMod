package pl.yasinvolved.discordoauth.server.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public record ApiResponse(int statusCode, JsonObject response) {
    public static ApiResponse fromJsonString(int statusCode, String raw) {
        JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
        return new ApiResponse(statusCode, obj);
    }
}
