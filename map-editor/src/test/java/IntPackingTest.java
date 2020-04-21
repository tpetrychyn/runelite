import org.joml.Vector4f;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntPackingTest {
    @Test
    public void testConcatenate() {
        int objectId = 2;
        int testSize = 1024;
        for (int x=0;x<testSize;x++) {
            for (int y=0;y<testSize;y++) {
                // PACKING THE BITS
                int colorPickerId = ((x & 0x3FFF) << 18) | ((y & 0x3FFF) << 4) | objectId & 0xF;

                int alpha = 0xFF - ((colorPickerId & 0xFF000000) >> 24);
                int red = (colorPickerId & 0x00FF0000) >> 16;
                int green = (colorPickerId & 0x0000FF00) >> 8;
                int blue = (colorPickerId & 0x000000FF);

                Vector4f color = new Vector4f(red/255.0f, green/255.0f, blue/255.0f, alpha/255.0f);

                float r = color.x * 0xFF;
                float g = color.y * 0xFF;
                float b = color.z * 0xFF;
                float a = color.w * 0xFF;

                // UNPACKING THE BITS
                int rgbaPacked = ((0xFF - (int) a) << 24) & 0xFF000000 | ((int) r << 16) & 0x00FF0000 | ((int) g << 8) & 0x0000FF00 | ((int) b) & 0xFF;
                int outX = (rgbaPacked >> 18) & 0x3FFF;
                int outY = (rgbaPacked >> 4) & 0x3FFF;
                int objId = rgbaPacked & 0xF;

                assertEquals(outX, x);
                assertEquals(outY, y);
                assertEquals(objId, objectId);
            }
        }
    }
}
