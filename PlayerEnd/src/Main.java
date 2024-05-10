import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

public class Main extends Application {

	PlayerGridSpace grid;

	int gridSize = 5;
	int height = 800;
	int width = 800;
	int boxSize = 120;

	@Override
	public void start(Stage stage) {
		stage.setHeight(height);
		stage.setWidth(width);
		stage.setTitle("");
		stage.centerOnScreen();
		stage.setResizable(false);

		grid = new PlayerGridSpace(gridSize, boxSize);
		BorderPane bPane = new BorderPane(grid);
		grid.setAlignment(Pos.CENTER);
		Scene scene = new Scene(bPane, width, height);
		bPane.setStyle("-fx-background-color: darkslategray;");
		grid.scene = scene;

		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
