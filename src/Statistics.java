import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A Statistics class that stores the score and name of the top player.
 * 
 * @author Jessica Jiang
 * @version 21 January 2015
 */
public class Statistics implements Serializable
{
	private String playerName;
	private int score;

	/**
	 * Creates a new Statistics object with all statistics set to 0 and no specified name
	 */
	public Statistics()
	{
		playerName = "N/A";
		score = 0;
	}

	/**
	 * Changes the playerName and score to the given playerName and score
	 * 
	 * @param playerName the name of the highest scoring player
	 * @param score the player's score
	 */
	public void changeTopPlayer(String playerName, int score)
	{
		this.playerName = playerName;
		this.score = score;
	}

	/**
	 * Checks if the given score is higher than the current high score
	 * 
	 * @param score the given score to compare
	 * @return true if the given score is higher, false otherwise (will return
	 *         false if equal)
	 */
	public boolean isHigher(int score)
	{
		return score > this.score;
	}

	/**
	 * Prints out all the Statistics as a String
	 * 
	 * @return the Statistics as a String
	 */
	public String toString()
	{

		return String.format("%28s %n%40s%n %-25s%-25s%n%-20s%17d",
				"HIGH SCORE",
				"-----------------------------------------------",
				"Player Name", "Score", playerName, score);
	}

	/**
	 * Writes all the Statistics object data to a file
	 * 
	 * @param fileName the name of the file
	 */
	public void writeToFile(String fileName)
	{
		try
		{
			// Write the entire Statistics object to a file
			ObjectOutputStream fileOut = new ObjectOutputStream(
					new FileOutputStream(fileName));
			fileOut.writeObject(this);
			fileOut.close();
		}
		catch (IOException exp)
		{
			System.out.println("Error writing to the file");
		}
	}

	/**
	 * Reads all the Statistics object data from a file and returns the
	 * Statistics
	 * 
	 * @param fileName the name of the file
	 * @return the Statistics object with all the data
	 */
	public static Statistics readFromFile(String fileName)
	{
		try
		{
			ObjectInputStream fileIn = new ObjectInputStream(
					new FileInputStream(fileName));
			Statistics stats = (Statistics) fileIn.readObject();
			fileIn.close();
			return stats;
		}
		catch (Exception exp)
		{
			return new Statistics();
		}
	}

}
