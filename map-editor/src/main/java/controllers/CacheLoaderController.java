package controllers;

import com.google.inject.Guice;
import di.BasicModule;
import di.StoreProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Store;
import net.runelite.cache.util.StoreLocation;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class CacheLoaderController {
    @Inject
    private StoreProvider storeProvider;
    @FXML
    private Button btnChooseDirectory;
    @FXML
    private Button btnLaunch;
    @FXML
    private Label lblErrorText;
    @FXML
    private TextField txtCacheLocation;

    @FXML
    private void initialize() {
        File defaultCache = StoreLocation.LOCATION;
        testLoadStore(defaultCache);

        btnChooseDirectory.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File f = directoryChooser.showDialog(null);
            testLoadStore(f);
        });

        btnLaunch.setOnAction(e -> {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            fxmlLoader.setControllerFactory(BasicModule.injector::getInstance);
            try {
                Parent root = fxmlLoader.load();
                javafx.scene.Scene jfxScene = new javafx.scene.Scene(root);
                Stage stage = new Stage();
                stage.setScene(jfxScene);
                stage.setTitle("FOSS Map Editor");
                stage.setX(0);
                stage.setY(0);
                stage.show();

                ((Node)(e.getSource())).getScene().getWindow().hide();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    private void testLoadStore(File f) {
        txtCacheLocation.setText(f.getAbsolutePath());
        try {
            storeProvider.setStoreLocation(f);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        Store s = storeProvider.get();
        if (s.getIndexes().size() == 0) {
            lblErrorText.setVisible(true);
            btnLaunch.setDisable(true);
            return;
        }

        lblErrorText.setVisible(false);
        btnLaunch.setDisable(false);
    }
}
