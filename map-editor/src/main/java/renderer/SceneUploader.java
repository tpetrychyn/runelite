package renderer;/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
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

import models.*;
import models.Renderable;
import renderer.helpers.GpuFloatBuffer;
import renderer.helpers.GpuIntBuffer;
import scene.SceneRegion;
import scene.SceneTile;
import net.runelite.api.*;
import scene.Scene;

public class SceneUploader {
    int sceneId = (int) (System.currentTimeMillis() / 1000L);
    private int offset;
    private int uvoffset;

    public void upload(Scene scene, GpuIntBuffer vertexbuffer, GpuFloatBuffer uvBuffer) {
        ++sceneId;
        offset = 0;
        uvoffset = 0;
        vertexbuffer.clear();
        uvBuffer.clear();

        for (int rx = 0; rx < scene.getRadius(); rx++) {
            for (int ry = 0; ry < scene.getRadius(); ry++) {
                for (int z = 0; z < Constants.MAX_Z; ++z) {
                    for (int x = 0; x < Constants.REGION_SIZE; ++x) {
                        for (int y = 0; y < Constants.REGION_SIZE; ++y) {
                            SceneRegion region = scene.getRegion(rx, ry);
                            if (region == null) {
                                continue;
                            }
                            SceneTile tile = region.getTiles()[z][x][y];
                            if (tile != null) {
                                reset(tile);
                            }
                        }
                    }
                }
            }
        }

        for (int rx = 0; rx < scene.getRadius(); rx++) {
            for (int ry = 0; ry < scene.getRadius(); ry++) {
                for (int z = 0; z < Constants.MAX_Z; ++z) {
                    for (int x = 0; x < Constants.REGION_SIZE; ++x) {
                        for (int y = 0; y < Constants.REGION_SIZE; ++y) {
                            SceneRegion region = scene.getRegion(rx, ry);
                            if (region == null) {
                                continue;
                            }
                            SceneTile tile = region.getTiles()[z][x][y];
                            if (tile != null) {
                                upload(tile, vertexbuffer, uvBuffer);
                            }
                        }
                    }
                }
            }
        }
    }

    private void reset(SceneTile tile) {
//		Tile bridge = tile.getBridge();
//		if (bridge != null)
//		{
//			reset(bridge);
//		}

        TilePaintImpl sceneTilePaint = tile.getTilePaint();
        if (sceneTilePaint != null) {
            sceneTilePaint.setBufferOffset(-1);
        }

        TileModelImpl sceneTileModel = tile.getTileModel();
        if (sceneTileModel != null) {
            sceneTileModel.setBufferOffset(-1);
        }

        WallDecoration wallDecoration = tile.getWallDecoration();
        if (wallDecoration != null) {
            Entity entityA = wallDecoration.getEntityA();
            if (entityA instanceof StaticObject) {
                ((StaticObject) wallDecoration.getEntityA()).setBufferOffset(-1);
            }
            Entity entityB = wallDecoration.getEntityB();
            if (entityB instanceof StaticObject) {
                ((StaticObject) wallDecoration.getEntityB()).setBufferOffset(-1);
            }
        }

        FloorDecoration floorDecoration = tile.getFloorDecoration();
        if (floorDecoration != null) {
            floorDecoration.getModel().setBufferOffset(-1);
        }

//		DecorativeObject decorativeObject = tile.getDecorativeObject();
//		if (decorativeObject != null)
//		{
//			if (decorativeObject.getRenderable() instanceof Model)
//			{
//				((Model) decorativeObject.getRenderable()).setBufferOffset(-1);
//			}
//		}
//
//		GameObject[] gameObjects = tile.getGameObjects();
//		for (GameObject gameObject : gameObjects)
//		{
//			if (gameObject == null)
//			{
//				continue;
//			}
//			if (gameObject.getRenderable() instanceof Model)
//			{
//				((Model) gameObject.getRenderable()).setBufferOffset(-1);
//			}
//		}
    }

