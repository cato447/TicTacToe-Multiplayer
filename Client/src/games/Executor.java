package games;

import javax.swing.*;

public class Executor extends JFrame {
    public Executor(){
        initUI();
    }

    private void initUI(){
        Board board = new Board();
        setTitle("TicTacToe - MinMax");
        add(board);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Executor exc = new Executor();
                exc.setVisible(true);
            }
        });
    }
}
