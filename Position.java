import java.awt.*;

public class Position {

    private int x;
    private int y;
    Rectangle bounds;

    public Position(int x, int y) {
        setPosition(x, y);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        if(bounds != null) {
            bounds.setLocation(x, y);
        }
    }

    public void setBoundsSize(int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }
}
