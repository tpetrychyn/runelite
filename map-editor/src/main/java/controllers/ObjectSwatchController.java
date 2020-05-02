package controllers;

import com.google.inject.Inject;
import com.jfoenix.controls.JFXMasonryPane;
import di.BasicModule;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ObjectSwatchItem;
import models.ObjectSwatchModel;

import java.io.IOException;

public class ObjectSwatchController {
    @Inject
    private ObjectSwatchModel model;
    @FXML
    private JFXMasonryPane swatchPane;
    @FXML
    private Button btnLaunchPicker;

    @FXML
    private void initialize() {
        btnLaunchPicker.setOnAction(e -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/object-picker.fxml"));
                fxmlLoader.setControllerFactory(BasicModule.injector::getInstance);
                Parent root1 = fxmlLoader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.NONE);
                stage.setTitle("ABC");
                stage.setScene(new Scene(root1));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        model.getObjectList().addListener((ListChangeListener<? super ObjectSwatchItem>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(o -> {
                        Region item = o.toView();
                        item.setOnMouseClicked(e -> {
                            model.getObjectList().forEach(ObjectSwatchItem::deselect);
                            model.setSelectedObject(o);
                        });

                        item.focusedProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                model.getObjectList().forEach(ObjectSwatchItem::deselect);
                                model.setSelectedObject(o);
                            }
                            if (!newVal) {
                                o.deselect();
                            }
                        });
                        swatchPane.getChildren().add(item);
                    });
                }
            }
        });
    }
}
