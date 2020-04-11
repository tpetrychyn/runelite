package net.runelite.cache.fs;

import net.runelite.cache.util.StoreLocation;

import java.io.IOException;

public class StoreProvider {
    private static Store store;

    public static Store getStore() {
        if (store == null) {
            try {
                store = new Store(StoreLocation.LOCATION);
                store.load();
            } catch (IOException e) {
                throw new RuntimeException(String.format("Store failed to be initialized from %s", StoreLocation.LOCATION));
            }
        }

        return store;
    }
}
