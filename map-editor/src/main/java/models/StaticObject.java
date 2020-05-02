package models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Model;
import net.runelite.api.Node;
import net.runelite.api.model.Triangle;
import net.runelite.api.model.Vertex;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.loaders.MapLoader;
import net.runelite.cache.models.FaceNormal;
import net.runelite.cache.models.VertexNormal;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StaticObject extends ModelDefinition implements Model {
    private int bufferOffset;
    private int uvBufferOffset;
    private int bufferLen;

    private int sceneId;

    private int[] faceColors1;
    private int[] faceColors2;
    private int[] faceColors3;

    byte[] field1840;
    int field1852;
    int[] field1844;
    int[] field1865;
    int[] field1846;
    private int boundsType;
    private int bottomY;
    private int xzRadius;
    private int radius;
    private int diameter;
    private int height;

    public StaticObject(ModelDefinition def, int ambient, int contrast, int x, int y, int z) {
        def.computeNormals();
        def.computeTextureUVCoordinates();
        int somethingMagnitude = (int) Math.sqrt(z * z + x * x + y * y);
        int var7 = somethingMagnitude * contrast >> 8;
        faceColors1 = new int[def.faceCount];
        faceColors2 = new int[def.faceCount];
        faceColors3 = new int[def.faceCount];
        if (def.textureTriangleCount > 0 && def.textureCoordinates != null)
        {
            int[] var9 = new int[def.textureTriangleCount];

            int var10;
            for (var10 = 0; var10 < def.faceCount; ++var10)
            {
                if (def.textureCoordinates[var10] != -1)
                {
                    ++var9[def.textureCoordinates[var10] & 255];
                }
            }

            field1852 = 0;

            for (var10 = 0; var10 < def.textureTriangleCount; ++var10)
            {
                if (var9[var10] > 0 && def.textureRenderTypes[var10] == 0)
                {
                    ++field1852;
                }
            }

            field1844 = new int[field1852];
            field1865 = new int[field1852];
            field1846 = new int[field1852];
            var10 = 0;


            for (int i = 0; i < def.textureTriangleCount; ++i)
            {
                if (var9[i] > 0 && def.textureRenderTypes[i] == 0)
                {
                    field1844[var10] = def.textureTriangleVertexIndices1[i] & '\uffff';
                    field1865[var10] = def.textureTriangleVertexIndices2[i] & '\uffff';
                    field1846[var10] = def.textureTriangleVertexIndices3[i] & '\uffff';
                    var9[i] = var10++;
                }
                else
                {
                    var9[i] = -1;
                }
            }

            field1840 = new byte[def.faceCount];

            for (int i = 0; i < def.faceCount; ++i)
            {
                if (def.textureCoordinates[i] != -1)
                {
                    field1840[i] = (byte) var9[def.textureCoordinates[i] & 255];
                }
                else
                {
                    field1840[i] = -1;
                }
            }
        }

        for (int faceIdx = 0; faceIdx < def.faceCount; ++faceIdx)
        {
            byte faceType;
            if (def.faceRenderTypes == null)
            {
                faceType = 0;
            }
            else
            {
                faceType = def.faceRenderTypes[faceIdx];
            }

            byte faceAlpha;
            if (def.faceAlphas == null)
            {
                faceAlpha = 0;
            }
            else
            {
                faceAlpha = def.faceAlphas[faceIdx];
            }

            short faceTexture;
            if (def.faceTextures == null)
            {
                faceTexture = -1;
            }
            else
            {
                faceTexture = def.faceTextures[faceIdx];
            }

            if (faceAlpha == -2)
            {
                faceType = 3;
            }

            if (faceAlpha == -1)
            {
                faceType = 2;
            }

            VertexNormal vertexNormal;
            int tmp;
            FaceNormal faceNormal;
            if (faceTexture == -1)
            {
                if (faceType != 0)
                {
                    if (faceType == 1)
                    {
                        faceNormal = def.faceNormals[faceIdx];
                        tmp = (y * faceNormal.y + z * faceNormal.z + x * faceNormal.x) / (var7 / 2 + var7) + ambient;
                        faceColors1[faceIdx] = method2608(def.faceColors[faceIdx] & '\uffff', tmp);
                        faceColors3[faceIdx] = -1;
                    }
                    else if (faceType == 3)
                    {
                        faceColors1[faceIdx] = 128;
                        faceColors3[faceIdx] = -1;
                    }
                    else
                    {
                        faceColors3[faceIdx] = -2;
                    }
                }
                else
                {
                    int var15 = def.faceColors[faceIdx] & '\uffff';
                    vertexNormal = def.vertexNormals[def.faceVertexIndices1[faceIdx]];

                    tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                    faceColors1[faceIdx] = method2608(var15, tmp);
                    vertexNormal = def.vertexNormals[def.faceVertexIndices2[faceIdx]];

                    tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                    faceColors2[faceIdx] = method2608(var15, tmp);
                    vertexNormal = def.vertexNormals[def.faceVertexIndices3[faceIdx]];

                    tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                    faceColors3[faceIdx] = method2608(var15, tmp);
                }
            }
            else if (faceType != 0)
            {
                if (faceType == 1)
                {
                    faceNormal = def.faceNormals[faceIdx];
                    tmp = (y * faceNormal.y + z * faceNormal.z + x * faceNormal.x) / (var7 / 2 + var7) + ambient;
                    faceColors1[faceIdx] = bound2to126(tmp);
                    faceColors3[faceIdx] = -1;
                }
                else
                {
                    faceColors3[faceIdx] = -2;
                }
            }
            else
            {
                vertexNormal = def.vertexNormals[def.faceVertexIndices1[faceIdx]];

                tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                faceColors1[faceIdx] = bound2to126(tmp);
                vertexNormal = def.vertexNormals[def.faceVertexIndices2[faceIdx]];

                tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                faceColors2[faceIdx] = bound2to126(tmp);
                vertexNormal = def.vertexNormals[def.faceVertexIndices3[faceIdx]];

                tmp = (y * vertexNormal.y + z * vertexNormal.z + x * vertexNormal.x) / (var7 * vertexNormal.magnitude) + ambient;
                faceColors3[faceIdx] = bound2to126(tmp);
            }
        }
        id = def.id;
        vertexCount = def.vertexCount;
        vertexPositionsX = def.vertexPositionsX;
        vertexPositionsY = def.vertexPositionsY;
        vertexPositionsZ = def.vertexPositionsZ;
        faceCount = def.faceCount;
        faceVertexIndices1 = def.faceVertexIndices1;
        faceVertexIndices2 = def.faceVertexIndices2;
        faceVertexIndices3 = def.faceVertexIndices3;
        faceRenderPriorities = def.faceRenderPriorities;
        faceTextureUCoordinates = def.faceTextureUCoordinates;
        faceTextureVCoordinates = def.faceTextureVCoordinates;
        faceAlphas = def.faceAlphas;
        priority = def.priority;
        faceTextures = def.faceTextures;
    }

    @Override
    public List<Vertex> getVertices() {
        return null;
    }

    @Override
    public List<Triangle> getTriangles() {
        return null;
    }

    @Override
    public int getVerticesCount() {
        return vertexCount;
    }

    @Override
    public int[] getVerticesX() { return vertexPositionsX; }

    @Override
    public int[] getVerticesY() {
        return vertexPositionsY;
    }

    @Override
    public int[] getVerticesZ() {
        return vertexPositionsZ;
    }

    @Override
    public int getTrianglesCount() {
        return faceCount;
    }

    @Override
    public int[] getTrianglesX() {
        return faceVertexIndices1;
    }

    @Override
    public int[] getTrianglesY() {
        return faceVertexIndices2;
    }

    @Override
    public int[] getTrianglesZ() {
        return faceVertexIndices3;
    }

    @Override
    public byte[] getTriangleTransparencies() {
        return faceAlphas;
    }

    @Override
    public Model getModel() {
        return this;
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
    public void calculateBoundsCylinder() {
        if (this.boundsType != 1) {
            this.boundsType = 1;
            this.bottomY = 0;
            this.xzRadius = 0;

            this.height = 0;
            for (int var1 = 0; var1 < this.vertexCount; ++var1) {
                int var2 = this.vertexPositionsX[var1];
                int var3 = this.vertexPositionsY[var1];
                int var4 = this.vertexPositionsZ[var1];

                if (-var3 > height) {
                    this.height = -var3;
                }

                if (var3 > this.bottomY) {
                    this.bottomY = var3;
                }

                int var5 = var2 * var2 + var4 * var4;
                if (var5 > this.xzRadius) {
                    this.xzRadius = var5;
                }
            }

            this.xzRadius = (int)(Math.sqrt((double)this.xzRadius) + 0.99D);
            this.radius = (int)(Math.sqrt((double)(this.xzRadius * this.xzRadius + this.height * this.height)) + 0.99D);
            this.diameter = this.radius + (int)(Math.sqrt((double)(this.xzRadius * this.xzRadius + this.bottomY * this.bottomY)) + 0.99D);
        }
    }

    @Override
    public void calculateExtreme(int orientation) { }

    @Override
    public int getCenterX() {
        return 0;
    }

    @Override
    public int getCenterY() {
        return 0;
    }

    @Override
    public int getCenterZ() {
        return 0;
    }

    @Override
    public int getExtremeX() {
        return 0;
    }

    @Override
    public int getExtremeY() {
        return 0;
    }

    @Override
    public int getExtremeZ() {
        return 0;
    }

    @Override
    public int getXYZMag() {
        return xzRadius;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

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

    void resetBounds() {
        boundsType = 0;
    }

    public StaticObject contourGround(MapLoader mapLoader, int xOff, int height, int yOff, boolean deepCopy, int clipType, int worldX, int worldY) {
        this.calculateBoundsCylinder();
        int left = xOff - this.xzRadius;
        int right = xOff + this.xzRadius;
        int top = yOff - this.xzRadius;
        int bottom = yOff + this.xzRadius;
        if (left >= 0 && right + 128 >> 7 < Constants.SCENE_SIZE && top >= 0 && bottom + 128 >> 7 < Constants.SCENE_SIZE) {
            left >>= 7;
            right = right + 127 >> 7;
            top >>= 7;
            bottom = bottom + 127 >> 7;
            if (height == mapLoader.getWorldTile(0, left, top).height && height == mapLoader.getWorldTile(0, right, top).height && height == mapLoader.getWorldTile(0, left, bottom).height && height == mapLoader.getWorldTile(0, right, bottom).height) {
                return this;
            } else {
                StaticObject model;
                if (deepCopy) {
                    model = new StaticObject();
                    model.id = this.id;
                    model.vertexCount = this.vertexCount;
                    model.vertexPositionsX = this.vertexPositionsX;
                    model.vertexPositionsY = new int[vertexCount];
                    model.vertexPositionsZ = this.vertexPositionsZ;
                    model.faceCount = this.faceCount;
                    model.faceVertexIndices1 = this.faceVertexIndices1;
                    model.faceVertexIndices2 = this.faceVertexIndices2;
                    model.faceVertexIndices3 = this.faceVertexIndices3;
                    model.faceRenderPriorities = this.faceRenderPriorities;
                    model.faceAlphas = this.faceAlphas;
                    model.priority = this.priority;
                    model.faceTextures = this.faceTextures;
                    model.faceColors1 = this.faceColors1;
                    model.faceColors2 = this.faceColors2;
                    model.faceColors3 = this.faceColors3;
                } else {
                    model = this;
                }

                int var12;
                int var13;
                int var14;
                int var15;
                int var16;
                int var17;
                int var18;
                int var19;
                int var20;
                int var21;
                if (clipType == 0) {
                    for (var12 = 0; var12 < model.vertexCount; ++var12) {
                        var13 = xOff + this.vertexPositionsX[var12];
                        var14 = yOff + this.vertexPositionsZ[var12];
                        var15 = var13 & 127;
                        var16 = var14 & 127;
                        var17 = var13 >> 7;
                        var18 = var14 >> 7;
                        var19 = mapLoader.getWorldTile(0, worldX + var17, worldY + var18).height * (128 - var15) + mapLoader.getWorldTile(0, worldX + var17+1, worldY + var18).height * var15 >> 7;
                        var20 = mapLoader.getWorldTile(0, worldX + var17, worldY + var18+1).height * (128 - var15) + var15 * mapLoader.getWorldTile(0, worldX + var17+1, worldY + var18+1).height >> 7;
                        var21 = var19 * (128 - var16) + var20 * var16 >> 7;
                        model.vertexPositionsY[var12] = var21 + this.vertexPositionsY[var12] - height;
                    }
                } else {
                    for (var12 = 0; var12 < model.vertexCount; ++var12) {
                        var13 = (-this.vertexPositionsY[var12] << 16) / this.height;
                        if (var13 < clipType) {
                            var14 = xOff + this.vertexPositionsX[var12];
                            var15 = yOff + this.vertexPositionsZ[var12];
                            var16 = var14 & 127;
                            var17 = var15 & 127;
                            var18 = var14 >> 7;
                            var19 = var15 >> 7;
                            var20 = mapLoader.getWorldTile(0, worldX + var18, worldY + var19).height * (128 - var16) + mapLoader.getWorldTile(0, worldX + var18+1, worldY + var19).height * var16 >> 7;
                            var21 = mapLoader.getWorldTile(0, worldX + var18, worldY + var19+1).height * (128 - var16) + var16 *  mapLoader.getWorldTile(0, worldX + var18+1, worldY + var19+1).height >> 7;
                            int var22 = var20 * (128 - var17) + var21 * var17 >> 7;
                            model.vertexPositionsY[var12] = (clipType - var13) * (var22 - height) / clipType + this.vertexPositionsY[var12];
                        }
                    }
                }

                model.resetBounds();
                return model;
            }
        } else {
            return this;
        }
    }

    static final int method2608(int var0, int var1)
    {
        var1 = ((var0 & 127) * var1) >> 7;
        var1 = bound2to126(var1);

        return (var0 & 65408) + var1;
    }

    static final int bound2to126(int var0)
    {
        if (var0 < 2)
        {
            var0 = 2;
        }
        else if (var0 > 126)
        {
            var0 = 126;
        }

        return var0;
    }
}
