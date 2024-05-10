import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

public class MasterGridSpace extends GridPane {

	TextArea textLog;
	EventHandler<KeyEvent> turnEvents;

	int p = -1;
	int size;
	int gridSize;
	GridSquare[][] squares;
	Player[] players;
	Scene scene;

	int playerCount;
	int turnCount;

	Demon demon;

	boolean validMove;
	boolean demonEncountered;

	boolean gameOver = false;
	boolean newTurn = true;
	int turn = 0;
	int postClicks = 0;
	List<String> placement;

	Random random = new Random();
	int[][] coordsTaken;

	boolean treasurePresent;
	boolean demonPresent = false;

	static int adjacentIndexConversion[][] = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

	public MasterGridSpace(TextArea txtWindow, int playerCount, int turnCount, int gridSize, int size) {
		this.size = size;
		textLog = txtWindow;
		this.playerCount = playerCount;
		this.turnCount = turnCount;
		squares = new GridSquare[gridSize][gridSize];
		this.gridSize = gridSize;
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				squares[i][j] = new GridSquare(size, i, j);
				this.add(squares[i][j], size * i, size * j);
				squares[i][j].setBoundary(i, j);
			}
		}
		this.setStyle("-fx-padding: 10 10 10 10;");

		coordsTaken = new int[playerCount + 4][2];
		int x;
		int y;

		int index = 0;
		int generations = 0;
		while (index < 4) {
			generations++;
			x = generateRand(gridSize);
			y = generateRand(gridSize);
			if (!adjacentCoordsExists(coordsTaken, x, y)) {
				coordsTaken[index][0] = x;
				coordsTaken[index][1] = y;
				index++;
				squares[x][y].setWall();
			} else if (generations > 25) {
				index = 0;
				for (int i = 0; i < coordsTaken.length; i++)
					squares[coordsTaken[i][0]][coordsTaken[i][1]].undoWall();
				generations = 0;
			}
		}

		Color[] playerColors = { Color.INDIANRED, Color.NAVY, Color.DARKGREEN, Color.DARKORANGE };

		players = new Player[playerCount];
		while (index < coordsTaken.length) {
			x = generateRand(gridSize);
			y = generateRand(gridSize);
			if (!coordsExists(coordsTaken, x, y)) {
				coordsTaken[index][0] = x;
				coordsTaken[index][1] = y;
				int rotation = random.nextInt(4) * 90;
				players[index - 4] = new Player(playerColors[index - 4], index - 4, x, y, rotation);
				squares[x][y].pId = index - 4;
				index++;
			}
		}

		textLog.append("Wallblocks");
		for (int i = 0; i < 4; i++)
			textLog.append("\tRow: " + (coordsTaken[i][1] + 1) + ", Col: " + (coordsTaken[i][0] + 1));
		textLog.append("\nAsk if anyone needs this information repeated\n");
		textLog.append("Once the game starts, read all text that shows up here");
		textLog.append("Remember not to disclose information shown on your grid!\n");
		textLog.append("Click on the button below to start the game!");
		textLog.display();

		turnEvents = new EventHandler<KeyEvent>() { 
			@Override
			public void handle(KeyEvent e) {
				validMove = true;
				if (turn <= turnCount && !gameOver) {
					if (turn < turnCount) { 
						if (newTurn) {
							if (demon != null)
								demon.move();
							newTurn = false;
							for (int i = 0; i < players.length; i++)
								players[i].turnOver = false;
							turn++;
							textLog.startTurnMessage();
						}
					}
					if (p != -1) {
						if (!players[p].pointer.hasMove) {
							players[p].turnDirection(180);
							players[p].pointer.hasMove = true;
							players[p].thisSquare.revert();
						} else {
							validMove = false;
							if (e.getCode() == KeyCode.UP && players[p].pointer.forward != null)
								players[p].thisSquare.move(0);
							if (e.getCode() == KeyCode.RIGHT && players[p].pointer.right != null)
								players[p].thisSquare.move(1);
							if (e.getCode() == KeyCode.LEFT && players[p].pointer.left != null)
								players[p].thisSquare.move(3);
						}
						if (!treasurePresent) {
							generateTreasure();
							if (!demonPresent)
								generateDemon();
						}
						if (validMove) {
							players[p].turnOver = true;
							if (p == players.length - 1 && turn < turnCount) {
								nextTurn();
							} else if (turn >= turnCount && p == players.length - 1) {
								gameOver = true;
							}
						}
					}
					if (validMove) {
						for (int i = 0; i < players.length; i++) {
							if (!players[i].turnOver) {
								p = i;
								players[p].turnOver = true;
								if (players[p].attacks > 0) {
									textLog.attacked(p + 1, players[p].attacks, players[p].demonAttacks,
											players[p].gold);
									textLog.newLine();
									players[p].attacks = 0;
									if (p == players.length - 1 && turn < turnCount)
										nextTurn();
									else if (turn >= turnCount && p == players.length - 1) {
										gameOver = true;
									}
								} else {
									players[p].thisSquare.select();
									textLog.turnMessage(players[p]);
									break;
								}
							}
						}
					}
				}
				if (gameOver) {
					if (postClicks == 0) {
						Arrays.sort(players, new Comparator<Player>() {
							@Override
							public int compare(Player o1, Player o2) {
								if (o1.gold > o2.gold)
									return -1;
								if (o1.gold < o2.gold)
									return 1;
								return 0;
							}
						});

						textLog.gameOver();
						String[] pos = { "1st", "2nd", "3rd", "4th" };
						placement = new LinkedList<String>();

						int i = 0;
						while (i < players.length) {
							String str = pos[i] + " place with " + players[i].gold + " gold:\t";
							str += "Player " + (players[i].id + 1) + ", ";
							for (int j = i + 1; j < pos.length && j < players.length; j++) {
								if (players[j].gold != players[i].gold)
									break;
								else {
									str += (players[j].id + 1) + ", ";
									++i;
								}
							}
							++i;
							placement.add(str.substring(0, str.length() - 2));
						}
						
					} else if(postClicks == 1) {
						textLog.results();
					}

					else {
						if (!placement.isEmpty())
							textLog.append(placement.remove(placement.size() - 1));
						else
							textLog.append("\n\nTHANK YOU FOR PLAYING!");
					}
					postClicks++;
				}
			}
		};

	}

	void nextTurn() {
		textLog.append("\nPress Any Key to Continue to the Next Turn...");
		textLog.display();
		newTurn = true;
		p = -1;
	}

	void generateDemon() {
		int x;
		int y;
		while (true) {
			x = generateRand(gridSize);
			y = generateRand(gridSize);
			if (squares[x][y].vacantSquare()) {
				demon = new Demon(x, y);
				break;
			}
		}
		textLog.append("A demon has appeared!");
	}

	void generateTreasure() {
		int x;
		int y;
		while (true) {
			x = generateRand(gridSize);
			y = generateRand(gridSize);
			if (squares[x][y].vacantSquare()) {
				squares[x][y].placeTreasureRoom();
				treasurePresent = true;
				break;
			}
		}
		textLog.treasureLocation(x + 1, y + 1);
	}

	void generatePlayer(int player) {
		int x;
		int y;
		while (true) {
			x = generateRand(gridSize);
			y = generateRand(gridSize);
			if (squares[x][y].vacantSquare()) {
				int r = random.nextInt(4);
				players[player].updatePosition(x, y, r);
				players[player].placePlayer();
				break;
			}
		}
	}

	int generateRand(int max) {
		int rand = random.nextInt(max);
		return rand;
	}

	boolean coordsExists(int[][] coords, int x, int y) {
		for (int i = 0; i < coords.length; i++)
			if (coords[i][0] == x && coords[i][1] == y)
				return true;
		return false;
	}

	boolean adjacentCoordsExists(int[][] coords, int x, int y) {
		for (int i = 0; i < coords.length; i++)
			if ((coords[i][0] == x || coords[i][0] == x + 1 || coords[i][0] == x - 1)
					&& (coords[i][1] == y || coords[i][1] == y + 1 || coords[i][1] == y - 1))
				return true;
		return false;
	}

	class GridSquare extends Pane {

		int pId = -1;

		int col;
		int row;
		double size;

		boolean treasure;
		boolean wall;
		boolean adjacentInBoundary[] = new boolean[4];

		boolean demon;

		public GridSquare(int size, int col, int row) {
			this.col = col;
			this.row = row;
			this.size = size;
			setPrefSize(size, size);
			setStyle("-fx-border-color: black; -fx-background-color: white;");
		}

		void select() {
			setStyle("-fx-border-color: black; -fx-background-color: lightblue;");
		}

		void revert() {
			setStyle("-fx-border-color: black; -fx-background-color: white;");
		}

		void move(int direction) {
			validMove = true;

			GridSquare newLocation;
			if (direction == 0)
				newLocation = players[p].pointer.forward;
			else if (direction == 1)
				newLocation = players[p].pointer.right;
			else
				newLocation = players[p].pointer.left;

			if (newLocation != null)
				if (!newLocation.wall) {
					if (newLocation.demon) {
						textLog.demonEncounter();
						demonEncountered = true;
						players[p].turnDirection(direction * 90);
						players[p].turnDirection(180);
					} else {
						if (newLocation.treasure) {
							textLog.treasure(players.length - 1);
							players[p].gold += players.length - 1;
							treasurePresent = false;
							newLocation.treasure = false;
							newLocation.revert();
						}

						if (newLocation.pId != -1) {
							int x = players[newLocation.pId].col;
							int y = players[newLocation.pId].row;

							players[p].thisSquare.getChildren().clear();
							players[p].thisSquare.revert();
							squares[x][y].getChildren().clear();
							squares[x][y].revert();

							if (players[squares[x][y].pId].gold >= 1) {
								players[p].gold++;
								players[squares[x][y].pId].gold--;
							}

							players[squares[x][y].pId].setRotation(players[p].rotation + direction * 90);
							players[squares[x][y].pId].turnDirection(180);
							players[squares[x][y].pId].attacks++;

							textLog.attack(players[squares[x][y].pId].gold);

							players[newLocation.pId].updatePosition(players[p].col, players[p].row);
							players[newLocation.pId].placePlayer();
							players[p].turnDirection(direction * 90);
							players[p].updatePosition(x, y);
							players[p].placePlayer();

						} else {
							players[p].updatePosition(newLocation.col, newLocation.row,
									players[p].rotation + 90 * direction);
							players[p].placePlayer();
							clearContents();
						}
					}
					revert();
				}
		}

		boolean vacantSquare() {
			if (!wall && pId == -1 && !treasure)
				return true;
			return false;
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

		void undoWall() {
			revert();
			wall = false;
		}

		void clearContents() {
			getChildren().clear();
			revert();
			pId = -1;
			demon = false;
		}

		void placeDemon() {
			demonPresent = true;
			demon = true;
			Circle body = addBody(Color.CRIMSON, Color.DARKRED);
			getChildren().add(body);
		}

		void setBoundary(int col, int row) {
			fillBooleanArrayTrue(squares[col][row].adjacentInBoundary);
			if (row == 0)
				squares[col][row].adjacentInBoundary[0] = false;
			if (col == gridSize - 1)
				squares[col][row].adjacentInBoundary[1] = false;
			if (row == gridSize - 1)
				squares[col][row].adjacentInBoundary[2] = false;
			if (col == 0)
				squares[col][row].adjacentInBoundary[3] = false;
		}

		void fillBooleanArrayTrue(boolean arr[]) {
			for (int i = 0; i < arr.length; i++)
				arr[i] = true;
		}

		Circle addBody(Color color, Color stroke) {
			Circle circ = new Circle();
			circ.setRadius(size * .35);
			circ.setCenterX(size / 2);
			circ.setCenterY(size / 2);
			circ.setFill(color);
			circ.setStroke(stroke);
			circ.setStrokeWidth(size / 20);
			return circ;
		}

		void placeTreasureRoom() {
			setStyle("-fx-border-color: black; -fx-background-color: yellow;");
			treasure = true;
		}

		int[] generateVacantCoords(int[] coords) {
			while (true) {
				int x = random.nextInt(5);
				int y = random.nextInt(5);
				if (squares[x][y].pId == -1 && !squares[x][y].treasure && !squares[x][y].wall) {
					coords[0] = x;
					coords[1] = y;
					return coords;
				}
			}
		}
	}

	class Demon {
		int col;
		int row;
		GridSquare thisSquare;

		List<GridSquare> avail;

		public Demon(int col, int row) {
			this.col = col;
			this.row = row;
			thisSquare = squares[col][row];
			thisSquare.placeDemon();
			avail = new LinkedList<GridSquare>();
			calculateAdjacent();
		}

		void updatePosition(int col, int row) {
			this.col = col;
			this.row = row;
			thisSquare = squares[col][row];
			thisSquare.placeDemon();
			avail = new LinkedList<GridSquare>();
			calculateAdjacent();
		}

		void calculateAdjacent() {
			for (int i = 0; i < 4; i++) {
				if (thisSquare.adjacentInBoundary[i]) {
					GridSquare square = squares[col + adjacentIndexConversion[i][0]][row
							+ adjacentIndexConversion[i][1]];
					if (!square.wall)
						avail.add(square);
				}
			}
		}

		void move() {
			int x = random.nextInt(avail.size());

			thisSquare.clearContents();
			if (thisSquare.treasure)
				thisSquare.placeTreasureRoom();
			GridSquare square = avail.get(x);

			if (square.pId != -1) {
				players[square.pId].demonAttacks = true;
				players[square.pId].attacks++;
				if (players[square.pId].gold >= 1)
					players[square.pId].gold--;
				generatePlayer(square.pId);
				square.clearContents();
			}
			updatePosition(square.col, square.row);
		}
	}

	class Player { 

		Player p;
		Pointer pointer;

		Color color;

		Group icon;
		Circle body;
		Polygon arrow;

		int id;
		int col;
		int row;
		int rotation;
		GridSquare thisSquare;

		String pos;

		boolean turnOver;
		int gold;

		int attacks;
		boolean demonAttacks;

		public Player(Color color, int id, int col, int row, int rotation) {
			this.id = id;
			this.col = col;
			this.row = row;
			this.color = color;
			this.rotation = rotation;

			thisSquare = squares[col][row];
			pointer = new Pointer();

			turnOver = false;
			gold = players.length - 1;

			attacks = 0;
			demonAttacks = false;

			pointer.calculatePointer();
			placePlayer();
		}

		void updatePosition(int col, int row, int rotation) {
			this.col = col;
			this.row = row;
			while (rotation >= 360)
				rotation -= 360;

			this.rotation = rotation;
			thisSquare = squares[col][row];
			thisSquare.pId = id;

			pointer.calculatePointer();
		}

		void updatePosition(int col, int row) {
			this.col = col;
			this.row = row;
			thisSquare = squares[col][row];
			thisSquare.pId = id;

			pointer.calculatePointer();
		}

		void setRotation(int rotation) {
			while (rotation >= 360)
				rotation -= 360;
			this.rotation = rotation;
			thisSquare = squares[col][row];
			pointer.calculatePointer();
		}

		void addRotation(int rotation) {
			this.rotation += rotation;
			while (this.rotation >= 360)
				this.rotation -= 360;
			pointer.calculatePointer();
		}

		Circle addBody(Color color) {
			Circle circ = new Circle();
			circ.setRadius(size * .3);
			circ.setCenterX(size / 2);
			circ.setCenterY(size / 2);
			circ.setFill(color);
			circ.setStroke(Color.BLACK);
			return circ;
		}

		Polygon addDirection(Color color) {
			Polygon poly = new Polygon();
			poly.getPoints()
					.addAll(new Double[] { size * .5, size * .075, size * .4, size * .15, size * .6, size * .15 });
			poly.setFill(color);
			poly.setStroke(Color.BLACK);
			return poly;
		}

		void placePlayer() {
			body = addBody(color);
			arrow = addDirection(color);
			icon = new Group(body, arrow);
			pointer.rotate(rotation);
			thisSquare.getChildren().add(icon);
		}

		void turnDirection(int amount) {
			pointer.rotate(amount);
			addRotation(amount);
		}

		class Pointer {

			GridSquare forward;
			GridSquare left;
			GridSquare right;

			boolean hasMove;

			public Pointer() {
				calculatePointer();
			}

			void rotate(int angle) {
				Rotate transformation = new Rotate();
				transformation.setPivotX(size / 2);
				transformation.setPivotY(size / 2);
				transformation.setAngle(angle);
				icon.getTransforms().addAll(transformation);
			}

			void calculatePointer() {
				hasMove = false;
				int direction = Math.abs(rotation / 90);

				while (direction > 3)
					direction -= 4;

				if (thisSquare.adjacentInBoundary[direction]) {
					forward = squares[col + adjacentIndexConversion[direction][0]][row
							+ adjacentIndexConversion[direction][1]];
					if (forward.wall)
						forward = null;
					else
						hasMove = true;
				} else
					forward = null;

				int leftDirection;
				if (direction - 1 < 0)
					leftDirection = 3;
				else
					leftDirection = direction - 1;
				if (thisSquare.adjacentInBoundary[leftDirection]) {
					left = squares[col + adjacentIndexConversion[leftDirection][0]][row
							+ adjacentIndexConversion[leftDirection][1]];
					if (left.wall)
						left = null;
					else
						hasMove = true;
				} else
					left = null;

				int rightDirection;
				if (direction + 1 > 3)
					rightDirection = 0;
				else
					rightDirection = direction + 1;
				if (thisSquare.adjacentInBoundary[rightDirection]) {
					right = squares[col + adjacentIndexConversion[rightDirection][0]][row
							+ adjacentIndexConversion[rightDirection][1]];
					if (right.wall)
						right = null;
					else
						hasMove = true;
				} else
					right = null;
			}
		}
	}
}