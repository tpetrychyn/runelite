import com.jogamp.newt.javafx.NewtCanvasJFX;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import layoutControllers.MainController;
import renderer.MapEditor;

import java.io.IOException;

public class JfxApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("FOSS Map Editor");
        // JFX
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();

        javafx.scene.Scene jfxScene = new javafx.scene.Scene(root);
        stage.setScene(jfxScene);
        stage.show();

        LoadMapRendererTask<NewtCanvasJFX> loadTask = new LoadMapRendererTask<>(controller) {
            @Override
            public NewtCanvasJFX call() {
                return new MapEditor().LoadMap(controller);
            }
        };
        loadTask.setOnSucceeded(e -> controller.getGroup().getChildren().add(loadTask.getValue()));
        new Thread(loadTask).start();
    }


}

abstract class LoadMapRendererTask<V> extends Task<V> {
    protected MainController controller;

    public LoadMapRendererTask(MainController controller) {
        this.controller = controller;
    }
}