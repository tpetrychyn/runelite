package controllers;

import com.google.inject.Inject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import net.runelite.api.Constants;
import scene.Scene;
import scene.SceneTile;

import java.util.Arrays;

public class MinimapController {
    @Inject
    private Scene scene;

    private IntegerProperty tileSize = new SimpleIntegerProperty(5);

    @FXML
    private ImageView imgMiniMap;
    private PixelWriter pw;

    @FXML
    private CheckBox chkHover;

    @FXML
    private Slider sliderZoom;

    @FXML
    private ScrollPane scrollPane;

    int sceneWidth;
    int sceneHeight;
    int canvasWidth;
    int canvasHeight;

    int mouseX;
    int mouseY;

    @FXML
    private void initialize() {
        imgMiniMap.setOnMouseMoved(e -> {
            mouseX = (int) e.getX();
            mouseY = (int) e.getY();

            scene.getHoveredEntity().set(scene.getTile(0, mouseX / tileSize.get(), (canvasHeight - mouseY) / tileSize.get()));
        });

        ChangeListener<SceneTile> c = (obs, oldVal, newVal) -> {
            if (oldVal == newVal) return;
            if (newVal == null) return;

            new Thread(() -> {
                if (oldVal != null && oldVal.getTilePaint() != null) {
                    int argb = 0xFF << 24 | oldVal.getTilePaint().getRgb();
                    drawTile(oldVal.getX(), sceneHeight - 1 - oldVal.getY(), argb);
                }
                drawTile(newVal.getX(), sceneHeight - 1 - newVal.getY(), Integer.MAX_VALUE);
            }).start();
        };

        chkHover.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                scene.getHoveredEntity().addListener(c);
            } else {
                scene.getHoveredEntity().removeListener(c);
            }
        });

        sliderZoom.valueProperty().bindBidirectional(tileSize);
        tileSize.addListener((obs, oldVal, newVal) -> drawFull());
//        sliderZoom.valueProperty().addListener((obs, oldVal, newVal) -> {
//            tileSize = newVal.intValue();
//            drawFull();
//        });

        // auto resize
//        ((VBox)imgMiniMap.getParent()).widthProperty().addListener((obs, oldVal, newVal) -> {
//            tileSize = newVal.intValue()/64;
//            drawFull();
//        });

        scrollPane.addEventFilter(ScrollEvent.ANY, e -> {
            e.consume();
            if (e.getDeltaY() > 0) {
                if (tileSize.get() < 25) {
                    tileSize.set(tileSize.get() + 1);
                }
            } else if (e.getDeltaY() < 0) {
                if (tileSize.get() > 1) {
                    tileSize.set(tileSize.get() - 1);
                }
            }
        });

//        borderPane.setOnScroll(e -> {
////            e.consume();
//            if (e.getDeltaY() > 0) {
//                if (tileSize < 25) {
//                    tileSize++;
//                }
//            } else if (e.getDeltaY() < 0) {
//                if (tileSize > 1) {
//                    tileSize--;
//                }
//            }
//            new Thread(this::drawFull).start();
//        });

        scene.getSceneChangeListeners().add(e -> new Thread(this::drawFull).start());
        new Thread(this::drawFull).start();
    }

    public void drawFull() {
        sceneWidth = sceneHeight = scene.getRadius() * Constants.REGION_SIZE;
        canvasWidth = canvasHeight = sceneWidth * tileSize.get();

        WritableImage img = new WritableImage(canvasWidth, canvasHeight);
        pw = img.getPixelWriter();
        imgMiniMap.setImage(img);

        imgMiniMap.setFitHeight(img.getHeight());
        imgMiniMap.setFitWidth(img.getWidth());
        imgMiniMap.toBack();

        int[] pixels = new int[canvasWidth * canvasHeight];

        for (int x = 0; x < sceneWidth; x++) {
            for (int y = 0; y < sceneHeight; y++) {
                SceneTile tile = scene.getTile(0, x, sceneHeight - 1 - y);
                if (tile != null && tile.getTilePaint() != null) {
                    int argb = 0xFF << 24 | tile.getTilePaint().getRgb();
                    for (int i = 0; i < tileSize.get(); i++) {
                        for (int j = 0; j < tileSize.get(); j++) {
                            int xPix = x * tileSize.get() + i;
                            int yPix = y * tileSize.get() + j;
                            pixels[xPix + yPix * canvasWidth] = argb;
                        }
                    }
                }
            }
        }
        pw.setPixels(0, 0, canvasWidth, canvasHeight, PixelFormat.getIntArgbInstance(), pixels, 0, canvasWidth);
    }

    public void drawTile(int x, int y, int rgb) {
        int argb = 0xFF << 24 | rgb; //argb, full alpha first bits
        int[] tilePixels = new int[tileSize.get() * tileSize.get()];
        Arrays.fill(tilePixels, argb);

        int xPix = x * tileSize.get();
        int yPix = y * tileSize.get();
        pw.setPixels(xPix, yPix, tileSize.get(), tileSize.get(), PixelFormat.getIntArgbInstance(), tilePixels, 0, tileSize.get());
    }
}
