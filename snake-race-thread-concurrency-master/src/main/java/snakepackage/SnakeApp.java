package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;


public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] initialPositions = {
            new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
            new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2)};
    private JFrame window;
    private static Board gameBoard;
    private JButton btnStart, btnPause, btnResume;
    private JLabel lblInfo;
    private boolean isGameStarted = false;
    private boolean isGamePaused = false;
    
    private final Object pauseLock = new Object();
    
    public SnakeApp() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window = new JFrame("The Snake Race");
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 100);
        window.setLocation(screenSize.width / 2 - window.getWidth() / 2,
                screenSize.height / 2 - window.getHeight() / 2);

        for (int i = 0; i < MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, initialPositions[i], i + 1);
        }

        gameBoard = new Board();

        window.add(gameBoard, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        btnStart = new JButton("Start");
        btnPause = new JButton("Pause");
        btnResume = new JButton("Resume");

        btnStart.addActionListener(e -> startGame());
        btnPause.addActionListener(e -> pauseGame());
        btnResume.addActionListener(e -> resumeGame());

        controlPanel.add(btnStart);
        controlPanel.add(btnPause);
        controlPanel.add(btnResume);

        window.add(controlPanel, BorderLayout.SOUTH);

        lblInfo = new JLabel("not started");
        window.add(lblInfo, BorderLayout.NORTH);

        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
    }

    public static void main(String[] args) {
       app = new SnakeApp();
        app.window.setVisible(true);
    }

    private void startGame() {
        if (!isGameStarted) {
            isGameStarted = true;
            for (int i = 0; i < MAX_THREADS; i++) {
                snakes[i].addObserver(gameBoard);
                Thread thread = new Thread(snakes[i]);
                thread.start();
            }
            btnStart.setEnabled(false);
            btnPause.setEnabled(true);
        }
    }

    private void pauseGame() {
        synchronized (pauseLock) {
            if (isGameStarted && !isGamePaused) {
                isGamePaused = true;
                for (Snake snake : snakes) {
                    snake.pause();
                }
                btnPause.setEnabled(false);
                btnResume.setEnabled(true);
                updateInfoLabel();
            }
        }
    }

    private void resumeGame() {
        synchronized (pauseLock) {
            if (isGameStarted && isGamePaused) {
                isGamePaused = false;
                for (Snake snake : snakes) {
                    snake.resume();
                }
                btnPause.setEnabled(true);
                btnResume.setEnabled(false);
                lblInfo.setText("Game resumed");
                updateInfoLabel();
            }
        }
    }

    private void updateInfoLabel() {
        Snake longestSnake = GetLongSnake();
        String worstSnake = GetWorstSnake();
        String info = "Longest snake: " + (longestSnake != null ? longestSnake.getIdt() + " (length: " + longestSnake.getBody().size() + ")" : "None") +
                ", First Death snake: " + worstSnake;
                lblInfo.setText(info);
    }

    private Snake GetLongSnake() {
        Snake longest = null;
        int maxLength = 0;
        for (Snake snake : snakes) {
            if (!snake.isSnakeEnd() && snake.getBody().size() > maxLength) {
                longest = snake;
                maxLength = snake.getBody().size();
            }
        }
        return longest;
    }

    private String GetWorstSnake() {
        String diedFirst = Snake.getDiedFirst();
        if (diedFirst.equals("No")) {
            diedFirst = "No one died";
        }
        return diedFirst;
    }

    public static SnakeApp getApp() {
        return app;
    }
}
