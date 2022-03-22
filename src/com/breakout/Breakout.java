package com.breakout;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Breakout extends WindowProgram {
    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of game board (usually the same) */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static final int NTURNS = 3;

    /* TODO:
        AUTHOR CONSTANTS */
    /** GAME SPEED: 9-10 - normal, 6-8 - faster, 5 - very fast, <=4 - unreal :D **/
    private static final double GAME_SPEED = 15;

    /** Ball diameter **/
    private static final double BALL_DIAMETER = BALL_RADIUS * 2;

    public void run() {
        /* Adds mouse tracking to control the paddle */
        addMouseListeners();
        /* Set background color */
        setBackground(Color.decode("#141414"));
        /* Run game menu with rules */
        gameMenu();
        /* Create destructible walls */
        createBricks();
        add(paddle);
        /* Start game */
        game();
    }

    /** Display of the start window, which contains the name of the game and the rules **/
    private void gameMenu() {
        /* Name of the game */
        GLabel breakout = setLabel("BREAKOUT", Color.YELLOW);
        breakout.setFont(new Font("Sans-Serif", Font.BOLD, 36));
        add(breakout, (getWidth() - breakout.getWidth()) / 2, HEIGHT + 40 - getHeight());

        /* Action to continue */
        GLabel start = setLabel("*** CLICK TO START GAME ***",  Color.RED);
        start.setFont(new Font("Sans-Serif", Font.BOLD, 22));
        add(start, WIDTH / 2.0, HEIGHT / 2.0 - 20);
        start.move(-start.getWidth() / 2.0, -20);

        /* Rules for win */
        GLabel rulesWin = setLabel("-> You win if you break all the bricks", Color.WHITE);
        rulesWin.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        add(rulesWin, (getWidth() - rulesWin.getWidth()) / 2, HEIGHT / 2.0);

        /* Rules for lose */
        GLabel rulesLose = setLabel("-> You lose if the ball drop 3 times", Color.WHITE);
        rulesLose.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        add(rulesLose, (getWidth() - rulesLose.getWidth()) / 2, HEIGHT / 2.0 + 25);

        /* The start window will be displayed until the user clicks on the mouse button anywhere */
        waitForClick();

        /* Sound to start game */
        double[] startGame = StdAudio.read("sound/start.wav");
        StdAudio.play(speedUp(speedUp(startGame)));
        removeAll();
    }

    /** Formation of the very logic of the game **/
    public void game() {
        /* Creation ball */
        GOval ball = createBall();
        add(ball);
        /* Number of lives remaining */
        GLabel lives = setLabel("LIVES: " + (NTURNS - fallBall), Color.WHITE);
        lives.setFont(new Font("Sans-Serif", Font.BOLD, 22));
        add(lives, (getWidth() - lives.getWidth()) / 2, HEIGHT - getHeight());

        pause(1000);
        /* Movement ball for game */
        movementBall(ball, lives);
    }

    /** Displaying information at the end of the game **/
    private void gameOver() {
        pause(300);
        removeAll();
        GLabel text;
        /* If a player drops the ball 3 times, then he loses */
        if (fallBall == NTURNS) {
            text = setLabel("YOU LOSE :(", Color.RED);
            text.setFont(new Font("Sans-Serif", Font.BOLD, 22));
            add(text, (getWidth() - text.getWidth()) / 2,
                    (getHeight() + text.getAscent()) / 2);

            /* Sound for game over if player loses */
            double[] loose = StdAudio.read("sound/loose.wav");
            StdAudio.play(speedUp(speedUp(loose)));
        }
        /* If the player knocks down all the bricks, then he wins */
        else if (totalBricks == 0) {
            text = setLabel("YOU WIN :)", Color.BLUE);
            text.setFont(new Font("Sans-Serif", Font.BOLD, 22));
            add(text, (getWidth() - text.getWidth()) / 2,
                    (getHeight() + text.getAscent()) / 2);

            /* Sound for game over if player win */
            double[] win = StdAudio.read("sound/win.wav");
            StdAudio.play(speedUp(speedUp(win)));
        }

        pause(3000);
        System.exit(0);
    }

    /** STEP 1
     * The first step is to make a racket. It must be controlled by the mouse,
     * so whenever the user does not move the mouse, the racket must move with the cursor,
     * and it must be centered on the cursor. The racket always has the same coordinate on the Y axis,
     * it never moves up and down. When the user removes the mouse outside the screen - the racket
     * remains completely on the screen. **/
    private GRect createPaddle() {
        GRect paddle = new GRect(getWidth() / 2.0 - PADDLE_WIDTH / 2.0,
                getHeight() - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setColor(Color.WHITE);
        paddle.setFilled(true);
        return paddle;
    }

    /** Creating paddle **/
    private final GRect paddle = createPaddle();

    /** Snapping mouse movements to the paddle **/
    public void mouseMoved(MouseEvent mouseEvent) {
        if ((mouseEvent.getX() < getWidth() - PADDLE_WIDTH / 2) && (mouseEvent.getX() > PADDLE_WIDTH / 2)) {
            paddle.setLocation(mouseEvent.getX() - PADDLE_WIDTH / 2.0,
                    getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
        }
    }

    /** STEP 2
     * In this step, you need to identify the ball and describe its rebound from the walls. **/
    private GOval createBall() {
        GOval ball = new GOval(getWidth() / 2.0 - BALL_RADIUS,
                getHeight() / 2.0 - BALL_RADIUS, BALL_RADIUS, BALL_RADIUS);
        ball.setColor(Color.WHITE);
        ball.setFilled(true);
        return ball;
    }

    /** Create randomizer for movement ball **/
    RandomGenerator random = RandomGenerator.getInstance();

    /** Ball drop counter **/
    int fallBall = 0;
    /** Total bricks counter **/
    int totalBricks = NBRICKS_PER_ROW * NBRICK_ROWS;

    /** Method describing the movement and physics of the ball **/
    private void movementBall (GOval ball, GLabel attempt) {
        /* Ball falling sound */
        double[] falling = StdAudio.read("sound/falling.wav");
        /* X, Y velocity */
        double vy = 3;
        double vx = random.nextDouble(1.0, 3.0);
        if (random.nextBoolean(0.5)) {
            vx = -vx;
        }
        /* Movement ball */
        while (true) {
            /* If the ball hits the racket, it bounces */
            if (getCollisionBall(ball) == paddle) {
                /* Specifying the angles of the path */
                double offset = ball.getX() - (paddle.getX() + (PADDLE_WIDTH / 2.0));
                if (offset <= -30) { vx = -3; }
                else if (offset <= -20) { vx = -2; }
                else if (offset <= -10) { vx = -1; }
                else if (offset == 0) { vx = -vx; }
                else if (offset < 10) { vx = 1; }
                else if (offset < 20) { vx = 2; }
                else if (offset <= 30) { vx = 3; }
                /* Bounce ball */
                if (ball.getY() >= getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT - BALL_DIAMETER
                        && ball.getY() < getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT - BALL_DIAMETER + BRICK_SEP) {
                    vy = -vy;
                }
            }

            /** STEP 5
             * Breaking bricks when hitting them **/
            GObject collision = getCollisionBall(ball);
            if (collision != null && collision != paddle && collision != attempt) {
                remove(collision);
                /* We subtract the broken brick from the total number of bricks */
                totalBricks--;
                /* Bounce ball */
                vy = -vy;
                /* If the player has broken all the bricks, the game ends */
                if (totalBricks <= 0) {
                    gameOver();
                }
            }
            /* The ball falls to the floor */
            if (ball.getY() + BALL_RADIUS > getHeight()) {
                fallBall++;
                /* If the player has not yet used all the attempts, then the game continues */
                if (fallBall < NTURNS) {
                    remove(ball);
                    StdAudio.play(speedUp(speedUp(falling)));
                    remove(attempt);
                    game();
                }
                /* If the player drops the ball 3 times, then the game ends */
                else {
                    gameOver();
                }
            }
            /* Ball bouncing off walls and ceiling */
            else if (ball.getY() < 0) {
                vy = random.nextDouble(2.0, 3.0);
            } else if (ball.getX() + BALL_RADIUS >= getWidth()) {
                vx = -vx;
            } else if (ball.getX() <= 0) {
                vx = random.nextDouble(1.0, 3.0);
            }
            ball.move(vx, vy);
            pause(GAME_SPEED);
        }
    }

    /** STEP 3
     * Processing of collisions with a racket **/
    private GObject getCollisionBall(GOval ball) {
        /* x, y */
        if (getElementAt(ball.getX(), ball.getY()) != null)
            return (getElementAt(ball.getX(), ball.getY()));
        /* x + 2r, y */
        else if (getElementAt((ball.getX() + BALL_DIAMETER), ball.getY()) != null)
            return (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()));
        /* x, y + 2r */
        else if (getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER) != null)
            return (getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER));
        /* x + 2r, y + 2r */
        else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY() + BALL_DIAMETER) != null)
            return (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY() + BALL_DIAMETER));

        else return null;
    }

    /** STEP 4
     * Adding bricks
     * Each two rows has a new color in the sequence: RED, ORANGE, YELLOW, GREEN, CYAN **/
    private void createBricks() {
        int posNextRow = 0;
        Color color = Color.RED;
        for (int i = 0; i < NBRICK_ROWS; i++) {
            for (int j = 0; j < NBRICKS_PER_ROW; j++) {
                add(createBrick(BRICK_SEP / 3.0 + j * (BRICK_WIDTH + BRICK_SEP),
                        BRICK_Y_OFFSET - posNextRow, color));
            }
            if (i + 1 == 2) { color = Color.ORANGE; }
            else if (i + 1 == 4) { color = Color.YELLOW; }
            else if (i + 1 == 6) { color = Color.GREEN; }
            else if (i + 1 == 8) { color = Color.CYAN; }

            posNextRow -= BRICK_HEIGHT + BRICK_SEP;
        }
    }

    private GRect createBrick(double x, double y, Color color) {
        GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        brick.setColor(Color.BLACK);
        brick.setFillColor(color);
        brick.setFilled(true);
        return brick;
    }

    /** Method for creating text labels **/
    private GLabel setLabel(String text, Color color) {
        GLabel label = new GLabel(text);
        label.setColor(color);
        return label;
    }

    /** Accelerating music **/
    private double[] speedUp(double[] clip) {
        /* We only need half as much space. */
        double[] result = new double[clip.length / 2];

        /* Sample from twice the current position. */
        for (int i = 0; i < result.length; i++) {
            result[i] = clip[i * 2];
        }

        return result;
    }
}
