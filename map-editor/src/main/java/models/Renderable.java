package models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Renderable {
    private int bufferOffset = -1;
    private int uvBufferOffset = -1;
    private int targetBufferOffset = -1;
    private int bufferLen;
}
