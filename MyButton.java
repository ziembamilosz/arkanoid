import javax.swing.*;

public class MyButton extends JButton {

    MyButton(String text) {
        this.setSize(80, 30);
        this.setFocusable(false);
        this.setText(text);
        this.setVerticalTextPosition(JButton.CENTER);
        this.setHorizontalTextPosition(JButton.CENTER);
    }
}
