package models;

import net.runelite.cache.definitions.providers.SpriteProvider;
import net.runelite.cache.item.RSTextureProvider;

public class TextureProviderImpl extends RSTextureProvider {
    private double brightness;

    public TextureProviderImpl(net.runelite.cache.definitions.providers.TextureProvider textureProvider, SpriteProvider spriteProvider) {
        super(textureProvider, spriteProvider);
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }
}
