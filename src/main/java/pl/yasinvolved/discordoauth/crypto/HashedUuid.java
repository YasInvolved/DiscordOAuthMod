package pl.yasinvolved.discordoauth.crypto;

import org.jetbrains.annotations.NotNull;

import java.util.HexFormat;

public class HashedUuid implements Comparable<HashedUuid> {
    private static final HexFormat HEX = HexFormat.of();
    private final String hex;

    HashedUuid(String hex) {
        this.hex = hex;
    }

    public static HashedUuid fromHex(@NotNull String hex) {
        if (hex.length() != 64 || !hex.chars().allMatch(HashedUuid::isHexDigit)) {
            throw new IllegalArgumentException("Not a valid HashedUuid hex string: " + hex);
        }

        return new HashedUuid(hex);
    }

    private static boolean isHexDigit(int c) {
        return (c >= '0' && c <= '9' || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
    }

    public String asHex() {
        return hex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashedUuid other)) return false;
        return hex.equals(other.hex);
    }

    @Override
    public int hashCode() {
        return hex.hashCode();
    }

    @Override
    public int compareTo(@NotNull HashedUuid o) {
        return hex.compareTo(o.hex);
    }

    @Override
    public String toString() {
        return hex;
    }
}
