import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Perspective;

@Getter
@Setter
public class Camera {
    private int yaw = 93;
    private int pitch = 186;
    private int scale = 540;

    private int cameraX = 6336;
    private int cameraY = 5811;
    private int cameraZ = -2071;

    private int cameraX2;
    private int cameraY2;
    private int cameraZ2;

    private int centerX;
    private int centerY;

    public void addX(int amt) {
        this.cameraX += amt;
    }

    public void addY(int amt) {
        this.cameraY += amt;
    }

    public void addZ(int amt) {
        this.cameraZ += amt;
    }

    public void addYaw(int amt) {
        this.yaw += amt;

        if (yaw >= 2048) {
            yaw = 0;
        }

        if (yaw < 0) {
            yaw = 2047;
        }
    }

    public void addPitch(int amt) {
        this.pitch += amt;
    }

    public int getPitchSin() {
        return Perspective.SINE[this.pitch];
    }

    public int getPitchCos() {
        return Perspective.COSINE[this.pitch];
    }

    public int getYawSin() {
        return Perspective.SINE[this.yaw];
    }

    public int getYawCos() {
        return Perspective.COSINE[this.yaw];
    }
}