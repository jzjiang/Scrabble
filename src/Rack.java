import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * A Rack Object. Includes methods to construct a new Rack, get a Tile from the
 * Rack from a given index or letter for the Tile to match, shuffle the Tiles in
 * the Rack, find how many Tiles are remaining in the Rack, add a Tile to the
 * Rack, remove a Tile with a given index or given Tile to remove, see if a
 * Point is contained in the Rack, get the Position of the Rack, pick up a Tile
 * from the rack, fill the Rack with Tiles, clear the Rack, draw the Rack, and
 * return a String representation of the Rack.
 * 
 * @author Jennifer Chan
 * @version 25 December 2014 merry christmas ya filthy animal
 *
 */
public class Rack
{
	private ArrayList<Tile> myRack;
	private Point position;
	private Point firstTilePos;
	private static int SPACING = 50;
	private static Image RACK_IMAGE = new ImageIcon("images\\Rack.png")
			.getImage();

	/**
	 * Constructs a new empty Rack object at the given position
	 * 
	 * @param position the given position for the Rack
	 */
	public Rack(Point position)
	{
		myRack = new ArrayList<Tile>(7);
		this.position = position;
		this.firstTilePos = new Point(position.x + 20, position.y - 3);

	}

	/**
	 * Finds the Tile in the given index of the Rack
	 * 
	 * @author Jessica Jiang
	 * @param index the index of the Tile to find
	 * @return the Tile in the given index of the Rack
	 */
	public Tile getTile(int index)
	{
		return myRack.get(index);
	}

	/**
	 * Finds the Tile with the given letter if it is in the player's Rack
	 * 
	 * @param letter the given letter of the Tile to find
	 * @return the Tile with the given letter from this Rack
	 */
	public Tile getTile(char letter)
	{
		// Find the Tile with the given letter and return it if found
		for (Tile nextTile : myRack)
			if (nextTile.getLetter() == letter)
				return nextTile;

		// Otherwise return null
		return null;
	}

	/**
	 * Shuffles the Tiles in the Rack using the Fisher-Yates shuffle
	 */
	public void shuffle()
	{
		// Racks with 1 or less Tiles cannot be shuffled
		if (myRack.size() > 1)
		{
			for (int currentTile = 0; currentTile < myRack.size(); currentTile++)
			{
				// Find a random Tile between the current Tile and the last Tile
				// in the rack
				int switchIndex = (int) (Math.random() * (myRack.size() - currentTile))
						+ currentTile;

				// Switch the current Tile and the random Tile
				Tile intermediate = myRack.get(switchIndex);
				myRack.set(switchIndex, myRack.get(currentTile));
				myRack.set(currentTile, intermediate);
			}

			// Update the position of the new first Tile
			myRack.get(0).setPosition(firstTilePos);

			// Update the positions of the rest of the Tiles
			for (int tileNo = 1; tileNo < myRack.size(); tileNo++)
				myRack.get(tileNo).setPosition(
						new Point(firstTilePos.x + SPACING * tileNo,
								firstTilePos.y));
		}
	}

	/**
	 * Finds the number of tiles in the rack
	 * 
	 * @return the number of tiles in the rack
	 */
	public int tilesLeft()
	{
		return myRack.size();
	}

	/**
	 * Adds the given Tile to the rack
	 * 
	 * @param tile the Tile to add
	 */
	public void addTile(Tile tile)
	{
		myRack.add(tile);

		// Update the position of the new Tile
		if (myRack.size() == 1)
			tile.setPosition(firstTilePos);
		else
			tile.setPosition(new Point(firstTilePos.x + SPACING
					* (myRack.size() - 1), firstTilePos.y));

	}

	/**
	 * Removes the given Tile from the rack
	 * 
	 * @author Jennifer Chan and Jessica Jiang
	 * @param tile the Tile to remove
	 */
	public void removeTile(Tile tile)
	{
		// Find the index of the given Tile
		int index = myRack.indexOf(tile);

		// Shift the subsequent Tiles left, and update
		// their positions
		for (int i = index + 1; i < myRack.size(); i++)
		{
			Point initialPos = myRack.get(i).getPosition();
			myRack.get(i).setPosition(
					new Point(initialPos.x - SPACING, initialPos.y));
		}

		// Remove given Tile
		myRack.remove(tile);

	}

	/**
	 * Removes the Tile at the given index
	 * 
	 * @author Jennifer Chan and Jessica Jiang
	 * @param index the given index of the Tile to remove
	 * @return the removed Tile
	 */
	public Tile removeTile(int index)
	{
		// Shift the subsequent Tiles left, and update
		// their positions
		for (int i = index + 1; i < myRack.size(); i++)
		{
			Point initialPos = myRack.get(i).getPosition();
			myRack.get(i).setPosition(
					new Point(initialPos.x - SPACING, initialPos.y));
		}

		// Remove given Tile
		return myRack.remove(index);

	}

	/**
	 * Checks to see if the given point is contained within this Rack
	 * 
	 * @param point the Point to check
	 * @return true if the point is in this Rack, false if not
	 */
	public boolean contains(Point point)
	{
		return (new Rectangle(position.x, position.y, 380, 50)).contains(point);
	}

	/**
	 * Finds the Rack's position
	 * 
	 * @return the Rack's position
	 */
	public Point getPosition()
	{
		return position;
	}

	/**
	 * Based on the point of selection, returns the Tile that can be picked up
	 * 
	 * @author Jessica Jiang
	 * @param point the Point of the Rack that is selected Precondition: the
	 *            point is contained in this Rack
	 * @return the Tile based on the point of selection
	 */
	public Tile pickUp(Point point)
	{
		// Find which Tile the mouse is on
		for (Tile nextTile : myRack)
		{
			if (nextTile.contains(point))
				// Return the Tile that the mouse is on
				return nextTile;
		}

		// Return nothing if the mouse is not on a Tile
		return null;
	}

	/**
	 * Fills the rack so it contains 7 Tiles
	 */
	public void fillRack(Bag myBag)
	{
		while (myRack.size() < 7 && myBag.tilesLeft() >= 1)
			this.addTile(myBag.dealTile());
	}

	/**
	 * Clears the Rack
	 */
	public void clear()
	{
		myRack.clear();
	}

	/**
	 * Returns a String representation of the Tiles on this Rack
	 * @return a String representation of the Tiles on this Rack
	 */
	public String toString()
	{
		StringBuilder rackStr = new StringBuilder(myRack.size() * 2);
		for (int currentTile = 0; currentTile < myRack.size(); currentTile++)
		{
			rackStr.append(myRack.get(currentTile));
			rackStr.append("\n");
		}
		return rackStr.toString();
	}

	/**
	 * Draws the rack and the tiles on the rack
	 * 
	 * @param g Graphics to draw the Rack in
	 */
	void draw(Graphics g)
	{
		// Draw the actual rack
		g.drawImage(RACK_IMAGE, position.x, position.y, null);

		// Draw the tiles on the rack
		for (Tile nextTile : myRack)
			nextTile.draw(g);
	}

}
