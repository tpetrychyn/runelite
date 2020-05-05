package di;

import com.google.inject.Provider;
import net.runelite.cache.fs.Store;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class StoreProvider implements Provider<Store> {
    private Store store;

    public StoreProvider() {}

    public void setStoreLocation(File f) throws IOException {
        store = new Store(f);
        store.load();
    }

    @Override
    public Store get() {
        return store;
    }
}
