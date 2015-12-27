import java.awt.Point;

/**
 * A Bag object. Includes methods to construct a new Bag, shuffle
 * the Tiles in the Bag, deal a Tile, exchange Tiles, find how many undealt
 * Tiles are left in the Bag, and return a String representation of the Tiles in
 * the Bag.
 * 
 * @author Jennifer Chan
 * @version 21 January 2015
 * 
 */
public class Bag
{

	private Tile[] bag;
	private int topTile;
	private Point position;
	
	// 0-25 index corresponds with A-Z, 26 is a blank tile, stores the amount of points for each letter
	// tile
	private static int[] VALUES = { 1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1,
			1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10, 0 };
	private static int[] NO_OF_EACH_TILE = { 9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1,
		4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1 , 2};

	/**
	 * Constructs a new Bag with all 100 Scrabble Tiles in it and sets the top
	 * Tile as the last Tile in the array
	 */
	public Bag(Point position)
	{
		this.bag = new Tile[100];
		int index = 0;
		this.position = position;
		
		// Put the 100 tiles in the bag
		for (int letter = 0; letter < 27; letter++)
		{
			for (int tileNo = 0; tileNo < NO_OF_EACH_TILE[letter]; tileNo++)
			{
				bag[index] = new Tile(position, (char) ('A' + letter),
						VALUES[letter]);
				index++;
			}
		}
		
		this.topTile = 99;
	}

	/**
	 * Shuffles the Tiles in the Bag using the Fisher-Yates shuffle, makes sure
	 * all Tiles go back in the Bag, and then resets the top Tile to the last
	 * Tile in the array
	 */
	public void shuffle()
	{
		for (int currentTile = 0; currentTile < bag.length; currentTile++)
		{
			// Find a random tile between the current tile and the last tile in
			// the bag
			int switchIndex = (int) (Math.random() * (bag.length - currentTile))
					+ currentTile;

			// Switch the current tile and the random tile
			Tile intermediate = bag[switchIndex];
			bag[switchIndex] = bag[currentTile];
			bag[currentTile] = intermediate;

			// Make sure all tiles are in the bag
			if (bag[currentTile].getPosition() != position)
				bag[currentTile].setPosition(position);
		}

		// Reset the top tile
		topTile = bag.length - 1;
	}

	/**
	 * Deals out a Tile from the Bag
	 * 
	 * @return the Tile that is dealt from the Bag, or null if the Bag is empty
	 */
	public Tile dealTile()
	{
		// Deal the top tile
		if (topTile >= 0)
		{
			topTile--;
			return bag[topTile + 1];
		}

		// If the bag is empty, return null
		return null;

	}

	/**
	 * Exchanges the given Tile with a random Tile in the Bag
	 * 
	 * @param myTile the given Tile to exchange
	 */
	public Tile exchange(Tile myTile, Rack myRack)
	{
		int foundTile = -1;

		// Find the tile in the Bag array
		for (int i = bag.length - 1; i > topTile && foundTile == -1; i--)
		{
			// If the Tile in the Bag is at the same position as the Tile in the
			// Rack, then we have found the same Tile 
			if (bag[i].equals(myTile))
			{
				foundTile = i;
			}
		}

		// Exchange the tile
		int exchangeIndex = (int) (Math.random() * (topTile + 1));

		// Switch the current tile and the random tile
		Tile intermediate = bag[exchangeIndex];
		bag[exchangeIndex] = bag[foundTile];
		bag[foundTile] = intermediate;

		// Change positions
		bag[exchangeIndex].setPosition(position);
		
		return bag[foundTile];
	}

	/**
	 * Finds out how many Tiles are left, that are not yet dealt, from the Bag
	 * 
	 * @return the number of Tiles that are not yet dealt from the Bag
	 */
	public int tilesLeft()
	{
		return topTile + 1;
	}

	/**
	 * Returns a String representation of the Tiles left in the Bag
	 * 
	 * @return a String representation of the Tiles left in the Bag
	 */
	public String toString()
	{
		StringBuilder bagStr = new StringBuilder(bag.length * 2);
		for (int currentTile = bag.length - 1; currentTile >= topTile; currentTile--)
		{
			bagStr.append(bag[currentTile].toString());
			bagStr.append(" ");
		}
		return bagStr.toString();
	}
}
