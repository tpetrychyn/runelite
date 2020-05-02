import com.google.inject.Guice;
import di.BasicModule;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JfxApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        BasicModule.injector = Guice.createInjector(new BasicModule());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(BasicModule.injector::getInstance);

        fxmlLoader.setLocation(getClass().getResource("/views/main.fxml"));
        Parent root = fxmlLoader.load();
        javafx.scene.Scene jfxScene = new javafx.scene.Scene(root);
        stage.setScene(jfxScene);
        stage.setTitle("FOSS Map Editor");
        stage.setX(0);
        stage.setY(0);
        stage.show();

        // LOAD MINIMAP
        FXMLLoader miniLoader = new FXMLLoader(getClass().getResource("/views/minimap.fxml"));
        miniLoader.setControllerFactory(BasicModule.injector::getInstance);
        Parent miniRoot = miniLoader.load();
        Stage miniStage = new Stage();
        miniStage.initModality(Modality.NONE);
        miniStage.setTitle("Minimap");
        miniStage.setScene(new Scene(miniRoot));
        miniStage.setX(-1000);
        miniStage.setY(0);
        miniStage.show();

        // LOAD OBJECT PICKER
//        FXMLLoader objLoader = new FXMLLoader(getClass().getResource("/views/object-picker.fxml"));
//        objLoader.setControllerFactory(injector::getInstance);
//        Parent objRoot = objLoader.load();
//        Stage objStage = new Stage();
//        objStage.initModality(Modality.NONE);
//        objStage.setTitle("Object Picker");
//        objStage.setScene(new Scene(objRoot));
//        objStage.show();
    }
}