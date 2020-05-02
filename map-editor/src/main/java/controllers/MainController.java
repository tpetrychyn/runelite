package controllers;

import com.jfoenix.controls.JFXMasonryPane;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import di.BasicModule;
import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
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
import lombok.SneakyThrows;
import models.ObjectSwatchItem;
import models.ObjectSwatchModel;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.definitions.ObjectDefinition;
import renderer.Camera;
import renderer.InputHandler;
import renderer.MapEditor;

import javax.inject.Inject;
import java.io.IOException;

@Getter
@Setter
public class MainController {
    @Inject
    private MapEditor mapEditor;
    @Inject
    private Camera camera;
    @Inject
    private InputHandler inputHandler;
    @Inject
    private ObjectSwatchModel objectSwatchModel;
    @Inject
    private ObjectManager objectManager;

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
    private JFXMasonryPane swatchPane;

    @SneakyThrows
    @FXML
    private void initialize() {
        LoadMapRendererTask<NewtCanvasJFX> loadTask = new LoadMapRendererTask<>(this) {
            @Override
            public NewtCanvasJFX call() {
                return mapEditor.LoadMap();
            }
        };
        loadTask.setOnSucceeded(e -> {
            // Two testing objs
            {
                ObjectDefinition cannon = objectManager.getObject(6);
                objectSwatchModel.getObjectList().add(new ObjectSwatchItem(cannon, null, "cannon"));
            }
            {
                ObjectDefinition bigHead = objectManager.getObject(21);
                objectSwatchModel.getObjectList().add(new ObjectSwatchItem(bigHead, null, "statue"));
            }
            // END FOR TESTING

            group.getChildren().add(loadTask.getValue());
        });
//        loadTask.run();


        new Thread(loadTask).start();

        optMinimap.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/minimap.fxml"));
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

                debugText.setText(camera.getDebugText());
            }
        }.start();
    }

    abstract static class LoadMapRendererTask<V> extends Task<V> {
        protected MainController controller;

        public LoadMapRendererTask(MainController controller) {
            this.controller = controller;
        }
    }
}
