package pl.yasinvolved.discordoauth.crypto;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import pl.yasinvolved.discordoauth.Config;

import java.nio.charset.StandardCharsets;

public class SecretLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void load() {
        Config.SecretSource source = Config.SECRET_SOURCE.get();

        String hex = switch (source) {
            case ENV_VAR -> loadFromEnv();
            case CONFIG_VALUE -> loadFromConfig();
        };

        HashedUuidFactory.init(hex.getBytes(StandardCharsets.UTF_8));
    }

    private static String loadFromEnv() {
        String varName = Config.SECRET_ENV_VAR_NAME.get();
        String value = System.getenv(varName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Config specifies secretSource=ENV_VAR but environment variable '"
                        + varName + "' is not set."
            );
        }
        return value;
    }

    private static String loadFromConfig() {
        String value = Config.SECRET_RAW_VALUE.get();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Config specifies secretSource=CONFIG_VALUE but secretRawValue is empty."
            );
        }
        LOGGER.warn("Loading secret value from mod config. This is not recommended for "
                + "production servers - the secret is stored in plaintext in the config file. "
                + "consider switching secretSource to ENV_VAR"
        );
        return value;
    }
}
