import com.jogamp.newt.javafx.NewtCanvasJFX;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layoutControllers.MainController;
import layoutControllers.MinimapController;
import layoutControllers.ObjectPickerController;
import renderer.MapEditor;

import java.io.IOException;

public class JfxApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("FOSS Map Editor");
        // JFX
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();

        javafx.scene.Scene jfxScene = new javafx.scene.Scene(root);
        stage.setScene(jfxScene);
        stage.show();

        // LOAD MINIMAP
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layout/minimap.fxml"));
        Parent root1 = fxmlLoader.load();
        Stage stage2 = new Stage();
        stage2.initModality(Modality.NONE);
        stage2.setTitle("Minimap");
        stage2.setScene(new Scene(root1));
        stage2.show();

        // LOAD OBJECT PICKER
        FXMLLoader objLoader = new FXMLLoader(getClass().getResource("/layout/object-picker.fxml"));
        Parent objRoot = objLoader.load();
        Stage objStage = new Stage();
        objStage.initModality(Modality.NONE);
        objStage.setTitle("Object Picker");
        objStage.setScene(new Scene(objRoot));
        objStage.show();

        ObjectPickerController objectPickerController = objLoader.getController();


        MinimapController minimapController = fxmlLoader.getController();
//        root1.setOnScroll(minimapController::onMouseWheelScroll);

        LoadMapRendererTask<NewtCanvasJFX> loadTask = new LoadMapRendererTask<>(controller) {
            @Override
            public NewtCanvasJFX call() {
                return new MapEditor().LoadMap(controller, minimapController, objectPickerController);
            }
        };
        loadTask.setOnSucceeded(e -> controller.getGroup().getChildren().add(loadTask.getValue()));
        new Thread(loadTask).start();

//        controller.getGroup().getChildren().add(new MapEditor().LoadMap(controller, minimapController));
    }


}

abstract class LoadMapRendererTask<V> extends Task<V> {
    protected MainController controller;

    public LoadMapRendererTask(MainController controller) {
        this.controller = controller;
    }
}