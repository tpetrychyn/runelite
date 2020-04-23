package renderer.helpers;

public enum PickerType {
    PASSTHROUGH(-2),
    BLOCK(-1),
    PICKABLE(0);

    private final int value;

    PickerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
