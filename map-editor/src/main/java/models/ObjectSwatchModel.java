package models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

@Getter
public class ObjectSwatchModel {
    private final ObservableList<ObjectSwatchItem> objectList = FXCollections.observableArrayList();
    private final ObjectProperty<ObjectSwatchItem> selectedObject = new SimpleObjectProperty<>(null);

    public ObjectProperty<ObjectSwatchItem> selectedObjectProperty() {
        return selectedObject;
    }

    public final ObjectSwatchItem getSelectedObject() {
        return selectedObject.get();
    }

    public final void setSelectedObject(ObjectSwatchItem o) {
        o.select();
        selectedObject.set(o);
    }

    public ObservableList<ObjectSwatchItem> getObjectList() {
        return objectList;
    }
}
