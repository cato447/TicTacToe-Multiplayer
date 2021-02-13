package client.games;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Board extends JPanel implements ActionListener {

    private final int B_WIDTH = 900;
    private final int B_HEIGHT = 900;
    private final int TILE_X = 300;
    private final int TILE_Y = 300;
    private final int DELAY = 50;

    private boolean ended = false;
    private boolean gameWon = false;

    int[] oldPlayfield;

    private Timer timer;
    private Game game;
    private Painter painter;

    public Board(){
        initBoard();
    }

    private void initBoard(){
        painter = new Painter(B_WIDTH,B_HEIGHT);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int column = e.getX()/TILE_X;
                int row = e.getY()/TILE_Y;
                game.place(column * 3 + row, 1);
            }
        });

        setBackground(Color.BLACK);
        setFocusable(true);
        setPreferredSize(new Dimension(B_WIDTH,B_HEIGHT));

        initGame();
    }

    private void initGame(){
        game = new Game();
        oldPlayfield = game.getPlayfield().clone();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        painter.paintGrid(g);
        updateBoard(g);
    }

    private void updateBoard(Graphics g){
        int actions = 0;
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                if (game.getPlayfield()[actions] == 1) {
                    painter.drawX(g, column, row);
                } else if (game.getPlayfield()[actions] == -1) {
                    painter.drawO(g, column, row);
                }
                actions++;
            }
        }
        if (gameWon) {
            painter.paintWinnerLine(g);
        }
    }

    public void resetBoard(){
        for (int i = 0; i < game.getPlayfield().length; i++){
            game.setPlayfield(i, 0);
        }
        timer.start();
        oldPlayfield = game.getPlayfield().clone();
        game.setTurnTaken(false);
        gameWon = false;
        repaint();
    }

    public void setWinningLine(){
        painter.setWinningX1(game.getWinningX1());
        painter.setWinningY1(game.getWinningY1());
        painter.setWinningX2(game.getWinningX2());
        painter.setWinningY2(game.getWinningY2());
    }

    //game controlling method
    @Override
    public void actionPerformed(ActionEvent e) {
        Thread actionThread = new Thread(){
            @Override
            public void run() {
                //check if game state evaluation needs to be done
                if (isChanged(oldPlayfield)) {
                    gameWon = game.checkWin();
                    //repaint board if not won
                    if (!gameWon) {
                        repaint();
                        oldPlayfield = game.getPlayfield().clone();
                    }
                    //stop timer if game won
                    if (gameWon || game.emptyTiles() == 0) {
                        if (gameWon) {
                            setWinningLine();
                        }
                        repaint();
                        timer.stop();
                        try {
                            Thread.sleep(1000);
                            int n = JOptionPane.showConfirmDialog(null, "Do you want to play again?");
                            if (n == 0){
                                resetBoard();
                            } else {
                                System.exit(0);
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }
                }
                //check if computer needs to take a turn
                if (game.isTurnTaken()){
                    game.setTurnTaken(false);
                    game.computersTurn();
                }
            }
        };
        actionThread.start();
    }

    private boolean isChanged(int[] playfield){
        return !Arrays.equals(game.getPlayfield(), playfield);
    }
}
