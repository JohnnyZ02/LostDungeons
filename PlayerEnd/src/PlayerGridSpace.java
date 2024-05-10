import java.util.Random;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

public class PlayerGridSpace extends GridPane {

	Scene scene;
	GridSquare[][] squares;
	int grid;

	Random random = new Random();

	Color newColor = Color.WHITE;
	Color oldBody = Color.WHITE;
	Color oldArrow = Color.WHITE;
	int oldRotation = 0;

	boolean move = false;

	GridSquare selected;
	boolean bodyFill = true;
	boolean bgFill = false;

	static int adjacentIndexConversion[][] = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

	public PlayerGridSpace(int grid, int size) {
		squares = new GridSquare[grid][grid];
		this.grid = grid;
		for (int i = 0; i < grid; i++) {
			for (int j = 0; j < grid; j++) {
				squares[i][j] = new GridSquare(size, i, j);
				this.add(squares[i][j], size * i, size * j);
				squares[i][j].setBoundary(i, j);
			}
		}
		this.setStyle("-fx-padding: 10 10 10 10;");
	}

	int generateRand(int max) {
		int rand = random.nextInt(max);
		return rand;
	}
	

	class GridSquare extends Pane {

		int col;
		int row;
		double size;

		Group icon;
		Circle body;
		Polygon arrow;

		int rotation;
		Pointer pointer;

		boolean wall;
		boolean adjacentInBoundary[] = new boolean[4];

