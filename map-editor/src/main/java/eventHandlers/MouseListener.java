package eventHandlers;

import com.jogamp.newt.event.MouseEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MouseListener implements com.jogamp.newt.event.MouseListener {
    private int mouseX;
    private int mouseY;

    private boolean mouseClicked;

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

    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {

    }
}