    public void upload(SceneTile tile, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
//		Tile bridge = tile.getBridge();
//		if (bridge != null)
//		{
//			upload(bridge, vertexBuffer, uvBuffer);
//		}

        TilePaintImpl sceneTilePaint = tile.getTilePaint();
        if (sceneTilePaint != null) {
            sceneTilePaint.setBufferOffset(offset);
            if (sceneTilePaint.getTexture() != -1) {
                sceneTilePaint.setUvBufferOffset(uvoffset);
            } else {
                sceneTilePaint.setUvBufferOffset(-1);
            }
            int len = upload(sceneTilePaint, vertexBuffer, uvBuffer);
            sceneTilePaint.setBufferLen(len);
            offset += len;
            if (sceneTilePaint.getTexture() != -1) {
                uvoffset += len;
            }
        }

        TileModelImpl sceneTileModel = tile.getTileModel();
        if (sceneTileModel != null) {
            sceneTileModel.setBufferOffset(offset);
            if (sceneTileModel.getTriangleTextureId() != null) {
                sceneTileModel.setUvBufferOffset(uvoffset);
            } else {
                sceneTileModel.setUvBufferOffset(-1);
            }
            int len = upload(sceneTileModel, vertexBuffer, uvBuffer);
            sceneTileModel.setBufferLen(len);
            offset += len;
            if (sceneTileModel.getTriangleTextureId() != null) {
                uvoffset += len;
            }
        }

        WallDecoration wallDecoration = tile.getWallDecoration();
        if (wallDecoration != null) {
            Entity modelA = wallDecoration.getEntityA();
            if (modelA instanceof StaticObject) {
                uploadModel(modelA.getModel(), vertexBuffer, uvBuffer);
            }

            Entity modelB = wallDecoration.getEntityA();
            if (modelB instanceof StaticObject) {
                uploadModel(modelB.getModel(), vertexBuffer, uvBuffer);
            }
        }

        FloorDecoration floorDecoration = tile.getFloorDecoration();
        if (floorDecoration != null) {
            uploadModel(floorDecoration.getModel(), vertexBuffer, uvBuffer);
        }

//		DecorativeObject decorativeObject = tile.getDecorativeObject();
//		if (decorativeObject != null)
//		{
//			Renderable renderable = decorativeObject.getRenderable();
//			if (renderable instanceof Model)
//			{
//				uploadModel((Model) renderable, vertexBuffer, uvBuffer);
//			}
//
//			Renderable renderable2 = decorativeObject.getRenderable2();
//			if (renderable2 instanceof Model)
//			{
//				uploadModel((Model) renderable2, vertexBuffer, uvBuffer);
//			}
//		}
//
//		GameObject[] gameObjects = tile.getGameObjects();
//		for (GameObject gameObject : gameObjects)
//		{
//			if (gameObject == null)
//			{
//				continue;
//			}
//
//			Renderable renderable = gameObject.getRenderable();
//			if (renderable instanceof Model)
//			{
//				uploadModel((Model) gameObject.getRenderable(), vertexBuffer, uvBuffer);
//			}
//		}
    }

    public void upload(Renderable renderable, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
        if (renderable instanceof FloorDecoration) {
            FloorDecoration fd = (FloorDecoration) renderable;
            int len = uploadModel(fd.getModel(), vertexBuffer, uvBuffer);
        }
    }

