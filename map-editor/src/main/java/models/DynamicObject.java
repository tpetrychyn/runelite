package models;

import lombok.Getter;
import net.runelite.api.Entity;
import net.runelite.api.Model;
import net.runelite.api.Node;
import net.runelite.cache.ConfigType;
import net.runelite.cache.IndexType;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.definitions.SequenceDefinition;
import net.runelite.cache.definitions.VarbitDefinition;
import net.runelite.cache.definitions.loaders.SequenceLoader;
import net.runelite.cache.definitions.loaders.VarbitLoader;
import net.runelite.cache.fs.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class DynamicObject implements Entity {
    private int id;
    private int type;
    private int orientation;
    private int plane;
    private int x;
    private int y;
    private SequenceDefinition sequenceDefinition;
    private int frame;
    private int cycleStart;

    private final ObjectManager objectManager;

    // FIXME: Have a global static cache for all definitions
    public static Map<Integer, SequenceDefinition> sequenceDefinitionCache = new HashMap<>();
    public static Map<Integer, VarbitDefinition> varbitDefinitionCache = new HashMap<>();

    public DynamicObject(ObjectManager objectManager, int id, int type, int orientation, int plane, int x, int y, int animId, boolean bool8, StaticObject entity) {
        this.objectManager = objectManager;
        this.id = id;
        this.type = type;
        this.orientation = orientation;
        this.plane = plane;
        this.x = x;
        this.y = y;
        if (animId != -1) {
            SequenceDefinition sequenceDefinition = sequenceDefinitionCache.get(animId);
            if (sequenceDefinition == null) {
                Store store = StoreProvider.getStore();
                Storage storage = store.getStorage();
                Index index = store.getIndex(IndexType.CONFIGS);
                Archive archive = index.getArchive(ConfigType.SEQUENCE.getId());

                FSFile seqFile = null;
                try {
                    byte[] archiveData = storage.loadArchive(archive);
                    ArchiveFiles files = archive.getFiles(archiveData);
                    seqFile = files.findFile(animId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (seqFile == null) {
                    return;
                }

                SequenceLoader loader = new SequenceLoader();
                sequenceDefinition = loader.load(seqFile.getFileId(), seqFile.getContents());
                sequenceDefinitionCache.put(animId, sequenceDefinition);
            }

            this.sequenceDefinition = sequenceDefinition;
            this.frame = 0;
            this.cycleStart = 0;

            if (bool8 && this.sequenceDefinition.frameCount != -1) {
                this.frame = (int)(Math.random() * (double)this.sequenceDefinition.frameIds.length);
                this.cycleStart -= (int)(Math.random() * (double)this.sequenceDefinition.frameLengths[this.frame]);
            }
        }
    }

    private static final long clientStart = System.currentTimeMillis();
    @Override
    public Model getModel() {
        if (this.sequenceDefinition == null) {
            return null;
        }
        int clientCycle = (int)((System.currentTimeMillis() - clientStart) / 20);
        int var1 = clientCycle - cycleStart;
        if (var1 > 100 && sequenceDefinition.frameCount > 0) {
            var1 = 100;
        }

        label55: {
            do {
                do {
                    if (var1 <= this.sequenceDefinition.frameLengths[this.frame]) {
                        break label55;
                    }

                    var1 -= this.sequenceDefinition.frameLengths[this.frame];
                    ++this.frame;
                } while(this.frame < this.sequenceDefinition.frameIds.length);

                this.frame -= this.sequenceDefinition.frameCount;
            } while(this.frame >= 0 && this.frame < this.sequenceDefinition.frameIds.length);

            this.sequenceDefinition = null;
        }

        this.cycleStart = clientCycle - var1;

        ObjectDefinition objectDefinition = objectManager.getObject(id);
        if (objectDefinition == null) {
            return null;
        }

        ModelDefinition modelDefinition = objectDefinition.getModelDynamic(type, orientation, sequenceDefinition, frame);
        if (modelDefinition == null) {
            return null;
        }
        return new StaticObject(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
    }

    @Override
    public int getModelHeight() {
        return 0;
    }

    @Override
    public void setModelHeight(int modelHeight) { }

    @Override
    public void draw(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) { }

    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public Node getPrevious() {
        return null;
    }

    @Override
    public long getHash() {
        return 0;
    }
}
