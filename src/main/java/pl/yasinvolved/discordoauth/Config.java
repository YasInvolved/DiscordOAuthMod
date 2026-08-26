package pl.yasinvolved.discordoauth;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<String> API_BASE_URL;

    public enum SecretSource {
        ENV_VAR,
        CONFIG_VALUE
    }

    public static final ModConfigSpec.EnumValue<SecretSource> SECRET_SOURCE;
    public static final ModConfigSpec.ConfigValue<String> SECRET_ENV_VAR_NAME;
    public static final ModConfigSpec.ConfigValue<String> SECRET_RAW_VALUE;

    // whitelist
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_UUIDS;

    static {
        BUILDER.push("Integration Settings");

        API_BASE_URL = BUILDER
                .comment(" Base URL of your OAuth microservice")
                .define("apiBaseUrl", "http://localhost:3000", Config::isValidUrl);

        BUILDER.pop();

        BUILDER.push("Security Settings");

        SECRET_SOURCE = BUILDER
                .comment(
                        "Where to load the server secret from.",
                        "ENV_VAR (recommended): reads the secret from an environment variable.",
                        "CONFIG_VALUE (unsafe): stores the secret directly in this file in plaintext.",
                        " USE CONFIG_VALUE ONLY FOR LOCAL TESTING CAREFULLY"
                )
                .defineEnum("secretSource", SecretSource.ENV_VAR);

        SECRET_ENV_VAR_NAME = BUILDER
                .comment("Name of the environment variable holding thhe secret (hex-encoded)")
                .define("secretEnvVarName", "MC_SECRET");

        SECRET_RAW_VALUE = BUILDER
                .comment("Used only if secretSource = CONFIG_VALUE. Leave blank otherwise.")
                .define("secretValue", "");

        BUILDER.pop();

        BUILDER.push("Whitelist");
        WHITELISTED_UUIDS = BUILDER
                .comment("Insert here UUIDs of players that don't need to have enforced login")
                .defineList("whitelisted_uuids", ArrayList::new, obj -> obj instanceof String);

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
