import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * Main Frame for Scrabble. Sets up menus and places the ScrabblePanel in the
 * frame.
 * 
 * @author Jessica Jiang
 * @version 21 January 2015
 *
 */
public class ScrabbleMain extends JFrame implements ActionListener
{
	private ScrabblePanel scrabbleArea;
	private JMenuItem newMenuItem, statsMenuItem, quitMenuItem, aboutMenuItem,
			instructionsMenuItem;

	/**
	 * Creates a ScrabbleMain
	 */
	public ScrabbleMain()
	{
		super("Scrabble");
		setResizable(false);

		addWindowListener(new CloseWindow());

		// Add in an Icon
		setIconImage(new ImageIcon("images\\S.png").getImage());

		// Game Menu
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic('G');

		// New Game
		newMenuItem = new JMenuItem("New Game");
		newMenuItem.addActionListener(this);

		// Statistics
		statsMenuItem = new JMenuItem("Statistics");
		statsMenuItem.addActionListener(this);

		// Quit
		quitMenuItem = new JMenuItem("Quit");
		quitMenuItem.addActionListener(this);

		// Add the menu items for Game Menu
		gameMenu.add(newMenuItem);
		gameMenu.add(statsMenuItem);
		gameMenu.addSeparator();
		gameMenu.add(quitMenuItem);
		menuBar.add(gameMenu);

		// Add the menu items for Help Menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		aboutMenuItem = new JMenuItem("About...");
		aboutMenuItem.addActionListener(this);
		instructionsMenuItem = new JMenuItem("Instructions");
		instructionsMenuItem.addActionListener(this);
		helpMenu.add(aboutMenuItem);
		helpMenu.add(instructionsMenuItem);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		setLayout(new BorderLayout());
		scrabbleArea = new ScrabblePanel(this);
		add(scrabbleArea, BorderLayout.CENTER);

		// Centre the frame in the middle (almost) of the screen
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setVisible(true);
		setLocation((screen.width - ScrabblePanel.WIDTH) / 2 - this.getWidth(),
				(screen.height - ScrabblePanel.HEIGHT) / 2 - this.getHeight());
	}

	/**
	 * Method that deals with the menu options
	 * 
	 * @author Jessica Jiang and Jennifer Chan
	 * @param event the event that triggered this method
	 */
	public void actionPerformed(ActionEvent event)
	{
		// User presses New Game
		if (event.getSource() == newMenuItem)
		{
			scrabbleArea.newGame();
		}
		// User presses Statistics
		else if (event.getSource() == statsMenuItem)
		{
			JOptionPane.showMessageDialog(scrabbleArea,
					scrabbleArea.getStats(), "Statistics",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// User presses Quit
		else if (event.getSource() == quitMenuItem)
		{
			// Update statistics file and then exit
			scrabbleArea.getStats().writeToFile("statisticsFile.dat");
			System.exit(0);
		}
		// User presses About
		else if (event.getSource() == aboutMenuItem)
		{
			JOptionPane
					.showMessageDialog(
							scrabbleArea,
							"Scrabble by Jennifer Chan\nand Jessica Jiang\n\u00a9 2015",
							"About Scrabble", JOptionPane.INFORMATION_MESSAGE);
		}
		// User presses Instructions
		else if (event.getSource() == instructionsMenuItem)
		{
			scrabbleArea.toHelp();
		}
	}

	/**
	 * Inner class to handle window closing
	 * 
	 * @author Jessica Jiang
	 * @version 21 January 2015
	 */
	private class CloseWindow extends WindowAdapter
	{
		/**
		 * Deals with window closing
		 * 
		 * @param event the event that triggered this method
		 */
		public void windowClosing(WindowEvent event)
		{
			// Save statistics before exiting
			scrabbleArea.getStats().writeToFile("statisticsFile.dat");
			System.exit(0);
		}
	}

	public static void main(String[] args)
	{
		ScrabbleMain frame = new ScrabbleMain();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
