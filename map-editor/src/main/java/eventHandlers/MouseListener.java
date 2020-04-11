package eventHandlers;

import com.jogamp.newt.event.MouseEvent;
import layoutControllers.MainController;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class MouseListener implements com.jogamp.newt.event.MouseListener {
    private int previousMouseX;
    private int previousMouseY;
    private int mouseX;
    private int mouseY;

    private boolean mouseClicked;

    private List<Function<MouseEvent, Void>> dragListeners = new ArrayList<>();

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseClicked = true;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dragListeners.forEach(l -> l.apply(e));
        previousMouseX = e.getX();
        previousMouseY = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

    }
}
