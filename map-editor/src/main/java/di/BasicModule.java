package di;

import com.google.inject.*;
import javafx.fxml.FXMLLoader;
import lombok.SneakyThrows;
import models.ObjectSwatchModel;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.TextureManager;
import net.runelite.cache.fs.Store;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.StoreLocation;
import renderer.Camera;
import scene.Scene;

import javax.inject.Singleton;
import java.io.IOException;

public class BasicModule extends AbstractModule {
    public static Injector injector;

    @SneakyThrows
    @Override
    protected void configure() {
        bind(FXMLLoader.class).toProvider(FXMLLoaderProvider.class);

        bind(ObjectSwatchModel.class).toInstance(new ObjectSwatchModel());
        bind(Scene.class).toInstance(new Scene());
        bind(Camera.class).toInstance(new Camera());


        // Ca
        bind(Store.class).toProvider(StoreProvider.class);
        bind(RSTextureProvider.class).toProvider(RSTextureProviderProvider.class);
        bind(SpriteManager.class).toProvider(SpriteManagerProvider.class);
        bind(TextureManager.class).toProvider(TextureManagerProvider.class);
        bind(RegionLoader.class).toProvider(RegionLoaderProvider.class);
        bind(ObjectManager.class).toProvider(ObjectManagerProvider.class);
    }
}

@Singleton
class FXMLLoaderProvider implements Provider<FXMLLoader> {
    private final FXMLLoader fxmlLoader;

    public FXMLLoaderProvider() {
        Injector injector = Guice.createInjector(new BasicModule());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(injector::getInstance);
        this.fxmlLoader = fxmlLoader;
    }

    @Override
    public FXMLLoader get() {
        return fxmlLoader;
    }
}

@Singleton
class StoreProvider implements Provider<Store> {
    private final Store store;

    public StoreProvider() throws IOException {
        store = new Store(StoreLocation.LOCATION);
        store.load();
    }

    @Override
    public Store get() {
        return store;
    }
}

@Singleton
class RSTextureProviderProvider implements Provider<RSTextureProvider> {
    private final TextureManager textureManager;
    private final SpriteManager spriteManager;

    @Inject
    public RSTextureProviderProvider(TextureManager textureManager, SpriteManager spriteManager) {
        this.textureManager = textureManager;
        this.spriteManager = spriteManager;
    }

    public RSTextureProvider get() {
        return new RSTextureProvider(textureManager, spriteManager);
    }
}

@Singleton
class ObjectManagerProvider implements Provider<ObjectManager> {
    private final ObjectManager objectManager;

    @Inject
    public ObjectManagerProvider(Store store) throws IOException {
        objectManager = new ObjectManager(store);
        objectManager.load();
    }

    public ObjectManager get() {
        return objectManager;
    }
}

@Singleton
class RegionLoaderProvider implements Provider<RegionLoader> {
    private final RegionLoader regionLoader;

    @Inject
    public RegionLoaderProvider(Store store) throws IOException {
        regionLoader = new RegionLoader(store);
        regionLoader.loadRegions();
    }

    public RegionLoader get() {
        return regionLoader;
    }
}

@Singleton
class TextureManagerProvider implements Provider<TextureManager> {
    private final TextureManager textureManager;

    @Inject
    public TextureManagerProvider(Store store) throws IOException {
        textureManager = new TextureManager(store);
        textureManager.load();
    }

    public TextureManager get() {
        return textureManager;
    }
}

@Singleton
class SpriteManagerProvider implements Provider<SpriteManager> {
    private final SpriteManager spriteManager;

    @Inject
    public SpriteManagerProvider(Store store) throws IOException {
        spriteManager = new SpriteManager(store);
        spriteManager.load();
    }

    public SpriteManager get() {
        return spriteManager;
    }
}