import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class Game extends JPanel implements Runnable, KeyListener, ActionListener {


    private static final int WIDTH_OF_FRAME = 566;
    private static final int HEIGHT_OF_FRAME = 466;
    private static final int HEIGHT_OF_BOTTOM_BAR = 20;
    private static final int ROWS = 7;
    private static final int COLS = 10;
    private static final int GAP_BETWEEN_BLOCKS = 6;
    private static final int GAP_BETWEEN_HEARTS = 10;
    private static final int MARGIN_FOR_HEARTS = 50;
    private static final int DEFAULT_BALL_X = WIDTH_OF_FRAME/2;
    private static final int DEFAULT_BALL_Y = HEIGHT_OF_FRAME-50;
    private static final int DEFAULT_PADDLE_X = DEFAULT_BALL_X - 50;
    private static final int DEFAULT_PADDLE_Y = HEIGHT_OF_FRAME-HEIGHT_OF_BOTTOM_BAR-10;

    //predkosci
    private static final int paddleDX = 8;
    private static int ballDX = 5;
    private static int ballDY = 5;

    //sciezki do grafiki
    private static final String partialPath = System.getProperty("user.dir");
    private static final String backgroundImagePath = partialPath + "\\graphics\\background.png";
    private static final String ballImagePath = partialPath + "\\graphics\\ball.png";
    private static final String paddleImagePath = partialPath + "\\graphics\\paddle.png";
    private static final String pointImagePath = partialPath + "\\graphics\\point.png";
    private static final String gameOverImagePath = partialPath + "\\graphics\\game-over.png";
    private static final String youWonImagePath = partialPath + "\\graphics\\you-won.png";
    private static final String heartImagePath = partialPath + "\\graphics\\heart.png";

    //potrzebne flagi
    boolean isRunning = false;
    boolean movePaddleLeft = false;
    boolean movePaddleRight = false;
    boolean eraseHeart = false;
    boolean showEscFrame = false;
    boolean freezeGame = false;
    int maxPoints = ROWS*COLS;
    int pointsCounter = 0;
    int lives = 3;
    boolean[][] pointHitted = new boolean[ROWS][COLS];

    Thread thread;
    Graphics graphics;

    BufferedImage view, background, ball, paddle, point, gameOver, youWon, heart;

    Position[][] positions = new Position[ROWS][COLS];
    Position[] heartPositions = new Position[lives];
    Position ballPosition;
    Position paddlePosition;
    EscFrame escFrame;

    String text;

    // konstruktor
    public Game() {
        this.setPreferredSize(new Dimension(WIDTH_OF_FRAME, HEIGHT_OF_FRAME));
        this.addKeyListener(this);
    }

    public static void main(String[] args) { new MyFrame(); }

    @Override
    public void addNotify() {
        super.addNotify();
        thread = new Thread(this);
        isRunning = true;
        thread.start();
    }

    @Override
    public void run() {
        this.requestFocus();
        this.initialize();
        while (lives > 0 && pointsCounter < maxPoints) {
            try {
                Thread.sleep(1000/70);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!freezeGame) {
                this.updateScreen();
            }
            if(showEscFrame) {
                escFrame = new EscFrame();
                escFrame.yes.addActionListener(this);
                escFrame.no.addActionListener(this);
                showEscFrame = false;
                freezeGame = true;
            }
        }
    }

    public void initialize() {
        try {
            view = new BufferedImage(WIDTH_OF_FRAME, HEIGHT_OF_FRAME, BufferedImage.TYPE_INT_RGB);
            graphics = view.getGraphics();

            background = ImageIO.read(new File(backgroundImagePath));
            ball = ImageIO.read(new File(ballImagePath));
            paddle = ImageIO.read(new File(paddleImagePath));
            point = ImageIO.read(new File(pointImagePath));
            gameOver = ImageIO.read(new File(gameOverImagePath));
            youWon = ImageIO.read(new File(youWonImagePath));
            heart = ImageIO.read(new File(heartImagePath));
        } catch(Exception e) {
            e.printStackTrace();
        }
            // inicjalizacja macierzy punktow
            for(int i = 0; i < ROWS; i++) {
                for(int j = 0; j < COLS; j++) {
                    Position pointPosition = new Position(GAP_BETWEEN_BLOCKS + j*(GAP_BETWEEN_BLOCKS + point.getWidth()),
                                                          GAP_BETWEEN_BLOCKS + i*(GAP_BETWEEN_BLOCKS + point.getHeight()));

                    pointPosition.setBoundsSize(point.getWidth(), point.getHeight());
                    positions[i][j] = pointPosition;
                    pointHitted[i][j] = false;
                }
            }

            // inicjalizacja dolnego paska serc
            for(int i = 0; i < lives; i++) {
                Position heartPosition = new Position(MARGIN_FOR_HEARTS + i*(heart.getWidth()+GAP_BETWEEN_HEARTS),
                                                      HEIGHT_OF_FRAME - HEIGHT_OF_BOTTOM_BAR);
                heartPosition.setBoundsSize(heart.getWidth(), heart.getHeight());
                heartPositions[i] = heartPosition;
            }

            ballPosition = new Position(DEFAULT_BALL_X, DEFAULT_BALL_Y);
            ballPosition.setBoundsSize(ball.getWidth(), ball.getHeight());

            paddlePosition = new Position(DEFAULT_PADDLE_X, DEFAULT_PADDLE_Y);
            paddlePosition.setBoundsSize(paddle.getWidth(), paddle.getHeight());

            text = "Ilość punktów: " + pointsCounter + "/" + maxPoints;

    }

    public void update() {

        // ruch pilki
        ballPosition.setPosition(ballPosition.getX() + ballDX, ballPosition.getY() + ballDY);

        // kolizja ze sciana boczna
        if(ballPosition.getX() < 0 || ballPosition.getX() > WIDTH_OF_FRAME - ball.getWidth()) {
            changeBallDirectionX();
        }

        // kolizja z gorna ramka
        if(ballPosition.getY() < 0) {
            changeBallDirectionY();
        }

        // kolizja z paletka i dodanie randomowego odbicia
        if(ballPosition.bounds.intersects(paddlePosition.bounds)) {
            Random random = new Random();
            ballDY = -(random.nextInt(5) + 4);
        }

        // kolizja z punktem
        for(int i = 0; i < ROWS; i++) { // odwrocic ROWS i COLS w petlach
            for(int j = 0; j < COLS; j++) {
                Position pointPosition = positions[i][j];
                if(pointPosition.bounds.intersects(ballPosition.bounds) && !pointHitted[i][j]) {

//                    boolean collisionLeft =  ballPosition.getX() + ball.getWidth() >= pointPosition.getX() &&
//                                             ballPosition.getY() < pointPosition.getY() + point.getHeight() &&
//                                             ballPosition.getY() > pointPosition.getY();
//                    boolean collisionRight = ballPosition.getX() <= pointPosition.getX() + point.getWidth() &&
//                                             ballPosition.getY() < pointPosition.getY() + point.getHeight() &&
//                                             ballPosition.getY() > pointPosition.getY();
//                    boolean collisionDown =  ballPosition.getY() < pointPosition.getY() + point.getHeight() &&
//                                             ballPosition.getX() + ball.getWidth() > pointPosition.getX() &&
//                                             ballPosition.getX() < pointPosition.getX() + point.getWidth();
//                    boolean collisionUp =    ballPosition.getY() + ball.getHeight() > pointPosition.getY() &&
//                                             ballPosition.getX() + ball.getWidth() > pointPosition.getX() &&
//                                             ballPosition.getX() < pointPosition.getX() + point.getWidth();
//                    if(collisionLeft) {
//                        pointPosition.setPosition(-100, 0);
//                        pointsCounter++;
//                        this.changeBallDirectionX();
//                    } else if(collisionRight) {
//                        pointPosition.setPosition(-100, 0);
//                        pointsCounter++;
//                        this.changeBallDirectionX();
//                    } else if(collisionDown) {
//                        pointPosition.setPosition(-100, 0);
//                        pointsCounter++;
//                        this.changeBallDirectionY();
//                    } else if(collisionUp) {
//                        pointPosition.setPosition(-100, 0);
//                        pointsCounter++;
//                        this.changeBallDirectionY();
//                    }
                    if(ballPosition.getX() <= pointPosition.getX() && ballPosition.getY() >= pointPosition.getY() + point.getHeight()/2) {
                        if(absoluteValue(ballPosition.getX() + ball.getWidth(), pointPosition.getX()) <
                                absoluteValue(ballPosition.getY(), pointPosition.getY() + point.getHeight())) {
                            this.changeBallDirectionX();
                        } else {
                            this.changeBallDirectionY();
                        }
                    } else if(ballPosition.getX() <= pointPosition.getX() && ballPosition.getY() <= pointPosition.getY() + point.getHeight()/2) {
                        if(absoluteValue(ballPosition.getX() + ball.getWidth(), pointPosition.getX()) <
                                absoluteValue(ballPosition.getY() + ball.getHeight(), pointPosition.getY())) {
                            this.changeBallDirectionX();
                        } else {
                            this.changeBallDirectionY();
                        }
                    } else if(ballPosition.getX() >= pointPosition.getX() && ballPosition.getY() >= pointPosition.getY() + point.getHeight()/2) {
                        if(absoluteValue(ballPosition.getX(), pointPosition.getX() + point.getWidth()) <
                                absoluteValue(pointPosition.getY() + point.getHeight(), ballPosition.getY())) {
                            this.changeBallDirectionX();
                        } else {
                            this.changeBallDirectionY();
                        }
                    } else if(ballPosition.getX() >= pointPosition.getX() && ballPosition.getY() <= pointPosition.getY() + point.getHeight()/2) {
                        if(absoluteValue(ballPosition.getX(), pointPosition.getX() + point.getWidth()) <
                                absoluteValue(pointPosition.getY(), ballPosition.getY() + ball.getHeight())) {
                            this.changeBallDirectionX();
                        } else {
                            this.changeBallDirectionY();
                        }
                    }
                    pointsCounter++;
                    pointPosition.setPosition(-100, 0);
                    pointHitted[i][j] = true;
                }
            }
        }

        // kolizja paletki ze sciana prawa
        if(movePaddleRight && paddlePosition.getX() < WIDTH_OF_FRAME - paddle.getWidth()) {
            paddlePosition.setPosition(paddlePosition.getX() + paddleDX, paddlePosition.getY());
        }

        // kolizja paletki ze sciana lewa
        if(movePaddleLeft && paddlePosition.getX() > 0) {
            paddlePosition.setPosition(paddlePosition.getX() - paddleDX, paddlePosition.getY());
        }

        // wypadniecie pilki poza pole gry na dole, odjecie punktow zycia i dodanie flagi wymazujacej
        // ikonke serca, dodatkowo reset gry
        if(ballPosition.getY() > HEIGHT_OF_FRAME - paddle.getHeight()) {
            lives--;
            eraseHeart = true;
            ballPosition.setPosition(DEFAULT_BALL_X, DEFAULT_BALL_Y);
            paddlePosition.setPosition(DEFAULT_PADDLE_X, DEFAULT_PADDLE_Y);
        }

        // aktualizacja wsywietlanych punktow
        text = "ilość punktów: " + pointsCounter + "/" + maxPoints;
    }

    public void draw() { // narysowanie nowych polozen

        graphics.drawImage(background, 0, 0, WIDTH_OF_FRAME, HEIGHT_OF_FRAME, null);
        graphics.drawImage(ball, ballPosition.getX(), ballPosition.getY(), ball.getWidth(), ball.getHeight(), null);
        graphics.drawImage(paddle, paddlePosition.getX(), paddlePosition.getY(), paddle.getWidth(), paddle.getHeight(), null);

        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLS; j++) {
                int xPos = positions[i][j].getX();
                int yPos = positions[i][j].getY();
                graphics.drawImage(point, xPos, yPos, point.getWidth(), point.getHeight(), null);
            }
        }

        for(int i = 0; i < lives; i++) {
            graphics.drawImage(heart, heartPositions[i].getX(), heartPositions[i].getY(),
                               heart.getWidth(), heart.getHeight(), null);
            if(eraseHeart) {
                heartPositions[lives].setPosition(-100, 0);
                eraseHeart = false;
            }
        }

        graphics.drawString(text, 300, HEIGHT_OF_FRAME-5);

        if(lives == 0 || !isRunning) {
            graphics.drawImage(gameOver,(WIDTH_OF_FRAME - gameOver.getWidth())/2,
                           (HEIGHT_OF_FRAME-gameOver.getHeight())/2, gameOver.getWidth(),
                               gameOver.getHeight(), null);
        }

        if(pointsCounter == maxPoints) {
            graphics.drawImage(youWon, (WIDTH_OF_FRAME - youWon.getWidth())/2,
                           (HEIGHT_OF_FRAME - youWon.getHeight())/2,
                               youWon.getWidth(), youWon.getHeight(), null);
        }

        Graphics g2 = getGraphics();
        g2.drawImage(view, 0, 0, WIDTH_OF_FRAME, HEIGHT_OF_FRAME, null);
        g2.dispose();
    }

    private void updateScreen() {
        this.update();
        this.draw();
    }

    private int absoluteValue(int a, int b) {
        if(a >= b)
            return a - b;
        else
            return b - a;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_RIGHT -> movePaddleRight = true;
            case KeyEvent.VK_LEFT -> movePaddleLeft = true;
            case KeyEvent.VK_ESCAPE -> showEscFrame = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_RIGHT -> movePaddleRight = false;
            case KeyEvent.VK_LEFT -> movePaddleLeft = false;
        }
    }

    private void changeBallDirectionX() {
        ballDX = -ballDX;
    }

    private void changeBallDirectionY() {
        ballDY = -ballDY;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == escFrame.yes) {
            escFrame.dispose();
            System.exit(0);
        } else if (e.getSource() == escFrame.no) {
            escFrame.dispose();
            try {
                Thread.sleep(500);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            freezeGame = false;
        }
    }
}
