package layoutControllers;

import com.jogamp.opengl.util.Animator;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import renderer.Camera;
import renderer.InputHandler;

import java.io.IOException;

@Getter
@Setter
public class MainController {
    private Camera camera;
    private InputHandler inputHandler;
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
    private TextArea debugText;

    @FXML
    private MenuItem optMinimap;

    @FXML
    private void initialize() {
        optMinimap.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/minimap.fxml"));
                Parent root1 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("ABC");
                stage.setScene(new Scene(root1));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (camera == null) {
                    return;
                }
                getLblMouseX().setText(String.valueOf(inputHandler.getMouseX()));
                getLblMouseY().setText(String.valueOf(inputHandler.getMouseY()));

                lblYaw.setText(String.valueOf(camera.getYaw()));
                getLblPitch().setText(String.valueOf(camera.getPitch()));

                getLblCameraX().setText(String.valueOf(camera.getCameraX()));
                getLblCameraY().setText(String.valueOf(camera.getCameraY()));
                getLblCameraZ().setText(String.valueOf(camera.getCameraZ()));

                lblFps.setText(String.valueOf(animator.getLastFPS()));

                debugText.setText(camera.getDebugText());
            }
        }.start();
    }
}
