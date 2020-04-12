package layoutControllers;

import com.jfoenix.controls.JFXButton;
import com.jogamp.opengl.util.Animator;
import eventHandlers.MouseListener;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import renderer.Camera;

@Getter
@Setter
public class MainController {
    private Camera camera;
    private MouseListener mouseListener;
    private Animator animator;

    @FXML
    private Group group;

    @FXML
    private Pane paneLoading;

    @FXML
    private Label lblMouseX;

    @FXML
    private Label lblMouseY;

    @FXML
    private Label lblYaw;
    @FXML
    private Label lblPitch;
    @FXML
    private Label lblCameraX;
    @FXML
    private Label lblCameraY;
    @FXML
    private Label lblCameraZ;

    @FXML
    private Label lblFps;

    @FXML
    private void initialize() {
        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (camera == null) {
                    return;
                }
                getLblMouseX().setText(String.valueOf(mouseListener.getMouseX()));
                getLblMouseY().setText(String.valueOf(mouseListener.getMouseY()));

                lblYaw.setText(String.valueOf(camera.getYaw()));
                getLblPitch().setText(String.valueOf(camera.getPitch()));

                getLblCameraX().setText(String.valueOf(camera.getCameraX()));
                getLblCameraY().setText(String.valueOf(camera.getCameraY()));
                getLblCameraZ().setText(String.valueOf(camera.getCameraZ()));

                lblFps.setText(String.valueOf(animator.getLastFPS()));
            }
        }.start();
    }
}
