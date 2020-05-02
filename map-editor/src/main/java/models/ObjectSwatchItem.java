package models;

import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.Data;
import net.runelite.cache.definitions.ObjectDefinition;

@Data
public class ObjectSwatchItem {
    private VBox container = new VBox();

    private ObjectDefinition objectDefinition;
    private WritableImage image;
    private String label;
    private boolean isSelected;

    public ObjectSwatchItem(ObjectDefinition objectDefinition, WritableImage image, String label) {
        this.objectDefinition = objectDefinition;
        this.image = image;
        this.label = label;
    }

    public Region toView() {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(125);
        iv.setFitHeight(75);
        iv.setPreserveRatio(true);
        iv.setPickOnBounds(true);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(iv, new Label(label));
        container.setFocusTraversable(true);
        return container;
    }

    public void select() {
        isSelected = true;
        container.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void deselect() {
        isSelected = false;
        container.setBackground(Background.EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof  ObjectSwatchItem)) return false;

        ObjectSwatchItem item = (ObjectSwatchItem) o;

        return objectDefinition.getId() == item.objectDefinition.getId();
    }
}
