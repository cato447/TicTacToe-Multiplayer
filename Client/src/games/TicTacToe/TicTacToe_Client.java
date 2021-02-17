package games.TicTacToe;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.Client;

public class TicTacToe_Client extends Application {

    GridPane grid;
    Client client;

    private void initializeGrid(){
        grid = new GridPane();
        grid.setMinSize(900,900);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(150);
        grid.setVgap(75);
        grid.setGridLinesVisible(true);
    }



    private void drawCross(int column, int row){
        Text cross = new Text("X");
        cross.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(cross, column, row);
    }

    private void drawCircle(int column, int row){
        Text circle = new Text("O");
        circle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(circle, column, row);
    }


    private void drawEmptyField(int column, int row) {
        Text emptyField = new Text(" ");
        emptyField.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(emptyField, column, row);
    }

    private void drawBoard(String gameState){
        if (gameState.length() != 9){
            System.err.println("Wrong length of gameState string");
            return;
        }
        for (int i = 0; i < gameState.length(); i++){
            int column = i % 3;
            int row = i / 3;
            if (gameState.charAt(i) == 'x'){
                this.drawCross(column, row);
            } else if (gameState.charAt(i) == 'o'){
                this.drawCircle(column, row);
            } else {
                this.drawEmptyField(column, row);
            }

        }
    }

    private Scene setScene(){
        Scene scene = new Scene(grid, 900, 900);
        scene.getStylesheets().add
                (TicTacToe_Client.class.getResource("TicTacToe_Client.css").toExternalForm());
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                client.sendToServer(String.format("(%f|%f)", event.getX(), event.getY()));
                client.getGameState();
            }
        });
        return scene;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TicTacToe");
        primaryStage.setResizable(false);

        this.initializeGrid();

        primaryStage.setScene(this.setScene());

        primaryStage.show();

        this.drawBoard("---------");

        client = new Client("localhost", 2589, "TestClient");
        client.handshake();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
