package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Entity;

@Getter
@Setter
@AllArgsConstructor
public class WallDecoration {
    private long tag;
    private int flags;
    private int x;
    private int y;
    private int height;
    private Entity entityA;
    private Entity entityB;
    private int orientationA;
    private int orientationB;
}
