import java.util.LinkedList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TextArea {

	Stage stage;
	BorderPane bPane;
	Scene scene;

	Button readyBtn;
	Scene otherScene;
	MasterGridSpace grid;

	Text textArea;
	List<String> text;

	public TextArea(double x, double y) {

		stage = new Stage();
		stage.setTitle("Text Log");
		stage.setHeight(800);
		stage.setWidth(600);
		stage.setX(x);
		stage.setY(y);

		text = new LinkedList<String>();
		text.add("This is where instructions to the Overseer (you) are displayed\n");
		text.add("First, make sure everyone is familar with the rules and settings");
		text.add("Once everyone is ready, assign each player with a player number\n");
		text.add("Announce the names and locations of the objects below: \n");

		textArea = new Text();
		textArea.setFont(Font.font(20));
		readyBtn = new Button();
		readyBtn.setFont(Font.font(30));
		readyBtn.setText("READY");

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(0, 0, 0, 10));
		vbox.getChildren().addAll(textArea, readyBtn);
		bPane = new BorderPane(vbox);
		scene = new Scene(bPane);
		stage.setScene(scene);
		stage.show();

		readyBtn.setOnMouseClicked(e -> {
			otherScene.addEventFilter(KeyEvent.KEY_PRESSED, grid.turnEvents);
			scene.addEventFilter(KeyEvent.KEY_PRESSED, grid.turnEvents);
			bPane.getChildren().clear();
			text.clear();
			bPane.getChildren().add(textArea);
			
			grid.generateTreasure(); //
			append("(Enter any key to continue)");

			display();
		});

	}

	void display() {
		String str = "";
		for (int i = 0; i < text.size(); i++)
			str += text.get(i) + "\n";
		textArea.setText(str);
	}

	void append(String str) {
		text.add(str);
		display();
	}
	
	void newLine() {
		append("");
	}

	void startTurnMessage() {
		text.clear();
		append("Turn " + grid.turn + " of " + grid.turnCount);
		append("--------------\n");
	}

	void turnMessage(MasterGridSpace.Player player) {
		append("Player " + (player.id + 1) + "'s turn");

		String moveChoices;
		if (!player.pointer.hasMove)
			moveChoices = "Nowhere to move, turning around (enter any key)...";
		else {
			moveChoices = "Choose to move\t| ";
			if (player.pointer.left != null)
				moveChoices += "Left | ";
			if (player.pointer.forward != null)
				moveChoices += "Forward | ";
			if (player.pointer.right != null)
				moveChoices += "Right | ";
		}
		append(moveChoices + "\n");
	}

	void treasureLocation(int x, int y) {
		append("Treasure Room - Row " + x + ", Col " + y + "\n");
	}
	
	void demonEncounter() {
		append("You saw the demon and immediately turned back\n");
	}
	
	void treasure(int x) {
		append("You found the treasure room and picked up " + x + " gold\n");
	}
	
	void attacked(int player, int attacks, boolean demon, int gold) {

		append("Player " + player);
		append("You were attacked " + attacks + " time(s)");
		if (demon) 
			append("A demon attacked you, and you fled to an unknown location");
			
		String str;
		if (gold <= 0) 
			str = "You now have no gold";
		else
			str = "You lost some of your gold";
		str += ", and you spent a turn recovering";
		append(str);
	}
	
	void attack(int victimGold) {
		String str = "You attacked another player";
		if (victimGold > 0)
			str += " and stole 1 gold";
		else
			str += " but they did not have any gold";
		append(str + "\n");
	}
	
	void gameOver() {
		append("\nGame Over");
		append("(Press any key to continue...)");
	}
	
	void results() {
		text.clear();
		append("The results are...");
		append("(Press any key to continue...)\n\n");
	}
}