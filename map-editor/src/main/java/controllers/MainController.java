package controllers;

import com.google.inject.Provider;
import com.jfoenix.controls.JFXMasonryPane;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import di.BasicModule;
import di.StoreProvider;
import javafx.animation.AnimationTimer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import models.ObjectSwatchModel;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.fs.Store;
import renderer.Camera;
import renderer.InputHandler;
import renderer.MapEditor;

import javax.inject.Inject;
import java.io.File;
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

    @FXML
    private FlowPane paneToolbox;

    @FXML
    private Pane paneToolProperties;

    @FXML
    private void initialize() {
        LoadMapRendererTask<NewtCanvasJFX> loadTask = new LoadMapRendererTask<>(this) {
            @Override
            public NewtCanvasJFX call() {
                return mapEditor.LoadMap();
            }
        };
        loadTask.setOnSucceeded(e -> group.getChildren().add(loadTask.getValue()));
        loadTask.run();

//        new Thread(loadTask).start();

        optMinimap.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/minimap.fxml"));
                fxmlLoader.setControllerFactory(BasicModule.injector::getInstance);
                Parent root1 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.setTitle("Minimap View");
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


        ////////////
        ToggleGroup toggleGroup = new ToggleGroup();
        ToggleButton selector = new ToggleButton("");
        selector.setStyle(" -fx-min-height: 55px;\n" +
                "    -fx-min-width: 55px;\n" +
                "    -fx-background-image: url('/image/zoom.png');\n" +
                "    -fx-background-size: 80% 80%;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center;");

        selector.setToggleGroup(toggleGroup);
        selector.setFocusTraversable(false);
        selector.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                selector.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/zoom.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            } else {
                selector.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/zoom-o.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            }
        });
        selector.setTooltip(new Tooltip("Inspector (I)"));
        selector.setOnAction(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/tools/inspector-tool.fxml"));
                fxmlLoader.setControllerFactory(BasicModule.injector::getInstance);
                Parent inspectorRoot = fxmlLoader.load();
                paneToolProperties.getChildren().add(inspectorRoot);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        paneToolbox.getChildren().add(selector);
//        selector.fire();

        ToggleButton terrain = new ToggleButton("");
        terrain.setStyle(" -fx-min-height: 55px;\n" +
                "    -fx-min-width: 55px;\n" +
                "    -fx-background-image: url('/image/soil-o.png');\n" +
                "    -fx-background-size: 80% 80%;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center;");

        terrain.setToggleGroup(toggleGroup);
        terrain.setFocusTraversable(false);
        terrain.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                terrain.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/soil.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            } else {
                terrain.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/soil-o.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            }
        });
        Tooltip terrainTooltip = new Tooltip("Terrain (T)");
        terrainTooltip.setShowDelay(Duration.millis(500));
        terrain.setTooltip(terrainTooltip);
        paneToolbox.getChildren().add(terrain);


        ToggleButton paintBrush = new ToggleButton("");
        paintBrush.setStyle(" -fx-min-height: 55px;\n" +
                "    -fx-min-width: 55px;\n" +
                "    -fx-background-image: url('/image/tools-and-utensils-o.png');\n" +
                "    -fx-background-size: 80% 80%;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center;");

        paintBrush.setToggleGroup(toggleGroup);
        paintBrush.setFocusTraversable(false);
        paintBrush.setTooltip(new Tooltip("Paintbrush (P)"));
        paintBrush.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                paintBrush.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/tools-and-utensils.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            } else {
                paintBrush.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/tools-and-utensils-o.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            }
        });
        paneToolbox.getChildren().add(paintBrush);


        ToggleButton eraser = new ToggleButton("");
        eraser.setStyle(" -fx-min-height: 55px;\n" +
                "    -fx-min-width: 55px;\n" +
                "    -fx-background-image: url('/image/eraser-o.png');\n" +
                "    -fx-background-size: 80% 80%;\n" +
                "    -fx-background-repeat: no-repeat;\n" +
                "    -fx-background-position: center;");
        eraser.setToggleGroup(toggleGroup);
        eraser.setFocusTraversable(false);
        eraser.setTooltip(new Tooltip("Eraser (E)"));
        eraser.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                eraser.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/eraser.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            } else {
                eraser.setStyle(" -fx-min-height: 55px;\n" +
                        "    -fx-min-width: 55px;\n" +
                        "    -fx-background-image: url('/image/eraser-o.png');\n" +
                        "    -fx-background-size: 80% 80%;\n" +
                        "    -fx-background-repeat: no-repeat;\n" +
                        "    -fx-background-position: center;");
            }
        });
        paneToolbox.getChildren().add(eraser);

    }

    abstract static class LoadMapRendererTask<V> extends Task<V> {
        protected MainController controller;

        public LoadMapRendererTask(MainController controller) {
            this.controller = controller;
        }
    }
}
