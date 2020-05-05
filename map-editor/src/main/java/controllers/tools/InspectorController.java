package controllers.tools;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import models.TilePaintImpl;
import net.runelite.cache.models.ObjExporter;
import scene.Scene;

import javax.inject.Inject;

public class InspectorController {
    @Inject
    private Scene scene;

    @FXML
    private AnchorPane root;

    @FXML
    private void initialize() {
//        scene.getSelectedEntity().addListener((obs, oldVal, newVal) -> {
//            if (newVal == null) { return; }
//
//            if (newVal instanceof TilePaintImpl) {
//                GridPane grid = new GridPane();
//                grid.setHgap(10);
//                java.awt.Color color = ObjExporter.rs2hsbToColor(((TilePaintImpl) newVal).getUnderlayDefinition().getColor());
//                Color c = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
//                ColorPicker colorPicker = new ColorPicker();
//                colorPicker.setValue(c);
//                grid.addRow(0, colorPicker, new Label("Color"));
//
//                Platform.runLater(() -> root.getChildren().add(grid));
//            }
//        });
    }
}
