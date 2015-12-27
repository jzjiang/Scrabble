import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

/**
 * A Tile class that keeps track of the value, letter, position, and image for a
 * Scrabble Tile. Includes methods to get the letter of a Tile, to move a Tile,
 * to set the position of a Tile, to draw the Tile, to determine if a Tile is
 * blank, to print the Tile as a String, and to get the position and value of
 * the Tile.
 * 
 * @author Jessica Jiang
 * @version 21 January 2015
 */
public class Tile extends Rectangle
{
	private int value;
	private char letter;
	Image image;

	/**
	 * Constructs a Tile object
	 * 
	 * @param position the position of the Tile
	 * @param imageName the file name of the image for the Tile
	 * @param letter the letter of the Tile
	 * @param value the value of the Tile
	 */
	public Tile(Point position, char letter, int value)
	{
		image = new ImageIcon("images\\" + letter + ".png").getImage();
		this.letter = letter;
		this.value = value;
		this.x = position.x;
		this.y = position.y;
		this.width = 40;
		this.height = 40;
	}

	/**
	 * Gets the letter of this Tile
	 * 
	 * @author Jennifer Chan
	 * @return the letter of this Tile
	 */
	public char getLetter()
	{
		return letter;
	}

	/**
	 * Checks if the Tile is a blank or not
	 * 
	 * @return true if the Tile is a blank, false otherwise
	 */
	public boolean isBlank()
	{
		return this.value == 0;
	}

	/**
	 * Sets the letter for blank Tiles
	 * 
	 * @param letter the letter to use
	 */
	public void setLetter(char letter)
	{
		this.letter = letter;
	}

	/**
	 * Moves a Tile by the amount between the initial and final position
	 * 
	 * @param initialPos the initial position to start dragging this Tile
	 * @param initialPos the final position to keep dragging this Tile
	 */
	public void move(Point initialPos, Point finalPos)
	{
		x += finalPos.x - initialPos.x;
		y += finalPos.y - initialPos.y;
	}

	/**
	 * Draws a Tile in a Graphics context
	 * 
	 * @param g Graphics to draw the Tile in
	 */
	public void draw(Graphics g)
	{
		g.drawImage(image, x, y, null);
	}

	/**
	 * Sets the current position of this Tile
	 * 
	 * @param position the Tile's current position
	 */
	public void setPosition(Point position)
	{
		this.x = position.x;
		this.y = position.y;
	}

	/**
	 * Gets the value of this Tile
	 * 
	 * @return the value of this Tile
	 */
	public int getValue()
	{
		return this.value;
	}

	/**
	 * Returns the Tile's current position
	 * 
	 * @return the Tile's current position
	 */
	public Point getPosition()
	{
		return new Point(x, y);
	}

	/**
	 * Prints out the Tile as a String
	 * 
	 * @return the Tile as a String
	 */
	public String toString()
	{
		return String.format("%s%n", letter);
	}
}
