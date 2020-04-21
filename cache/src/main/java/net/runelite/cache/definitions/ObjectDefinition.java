/*
 * Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.cache.definitions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import net.runelite.cache.IndexType;
import net.runelite.cache.definitions.loaders.ModelLoader;
import net.runelite.cache.fs.*;

@Data
public class ObjectDefinition {
    protected int id;
    protected short[] retextureToFind;
    protected int decorDisplacement = 16;
    protected boolean isHollow = false;
    protected String name = "null";
    protected int[] modelIds;
    protected int[] models;
    protected short[] recolorToFind;
    protected int mapAreaId = -1;
    protected short[] textureToReplace;
    protected int sizeX = 1;
    protected int sizeY = 1;
    protected int anInt2083 = 0;
    protected int[] anIntArray2084;
    protected int offsetX = 0;
    protected boolean mergeNormals = false;
    protected int wallOrDoor = -1;
    protected int animationID = -1;
    protected int transformVarbit = -1;
    protected int ambient = 0;
    protected int contrast = 0;
    protected String[] actions = new String[5];
    protected int interactType = 2;
    protected int mapSceneID = -1;
    protected int blockingMask = 0;
    protected short[] recolorToReplace;
    protected boolean shadow = true;
    protected int modelSizeX = 128;
    protected int modelSizeHeight = 128;
    protected int modelSizeY = 128;
    protected int objectID;
    protected int offsetHeight = 0;
    protected int offsetY = 0;
    protected boolean obstructsGround = false;
    protected int contouredGround = -1;
    protected int supportsItems = -1;
    protected int[] transforms;
    protected boolean isRotated = false;
    protected int transformVarp = -1;
    protected int ambientSoundId = -1;
    protected boolean aBool2111 = false;
    protected int anInt2112 = 0;
    protected int anInt2113 = 0;
    protected boolean blocksProjectile = true;
    protected Map<Integer, Object> params = null;

    public static Map<Long, ModelDefinition> litModelCache = new HashMap<>();
    public static Map<Integer, ModelDefinition> modelDefCache = new HashMap<>();

    public ModelDefinition getModel(int type, int orientation) {
        long modelTag;
        if (this.models == null) {
            modelTag = orientation + (this.id << 10);
        } else {
            modelTag = orientation + (type << 3) + (this.id << 10);
        }

        ModelDefinition litModel = litModelCache.get(modelTag);
        if (litModel == null) {
            litModel = getModelDefinition(type, orientation);
            if (litModel == null) {
                return null;
            }

            // TODO: flat shading

            litModelCache.put(modelTag, litModel);
        }

        return litModel;
    }

    public ModelDefinition getModelDynamic(int type, int orientation, SequenceDefinition sequenceDefinition, int frame) {
        long modelTag;
        if (this.models == null) {
            modelTag = orientation + (this.id << 10);
        } else {
            modelTag = orientation + (type << 3) + (this.id << 10);
        }

        ModelDefinition litModel = litModelCache.get(modelTag);
        if (litModel == null) {
            litModel = getModelDefinition(type, orientation);
            if (litModel == null) {
                return null;
            }

            // TODO: flat shading

            litModelCache.put(modelTag, litModel);
        }

        if (sequenceDefinition == null) {
            return litModel;
        }

        litModel = sequenceDefinition.transformObjectModel(litModel, frame, orientation);

        return litModel;
    }

    private ModelDefinition getModelDefinition(int type, int orientation) {
        ModelDefinition modelDefinition = null;
        if (this.models == null) {
            if (type != 10 || this.modelIds == null) {
                return null;
            }

            for (int i=0;i<modelIds.length;i++) {
                int modelId = modelIds[i];
                if (isRotated) {
                    modelId += 65536;
                }

                modelDefinition = modelDefCache.get(modelId);
                if (modelDefinition == null) {
                    Store store = StoreProvider.getStore();
                    Storage storage = store.getStorage();
                    Index index = store.getIndex(IndexType.MODELS);

                    Archive archive = index.getArchive(modelId);
                    if (archive == null) {
                        return null;
                    }

                    byte[] contents;
                    try {
                        contents = archive.decompress(storage.loadArchive(archive));
                    } catch (IOException e) {
                        return null;
                    }

                    ModelLoader loader = new ModelLoader();
                    modelDefinition = loader.load(modelId, contents);
                    if (modelDefinition == null) {
                        return null;
                    }

                    if (isRotated) {
                        modelDefinition.rotateMulti();
                    }

                    modelDefCache.put(modelId, modelDefinition);
                }
            }
        } else {
            int modelIdx = -1;
            for (int i = 0; i < this.models.length; i++) {
                if (this.models[i] == type) {
                    modelIdx = i;
                    break;
                }
            }

            if (modelIdx == -1) {
                return null;
            }

            int modelId = modelIds[modelIdx];
            boolean isRotated = this.isRotated ^ orientation > 3;
            if (isRotated) {
                modelId += 65536;
            }

            modelDefinition = modelDefCache.get(modelId);
            if (modelDefinition == null) {
                Store store = StoreProvider.getStore();
                Storage storage = store.getStorage();
                Index index = store.getIndex(IndexType.MODELS);

                Archive archive = index.getArchive(modelId);
                if (archive == null) {
                    return null;
                }

                byte[] contents;
                try {
                    contents = archive.decompress(storage.loadArchive(archive));
                } catch (IOException e) {
                    return null;
                }

                ModelLoader loader = new ModelLoader();
                modelDefinition = loader.load(modelId, contents);
                if (modelDefinition == null) {
                    return null;
                }

                if (isRotated) {
                    modelDefinition.rotateMulti();
                }

                modelDefCache.put(modelId, modelDefinition);
            }
        }

        assert modelDefinition != null;
        ModelDefinition copy = new ModelDefinition(modelDefinition, orientation == 0, recolorToFind == null, retextureToFind == null);

        orientation &= 0x3;
        if (orientation == 1) {
            copy.rotateY90Ccw();
        } else if (orientation == 2) {
            copy.rotateY180();
        } else if (orientation == 3) {
            copy.rotateY270Ccw();
        }

        if (recolorToFind != null) {
            for (int i = 0; i < recolorToFind.length; i++) {
                copy.recolor(recolorToFind[i], recolorToReplace[i]);
            }
        }

        if (retextureToFind != null) {
            for (int i = 0; i < retextureToFind.length; i++) {
                copy.retexture(retextureToFind[i], textureToReplace[i]);
            }
        }

        return copy;
    }

//    public ObjectDefinition transform() {
//        int var1 = -1;
//        if (this.transformVarbit != -1) {
//            var1 = getVa
//        }
//    }
}