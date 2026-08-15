package pl.yasinvolved.discordoauth.crypto;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {
    private static final ConcurrentHashMap<HashedUuid, UUID> states = new ConcurrentHashMap<>();

    public static HashedUuid createState(UUID uuid) {
        HashedUuid hashed = HashedUuidFactory.getInstance().derive(uuid);
        states.put(hashed, uuid);
        return hashed;
    }

    public static UUID close(HashedUuid hashed) {
        return states.remove(hashed);
    }
}
