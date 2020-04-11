package renderer;

import layoutControllers.MainController;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;

@Getter
@Setter
public class Camera {

    private int yaw = 0; // yaw 0 true north, same with 2047 and 1
    private int pitch = 220;
    private int scale = 540;

    // lock camera to center of scene!
    private int cameraX = Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE / 2;
    private int cameraY = Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE / 2;
    private int cameraZ = -2500;

    private int centerX = 400; // HALF OF VIEWPORT!
    private int centerY = 300; // HALF OF VIEWPORT!

    public boolean addX(int amt) {
        this.cameraX += amt;

        if (cameraX >= Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE) {
            cameraX = 0;
            return true;
        }

        if (cameraX < 0) {
            cameraX = Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE - Perspective.LOCAL_TILE_SIZE;
            return true;
        }

        return false;
    }

    // true if boundary edge triggered
    public boolean addY(int amt) {
        this.cameraY += amt;

        if (cameraY >= Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE) {
            cameraY = 0;
            return true;
        }

        if (cameraY < 0) {
            cameraY = Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE - Perspective.LOCAL_TILE_SIZE;
            return true;
        }
        return false;
    }

    public void addZ(int amt) {
        this.cameraZ += amt;
    }

    public void addYaw(int amt) {
        int newYaw = this.yaw + amt;
        if (newYaw > 2047) {
            newYaw = 0;
        }

        if (newYaw < 0) {
            newYaw = 2047;
        }

        this.yaw = newYaw;
    }

    public void addPitch(int amt) {
        int newPitch = this.pitch + amt;
        // straight down is 500, straight up is 1500 roughly
        if (newPitch > 550 && newPitch < 1500) {
            return;
        }

        if (newPitch < 0) {
            newPitch = 2047;
        }

        if (newPitch > 2047) {
            newPitch = 0;
        }

        this.pitch = newPitch;
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