    public int upload(TilePaintImpl tile, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
        int swHeight = tile.getSwHeight();
        int seHeight = tile.getSeHeight();
        int neHeight = tile.getNeHeight();
        int nwHeight = tile.getNwHeight();

        final int neColor = tile.getNeColor();
        final int nwColor = tile.getNwColor();
        final int seColor = tile.getSeColor();
        final int swColor = tile.getSwColor();

        if (neColor == 12345678) {
            return 0;
        }

        vertexBuffer.ensureCapacity(24);
        uvBuffer.ensureCapacity(24);

        // 0,0
        int vertexDx = 0;
        int vertexDy = 0;
        int vertexDz = swHeight;
        final int c1 = swColor;

        // 1,0
        int vertexCx = Perspective.LOCAL_TILE_SIZE;
        int vertexCy = 0;
        int vertexCz = seHeight;
        final int c2 = seColor;

        // 1,1
        int vertexAx = Perspective.LOCAL_TILE_SIZE;
        int vertexAy = Perspective.LOCAL_TILE_SIZE;
        int vertexAz = neHeight;
        final int c3 = neColor;

        // 0,1
        int vertexBx = 0;
        int vertexBy = Perspective.LOCAL_TILE_SIZE;
        int vertexBz = nwHeight;
        final int c4 = nwColor;

        vertexBuffer.put(vertexAx, vertexAz, vertexAy, c3);
        vertexBuffer.put(vertexBx, vertexBz, vertexBy, c4);
        vertexBuffer.put(vertexCx, vertexCz, vertexCy, c2);

        vertexBuffer.put(vertexDx, vertexDz, vertexDy, c1);
        vertexBuffer.put(vertexCx, vertexCz, vertexCy, c2);
        vertexBuffer.put(vertexBx, vertexBz, vertexBy, c4);

        if (tile.getTexture() != -1) {
            float tex = tile.getTexture() + 1f;
            uvBuffer.put(tex, 1.0f, 1.0f, 0f);
            uvBuffer.put(tex, 0.0f, 1.0f, 0f);
            uvBuffer.put(tex, 1.0f, 0.0f, 0f);

            uvBuffer.put(tex, 0.0f, 0.0f, 0f);
            uvBuffer.put(tex, 1.0f, 0.0f, 0f);
            uvBuffer.put(tex, 0.0f, 1.0f, 0f);
        }

        return 6;
    }

    public int upload(TileModelImpl sceneTileModel, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
        final int[] faceX = sceneTileModel.getFaceX();
        final int[] faceY = sceneTileModel.getFaceY();
        final int[] faceZ = sceneTileModel.getFaceZ();

        final int[] vertexX = sceneTileModel.getVertexX();
        final int[] vertexY = sceneTileModel.getVertexY();
        final int[] vertexZ = sceneTileModel.getVertexZ();

        final int[] triangleColorA = sceneTileModel.getTriangleColorA();
        final int[] triangleColorB = sceneTileModel.getTriangleColorB();
        final int[] triangleColorC = sceneTileModel.getTriangleColorC();

        final int[] triangleTextures = sceneTileModel.getTriangleTextureId();

        final int faceCount = faceX.length;

        vertexBuffer.ensureCapacity(faceCount * 12);
        uvBuffer.ensureCapacity(faceCount * 12);

        int cnt = 0;
        for (int i = 0; i < faceCount; ++i) {
            final int triangleA = faceX[i];
            final int triangleB = faceY[i];
            final int triangleC = faceZ[i];

            final int colorA = triangleColorA[i];
            final int colorB = triangleColorB[i];
            final int colorC = triangleColorC[i];

            if (colorA == 12345678) {
                continue;
            }

            cnt += 3;

            int vertexXA = vertexX[triangleA];
            int vertexZA = vertexZ[triangleA];

            int vertexXB = vertexX[triangleB];
            int vertexZB = vertexZ[triangleB];

            int vertexXC = vertexX[triangleC];
            int vertexZC = vertexZ[triangleC];

            vertexBuffer.put(vertexXA, vertexY[triangleA], vertexZA, colorA);
            vertexBuffer.put(vertexXB, vertexY[triangleB], vertexZB, colorB);
            vertexBuffer.put(vertexXC, vertexY[triangleC], vertexZC, colorC);

            if (triangleTextures != null) {
                if (triangleTextures[i] != -1) {
                    float tex = triangleTextures[i] + 1f;
                    uvBuffer.put(tex, vertexXA / 128f, vertexZA / 128f, 0f);
                    uvBuffer.put(tex, vertexXB / 128f, vertexZB / 128f, 0f);
                    uvBuffer.put(tex, vertexXC / 128f, vertexZC / 128f, 0f);
                } else {
                    uvBuffer.put(0, 0, 0, 0f);
                    uvBuffer.put(0, 0, 0, 0f);
                    uvBuffer.put(0, 0, 0, 0f);
                }
            }
        }

        return cnt;
    }

