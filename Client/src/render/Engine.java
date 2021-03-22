package render;

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

import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class Engine extends Application {

    private GridPane grid;
    private Scene scene;
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static Engine engine = null;
    private boolean mouseClicked = false;
    private boolean windowClosed = false;
    private Point coordinates = new Point();
    private Stage primaryStage;

    public Engine() {
        setEngine(this);
    }

    private void initializeGrid() {
        grid = new GridPane();
        grid.setPrefSize(900, 900);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(150);
        grid.setVgap(75);
    }

    private Scene setScene() {
        scene = new Scene(grid, 900, 900);
        scene.getStylesheets().add("res/TicTacToe_Client.css");
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                onMouseClick(event);
            }
        });
        return scene;
    }

    public static Engine waitForEngine() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return engine;
    }

    public static void setEngine(Engine new_engine) {
        engine = new_engine;
        latch.countDown();
    }

    public void updateTitle(String title) {
        primaryStage.setTitle(title);
    }

    public void drawCross(int column, int row) {
        Text cross = new Text("X");
        cross.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(cross, column, row);
    }

    public void drawCircle(int column, int row) {
        Text circle = new Text("O");
        circle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 200));
        grid.add(circle, column, row);
    }

    public void drawEmptyField(int column, int row) {
        Text emptyField = new Text("  ");
        emptyField.setFont(Font.font("Tahoma", FontWeight.NORMAL, 220));
        grid.add(emptyField, column, row);
    }

    public void drawBoard(String gameState) {
        if (gameState.equals("---------")) {
            grid.getChildren().clear();
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

    private void onMouseClick(MouseEvent event) {
        mouseClicked = true;
        coordinates.setLocation(event.getX(), event.getY());
    }

    public boolean isWindowClosed() {return windowClosed;}

    public boolean isMouseClicked() {
        return mouseClicked;
    }

    public void setMouseClicked(boolean mouseClicked) {
        this.mouseClicked = mouseClicked;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize window
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Test");
        primaryStage.setResizable(true);
        this.initializeGrid();
        primaryStage.setScene(this.setScene());
        primaryStage.sizeToScene();
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            windowClosed = true;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
