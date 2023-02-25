import javax.swing.*;

public class EscFrame extends JFrame {

    private static final String TITLE = "Zakończenie gry";
    private static final int WIDTH_OF_FRAME = 300;
    MyButton yes = new MyButton("Tak");
    MyButton no = new MyButton("Nie");
    JLabel textField = new JLabel("Czy chcesz zakończyć grę?");

    public EscFrame() {
        super(TITLE);
        this.setResizable(false);
        this.setLayout(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(300, 200, 300, 200);
        this.setLocationRelativeTo(null);
        this.add(yes);
        this.add(no);
        this.add(textField);
        textField.setBounds(50, 0, 200, 100);
        textField.setVerticalTextPosition(JLabel.CENTER);
        textField.setHorizontalTextPosition(JLabel.CENTER);
        yes.setLocation(300 / 2 - 10 - 80, 120);
        no.setLocation(300 / 2 + 10, 120);
        this.setVisible(true); // making the frame visible
    }
}