		public GridSquare(int size, int col, int row) {
			this.col = col;
			this.row = row;
			this.size = size;

			rotation = 0;
			pointer = new Pointer(col, row);

			setPrefSize(size, size);
			setStyle("-fx-border-color: black; -fx-background-color: white;");

			setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY) {
					if (!wall) {
						if (selected != null && !selected.wall)
							selected.setStyle("-fx-border-color: black; -fx-background-color: white;");
						selected = squares[col][row];
						setStyle("-fx-border-color: black; -fx-background-color: lightblue;");
					}
				} else {
					if (!bgFill) {
						clearContents();
						setWall();
						if (selected != null && !selected.wall)
							selected.setStyle("-fx-border-color: black; -fx-background-color: white;");
					} else {
						if (selected != null && !selected.wall)
							selected.setStyle("-fx-border-color: black; -fx-background-color: white;");
						selected = null;
						this.setBackground(new Background(new BackgroundFill(newColor, null, null)));
					}
				}
				scene.setOnKeyPressed(e2 -> {
					newColor = colorPicker(e2);
					if (e2.getCode() == KeyCode.ALT)
						bgFill = toggle(bgFill);
					else if (e2.getCode() == KeyCode.SHIFT)
						bodyFill = toggle(bodyFill);
					if (selected != null) {
						if (e2.getCode() == KeyCode.SPACE) {
							if (icon == null) {
								if (move) {
									placeIcon(oldRotation, oldBody, oldArrow);
									move = false;
								}
							} else if (selected.icon != null) {
								if (!wall) {
									oldBody = (Color) body.getFill();
									oldArrow = (Color) arrow.getFill();
									oldRotation = rotation;
									clearContents();
									move = toggle(move);
								}
							}
						} else if (e2.getCode() == KeyCode.DELETE) {
							setStyle("-fx-border-color: black; -fx-background-color: white;");
							clearContents();
							wall = false;
						} else if (e2.getCode() == KeyCode.ENTER) {
							if (!wall && icon == null) {
								if (bodyFill)
									placeIcon(newColor, oldArrow);
								else
									placeIcon(oldBody, newColor);
							}
						} else if (selected.icon != null) {
							if (e2.getCode() == KeyCode.R)
								selected.rotate(90);
							else if (e2.getCode() == KeyCode.UP)
								selected.setRotation(0);
							else if (e2.getCode() == KeyCode.RIGHT)
								selected.setRotation(90);
							else if (e2.getCode() == KeyCode.DOWN)
								selected.setRotation(180);
							else if (e2.getCode() == KeyCode.LEFT)
								selected.setRotation(270);
							else if (e2.getCode() == KeyCode.W)
								selected.move(0);
							else if (e2.getCode() == KeyCode.D)
								selected.move(1);
							else if (e2.getCode() == KeyCode.A)
								selected.move(3);
							else if (e2.getCode() == KeyCode.S)
								selected.rotate(180);
						}
					}
				});
			});
		}

		boolean toggle(boolean bool) {
			if (bool)
				return false;
			return true;
		}

		void setWall() {
			setStyle("-fx-border-color: black; -fx-background-color: black;");
			wall = true;
		}

		void clearContents() {
			getChildren().clear();
			rotation = 0;
			if (icon != null)
				setRotation(0);
			icon = null;
		}

		Color colorPicker(KeyEvent e2) {
			if (e2.getCode() == KeyCode.DIGIT0)
				return Color.BEIGE;
			else if (e2.getCode() == KeyCode.DIGIT1)
				return Color.RED;
			else if (e2.getCode() == KeyCode.DIGIT2)
				return Color.ORANGE;
			else if (e2.getCode() == KeyCode.DIGIT3)
				return Color.YELLOW;
			else if (e2.getCode() == KeyCode.DIGIT4)
				return Color.GREEN;
			else if (e2.getCode() == KeyCode.DIGIT5)
				return Color.CYAN;
			else if (e2.getCode() == KeyCode.DIGIT6)
				return Color.BLUE;
			else if (e2.getCode() == KeyCode.DIGIT7)
				return Color.PURPLE;
			else if (e2.getCode() == KeyCode.DIGIT8)
				return Color.PINK;
			else if (e2.getCode() == KeyCode.DIGIT9)
				return Color.GRAY;
			return newColor;
		}

		void placeIcon(Color color1, Color color2) {
			body = addPlayer(color1);
			arrow = addDirection(color2);
			icon = new Group(body, arrow);
			getChildren().add(icon);
			rotate(random.nextInt(4) * 90);
		}

		void placeIcon(int rotation, Color color1, Color color2) {
			body = addPlayer(color1);
			arrow = addDirection(color2);
			icon = new Group(body, arrow);
			getChildren().add(icon);
			rotate(rotation);
		}

		void setBoundary(int col, int row) {
			fillBooleanArrayTrue(squares[col][row].adjacentInBoundary);
			if (row == 0)
				squares[col][row].adjacentInBoundary[0] = false;
			if (col == grid - 1)
				squares[col][row].adjacentInBoundary[1] = false;
			if (row == grid - 1)
				squares[col][row].adjacentInBoundary[2] = false;
			if (col == 0)
				squares[col][row].adjacentInBoundary[3] = false;
		}

		void fillBooleanArrayTrue(boolean arr[]) {
			for (int i = 0; i < arr.length; i++)
				arr[i] = true;
		}

		Circle addPlayer(Color color) {
			oldBody = color;
			Circle circ = new Circle();
			circ.setRadius(size * .3);
			circ.setCenterX(size / 2);
			circ.setCenterY(size / 2);
			circ.setFill(color);
			circ.setStroke(Color.BLACK);
			return circ;
		}

		Polygon addDirection(Color color) {
			oldArrow = color;
			Polygon poly = new Polygon();
			poly.getPoints()
					.addAll(new Double[] { size * .5, size * .075, size * .4, size * .15, size * .6, size * .15 });
			poly.setFill(color);
			poly.setStroke(Color.BLACK);
			return poly;
		}

		void rotate(int angle) {
			addRotation(angle);
			Rotate transformation = new Rotate();
			transformation.setPivotX(size / 2);
			transformation.setPivotY(size / 2);
			transformation.setAngle(angle);
			icon.getTransforms().addAll(transformation);
		}

		void addRotation(int x) {
			rotation += x;
			while (rotation >= 360)
				rotation -= 360;
		}

		void setRotation(int newRotation) {
			rotate(newRotation + 360 - rotation);
			rotation = newRotation;
		}

		void move(int direction) {
			pointer.adjust(rotation);
			GridSquare newLocation;
			if (direction == 0)
				newLocation = pointer.forward;
			else if (direction == 1)
				newLocation = pointer.right;
			else
				newLocation = pointer.left;
			rotate(direction * 90);
			if (newLocation != null)
				if (!newLocation.wall && newLocation.icon == null) {
					newLocation.placeIcon(rotation, (Color) body.getFill(), (Color) arrow.getFill());
					clearContents();
					selected = newLocation;
					setStyle("-fx-border-color: black; -fx-background-color: white;");
					selected.setStyle("-fx-border-color: black; -fx-background-color: lightblue;");
				}
		}

		class Pointer {

			int col;
			int row;
			int direction;

			GridSquare forward;
			GridSquare left;
			GridSquare right;

			public Pointer(int col, int row) {
				this.col = col;
				this.row = row;
			}

			void adjust(int rotation) {
				direction = rotation / 90;

				while (direction > 3)
					direction -= 4;

				if (adjacentInBoundary[direction])
					forward = squares[col + adjacentIndexConversion[direction][0]][row
							+ adjacentIndexConversion[direction][1]];

				int leftDirection;
				if (direction - 1 < 0)
					leftDirection = 3;
				else
					leftDirection = direction - 1;
				if (adjacentInBoundary[leftDirection])
					left = squares[col + adjacentIndexConversion[leftDirection][0]][row
							+ adjacentIndexConversion[leftDirection][1]];

				int rightDirection;
				if (direction + 1 > 3)
					rightDirection = 0;
				else
					rightDirection = direction + 1;
				if (adjacentInBoundary[rightDirection])
					right = squares[col + adjacentIndexConversion[rightDirection][0]][row
							+ adjacentIndexConversion[rightDirection][1]];
			}

			void printCoords() {
				System.out.println(forward.col + " " + forward.row);
				System.out.println(right.col + " " + right.row);
				System.out.println(left.col + " " + left.row);
			}
		}

		void printArr(boolean[] arr) {
			for (int i = 0; i < arr.length; i++) {
				System.out.println(arr[i]);
			}
			System.out.println();
		}
	}
}
