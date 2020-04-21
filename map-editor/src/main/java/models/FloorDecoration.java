package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import java.awt.*;

@Getter
@Setter
public class FloorDecoration extends Renderable {
    private int id;
    private int orientation;
    private Model model;
    private int x;
    private int y;
    private int height;
    private long tag;
    private int flags;

    public String toString() {
        return String.format("Floor Decoration: id %d, x %d, y %d, height %d", id, x, y, height);
    }
}
