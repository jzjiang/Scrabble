import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * Contains most of the methods needed for a game of Scrabble. Includes
 * MouseListener and MouseMotionListener methods for user input, methods to
 * start a new game, to clear any of the boards, to merge the board containing
 * the player or AI's move with the board with all previous moves, to check if a
 * given point is on the board, to move to the help menu, to make a move (check
 * validity of placement and words created), to calculate the score, to paint
 * the board, and additional methods for AI moves.
 * 
 * @author Jennifer Chan and Jessica Jiang
 * @version 21 January 2015
 */
public class ScrabblePanel extends JPanel implements MouseListener,
		MouseMotionListener
{
	// Constants needed for the game's images and size
	public static final int WIDTH = 900;
	public static final int HEIGHT = 680;
	public static final int SQUARE_SIZE = 40;
	private static final Image MAIN_MENU = new ImageIcon(
			"images\\Main Menu.png").getImage();
	private static final Image[] HELP_IMAGES = new Image[17];
	private static final Image BOARD = new ImageIcon("images\\Board.png")
			.getImage();
	private static final Point PLAYER_RACK_POSITION = new Point(140, 625);
	private static final Point AI_RACK_POSITION = new Point(700, 225);
	private static final Point BAG_POSITION = new Point(100, 100);

	// Variables needed for the game
	private ScrabbleMain parentFrame;
	private Rack playerRack;
	private Rack AIRack;
	private Bag myBag;
	private Tile[][] board;
	private Tile[][] movesBoard; // Keeps track of moves from the current turn
	private int[][] bonuses; // Keeps track of where the bonuses on the board
								// are; DL = 1, TL = 2, DW = -1, TW = -2
	private Tile selectedTile;
	private Point lastPoint;
	private int currentTurn; // -1 = AI, 1 = player
	private HashSet<String> dictionary;
	private int playerLastRackSize;
	private int AILastRackSize;
	private int playerScore;
	private int AIScore;
	private boolean firstMove;
	private int passCount;
	private Statistics stats;
	private String difficulty;
	private int gameState;// Keeps track of which page the user is on (0 - Main
							// menu, 1 - Help, 2 - Game, 3 - End Game)
	private int lastState; // Keeps track of the last state the user was in
							// before entering the help
	private int helpPage; // Keeps track of the help page
	private int AILastScore;
	private int playerLastScore;

	/**
	 * Constructs a ScrabblePanel by setting up the Panel, setting up the help
	 * images, initializing all of the boards, filling in the dictionary, and
	 * initializing the Racks and Bags Also sets up listeners for mouse events
	 * 
	 * @param parentFrame the main Frame that holds this panel
	 */
	public ScrabblePanel(ScrabbleMain parentFrame)
	{
		// Set up the size and background image
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.parentFrame = parentFrame;
		gameState = 0;

		// Add mouse listeners to the Scrabble panel
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		// Fill in the Help Images
		for (int i = 0; i < HELP_IMAGES.length; i++)
		{
			HELP_IMAGES[i] = new ImageIcon("images\\Help" + i + ".png")
					.getImage();
		}

		// Initialize board, bonuses and dictionary
		board = new Tile[15][15];
		movesBoard = new Tile[15][15];
		bonuses = new int[15][15];
		dictionary = new HashSet<String>();
		try
		{
			Scanner sc = new Scanner(new File("board.txt"));
			for (int row = 0; row < bonuses.length; row++)
				for (int col = 0; col < bonuses[row].length; col++)
					bonuses[row][col] = sc.nextInt();

			sc = new Scanner(new File("dictionary.txt"));
			while (sc.hasNext())
				dictionary.add(sc.next().toUpperCase());

			sc.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("BAD FILE NAME");
		}

		// Initialize the Racks
		playerRack = new Rack(PLAYER_RACK_POSITION);
		AIRack = new Rack(AI_RACK_POSITION);

		// Initialize the Bag
		myBag = new Bag(BAG_POSITION);

		// Read in the statistics
		stats = Statistics.readFromFile("statisticsFile.dat");
	}

	/**
	 * Starts a new game of Scrabble
	 */
	public void newGame()
	{
		// Set up the difficulty level for the AI
		String[] difficultyLevels = { "Easy", "Hard" };
		difficulty = (String) JOptionPane.showInputDialog(parentFrame,
				"Select a difficulty: ", "AI Difficulty",
				JOptionPane.QUESTION_MESSAGE, null, difficultyLevels,
				difficultyLevels[0]);

		// Don't start a new game if the user hasn't selected a difficulty
		if (difficulty == null)
			return;

		// Change the game state to in game
		gameState = 2;

		// Let the player go first
		currentTurn = 1;

		// Clear the Racks and Board
		playerRack.clear();
		AIRack.clear();
		clearBoard(board);
		clearBoard(movesBoard);

		// Reset scores
		playerScore = 0;
		AIScore = 0;

		// Shuffle the pieces in the Bag
		myBag.shuffle();

		// Fill the Racks
		playerRack.fillRack(myBag);
		AIRack.fillRack(myBag);

		// No selected piece at the beginning
		selectedTile = null;

		// Reset other variables
		firstMove = true;
		playerLastRackSize = playerRack.tilesLeft();
		AILastRackSize = AIRack.tilesLeft();
		passCount = 0;
		AILastScore = 0;
		playerLastScore = 0;

		// Paint the board
		repaint();
	}

	/**
	 * Clears the given board
	 * 
	 * @author Jennifer Chan
	 * @param thisBoard the given board to clear
	 */
	public void clearBoard(Tile[][] thisBoard)
	{
		for (int row = 0; row < thisBoard.length; row++)
			for (int col = 0; col < thisBoard[row].length; col++)
				thisBoard[row][col] = null;
	}

	/**
	 * Puts the tiles from the movesBoard onto the actual board to keep track of
	 * moves that were made before this turn, and clears the movesBoard
	 * 
	 * @author Jennifer Chan
	 */
	public void mergeBoards()
	{
		for (int row = 0; row < movesBoard.length; row++)
		{
			for (int col = 0; col < movesBoard[0].length; col++)
			{
				if (movesBoard[row][col] != null)
				{
					board[row][col] = movesBoard[row][col];
					movesBoard[row][col] = null;
				}
			}
		}
	}

	/**
	 * Changes the gameState to help and puts it at page one
	 * 
	 * @author Jessica Jiang
	 */
	public void toHelp()
	{
		if (gameState != 1)
			lastState = gameState;
		gameState = 1;
		helpPage = 0;
		repaint();
	}

	/**
	 * Checks to see if the given Point is on the board
	 * 
	 * @author Jessica Jiang
	 * @param point the given Point
	 * @return true if the Point is on the board, false otherwise
	 */
	public boolean isOnBoard(Point point)
	{
		return new Rectangle(30, 18, 600, 600).contains(point);
	}

	/**
	 * Checks if the Tiles placed on the board form a valid move and calculates
	 * the score for the move
	 * 
	 * @return a value > 0 if the Tiles placed form a valid move (this
	 *         represents the score for the move), -1 if no Tiles were placed on
	 *         rack, -2 if the user has not placed a Tile on the star on their
	 *         first move, -3 if Tiles are not placed in the same row or column
	 *         or are not attached, -4 if Tiles placed on board are not attached
	 *         to existing tiles (unless it is the first move), and -5 if the
	 *         word created is invalid (not in dictionary)
	 */
	public int makeMove()
	{
		// Player has not placed any tiles
		if (playerRack.tilesLeft() == playerLastRackSize && currentTurn == 1)
			return -1;

		// Make sure user has placed Tile on red star on first move
		if (firstMove && movesBoard[7][7] == null)
			return -2;

		int direction = -1;
		Tile[][] affectedBoard = new Tile[15][15];
		int startRow = -1;
		int startCol = -1;
		// Find the first Tile the player placed (top left Tile)
		for (int row = 0; row < movesBoard.length && startRow == -1; row++)
			for (int col = 0; col < movesBoard[row].length && startCol == -1; col++)
				if (movesBoard[row][col] != null)
				{
					// Check the direction for the player
					if (currentTurn == 1)
					{
						direction = checkDirection(row, col, playerRack);
						if (direction == -1)
							return -3;
					}
					// Check the direction for the AI
					else
						direction = checkDirection(row, col, AIRack);

					// Find all Tiles affected by the move made
					affectedBoard = findAffected(direction, row, col);
					startRow = row;
					startCol = col;
				}

		// If the affected board and the move made are identical and
		// it is not the first move, that means that the move made is not
		// attached to any existing Tiles
		boolean change = false;
		for (int row = 0; row < board.length; row++)
		{
			for (int col = 0; col < board[row].length; col++)
			{
				if (movesBoard[row][col] != affectedBoard[row][col])
					change = true;
			}
		}

		// User has not made a move connected to any part of the board
		if (!change && !firstMove)
			return -4;

		// Calculate the score
		int score = calculateScore(direction, startRow, startCol, affectedBoard);

		// A word was not found in the dictionary
		if (score == -1)
			return -5;

		// Move is completely valid
		else
		{
			// No longer first move
			firstMove = false;

			// Check for 50 point bonus if all Tiles were used
			if ((currentTurn == 1 && playerRack.tilesLeft() == 0)
					|| (currentTurn == -1 && AIRack.tilesLeft() == 0))
				return score += 50;
			else
				return score;
		}

	}

	/**
	 * Makes the move for the AI.
	 */
	public void AIMove()
	{
		// Update the panel to show that it is the AI's turn
		this.paintImmediately(0, 0, WIDTH, HEIGHT);

		// Set up variables to store the highest scoring word
		String highestWord = null;
		int highestScore = 0;
		int highestRow = -1;
		int highestCol = -1;
		int highestDirection = -1;

		// A boolean array to ensure tiles are only used
		// once
		boolean[] usedTiles = { false, false, false, false, false, false,
				false, false };

		// Find all permutations of the letters on the Rack
		ArrayList<String> words = new ArrayList<String>();
		formWords(usedTiles, "", 0, words);

		// Randomize the list for easy difficulty
		if (difficulty.equals("Easy"))
			Collections.shuffle(words);

		// Special case for first move (need to start in center, and not
		// attached to any existing Tiles
		if (firstMove)
		{
			// Remove words that are not valid first
			for (int i = 0; i < words.size(); i++)
			{
				if (!dictionary.contains(words.get(i)))
				{
					words.remove(i);
					i--;
				}
			}

			// Try placing each word on the board
			for (String nextWord : words)
			{
				placeOnBoard(nextWord, 7, 7, 1);

				// Get the score for this move
				int score = makeMove();

				// Found a valid move
				if (score > highestScore)
				{
					// Place the first first word found for easy mode (random
					// word)
					if (difficulty.equals("Easy"))
					{
						// Reset passCount
						passCount = 0;
						endAITurn(score);
						return;
					}
					// Store the highest-scoring word
					highestScore = score;
					highestWord = nextWord;
					highestRow = 7;
					highestCol = 7;
					highestDirection = 1;
				}

				// Return Tiles to rack
				returnToRack(AIRack);
			}
		}
		// Not first move
		else
		{

			// Go through each Tile on the board
			for (int row = 0; row < board.length; row++)
				for (int col = 0; col < board[row].length; col++)
				{
					// First Tile found
					if (board[row][col] != null)
					{
						// Check top slot
						if (row - 1 >= 0 && board[row - 1][col] == null)
						{
							// Try to place each word on the board
							for (String nextWord : words)
							{
								// Try placing the word vertically first
								int length = nextWord.length();
								int startRow = row - length;
								while (startRow < 0)
									startRow++;

								// Start placing the word
								placeOnBoard(nextWord, startRow, col, 0);

								int score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = startRow;
									highestCol = col;
									highestDirection = 0;
								}

								// Return tiles to rack
								returnToRack(AIRack);

								// Try placing the word horizontally
								placeOnBoard(nextWord, row - 1, col, 1);

								score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = row - 1;
									highestCol = col;
									highestDirection = 1;
								}

								// Return tiles to rack
								returnToRack(AIRack);
							}
						}
						// Check bottom slot
						if (row + 1 < board.length
								&& board[row + 1][col] == null)
						{
							// Place each word on the board
							for (String nextWord : words)
							{
								// Try placing the word horizontally
								placeOnBoard(nextWord, row + 1, col, 1);

								int score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = row + 1;
									highestCol = col;
									highestDirection = 1;
								}

								// Return tiles to rack
								returnToRack(AIRack);
							}
						}
						// Check left slot
						if (col - 1 >= 0 && board[row][col - 1] == null)
						{
							// Place each word on the board
							for (String nextWord : words)
							{
								// Try placing the word horizontally first
								int length = nextWord.length();
								int startCol = col - length;
								while (startCol < 0)
									startCol++;

								// Start placing the word
								placeOnBoard(nextWord, row, startCol, 1);

								int score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = row;
									highestCol = startCol;
									highestDirection = 1;
								}

								// Return tiles to rack
								returnToRack(AIRack);

								// Try placing the word vertically
								placeOnBoard(nextWord, row, col - 1, 0);

								score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = row;
									highestCol = col - 1;
									highestDirection = 0;
								}

								// Return tiles to rack
								returnToRack(AIRack);

							}
						}
						// Check right slot
						if (col + 1 < board[row].length
								&& board[row][col + 1] == null)
						{
							// Place each word on the board
							for (String nextWord : words)
							{
								// Try placing the word vertically
								placeOnBoard(nextWord, row, col + 1, 0);

								int score = makeMove();

								if (score > highestScore)
								{
									// Place the first first word found for easy
									// mode
									if (difficulty.equals("Easy"))
									{
										// Reset passCount
										passCount = 0;
										endAITurn(score);
										return;
									}
									// Store the highest-scoring word
									highestScore = score;
									highestWord = nextWord;
									highestRow = row;
									highestCol = col + 1;
									highestDirection = 0;
								}

								// Return tiles to rack
								returnToRack(AIRack);

							}
						}
					}
				}
		}

		// Place the highestWord on the board
		if (highestScore > 0)
		{
			placeOnBoard(highestWord, highestRow, highestCol, highestDirection);

			// Reset passCount
			passCount = 0;
		}
		// Could not find any valid word
		else
		{
			// Pass
			passCount++;
			JOptionPane.showMessageDialog(parentFrame, "AI has passed.",
					"AI Move", JOptionPane.INFORMATION_MESSAGE);
		}

		// End the AI turn
		endAITurn(highestScore);
	}

	/**
	 * Ends the AI turn by checking for a winner, adding the scores, refilling
	 * the rack, merging the boards and switching to the appropriate player or
	 * game state
	 * 
	 * @param score the score to add to the AI's score
	 */
	private void endAITurn(int score)
	{
		AIScore += score;
		AILastScore = score;
		AIRack.fillRack(myBag);
		AILastRackSize = AIRack.tilesLeft();
		mergeBoards();

		int winner = checkWinner();

		// Winner is either player or AI
		if (winner != 0)
			// Change gameState to end game so the player cannot move anything
			gameState = 3;
		else
			currentTurn *= -1;

	}

	/**
	 * Generates all possible permutations using the AI Rack
	 * 
	 * @author Jessica Jiang
	 * @param usedTiles the Tiles in the Rack that have been used
	 * @param word the current word being spelled
	 * @param length the length of the word currently being spelled
	 * @param words the list of possible permutations
	 */
	public void formWords(boolean usedTiles[], String word, int length,
			ArrayList<String> words)
	{
		// We have used all the tiles in the Rack
		if (length == AIRack.tilesLeft())
			return;

		// Go through each tile in the Rack
		for (int i = 0; i < AIRack.tilesLeft(); i++)
		{
			// If the Tile has not been used yet, then use the Tile
			if (!usedTiles[i])
			{
				// Add the Tile to the current word being built, and add it to
				// the list of possible words
				usedTiles[i] = true;
				StringBuilder newWord = new StringBuilder(word);
				Tile nextTile = AIRack.getTile(i);

				// Make all blank tiles 'E' for simplicity
				if (nextTile.isBlank())
					nextTile.setLetter('E');

				newWord.append(AIRack.getTile(i).getLetter());
				words.add(newWord.toString());

				// Find all other permutations using the rest of the Tiles in
				// the Rack
				formWords(usedTiles, newWord.toString(), length + 1, words);

				// Unmark the tile
				usedTiles[i] = false;
			}
		}
	}

	/**
	 * AI helper method. Places the Tiles of a given word onto the Board
	 * 
	 * @param word the word to place on the board
	 * @param row the row to start placing the word on
	 * @param col the column to start placing the word on
	 * @param direction the direction to place the word in
	 */
	public void placeOnBoard(String word, int row, int col, int direction)
	{
		// If placing the word (not including any existing pieces on the board)
		// will end up going off the board, then exit early
		if ((direction == 0 && row + word.length() > board.length)
				|| (direction == 1 && col + word.length() > board[0].length))
			return;

		// Try placing each letter on the board
		for (int letter = 0; letter < word.length()
				&& (row < board.length && col < board[row].length); letter++)
		{
			// Get the corresponding Tile from the Rack for each letter in the
			// word
			Tile nextTile = AIRack.getTile(word.charAt(letter));

			// Place the Tile on the current spot if there isn't a Tile there
			if (board[row][col] == null && movesBoard[row][col] == null)
			{
				placeOn(nextTile, row, col);
				AIRack.removeTile(nextTile);
			}
			// If there is a tile there, then don't move on the to next letter
			else
				letter--;

			// Increase row or column depending on direction
			if (direction == 1)
				col++;
			else
				row++;
		}
	}

	/**
	 * Places the given Tile on the board at the given row and column
	 * 
	 * @author Jessica Jiang
	 * @param tile the given Tile to place
	 * @param row the row to place the Tile on
	 * @param col the column to place the Tile on Precondition: There is no Tile
	 *            at the given row and column in movesBoard
	 */
	public void placeOn(Tile tile, int row, int col)
	{
		// Put Tile on board
		movesBoard[row][col] = tile;

		// Change position of Tile
		tile.setPosition(new Point(30 + col * SQUARE_SIZE, 18 + row
				* SQUARE_SIZE));
	}

	/**
	 * Checks if the Tiles on the board are placed in a valid order (all in one
	 * row or column) and returns the direction
	 *
	 * @param startRow the row to start checking in
	 * @param startCol the column to start checking in
	 * @param rack the Rack to check
	 * @return -1 if the direction is not valid, 0 if the word is going
	 *         vertical, 1 if the word is horizontal
	 */
	private int checkDirection(int startRow, int startCol, Rack rack)
	{
		int count = rack.tilesLeft() + 1;

		// One piece was placed
		if ((currentTurn == 1 && count == playerLastRackSize)
				|| (currentTurn == -1 && count == AILastRackSize))
		{
			// Check around to see if it forms a direction with pieces already
			// on the board
			if ((startCol + 1 < movesBoard[0].length && board[startRow][startCol + 1] != null)
					|| (startCol - 1 >= 0 && board[startRow][startCol - 1] != null))
				return 1;
			else if ((startRow + 1 < movesBoard.length && board[startRow + 1][startCol] != null)
					|| (startRow - 1 >= 0 && board[startRow - 1][startCol] != null))
				return 0;
		}

		// Left to right
		for (int checkCol = startCol + 1; checkCol < movesBoard[0].length
				&& (board[startRow][checkCol] != null || movesBoard[startRow][checkCol] != null); checkCol++)
		{
			if (movesBoard[startRow][checkCol] != null)
				count++;
		}

		// Check that the number of Tiles in the column is the same as the
		// missing number of Tiles on the Rack (this means that all Tiles are
		// placed in this row)
		if ((currentTurn == 1 && count == playerLastRackSize)
				|| (currentTurn == -1 && count == AILastRackSize))
			return 1;
		else if (count > rack.tilesLeft() + 1)
			return -1;

		// Up to down
		for (int checkRow = startRow + 1; checkRow < movesBoard[0].length
				&& (board[checkRow][startCol] != null || movesBoard[checkRow][startCol] != null); checkRow++)
		{
			if (movesBoard[checkRow][startCol] != null)
				count++;
		}

		// Check that the number of Tiles in the column is the same as the
		// missing number of Tiles on the Rack
		if ((currentTurn == 1 && count == playerLastRackSize)
				|| (currentTurn == -1 && count == AILastRackSize))
			return 0;
		else
			return -1;
	}

	/**
	 * Finds all Tiles affected by the player's move.
	 * 
	 * @author Jessica Jiang
	 * @param direction the direction the Tiles placed on the board are going
	 * @param startRow the given row to check
	 * @param startCol the given column to check
	 * @return a 2D array with the Tiles affected by the player's move
	 */
	private Tile[][] findAffected(int direction, int startRow, int startCol)
	{
		Tile[][] affectedBoard = new Tile[15][15];

		// Add the player's moves
		for (int row = 0; row < movesBoard.length; row++)
		{
			for (int col = 0; col < movesBoard[row].length; col++)
			{
				affectedBoard[row][col] = movesBoard[row][col];
			}
		}

		// Horizontal
		if (direction == 1)
		{

			// Go left
			for (int col = startCol - 1; col >= 0
					&& board[startRow][col] != null; col--)
			{

				affectedBoard[startRow][col] = board[startRow][col];

			}

			// Go right
			for (int col = startCol + 1; col < board[0].length
					&& (board[startRow][col] != null || movesBoard[startRow][col] != null); col++)
			{
				if (board[startRow][col] != null)
					affectedBoard[startRow][col] = board[startRow][col];
			}

			// Vertically check each letter
			for (int col = startCol; col < board[0].length; col++)
			{
				// Only check if the user placed a piece in that column
				if (movesBoard[startRow][col] != null)
				{
					// Check vertically up
					for (int row = startRow - 1; row >= 0
							&& board[row][col] != null; row--)
					{
						affectedBoard[row][col] = board[row][col];
					}

					// Include anything after the letter
					for (int row = startRow + 1; row < board.length
							&& board[row][col] != null; row++)
					{
						affectedBoard[row][col] = board[row][col];
					}
				}
			}
		}
		// Vertical
		else
		{

			// Go up
			for (int row = startRow - 1; row >= 0
					&& board[row][startCol] != null; row--)
			{
				affectedBoard[row][startCol] = board[row][startCol];
			}

			// Go down
			for (int row = startRow + 1; row < board.length
					&& (board[row][startCol] != null || movesBoard[row][startCol] != null); row++)
			{
				if (board[row][startCol] != null)
					affectedBoard[row][startCol] = board[row][startCol];
			}

			// Horizontally check each letter
			for (int row = startRow; row < board.length; row++)
			{
				// Only check if the user placed a piece in that row
				if (movesBoard[row][startCol] != null)
				{
					// Check horizontally left
					for (int col = startCol - 1; col >= 0
							&& board[row][col] != null; col--)
					{
						affectedBoard[row][col] = board[row][col];
					}

					// Include anything after the letter
					for (int col = startCol + 1; col < board.length
							&& board[row][col] != null; col++)
					{
						affectedBoard[row][col] = board[row][col];
					}

				}
			}
		}

		return affectedBoard;
	}

	/**
	 * Finds all words on the affected board and calculates the score for the
	 * move.
	 *
	 * @param direction the direction the Tiles placed on the board are going
	 * @param startRow the given row of the first Tile the user placed
	 * @param startCol the given column of the first Tile the user placed
	 * @param affectedBoard the board with all affected Tiles from the user's
	 *            move
	 * @return -1 if the words created are not in the dictionary, a value > 0 if
	 *         all words are in the dictionary (the total score for the move
	 *         made)
	 */
	private int calculateScore(int direction, int startRow, int startCol,
			Tile[][] affectedBoard)
	{
		int totalScore = 0;
		StringBuilder word = new StringBuilder();
		int wordScore = 0;
		int bonus = 0;

		// Horizontal
		if (direction == 1)
		{
			// Check horizontally once
			// Go to the left of the Tile the user placed
			for (int col = startCol - 1; col >= 0
					&& affectedBoard[startRow][col] != null; col--)
			{
				// Add to the front of the StringBuilder
				word.insert(0, affectedBoard[startRow][col].getLetter());
				wordScore += board[startRow][col].getValue();
			}
			// Go to the right of the Tile the user placed, including the Tile
			// itself
			for (int col = startCol; col < affectedBoard[0].length
					&& affectedBoard[startRow][col] != null; col++)
			{
				// Add to the end of the StringBuilder
				word.append(affectedBoard[startRow][col].getLetter());

				// Calculate the score
				if (movesBoard[startRow][col] != null)
				{
					int bonusValue = calculateBonus(startRow, col);
					if (bonusValue > 0)
						wordScore += bonusValue;
					else
					{
						wordScore += movesBoard[startRow][col].getValue();
						bonus = bonusValue;
					}
				}
				else
					wordScore += board[startRow][col].getValue();
			}

			// Calculate total score
			int currentTotal = addTotalScore(bonus, wordScore, word.toString(),
					totalScore);
			if (currentTotal == -1)
				return -1;
			else if (currentTotal > 0)
				totalScore = currentTotal;

			// Check vertically for each letter
			for (int col = startCol; col < board[0].length; col++)
			{
				// Only check if the user placed a piece in that row
				if (movesBoard[startRow][col] != null)
				{
					word = new StringBuilder();
					bonus = 0;
					wordScore = 0;
					// Calculate the score (since we are currently on a piece
					// that the user placed)
					if (movesBoard[startRow][col] != null)
					{
						int bonusValue = calculateBonus(startRow, col);
						if (bonusValue > 0)
							wordScore += bonusValue;
						else
						{
							wordScore += movesBoard[startRow][col].getValue();
							bonus = bonusValue;
						}
					}
					// Check vertically up
					for (int row = startRow - 1; row >= 0
							&& affectedBoard[row][col] != null; row--)
					{
						// Add to the front of the StringBuilder
						word.insert(0, affectedBoard[row][col].getLetter());
						wordScore += board[row][col].getValue();
					}

					if (word.length() > 0
							|| (startRow + 1 < board.length && affectedBoard[startRow + 1][col] != null))
						// Include the letter the user placed
						word.append(affectedBoard[startRow][col].getLetter());

					// Include anything after the letter
					for (int row = startRow + 1; row < affectedBoard.length
							&& affectedBoard[row][col] != null; row++)
					{
						// Add to the end of the StringBuilder
						word.append(affectedBoard[row][col].getLetter());
						wordScore += board[row][col].getValue();
					}

					// Calculate total score
					currentTotal = addTotalScore(bonus, wordScore,
							word.toString(), totalScore);
					if (currentTotal == -1)
						return -1;
					else if (currentTotal > 0)
						totalScore = currentTotal;
				}
			}

		}
		// Vertical
		else
		{
			// Check vertically once
			// Check anything above the first Tile the user placed
			for (int row = startRow - 1; row >= 0
					&& affectedBoard[row][startCol] != null; row--)
			{
				// Add to the front of the StringBuilder
				word.insert(0, affectedBoard[row][startCol].getLetter());
				wordScore += board[row][startCol].getValue();
			}

			// Check anything below the first Tile the user placed, including
			// the Tile
			for (int row = startRow; row < affectedBoard.length
					&& affectedBoard[row][startCol] != null; row++)
			{
				// Add to the end of the StringBuilder
				word.append(affectedBoard[row][startCol].getLetter());

				// Calculate the score
				if (movesBoard[row][startCol] != null)
				{
					int bonusValue = calculateBonus(row, startCol);
					if (bonusValue > 0)
						wordScore += bonusValue;
					else
					{
						wordScore += movesBoard[row][startCol].getValue();
						bonus = bonusValue;
					}
				}
				else
					wordScore += board[row][startCol].getValue();
			}

			// Calculate total score
			int currentTotal = addTotalScore(bonus, wordScore, word.toString(),
					totalScore);
			if (currentTotal == -1)
				return -1;
			else if (currentTotal > 0)
				totalScore = currentTotal;

			// Check horizontally for each letter
			for (int row = startRow; row < board.length; row++)
			{
				// Only check if the user placed a piece in that row
				if (movesBoard[row][startCol] != null)
				{
					word = new StringBuilder();
					bonus = 0;
					wordScore = 0;
					// Calculate the score (since we are currently on a piece
					// that the user placed)
					if (movesBoard[row][startCol] != null)
					{
						int bonusValue = calculateBonus(row, startCol);
						if (bonusValue > 0)
							wordScore += bonusValue;
						else
						{
							wordScore += movesBoard[row][startCol].getValue();
							bonus = bonusValue;
						}
					}

					// Check horizontally left
					for (int col = startCol - 1; col >= 0
							&& affectedBoard[row][col] != null; col--)
					{
						// Add to the front of the StringBuilder
						word.insert(0, affectedBoard[row][col].getLetter());
						wordScore += board[row][col].getValue();
					}

					if (word.length() > 0
							|| (startCol + 1 < board[0].length && affectedBoard[row][startCol + 1] != null))
						// Include the letter the user placed
						word.append(affectedBoard[row][startCol].getLetter());

					// Include anything after the letter
					for (int col = startCol + 1; col < affectedBoard.length
							&& affectedBoard[row][col] != null; col++)
					{
						// Add to the end of the StringBuilder
						word.append(affectedBoard[row][col].getLetter());
						wordScore += board[row][col].getValue();
					}

					// Calculate total score
					currentTotal = addTotalScore(bonus, wordScore,
							word.toString(), totalScore);
					if (currentTotal == -1)
						return -1;
					else if (currentTotal > 0)
						totalScore = currentTotal;
				}
			}
		}

		// Return the total score for this move
		return totalScore;
	}

	/**
	 * Calculates the amount of bonus points scored for a Tile at the given row
	 * and column
	 * 
	 * @param row the given row
	 * @param col the given column
	 * @return a value > 0 if there is no bonus or if the bonus is a DL or TL
	 *         which represents the total amount of bonus points scored for that
	 *         piece, a value < 0 if the bonus is a DW or TL
	 */
	private int calculateBonus(int row, int col)
	{
		// No bonus
		if (bonuses[row][col] == 0)
			return movesBoard[row][col].getValue();

		// DL
		else if (bonuses[row][col] == 1)
			return movesBoard[row][col].getValue() * 2;

		// TL
		else if (bonuses[row][col] == 2)
			return movesBoard[row][col].getValue() * 3;

		// DW
		else if (bonuses[row][col] == -1)
			return bonuses[row][col];

		// TW
		else
			return bonuses[row][col];
	}

	/**
	 * Adds the double or triple word bonus if applicable, then adds the word's
	 * score to the player's total score
	 * 
	 * @param bonus the double or triple word bonus if applicable
	 * @param wordScore the score of the given word without double/triple
	 *            bonuses
	 * @param currentWord the given word
	 * @param totalScore the total score of the player so far
	 * @return the new total score of the player, -1 if the given word was
	 *         invalid, -2 if if the given word did not contain any letters
	 */
	private int addTotalScore(int bonus, int wordScore, String currentWord,
			int totalScore)
	{
		// Apply DW or TW bonus if applicable
		if (bonus == -1)
			wordScore *= 2;
		else if (bonus == -2)
			wordScore *= 3;

		if (currentWord.length() > 0)
		{
			// Word is not in the dictionary
			if (!dictionary.contains(currentWord.toString()))
			{
				if (currentTurn == 1)
					JOptionPane.showMessageDialog(parentFrame, currentWord
							+ " is not a valid word!", "Invalid Move!",
							JOptionPane.INFORMATION_MESSAGE);
				return -1;
			}
			// Increase the score
			else
				return totalScore + wordScore;
		}
		return -2;
	}

	/**
	 * Returns active Tiles back to the given Rack
	 * 
	 * @param rack the given Rack to return Tiles to
	 */
	public void returnToRack(Rack rack)
	{
		for (int row = 0; row < movesBoard.length; row++)
			for (int col = 0; col < movesBoard[row].length; col++)
			{
				if (movesBoard[row][col] != null)
				{
					rack.addTile(movesBoard[row][col]);
					movesBoard[row][col] = null;
				}
			}
	}

	/**
	 * Checks if the game has ended (4 passes in a row or one player uses all of
	 * their Tiles and the Bag is empty) and then determines the winner if the
	 * game has ended and displays a congratulatory message
	 * 
	 * @author Jennifer Chan
	 * @return 0 if the game has not ended yet, -1 if the AI has won, 1 if the
	 *         player has won or tied
	 */
	private int checkWinner()
	{
		// Four passes in a row or the Bag is empty and one of the Racks is
		// empty
		if (passCount >= 4
				|| (myBag.tilesLeft() == 0 && (AIRack.tilesLeft() == 0 || playerRack
						.tilesLeft() == 0)))
		{
			// Point values of tiles left on a player's rack are deducted from
			// their score and added to opposing player's score
			int playerDeduction = 0;
			for (int i = 0; i < playerRack.tilesLeft(); i++)
				playerDeduction += playerRack.getTile(i).getValue();
			playerScore -= playerDeduction;

			if (AIRack.tilesLeft() == 0)
				AIScore += playerDeduction;

			int AIDeduction = 0;
			for (int i = 0; i < AIRack.tilesLeft(); i++)
				AIDeduction += AIRack.getTile(i).getValue();
			AIScore -= AIDeduction;
			if (playerRack.tilesLeft() == 0)
				playerScore += AIDeduction;

			// Make sure the score is not negative
			if (playerScore < 0)
				playerScore = 0;

			// Show the final score
			this.paintImmediately(0, 0, WIDTH, HEIGHT);

			// Player wins if their score is higher than AI
			if (playerScore > AIScore)
			{
				JOptionPane
						.showMessageDialog(
								parentFrame,
								"Congratulations, you won!\nPress New Game in the Game Menu to play again!",
								"Game Over", JOptionPane.INFORMATION_MESSAGE);
				updateHighScore();
				return 1;
			}
			// AI wins
			else if (playerScore < AIScore)
			{
				JOptionPane
						.showMessageDialog(
								parentFrame,
								"AI won!\nPress New Game in the Game Menu to play again!",
								"Game Over", JOptionPane.INFORMATION_MESSAGE);
				updateHighScore();
				return -1;
			}
			// Tie
			else if (AIScore == playerScore)
			{
				JOptionPane
						.showMessageDialog(
								parentFrame,
								"You tied!\nPress New Game in the Game Menu to play again!",
								"Game Over", JOptionPane.INFORMATION_MESSAGE);
				updateHighScore();
				return 1;
			}
		}

		// Game not over
		return 0;
	}

	/**
	 * Displays a prompt for the player to enter their name and updates the new
	 * high score if the player has gotten a new high score
	 * 
	 * @author Jessica Jiang
	 */
	public void updateHighScore()
	{
		if (stats.isHigher(playerScore))
		{
			String playerName = JOptionPane
					.showInputDialog(
							parentFrame,
							"Congratulations! You have gotten a new high score!\nPlease enter your name: ",
							"New High Score!", JOptionPane.INFORMATION_MESSAGE);
			stats.changeTopPlayer(playerName, playerScore);
		}
	}

	/**
	 * A method that returns the Statistics object for this ScrabblePanel
	 * 
	 * @return the Statistics object
	 */
	public Statistics getStats()
	{
		return stats;
	}

	/**
	 * Draws the information in this ScrabblePanel. Draws the board, menus and
	 * Tiles for this ScrabblePanel.
	 * 
	 * @param g the Graphics context to do the drawing
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// Draw the main menu
		if (gameState == 0)
			g.drawImage(MAIN_MENU, 0, 0, this);
		// Draw the help images
		else if (gameState == 1)
		{
			g.drawImage(HELP_IMAGES[helpPage], 0, 0, this);
		}
		// Draw the game
		else if (gameState == 2 || gameState == 3)
		{
			g.drawImage(BOARD, 0, 0, this);

			// Write whose turn it is
			g.setFont(new Font("Arial", Font.BOLD, 16));

			// Only print whose turn it is if the user is still in game
			if (gameState == 2)
			{
				g.setColor(Color.RED);
				if (currentTurn == 1)
				{
					g.drawString("Your turn", 672, 180);
				}
				else
				{
					g.drawString("AI is thinking...", 672, 275);
				}
				g.setColor(Color.BLACK);
			}

			// Print out AI difficulty level
			g.drawString("(" + difficulty + ")", 710, 240);

			// Write the last points gained by the Player and AI
			g.drawString("Points Gained by Player: " + playerLastScore, 660, 60);
			g.drawString("Points Gained by AI:         " + AILastScore, 660, 85);

			// Draw scores
			g.setFont(new Font("Arial", Font.BOLD, 24));
			g.setColor(Color.BLACK);
			g.drawString("" + playerScore, 820, 150);
			g.drawString("" + AIScore, 820, 245);

			// Display number of Tiles left in Bag
			g.setFont(new Font("Arial", Font.BOLD, 20));
			g.setColor(Color.WHITE);
			g.drawString("Tiles Left in Bag: " + myBag.tilesLeft(), 700, 670);

			// Draw the board with current pieces.
			for (int row = 0; row < 15; row++)
				for (int column = 0; column < 15; column++)
				{
					// Draw the Tiles
					if (board[row][column] != null)
					{
						board[row][column].draw(g);

						// Draw letter for blank tile
						if (board[row][column].isBlank())
						{
							g.setFont(new Font("Berlin Sans FB", Font.PLAIN, 26));
							g.setColor(Color.BLACK);
							g.drawString("" + board[row][column].getLetter(),
									board[row][column].getPosition().x + 12,
									board[row][column].getPosition().y + 30);
						}
					}
					if (movesBoard[row][column] != null)
					{
						movesBoard[row][column].draw(g);

						// Draw letter for blank tile
						if (movesBoard[row][column].isBlank())
						{
							g.setFont(new Font("Berlin Sans FB", Font.PLAIN, 26));
							g.setColor(Color.BLACK);
							g.drawString(
									"" + movesBoard[row][column].getLetter(),
									movesBoard[row][column].getPosition().x + 12,
									movesBoard[row][column].getPosition().y + 30);
						}
					}
				}

			// Draw the player's Rack
			playerRack.draw(g);

			// Draw selected Tile on top
			if (selectedTile != null)
				selectedTile.draw(g);
		}
	}

	/**
	 * Handles the mouse dragged events to drag Tiles
	 * 
	 * @author Jessica Jiang
	 * @param event event information for mouse dragged
	 */
	public void mouseDragged(MouseEvent event)
	{
		Point currentPoint = event.getPoint();

		// Moves the tile
		if (selectedTile != null)
		{
			selectedTile.move(lastPoint, currentPoint);
			lastPoint = currentPoint;
			repaint();
		}

	}

	/**
	 * Handles the mouse moved events to show which items can be clicked
	 * 
	 * @author Jessica Jiang
	 * @param event event information for mouse moved
	 */
	public void mouseMoved(MouseEvent event)
	{
		Point currentPoint = event.getPoint();

		// User is hovering over play or help
		if (gameState == 0)
		{
			if ((currentPoint.x >= 326 && currentPoint.x <= 573
					&& currentPoint.y >= 360 && currentPoint.y <= 458)
					|| (currentPoint.x >= 326 && currentPoint.x <= 573
							&& currentPoint.y >= 487 && currentPoint.y <= 586))
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
		}
		// User is in help
		else if (gameState == 1)
		{
			// User hovering over 'x'
			if (currentPoint.x >= 748 && currentPoint.x <= 778
					&& currentPoint.y >= 71 && currentPoint.y <= 101)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}

			// User hovering over links
			else if (helpPage == 0
					&& ((currentPoint.x >= 123 && currentPoint.x <= 274
							&& currentPoint.y >= 206 && currentPoint.y <= 244)
							|| (currentPoint.x >= 123 && currentPoint.x <= 391
									&& currentPoint.y >= 256 && currentPoint.y <= 294)
							|| (currentPoint.x >= 123 && currentPoint.x <= 631
									&& currentPoint.y >= 304 && currentPoint.y <= 342)
							|| (currentPoint.x >= 123 && currentPoint.x <= 420
									&& currentPoint.y >= 350 && currentPoint.y <= 388) || (currentPoint.x >= 123
							&& currentPoint.x <= 616 && currentPoint.y >= 396 && currentPoint.y <= 434)))
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
			// User hovers over next on page 0 - 15
			if (helpPage >= 0 && helpPage <= 15)
			{
				// User hovers over next
				if (currentPoint.x >= 679 && currentPoint.x <= 766
						&& currentPoint.y >= 550 && currentPoint.y <= 585)
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					return;
				}
			}
			// User hovers over back on page 1 - 16
			if (helpPage >= 1 && helpPage <= 16)
			{
				// User hovers over back
				if (currentPoint.x >= 139 && currentPoint.x <= 225
						&& currentPoint.y >= 550 && currentPoint.y <= 585)
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					return;
				}
			}
		}
		// User in game
		else if (gameState == 2)
		{
			// It is the user's turn
			if (currentTurn == 1)
			{
				// User hovering over Exchange, Shuffle, Return, Dictionary,
				// Play or Pass
				if ((currentPoint.x >= 677 && currentPoint.x <= 752
						&& currentPoint.y >= 338 && currentPoint.y <= 413)
						|| (currentPoint.x >= 775 && currentPoint.x <= 850
								&& currentPoint.y >= 338 && currentPoint.y <= 413)
						|| (currentPoint.x >= 677 && currentPoint.x <= 752
								&& currentPoint.y >= 438 && currentPoint.y <= 513)
						|| (currentPoint.x >= 775 && currentPoint.x <= 850
								&& currentPoint.y >= 438 && currentPoint.y <= 513)
						|| (currentPoint.x >= 677 && currentPoint.x <= 752
								&& currentPoint.y >= 538 && currentPoint.y <= 613)
						|| (currentPoint.x >= 775 && currentPoint.x <= 850
								&& currentPoint.y >= 538 && currentPoint.y <= 613))
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					return;
				}

				// The user is hovering over a Tile in their Rack or on the
				// movedBoard
				// User presses rack
				else if (playerRack.contains(currentPoint)
						&& playerRack.pickUp(currentPoint) != null)
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					return;
				}
				// User is on board
				else if (isOnBoard(currentPoint))
				{
					for (int row = 0; row < movesBoard.length; row++)
						for (int col = 0; col < movesBoard[0].length; col++)
						{
							if (movesBoard[row][col] != null)
							{
								if (movesBoard[row][col].contains(currentPoint))
								{
									setCursor(Cursor
											.getPredefinedCursor(Cursor.HAND_CURSOR));
									return;
								}
							}
						}
				}
			}
		}

		// Otherwise just use the default cursor
		setCursor(Cursor.getDefaultCursor());

	}

	@Override
	public void mouseClicked(MouseEvent event)
	{

	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Handles the mouse pressed events to open menus, move Tiles and press
	 * buttons
	 * 
	 * @param event event information for mouse pressed
	 */
	public void mousePressed(MouseEvent event)
	{
		Point selectedPoint = event.getPoint();

		// User is on main menu
		if (gameState == 0)
		{
			// User clicks play
			if (selectedPoint.x >= 326 && selectedPoint.x <= 573
					&& selectedPoint.y >= 360 && selectedPoint.y <= 458)
			{
				// Start a new game
				newGame();
			}
			// User clicks help
			else if (selectedPoint.x >= 326 && selectedPoint.x <= 573
					&& selectedPoint.y >= 487 && selectedPoint.y <= 586)
			{
				// Move to help page
				toHelp();
			}
		}
		// User is on help menus
		else if (gameState == 1)
		{
			// User presses close
			if (selectedPoint.x >= 748 && selectedPoint.x <= 778
					&& selectedPoint.y >= 71 && selectedPoint.y <= 101)
			{
				gameState = lastState;
			}

			// Create links to jump to certain sections
			else if (helpPage == 0)
			{
				if (selectedPoint.x >= 123 && selectedPoint.x <= 274
						&& selectedPoint.y >= 206 && selectedPoint.y <= 244)
					helpPage = 1;
				else if (selectedPoint.x >= 123 && selectedPoint.x <= 391
						&& selectedPoint.y >= 256 && selectedPoint.y <= 294)
					helpPage = 2;
				else if (selectedPoint.x >= 123 && selectedPoint.x <= 631
						&& selectedPoint.y >= 304 && selectedPoint.y <= 342)
					helpPage = 3;
				else if (selectedPoint.x >= 123 && selectedPoint.x <= 420
						&& selectedPoint.y >= 350 && selectedPoint.y <= 388)
					helpPage = 8;
				else if (selectedPoint.x >= 123 && selectedPoint.x <= 616
						&& selectedPoint.y >= 396 && selectedPoint.y <= 434)
					helpPage = 11;
			}

			// User clicks next on page 0 - 15
			if (helpPage >= 0 && helpPage <= 15)
			{
				// User clicks next
				if (selectedPoint.x >= 679 && selectedPoint.x <= 766
						&& selectedPoint.y >= 550 && selectedPoint.y <= 585)
				{
					helpPage++;
				}
			}
			// User clicks back on page 1 - 16
			if (helpPage >= 1 && helpPage <= 16)
			{
				// User clicks back
				if (selectedPoint.x >= 139 && selectedPoint.x <= 225
						&& selectedPoint.y >= 550 && selectedPoint.y <= 585)
				{
					helpPage--;
				}
			}
		}
		// User is in game
		else if (gameState == 2)
		{

			if (selectedTile != null)
				return;

			// User presses rack
			if (playerRack.contains(selectedPoint))
			{
				lastPoint = selectedPoint;
				selectedTile = playerRack.pickUp(selectedPoint);
				if (selectedTile != null)
					playerRack.removeTile(selectedTile);
			}
			// User is on board
			else if (isOnBoard(selectedPoint))
			{
				for (int row = 0; row < movesBoard.length; row++)
					for (int col = 0; col < movesBoard[0].length; col++)
					{
						if (movesBoard[row][col] != null)
						{
							Tile nextTile = movesBoard[row][col];
							if (nextTile.contains(selectedPoint))
							{
								lastPoint = selectedPoint;
								selectedTile = nextTile;
								movesBoard[row][col] = null;
							}
						}

					}
			}

			// User presses exchange button
			else if (selectedPoint.x >= 677 && selectedPoint.x <= 752
					&& selectedPoint.y >= 338 && selectedPoint.y <= 413)
			{
				// Exchange tile
				if (myBag.tilesLeft() >= 7)
				{
					// Create a panel with check boxes
					JPanel panel = new JPanel();
					Border lowerEtched = BorderFactory
							.createEtchedBorder(EtchedBorder.RAISED);

					panel.setBorder(BorderFactory.createTitledBorder(
							lowerEtched, "What do you want to exchange?"));
					int noOfTiles = playerRack.tilesLeft();
					int noOfColumns = (int) Math.sqrt(noOfTiles);
					panel.setLayout(new GridLayout(noOfTiles / noOfColumns,
							noOfColumns));

					// Create a group of check boxes to add to the Panel
					JCheckBox[] buttonList = new JCheckBox[noOfTiles];

					// Create and add each check box button to the panel
					for (int index = 0; index < buttonList.length; index++)
					{
						if (playerRack.getTile(index).isBlank())
							buttonList[index] = new JCheckBox("Blank");
						else
							buttonList[index] = new JCheckBox(""
									+ playerRack.getTile(index).getLetter());
						panel.add(buttonList[index]);
					}

					// Show a dialog with the panel attached
					int choice = JOptionPane.showConfirmDialog(this, panel,
							"Exchange", JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.DEFAULT_OPTION);

					// Update tiles if OK is selected
					if (choice == JOptionPane.OK_OPTION)
					{
						boolean selected = false;

						// Start from the last index so that when Tiles are
						// removed and added, it will not affect indexes of
						// Tiles before it
						for (int index = buttonList.length - 1; index >= 0; index--)
						{
							if (buttonList[index].isSelected())
							{
								Tile remove = playerRack.getTile(index);
								Tile add = myBag.exchange(remove, playerRack);
								playerRack.removeTile(remove);
								playerRack.addTile(add);
								selected = true;
							}
						}

						// Change turn
						if (selected)
						{
							// Return pieces to Rack
							returnToRack(playerRack);

							// Reset the passCount
							passCount = 0;

							playerLastScore = 0;

							currentTurn *= -1;
							AIMove();

						}
					}

				}
				else
				{
					JOptionPane
							.showMessageDialog(
									parentFrame,
									"Cannot exchange tiles when there are less than 7 tiles in the bag!",
									"Exchange", JOptionPane.INFORMATION_MESSAGE);
				}

			}

			// User presses shuffle button
			else if (selectedPoint.x >= 775 && selectedPoint.x <= 850
					&& selectedPoint.y >= 338 && selectedPoint.y <= 413)
			{
				playerRack.shuffle();
			}

			// User presses return button
			else if (selectedPoint.x >= 677 && selectedPoint.x <= 752
					&& selectedPoint.y >= 438 && selectedPoint.y <= 513)
			{
				returnToRack(playerRack);
			}

			// User presses dictionary button
			else if (selectedPoint.x >= 775 && selectedPoint.x <= 850
					&& selectedPoint.y >= 438 && selectedPoint.y <= 513)
			{
				// Get the word the user wants to look up
				String searchWord = JOptionPane.showInputDialog(parentFrame,
						"Please enter the word you would like to search up: ",
						"Dictionary", JOptionPane.INFORMATION_MESSAGE);

				while (searchWord != null)
				{
					searchWord = searchWord.toUpperCase();
					// If it is a valid word, return a message saying so
					if (dictionary.contains(searchWord))
					{
						JOptionPane.showMessageDialog(parentFrame, searchWord
								+ " is a valid word.", "Dictionary",
								JOptionPane.INFORMATION_MESSAGE);
					}
					// If it is not a valid word, return a message saying so
					else if (searchWord.length() > 0)
					{
						JOptionPane.showMessageDialog(parentFrame, searchWord
								+ " is not a valid word.", "Dictionary",
								JOptionPane.INFORMATION_MESSAGE);
					}
					// If nothing was entered in, return a message saying so
					else
					{
						JOptionPane.showMessageDialog(parentFrame,
								"You did not enter a word.", "Dictionary",
								JOptionPane.INFORMATION_MESSAGE);
					}

					// Prompt player to enter another word to look up
					searchWord = JOptionPane
							.showInputDialog(
									parentFrame,
									"Please enter the word you would like to search up: ",
									"Dictionary",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}

			// User presses play button
			else if (selectedPoint.x >= 677 && selectedPoint.x <= 752
					&& selectedPoint.y >= 538 && selectedPoint.y <= 613)
			{
				// Checks if the move is valid
				int score = makeMove();

				if (score > 0)
				{
					// Reset the passCount
					passCount = 0;

					// Refill the player's Rack
					playerRack.fillRack(myBag);
					playerLastRackSize = playerRack.tilesLeft();

					// Merge boards
					mergeBoards();

					// Calculate score
					playerScore += score;
					playerLastScore = score;
					
					// Check for the winner
					int winner = checkWinner();
					if (winner != 0)
						gameState = 3;
					else
					{
						currentTurn *= -1;
						AIMove();
					}



				

				}
				else if (score == -1)
				{
					// No move
					JOptionPane.showMessageDialog(parentFrame,
							"Please place at least one (1) tile on the board.",
							"Invalid Move!", JOptionPane.INFORMATION_MESSAGE);
				}
				else if (score == -2)
				{
					// First move not on center
					JOptionPane
							.showMessageDialog(
									parentFrame,
									"Please place a tile on the centre of the board (the star).",
									"Invalid Move!",
									JOptionPane.INFORMATION_MESSAGE);
				}
				else if (score == -3)
				{
					// Tiles placed on board are not in the same row or
					// column
					JOptionPane
							.showMessageDialog(
									parentFrame,
									"The tiles just placed on the board are not in the same row or column or are not attached.",
									"Invalid Move!",
									JOptionPane.INFORMATION_MESSAGE);
				}
				else if (score == -4)
				{
					// Tiles placed on board are not attached to existing
					// tiles
					JOptionPane
							.showMessageDialog(
									parentFrame,
									"The tiles just placed on the board are not attached to existing tiles.",
									"Invalid Move!",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}

			// User presses pass button
			else if (selectedPoint.x >= 776 && selectedPoint.x <= 851
					&& selectedPoint.y >= 538 && selectedPoint.y <= 613)
			{
				// The confirm dialog can be used with an if to check
				// which button was selected on the dialog box

				if ((passCount < 2 && JOptionPane.showConfirmDialog(
						parentFrame, "Are you sure you want to pass?", "Pass",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						|| (passCount >= 2 && JOptionPane
								.showConfirmDialog(
										parentFrame,
										"Are you sure you want to pass?\nBoth players each passing twice in a row will end the game",
										"Pass", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION))
				{
					passCount++;
					// Return pieces to Rack
					returnToRack(playerRack);
					// Change turn
					currentTurn *= -1;

					playerLastScore = 0;

					AIMove();
				}

			}
		}
		repaint();
	}

	/**
	 * Handles the mouse released events to drop a Tile
	 * 
	 * @author Jessica Jiang
	 * @param event event information for mouse released
	 */
	public void mouseReleased(MouseEvent event)
	{
		if (selectedTile != null && gameState == 2)
		{
			// Convert mouse-released location to board row and column
			Point releasedPoint = event.getPoint();

			int col = (int) ((double) (releasedPoint.x - 30) / SQUARE_SIZE);
			int row = (int) ((double) (releasedPoint.y - 18) / SQUARE_SIZE);

			if (isOnBoard(releasedPoint) && board[row][col] == null
					&& movesBoard[row][col] == null)
			{
				// Blank tile
				if (selectedTile.isBlank())
				{
					// Get the letter for the blank tile
					String letter = JOptionPane.showInputDialog(parentFrame,
							"Please enter the letter you would like to use: ",
							"Blank Tile", JOptionPane.INFORMATION_MESSAGE);
					while (letter != null
							&& (letter.length() != 1 || !Character
									.isLetter(letter.charAt(0))))
					{
						// Entered more than one character
						letter = JOptionPane
								.showInputDialog(
										parentFrame,
										"Please enter one letter (A to Z). Please try again: ",
										"Blank Tile",
										JOptionPane.INFORMATION_MESSAGE);
					}

					// Return to rack if player clicks cancel or enter without
					// entering a letter
					if (letter == null)
					{
						playerRack.addTile(selectedTile);
						selectedTile = null;
						repaint();
						return;
					}

					letter = letter.toUpperCase();

					selectedTile.setLetter(letter.charAt(0));
				}

				placeOn(selectedTile, row, col);
				selectedTile = null;
			}

			// Return to Rack if not a valid move
			else
			{
				playerRack.addTile(selectedTile);
				selectedTile = null;
			}
			repaint();

		}
	}
}
