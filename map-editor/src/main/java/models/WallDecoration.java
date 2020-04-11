package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WallDecoration {
    private long tag;
    private int flags;
    private int x;
    private int y;
    private int height;
    private ModelImpl modelA;
    private ModelImpl modelB;
    private int orientationA;
    private int orientationB;
//    private int xOff;
//    private int yOff;
}
