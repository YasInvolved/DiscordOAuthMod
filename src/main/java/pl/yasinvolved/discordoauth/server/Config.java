package pl.yasinvolved.discordoauth.server;

import net.neoforged.neoforge.common.ModConfigSpec;
import pl.yasinvolved.discordoauth.server.crypto.TokenGenerator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> API_BASE_URL;

    public static final ModConfigSpec.ConfigValue<String> WEBHOOK_SECRET;
    public static final ModConfigSpec.ConfigValue<Integer> WEBHOOK_PORT;

    public static final ModConfigSpec.ConfigValue<Boolean> SERVER_CHECK;
    public static final ModConfigSpec.ConfigValue<String> SERVER_CHECK_ID;

    // whitelist
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_UUIDS;

    static {
        BUILDER.push("Integration Settings");

        API_BASE_URL = BUILDER
                .comment(" Base URL of your OAuth microservice")
                .define("apiBaseUrl", "http://127.0.0.1:3000", Config::isValidUrl);

        BUILDER.pop();

        BUILDER.push("Webhook Server Settings");
        WEBHOOK_SECRET = BUILDER
                .comment("Secret Key for webhook verification")
                .define("secretValue", TokenGenerator.generateChallengeToken());

        WEBHOOK_PORT = BUILDER
                .comment("Port on which the webhook server should be listening")
                .defineInRange("webhookPort", 8080, 0, (int)Short.MAX_VALUE * 2);
        BUILDER.pop();


        BUILDER.push("Server check");
        SERVER_CHECK = BUILDER
                .comment("Enable server check")
                .define("serverCheck", false);

        SERVER_CHECK_ID = BUILDER
                .comment("ID of the Discord server")
                .define("serverId", "");
        BUILDER.pop();

        BUILDER.push("Whitelist");
        WHITELISTED_UUIDS = BUILDER
                .comment("Insert here UUIDs of players that don't need to have enforced login")
                .defineList("whitelisted_uuids", ArrayList::new, obj -> obj instanceof String);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static URI getBaseUri() {
        String urlStr = API_BASE_URL.get().trim();

        if (!urlStr.endsWith("/")) {
            urlStr += "/";
        }

        return URI.create(urlStr);
    }

    public static boolean isValidUrl(Object o) {
        if (!(o instanceof String urlString))
            return false;

        if (urlString.isBlank())
            return false;

        try
        {
            URI uri = new URI(urlString);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return false;
            }

            String scheme = uri.getScheme().toLowerCase();
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return false;
            }

            uri.toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