    public int uploadModel(Model model, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
        if (model.getBufferOffset() > 0) {
            return -1;
        }

        model.setBufferOffset(offset);
        if (model.getFaceTextures() != null) {
            model.setUvBufferOffset(uvoffset);
        } else {
            model.setUvBufferOffset(-1);
        }
        model.setSceneId(sceneId);

        vertexBuffer.ensureCapacity(model.getTrianglesCount() * 12);
        uvBuffer.ensureCapacity(model.getTrianglesCount() * 12);

        final int triangleCount = model.getTrianglesCount();
        int len = 0;
        for (int i = 0; i < triangleCount; ++i) {
            len += pushFace(model, i, vertexBuffer, uvBuffer);
        }

        offset += len;
        if (model.getFaceTextures() != null) {
            uvoffset += len;
        }

        return len;
    }

    public int pushFace(Model model, int face, GpuIntBuffer vertexBuffer, GpuFloatBuffer uvBuffer) {
        final int[] vertexX = model.getVerticesX();
        final int[] vertexY = model.getVerticesY();
        final int[] vertexZ = model.getVerticesZ();

        final int[] trianglesX = model.getTrianglesX();
        final int[] trianglesY = model.getTrianglesY();
        final int[] trianglesZ = model.getTrianglesZ();

        final int[] color1s = model.getFaceColors1();
        final int[] color2s = model.getFaceColors2();
        final int[] color3s = model.getFaceColors3();

        final byte[] transparencies = model.getTriangleTransparencies();
        final short[] faceTextures = model.getFaceTextures();
        final byte[] facePriorities = model.getFaceRenderPriorities();

        int triangleA = trianglesX[face];
        int triangleB = trianglesY[face];
        int triangleC = trianglesZ[face];

        int color1 = color1s[face];
        int color2 = color2s[face];
        int color3 = color3s[face];

        int alpha = 0;
        if (transparencies != null && (faceTextures == null || faceTextures[face] == -1)) {
            alpha = (transparencies[face] & 0xFF) << 24;
        }
        int priority = 0;
        if (facePriorities != null) {
            priority = (facePriorities[face] & 0xff) << 16;
        }

        if (color3 == -1) {
            color2 = color3 = color1;
        } else if (color3 == -2) {
            vertexBuffer.put(0, 0, 0, 0);
            vertexBuffer.put(0, 0, 0, 0);
            vertexBuffer.put(0, 0, 0, 0);

            if (faceTextures != null) {
                uvBuffer.put(0, 0, 0, 0f);
                uvBuffer.put(0, 0, 0, 0f);
                uvBuffer.put(0, 0, 0, 0f);
            }
            return 3;
        }

        int a, b, c;

        a = vertexX[triangleA];
        b = vertexY[triangleA];
        c = vertexZ[triangleA];

        vertexBuffer.put(a, b, c, alpha | priority | color1);

        a = vertexX[triangleB];
        b = vertexY[triangleB];
        c = vertexZ[triangleB];

        vertexBuffer.put(a, b, c, alpha | priority | color2);

        a = vertexX[triangleC];
        b = vertexY[triangleC];
        c = vertexZ[triangleC];

        vertexBuffer.put(a, b, c, alpha | priority | color3);

        float[][] u = model.getFaceTextureUCoordinates();
        float[][] v = model.getFaceTextureVCoordinates();
        float[] uf, vf;
        if (faceTextures != null) {
            if (u != null && v != null && (uf = u[face]) != null && (vf = v[face]) != null) {
                float texture = faceTextures[face] + 1f;
                uvBuffer.put(texture, uf[0], vf[0], 0f);
                uvBuffer.put(texture, uf[1], vf[1], 0f);
                uvBuffer.put(texture, uf[2], vf[2], 0f);
            } else {
                uvBuffer.put(0f, 0f, 0f, 0f);
                uvBuffer.put(0f, 0f, 0f, 0f);
                uvBuffer.put(0f, 0f, 0f, 0f);
            }
        }

        return 3;
    }
}
