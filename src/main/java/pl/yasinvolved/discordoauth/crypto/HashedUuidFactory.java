package pl.yasinvolved.discordoauth.crypto;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;

public final class HashedUuidFactory {
    private static final String HMAC_ALGO = "HmacSHA256";
    private static HashedUuidFactory instance;
    private final byte[] secret;

    private volatile boolean closed = false;

    private HashedUuidFactory(byte[] secret) {
        this.secret = secret;
    }

    public static void init(byte[] secret) {
        instance = new HashedUuidFactory(secret);
    }

    public static HashedUuidFactory getInstance() {
        return instance;
    }

    public static HashedUuidFactory fromEnv(String envVarName) {
        String hex = System.getenv(envVarName);
        if (hex == null || hex.isBlank()) {
            throw new IllegalStateException("Environment variable " + envVarName + " is not set.");
        }

        return fromHex(hex);
    }

    public static HashedUuidFactory fromHex(String hex) {
        byte[] secret = HexFormat.of().parseHex(hex);
        return new HashedUuidFactory(secret);
    }

    public HashedUuid derive(@NotNull UUID uuid) {
        if (closed) {
            throw new IllegalStateException("HashedUuidFactory has been closed");
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
            byte[] result = mac.doFinal(uuid.toString().getBytes(StandardCharsets.UTF_8));
            return HashedUuid.fromHex(HexFormat.of().formatHex(result));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to derive HashedUuid", e);
        }
    }

    public void close() {
        Arrays.fill(secret, (byte)0);
        closed = true;
    }
}
