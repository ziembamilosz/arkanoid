import javax.swing.*;

public class MyFrame extends JFrame {

    private static final String TITLE = "Arkanoid";
    private static final String partialPath = System.getProperty("user.dir");

    public MyFrame() {

        super(TITLE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new Game());
        this.pack();
        this.setLocationRelativeTo(null);
        ImageIcon image = new ImageIcon(partialPath + "\\graphics\\logo.png");
        this.setIconImage(image.getImage());
        this.setVisible(true);
    }
}
