import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Main extends Application {

	Stage gameStage;
	Stage popup;
	MasterGridSpace grid;
	Scene mainScene;

	int gridSize = 5;
	int boxSize = 120;

	int playerCount;
	int turnCount;

	@Override
	public void start(Stage stage) {
		gameStage = stage;
		popup = new Stage();
		popup.setHeight(400);
		popup.setWidth(600);
		popup.centerOnScreen();
		popup.setResizable(false);

		VBox vBox = new VBox();
		VBox vBox2 = new VBox();
		HBox hBox1 = new HBox();
		HBox hBox2 = new HBox();

		Text header = new Text();
		header.setFont(Font.font(30));
		header.setTextAlignment(TextAlignment.CENTER);
		header.setText("Hello Overseer!");

		Text header2 = new Text();
		header2.setFont(Font.font(24));
		header2.setTextAlignment(TextAlignment.CENTER);
		header2.setText("Choose the settings for the game below\n");

		Button btn2Player = new Button();
		Button btn3Player = new Button();
		Button btn4Player = new Button();
		setButton(btn2Player, "2");
		setButton(btn3Player, "3");
		setButton(btn4Player, "4");
		btn2Player.setOnMouseClicked(e -> setPlayers(2));
		btn3Player.setOnMouseClicked(e -> setPlayers(3));
		btn4Player.setOnMouseClicked(e -> setPlayers(4));

		Button btn15Turn = new Button();
		Button btn20Turn = new Button();
		Button btn25Turn = new Button();
		setButton(btn15Turn, "15");
		setButton(btn20Turn, "20");
		setButton(btn25Turn, "25");
		btn15Turn.setOnMouseClicked(e -> setTurns(15));
		btn20Turn.setOnMouseClicked(e -> setTurns(20));
		btn25Turn.setOnMouseClicked(e -> setTurns(25));

		Text pCount = new Text();
		pCount.setFont(Font.font(24));
		pCount.setText("\tPlayer Count: ");

		Text tCount = new Text();
		tCount.setFont(Font.font(24));
		tCount.setText("\tTurn Count:   ");

		hBox1.setSpacing(20);
		hBox2.setSpacing(20);

		hBox1.getChildren().addAll(pCount, btn2Player, btn3Player, btn4Player);
		hBox2.getChildren().addAll(tCount, btn15Turn, btn20Turn, btn25Turn);

		vBox2.getChildren().addAll(hBox1, hBox2);
		vBox2.setSpacing(10);

		vBox.getChildren().addAll(header, header2, vBox2);
		vBox.setAlignment(Pos.CENTER);

		BorderPane bPane = new BorderPane(vBox);

		Scene scene = new Scene(bPane);
		popup.setScene(scene);
		popup.show();

		popup.setResizable(false);
		popup.setAlwaysOnTop(true);

	}
	
	public static void main(String[] args) {
		launch(args);
	}

	private void createGameWindow(Stage stage) {
		
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double maxHeight = screenBounds.getMaxY();
		double maxWidth = screenBounds.getMaxX();
		
		stage.setHeight(800);
		stage.setWidth(800);
		stage.setTitle("The Lost Dungeon");
		stage.setX(maxWidth/2 - 100);
		stage.setY(maxHeight/2 - 420);
		stage.setResizable(false);
		
		TextArea txtWindow = new TextArea(maxWidth/2 - 700, maxHeight/2 - 420);
		
		BorderPane bPane = new BorderPane();
		grid = new MasterGridSpace(txtWindow, playerCount, turnCount, gridSize, boxSize);
		bPane = new BorderPane(grid);
		grid.setAlignment(Pos.CENTER);

		mainScene = new Scene(bPane, 800, 800);
		bPane.setStyle("-fx-background-color: lightgray;");
		
		grid.scene = mainScene;
		txtWindow.otherScene = mainScene;
		txtWindow.grid = grid;
		
		stage.setScene(mainScene);
		stage.show();
	}

	void setPlayers(int x) {
		playerCount = x;
		if (turnCount > 0) {
			popup.close();
			createGameWindow(gameStage);
		}
	}

	void setTurns(int x) {
		turnCount = x;
		if (playerCount > 0){
			popup.close();
			createGameWindow(gameStage);
		}
	}

	void setButton(Button btn, String label) {
		btn.setText(label);
		btn.setFont(Font.font(18));
		btn.setMinWidth(50);
	}
}
