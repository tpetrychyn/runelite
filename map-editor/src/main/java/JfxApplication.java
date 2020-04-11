import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import layoutControllers.MainController;

public class JfxApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("FOSS Map Editor");
        // JFX
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        stage.setOnShown(e -> {
            MapEditor mapEditor = new MapEditor();
            mapEditor.LoadMap(controller);
        });

        javafx.scene.Scene jfxScene = new javafx.scene.Scene(root);
        stage.setScene(jfxScene);
        stage.show();
    }
}
