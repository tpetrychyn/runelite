package controllers;

import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.models.ObjExporter;

import java.util.List;

public class JavaFxHelpers {

    public static MeshView[] modelToMeshViews(ModelDefinition md, RSTextureProvider textureProvider) {
//        md.computeNormals();
//        md.computeTextureUVCoordinates();

        MeshView[] meshViews = new MeshView[md.faceCount];
        for (int i = 0; i < md.faceCount; i++) {
            TriangleMesh mesh = new TriangleMesh();

            int faceA = md.faceVertexIndices1[i];
            int faceB = md.faceVertexIndices2[i];
            int faceC = md.faceVertexIndices3[i];

            mesh.getPoints().addAll(md.vertexPositionsX[faceA], md.vertexPositionsY[faceA], md.vertexPositionsZ[faceA]);
            mesh.getPoints().addAll(md.vertexPositionsX[faceB], md.vertexPositionsY[faceB], md.vertexPositionsZ[faceB]);
            mesh.getPoints().addAll(md.vertexPositionsX[faceC], md.vertexPositionsY[faceC], md.vertexPositionsZ[faceC]);

//            mesh.getNormals().addAll(md.vertexNormals[faceA].x, md.vertexNormals[faceA].y, md.vertexNormals[faceA].z);
//            mesh.getNormals().addAll(md.vertexNormals[faceB].x, md.vertexNormals[faceB].y, md.vertexNormals[faceB].z);
//            mesh.getNormals().addAll(md.vertexNormals[faceC].x, md.vertexNormals[faceC].y, md.vertexNormals[faceC].z);

            if (md.faceTextureVCoordinates != null && md.faceTextureUCoordinates != null
                    && md.faceTextureUCoordinates[i] != null && md.faceTextureVCoordinates[i] != null) {
                mesh.getTexCoords().addAll(md.faceTextureUCoordinates[i][0], md.faceTextureVCoordinates[i][0]);
                mesh.getTexCoords().addAll(md.faceTextureUCoordinates[i][1], md.faceTextureVCoordinates[i][1]);
                mesh.getTexCoords().addAll(md.faceTextureUCoordinates[i][2], md.faceTextureVCoordinates[i][2]);
            } else {
                mesh.getTexCoords().addAll(0, 0, 1, 0, 0, 1);
            }

            mesh.getFaces().addAll(0, 0, 1, 1, 2, 2);

            PhongMaterial mat = new PhongMaterial();

            int textureId = md.faceTextures != null ? md.faceTextures[i] : -1;
            if (textureId == -1) {
                java.awt.Color color = ObjExporter.rs2hsbToColor(md.faceColors[i]);
                Color c = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, md.faceAlphas != null ? 1 - (md.faceAlphas[i] & 0xFF) / 255f : 1);
                mat.setDiffuseColor(c);
            } else {
                int[] texture = textureProvider.load(textureId);
                WritableImage writableImage = new WritableImage(128, 128);
                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        int argb = 0xFF << 24 | texture[x + y * 128]; //argb, full alpha first bits
                        if (texture[x + y * 128] == 0) {
                            argb = 0;
                        }
                        writableImage.getPixelWriter().setArgb(x, y, argb);
                    }
                }
                mat.setDiffuseMap(writableImage);
            }

            MeshView mv = new MeshView();
            mv.setMesh(mesh);
            mv.setMaterial(mat);
            meshViews[i] = mv;
        }

        return meshViews;
    }

    // TODO: Works with osrs model format of using a material for every face
    // does not work with real objs
    public static ModelDefinition objToModelDefinition(Obj obj, List<Mtl> mtlList) {
        ModelDefinition m = new ModelDefinition();
        m.vertexCount = obj.getNumVertices();
        m.faceCount = obj.getNumFaces();

        m.vertexPositionsX = new int[m.vertexCount];
        m.vertexPositionsY = new int[m.vertexCount];
        m.vertexPositionsZ = new int[m.vertexCount];
        for (int i = 0; i < m.vertexCount; i++) {
            m.vertexPositionsX[i] = (int) obj.getVertex(i).getX();
            m.vertexPositionsY[i] = -(int) obj.getVertex(i).getY();
            m.vertexPositionsZ[i] = -(int) obj.getVertex(i).getZ();
        }

        m.faceVertexIndices1 = new int[m.faceCount];
        m.faceVertexIndices2 = new int[m.faceCount];
        m.faceVertexIndices3 = new int[m.faceCount];
        m.faceColors = new short[m.faceCount];

        for (int i = 0; i < m.faceCount; i++) {
            m.faceVertexIndices1[i] = obj.getFace(i).getVertexIndex(0);
            m.faceVertexIndices2[i] = obj.getFace(i).getVertexIndex(1);
            m.faceVertexIndices3[i] = obj.getFace(i).getVertexIndex(2);

            Mtl mtl = mtlList.get(i);
            float[] hsbVals = java.awt.Color.RGBtoHSB((int) (mtl.getKd().getX() * 255f), (int) (mtl.getKd().getY() * 255f), (int) (mtl.getKd().getZ() * 255f), null);

            int encodeHue = ((int) (hsbVals[0] * 63) & 0x3f) << 10;
            int encodeSat = ((int) (hsbVals[1] * 7) & 0x07) << 7;
            int encodeBri = ((int) (hsbVals[2] * 127) & 0x7f);
            short hsbCol = (short) (encodeHue | encodeSat | encodeBri + 1);
            m.faceColors[i] = hsbCol;
        }

        return m;
    }
}
