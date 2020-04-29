package layoutControllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.Setter;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.TextureManager;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.StoreProvider;
import renderer.MapEditor;

import java.io.IOException;

@Getter
@Setter
public class ObjectPickerController {
    private MapEditor mapEditor;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private void initialize() {
        Store store = StoreProvider.getStore();
        ObjectManager objectManager = new ObjectManager(store);
        TextureManager textureManager = new TextureManager(store);
        try {
            objectManager.load();
            textureManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Group g = new Group();
        StackPane p = new StackPane(g);
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1000000);
        // in order for depth to work, camera and model have to stay in positive Z
        // by setting them to a very large +z at the start it gives lots of room to zoom in and out
        g.setTranslateZ(10000);
        camera.setTranslateZ(9000);
        p.getChildren().add(camera);

        SubScene subScene = new SubScene(p, 400, 400, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        stackPane.getChildren().add(subScene);
        subScene.heightProperty().bind(stackPane.heightProperty());
        subScene.widthProperty().bind(stackPane.widthProperty());

        initMouseControl(g, stackPane, camera);

        VBox vBox = new VBox();
        for (int i = 0; i < 1500; i++) {
            ObjectDefinition o = objectManager.getObject(i);
            if (o == null) continue;
            ModelDefinition m = o.getModel(10, 0);
            if (m == null) continue;
            Label l = new Label(o.getName());
            l.setOnMouseClicked((e) -> {
                MeshView[] mv = JavaFxHelpers.modelToMeshViews(m);
                g.getChildren().clear();
                g.getChildren().addAll(mv);
                mapEditor.injectWallDecoration(m, o);
            });
            vBox.getChildren().addAll(l);
        }

        vBox.setSpacing(10);
        scrollPane.setContent(vBox);
    }

    //Tracks drag starting point for x and y
    private double anchorX, anchorY;
    //Keep track of current angle for x and y
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    //We will update these after drag. Using JavaFX property to bind with object
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private void initMouseControl(Group group, Pane scene, Camera camera) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        //Attach a scroll listener
        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            //Get how much scroll was done in Y axis.
            double delta = event.getDeltaY();
            //Add it to the Z-axis location.
            camera.setTranslateZ(camera.getTranslateZ() + delta);
        });
    }
}