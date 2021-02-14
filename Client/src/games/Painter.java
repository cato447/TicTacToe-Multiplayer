package games;

import java.awt.*;

public class Painter {

    private final int TILE_X;
    private final int TILE_Y;

    private int winningX1, winningY1, winningX2, winningY2;

    public Painter(int boardWidth, int boardHeight){
        TILE_X = boardWidth/3;
        TILE_Y = boardHeight/3;
    }

    public void drawX(Graphics g, int column, int row) {
        Graphics2D g2d = (Graphics2D) g;

        int nextColumn = column + 1;
        int nextRow = row + 1;
        int x1 = column *  TILE_X + 25;
        int x2 = nextColumn * TILE_Y - 25;
        int y1 = row * TILE_X + 25;
        int y2 = nextRow * TILE_Y - 25;

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.drawLine(x1, y2, x2, y1);
    }

    public void drawO(Graphics g, int column, int row){
        int x = column * TILE_X + 25;
        int y = row * TILE_Y + 25;
        g.drawOval(x,y,250,250);
    }

    public void paintGrid(Graphics g){
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(10));
        //horizontal
        for (int i = 1; i < 3; i++) {
            g2d.drawLine(0, TILE_Y*i, TILE_X*3, TILE_Y*i);
        }
        //vertical
        for (int i = 1; i < 3; i++){
            g2d.drawLine(TILE_X*i, 0, TILE_X*i, TILE_Y*3);
        }
    }

    public void paintWinnerLine(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(40));
        g2d.drawLine(winningX1, winningY1, winningX2, winningY2);
    }

    public void setWinningX1(int winningX1) {
        this.winningX1 = winningX1;
    }

    public void setWinningX2(int winningX2) {
        this.winningX2 = winningX2;
    }

    public void setWinningY1(int winningY1) {
        this.winningY1 = winningY1;
    }

    public void setWinningY2(int winningY2) {
        this.winningY2 = winningY2;
    }
}
