package renderer;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import javafx.animation.AnimationTimer;
import lombok.Getter;
import scene.Scene;

@Getter
public class InputHandler implements KeyListener, MouseListener {
    private Camera camera;
    private MapEditor mapEditor;

    private boolean isLeftMouseDown = false;
    public boolean leftMousePressed = false;
    private boolean isRightMouseDown = false;
    private boolean[] keys = new boolean[250];
    public boolean mouseClicked;
    private int previousMouseX;
    private int previousMouseY;
    private int mouseX;
    private int mouseY;

    InputHandler(Camera camera, MapEditor mapEditor) {
        this.camera = camera;
        this.mapEditor = mapEditor;

        new AnimationTimer() {
            long lastNanoTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double dt = (now - lastNanoTime) / 1000000;
                lastNanoTime = now;
                handleKeys(dt);
            }
        }.start();
    }

    private void handleKeys(double dt) {
        if (dt > 1000) { // big lag spike, don't send the user flying
            return;
        }
        double xVec = -(double) camera.getYawSin() / 65535;
        double yVec = (double) camera.getYawCos() / 65535;
        double zVec = (double) camera.getPitchSin() / 65535;

        int speed = 1;
        if (keys[KeyEvent.VK_SHIFT]) {
            speed = 4;
        }

        if (keys[KeyEvent.VK_W]) {
            camera.addX((int) (dt * xVec * speed));
            camera.addY((int) (dt * yVec * speed));
            camera.addZ((int) (dt * zVec * speed));
        }

        if (keys[KeyEvent.VK_S]) {
            camera.addX(-(int) (dt * xVec * speed));
            camera.addY(-(int) (dt * yVec * speed));
            camera.addZ(-(int) (dt * zVec * speed));
        }

        if (keys[KeyEvent.VK_A]) {
            // X uses yVec because we want to move perpendicular
            camera.addX(-(int) (dt * yVec * speed));
            camera.addY((int) (dt * xVec * speed));
        }

        if (keys[KeyEvent.VK_D]) {
            camera.addX((int) (dt * yVec * speed));
            camera.addY(-(int) (dt * xVec * speed));
        }

        if (keys[KeyEvent.VK_SPACE]) {
            camera.addZ(-(int) dt * speed);
        }

        if (keys[KeyEvent.VK_X]) {
            camera.addZ((int) dt * speed);
        }

        if (keys[KeyEvent.VK_K]) {
            mapEditor.changeScene(new Scene(mapEditor.sceneRegionBuilder, 13360, 5));
        }
        if (keys[KeyEvent.VK_L]) {
            mapEditor.changeScene(new Scene(mapEditor.sceneRegionBuilder, 13408, 5));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isAutoRepeat()) {
            return;
        }

        keys[e.getKeyCode()] = false;
    }

    private void handleCameraDrag(MouseEvent e) {
        // TODO: use screen size or adjustable speeds
        int xSpeed = 2;
        if (previousMouseX < e.getX()) {
            camera.addYaw(-xSpeed);
        } else if (previousMouseX > e.getX()) {
            camera.addYaw(xSpeed);
        }

        if (previousMouseY < e.getY()) {
            camera.addPitch(1);
        } else if (previousMouseY > e.getY()) {
            camera.addPitch(-1);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseClicked = true;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            isRightMouseDown = true;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMousePressed = true;
            isLeftMouseDown = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            isRightMouseDown = false;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            isLeftMouseDown = false;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isRightMouseDown) {
            handleCameraDrag(e);
        }

        this.mouseX = e.getX();
        this.mouseY = e.getY();
        previousMouseX = e.getX();
        previousMouseY = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

    }
}