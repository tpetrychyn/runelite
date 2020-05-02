package controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.Setter;
import models.ObjectSwatchItem;
import models.ObjectSwatchModel;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.fs.Store;
import renderer.MapEditor;

import javax.inject.Inject;

@Getter
@Setter
public class ObjectPickerController {
    @Inject
    private ObjectManager objectManager;
    @Inject
    private Store store;
    @Inject
    private ObjectSwatchModel objectSwatchModel;
    @Inject
    private MapEditor mapEditor;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private HBox paneAddToSwatch;
    @FXML
    private TextField txtAddToSwatchName;
    @FXML
    private Button btnAddToSwatch;

    @FXML
    private ListView<ObjectDefinition> listView;
    ObservableList<ObjectDefinition> entries = FXCollections.observableArrayList();

    @FXML
    private TextField searchBox;

    private ObjectDefinition selectedObject;

    @FXML
    private void initialize() {
        searchBox.textProperty().addListener((observable, oldVal, newVal) -> search(oldVal, newVal));

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
        subScene.toBack();
        subScene.heightProperty().bind(stackPane.heightProperty());
        subScene.widthProperty().bind(stackPane.widthProperty());

        initMouseControl(g, stackPane, camera);

        entries.addAll(objectManager.getObjects());
        listView.setItems(entries);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == null || newVal == oldVal) {
                        return;
                    }
                    g.getChildren().clear();
                    ModelDefinition m = newVal.getModel(store, 10, 0);
                    if (m == null) {
                        return;
                    }
                    MeshView[] mv = JavaFxHelpers.modelToMeshViews(m);
                    g.getChildren().addAll(mv);

                    paneAddToSwatch.setVisible(true);
                    txtAddToSwatchName.setText(newVal.toString());
                    selectedObject = newVal;
                }
        );

        btnAddToSwatch.setOnAction((e) -> {
            if (selectedObject == null) {
                return;
            }
            SnapshotParameters ss = new SnapshotParameters();
            ss.setFill(Color.TRANSPARENT);
            WritableImage snapshot = subScene.snapshot(ss, null);

            ObjectSwatchItem item = new ObjectSwatchItem(selectedObject, snapshot, txtAddToSwatchName.getText());
            int idx = objectSwatchModel.getObjectList().indexOf(item);
            if (idx > -1) {
                objectSwatchModel.getObjectList().set(idx, item);
            } else {
                objectSwatchModel.getObjectList().add(item);
            }
        });
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

    public void search(String oldVal, String newVal) {
        if (oldVal != null && (newVal.length() < oldVal.length())) {
            listView.setItems(entries);
        }
        String value = newVal.toUpperCase();
        ObservableList<ObjectDefinition> subentries = FXCollections.observableArrayList();
        for (ObjectDefinition entry : listView.getItems()) {
            if (entry.toString().toUpperCase().contains(value)) {
                subentries.add(entry);
            }
        }
        listView.setItems(subentries);
    }
}