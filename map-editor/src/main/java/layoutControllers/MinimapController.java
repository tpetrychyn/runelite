package layoutControllers;

import com.jogamp.nativewindow.javafx.JFXAccessor;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import net.runelite.api.Constants;
import scene.Scene;
import scene.SceneTile;

public class MinimapController {
    private Scene scene;
    private int tileSize = 5;
    private int[][] tiles;

    @FXML
    private Canvas canvas;
    GraphicsContext gc;

    @FXML
    private void initialize() {
        gc = canvas.getGraphicsContext2D();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        canvas.setWidth(tileSize * scene.getRadius() * Constants.REGION_SIZE);
        canvas.setHeight(tileSize * scene.getRadius() * Constants.REGION_SIZE);
        buildFullScene();
    }

    public void buildFullScene() {
        gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
        this.tiles = new int[scene.getRadius() * Constants.REGION_SIZE][scene.getRadius() * Constants.REGION_SIZE];
        JFXAccessor.runOnJFXThread(false, () -> {
            for (int x = 0; x < scene.getRadius() * Constants.REGION_SIZE; x++) {
                for (int y = 0; y < scene.getRadius() * Constants.REGION_SIZE; y++) {
                    SceneTile tile = scene.getTile(0, x, y);
                    if (tile != null && tile.getTilePaint() != null) {
                        int rgb = tile.getTilePaint().getRgb();
                        setTile(rgb, x, y);
                    }
                }
            }
        });
    }

    public void setTile(int rgb, int x, int y) {
        if (tiles[x][y] == rgb) {
            return;
        }

        tiles[x][y] = rgb;
        JFXAccessor.runOnJFXThread(false, () -> {
            int r = (rgb >> 16);
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb & 0xFF);
            gc.setFill(Color.rgb(r, g, b));
            gc.fillRect(x*tileSize, y*tileSize, tileSize, tileSize);
        });
    }

    public void onMouseWheelScroll(ScrollEvent e) {
        System.out.printf("e %s\n", e);
        if (e.getDeltaY() > 0) {
            tileSize++;
            buildFullScene();
        }
        if (e.getDeltaY() < 0) {
            tileSize--;
            buildFullScene();
        }
    }
}
