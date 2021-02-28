package game;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.Client;

import java.util.Arrays;
import java.util.LinkedList;

public class TicTacToe_Client extends Application {

    GridPane grid;
    Client client;
    Scene scene;

    private void initializeGrid() {
        grid = new GridPane();
        grid.setMinSize(900, 900);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(150);
        grid.setVgap(75);
    }

    private void drawCross(int column, int row) {
        Text cross = new Text("X");
        cross.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(cross, column, row);
    }

    private void drawCircle(int column, int row) {
        Text circle = new Text("O");
        circle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(circle, column, row);
    }

    private void drawEmptyField(int column, int row) {
        Text emptyField = new Text("  ");
        emptyField.setFont(Font.font("Tahoma", FontWeight.NORMAL, 220));
        grid.add(emptyField, column, row);
    }

    private void drawBoard(String gameState) {
        if (gameState.length() != 9) {
            System.err.println("Wrong length of gameState string");
            return;
        }
        for (int i = 0; i < gameState.length(); i++) {
            int column = i / 3;
            int row = i % 3;
            if (gameState.charAt(i) == 'x') {
                this.drawCross(column, row);
            } else if (gameState.charAt(i) == 'o') {
                this.drawCircle(column, row);
            } else {
                this.drawEmptyField(column, row);
            }

        }
    }

    private Scene setScene() {
        scene = new Scene(grid, 900, 900);
        scene.getStylesheets().add
                (TicTacToe_Client.class.getResource("TicTacToe_Client.css").toExternalForm());
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onMouseClick(event);
            }
        });
        return scene;
    }

    private void onMouseClick(MouseEvent event) {
        client.sendToServer("update");
        client.sendToServer(String.format("%f|%f", event.getX(), event.getY()));
        String gameState = client.getResponse();
        System.out.println(gameState);
        if (gameState.length() == 9) {
            drawBoard(gameState);
            if (!client.getGameEnded()) {
                gameState = client.getResponse();
                drawBoard(gameState);
            } else {
                LinkedList<Integer> winCoordinates = new LinkedList<>();
                String response = client.getResponse();
                for (String s : Arrays.copyOfRange(response.split(";"), 0, 4)) {
                    winCoordinates.add(Integer.valueOf(s) * 300);
                }
                this.drawWinningLine(winCoordinates);
                client.exitProcess();
            }
        } else {
            int column = (int) event.getX() / 300;
            int row = (int) event.getY() / 300;
            System.err.printf("You are not allowed to place at %d|%d%n", column, row);
        }
    }

    private void drawWinningLine(LinkedList<Integer> winCoordinates) {
        Line winningLine = new Line(winCoordinates.get(0), winCoordinates.get(1), winCoordinates.get(2), winCoordinates.get(3));
        winningLine.setFill(Color.RED);
        grid.add(winningLine, winCoordinates.get(0) / 300, winCoordinates.get(1) / 300, 3, 3);
    }

    @Override
    public void start(Stage primaryStage) {
        client = new Client("server", 2589, "Cato");
        client.handshake();

        primaryStage.setTitle("TicTacToe");
        primaryStage.setResizable(false);

        this.initializeGrid();
        primaryStage.setScene(this.setScene());
        primaryStage.show();

        this.drawBoard(client.getGameState());
    }

    public static void main(String[] args) {
        launch(args);
    }

}
