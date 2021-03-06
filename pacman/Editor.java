package pacman;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;

public class Editor extends JFrame {
	
	//double buffering
	Image dbImage;
	Graphics dbg;
	
	//level field
	final int LEVEL;
	
	//maze field
	Maze maze;
	
	//leaderboard for removing scores from edited files
	Leaderboard leaderboard;
	
	//if the jframe is queued to be disposed or not
	boolean isDisposable = false;
	
	//file to save and edit to
	final String FILEPATH;
	
	//maze dimensions
	final static int MAZEX = 30;
	final static int MAZEY = 15;
	
	//width of each block
	final int BLOCKWIDTH = 30;
	
	//mouse x and y coordinates
	int mouseX = 100;
	int mouseY = 100;
	
	//x and y index for the selected block
	int blockX = 0;
	int blockY = 0;
	
	final static int SCREENX = MAZEX * 33;
	final static int SCREENY = MAZEY * 33 + 100;
	
	//main method
	public Editor(String s, int level) {
		
		LEVEL = level;
		
		try {
			leaderboard = new Leaderboard();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		FILEPATH = s;
		
//		maze = new Maze(MAZEX, MAZEY);
		try {
			load();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		maze.fillEdges();
		//assigns value to maze field
		
		//keyboard listener
		addKeyListener(new keyboard());
		
		//mouse listener
		addMouseListener(new mouse());
		
		//sets the properties of the screen
//		this.setLocation(500, 100);
		setTitle("Pac-Man Level Editor");
		setVisible(true);
		setSize(SCREENX, SCREENY);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBackground(Color.black);
		
	}

	//class that handles mouse input
	public class mouse extends MouseAdapter {
		
		public void mousePressed(MouseEvent e) {
			
			mouseX = e.getX();
			mouseY = e.getY();
			
			blockX = (mouseX - 27) / BLOCKWIDTH;
			blockY = (mouseY - 54) / BLOCKWIDTH;
			
			if (blockX < 0) {
				blockX = 0;
			}
			if (blockY < 0) {
				blockY = 0;
			}
			
			if (blockX > maze.maze.length - 1) {
				blockX = maze.maze.length - 1;
			}
			if (blockY > maze.maze[0].length - 1) {
				blockY = maze.maze[0].length - 1;
			}
			
			if (e.isAltDown() || e.isControlDown() || e.isShiftDown()
					|| e.getButton() == e.BUTTON2) {
				updateAltState(blockX, blockY);				
			}
			else {
				updateState(blockX, blockY);
			}
			
			
		}
		
		public void mouseReleased(MouseEvent e) {
			
			
			
		}
		
	}
	
	//saves the current state into the custom.txt file
	public void save() throws FileNotFoundException{
		
		leaderboard.removeFromLevel(LEVEL);
		leaderboard.writeToFile();
		
		File f = new File(FILEPATH);
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			
			String content = "";
			
			for (int c = 0; c < maze.maze[0].length; c++) {
				for (int r = 0; r < maze.maze.length; r++) {
					content += maze.maze[r][c].getState() + " ";
				}
				content += "\n";
			}
			
			bw.write(content);
			
			// no need to close it.
			//bw.close();
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void load() throws FileNotFoundException{
		
		File f = new File(FILEPATH);
		
		if (!f.exists()) {
//			System.out.println("file not found");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println("new file created");
			maze = new Maze(MAZEX, MAZEY);
			save();
			return;
		}
		
		Scanner input = new Scanner(f);
		
		int totalRows = 0;
		int totalCols = 0;
		
		//gets number of rows and columns
		while (input.hasNext()) {
			String line = input.nextLine();
			totalCols = line.length() / 2;
			totalRows ++;
		}
		maze = new Maze(totalCols, totalRows);
		
		Scanner update = new Scanner(new File(FILEPATH));
		int row = 0;
		while (update.hasNext()) {
			String line = update.nextLine();
			for (int i = 0; i < totalCols; i++) {
				maze.maze[i][row].setState(Integer.parseInt(line.substring(i * 2, i * 2 + 1)));
			}
			row ++;
		}
		
	}
	
	//updates the state from the given point
	public void updateState(int r, int c) {
		
		int blockState = maze.maze[r][c].getState();
		blockState ++;
		
		if (blockState > Tile.PILL) {
			blockState = Tile.BLANK;
		}
		maze.maze[r][c].setState(blockState);
		try {
			save();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		
	}
	
	//updates the state from the given point using the alt items
	public void updateAltState(int r, int c) {
		
		
		int blockState = maze.maze[r][c].getState();
		blockState ++;
		
		if (blockState > Tile.TELEPORTER2 || blockState < Tile.SPAWN) {
			blockState = Tile.SPAWN;
		}
		
		maze.maze[r][c].setState(blockState);
		try {
			save();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		
	}
	
	//class that handles key input
	public class keyboard extends KeyAdapter {
		
		public void keyPressed(KeyEvent e) {
			
			int key = e.getKeyCode();
			
			switch (key) {
			
			case KeyEvent.VK_Q:
				isDisposable = true;
				break;
			case KeyEvent.VK_R:
				maze = new Maze(MAZEX, MAZEY);
				maze.fillEdges();
				try {
					save();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A:
				if (blockX > 0) {
					blockX --;
				}
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
				if (blockY > 0) {
					blockY --;
				}
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D:
				if (blockX < maze.maze.length - 1) {
					blockX ++;
				}
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
				if (blockY < maze.maze[0].length - 1) {
					blockY ++;
				}
				break;
			case KeyEvent.VK_Z:
				updateState(blockX, blockY);
				break;
			case KeyEvent.VK_X:
				updateAltState(blockX, blockY);
				break;
			case KeyEvent.VK_C:
				maze.maze[blockX][blockY].setState(Tile.GHOSTSPAWN);
				try {
					save();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;	
				
			case KeyEvent.VK_V:
				maze.maze[blockX][blockY].setState(Tile.POWERPELLET);
				try {
					save();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case KeyEvent.VK_H:
				maze.fillBlankWithDots();
				try {
					save();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			}
			
		}
		
		public void keyReleased(KeyEvent e) {
			
			int key = e.getKeyCode();
			
		}
		
	}
	
	//paints stuff to the jframe
	public void paintComponent(Graphics g) {
		
		if (isDisposable) {
			this.dispose();
		}
		
		for (int r = 0; r < maze.maze.length; r++) {
			for (int c = 0; c < maze.maze[0].length; c++) {
				
				int xloc = 25 + r * BLOCKWIDTH;
				int yloc = 50 + c * BLOCKWIDTH;
				
				int state = maze.maze[r][c].getState();
				
				//draws grid graphics
				switch (state) {
				
				case Tile.WALL:
					g.setColor(Color.blue);
					g.fillRect(xloc, yloc, BLOCKWIDTH, BLOCKWIDTH);
					break;
					
				case Tile.BLANK:
					g.setColor(Color.black);
					g.fillRect(xloc, yloc, BLOCKWIDTH, BLOCKWIDTH);
					break;
					
				case Tile.PILL:
					g.setColor(Color.white);
					g.fillOval(xloc + BLOCKWIDTH * 3 /7,
					yloc + BLOCKWIDTH * 3 / 7, BLOCKWIDTH / 4, BLOCKWIDTH / 4);
					break;
				
				case Tile.SPAWN:
					g.setColor(Color.BLUE);
					g.fillOval(xloc + BLOCKWIDTH / 4,
							yloc + BLOCKWIDTH / 4, BLOCKWIDTH / 2, BLOCKWIDTH / 2);
					break;
					
				case Tile.TELEPORTER:
					g.setColor(Color.yellow);
					g.fillOval(xloc + BLOCKWIDTH / 4,
							yloc + BLOCKWIDTH / 4, BLOCKWIDTH / 2, BLOCKWIDTH / 2);
					break;
					
				case Tile.TELEPORTER2:
					g.setColor(Color.MAGENTA);
					g.fillOval(xloc + BLOCKWIDTH / 4,
							yloc + BLOCKWIDTH / 4, BLOCKWIDTH / 2, BLOCKWIDTH / 2);
					break;
					
				case Tile.GHOSTSPAWN:
					g.setColor(Color.red);
					g.fillOval(xloc + BLOCKWIDTH / 4,
							yloc + BLOCKWIDTH / 4, BLOCKWIDTH / 2, BLOCKWIDTH / 2);
					break;
					
				case Tile.POWERPELLET:
					g.setColor(Color.white);
					g.fillOval(xloc + BLOCKWIDTH / 4,
					yloc + BLOCKWIDTH / 4, BLOCKWIDTH / 2, BLOCKWIDTH / 2);
					break;
					
				}
				
				g.setColor(Color.blue);
				g.drawRect(xloc, yloc, BLOCKWIDTH, BLOCKWIDTH);
				
			}
		}
		
		//cursor
		g.setColor(Color.red);
		g.drawRect(25 + blockX * BLOCKWIDTH - 1, 50 + blockY * BLOCKWIDTH - 1,
				BLOCKWIDTH + 2, BLOCKWIDTH + 2);
		g.drawRect(25 + blockX * BLOCKWIDTH - 2, 50 + blockY * BLOCKWIDTH - 2,
				BLOCKWIDTH + 4, BLOCKWIDTH + 4);
		
		g.setColor(Color.white);
		
		
		g.drawString("Block X: " + blockX + " Block Y: " + blockY, 50, SCREENY - 30);
		String statestring = "";
		
		try {
			//cursor info in the bottom left corner
			switch(maze.maze[blockX][blockY].getState()) {
			case Tile.BLANK:
				statestring = "Blank";
				break;
			case Tile.WALL:
				statestring = "Wall";
				break;
			case Tile.PILL:
				statestring = "Pill";
				break;
			case Tile.SPAWN:
				statestring = "Spawn";
				break;
			case Tile.TELEPORTER:
				statestring = "Teleporter";
				break;
			case Tile.TELEPORTER2:
				statestring = "Teleporter 2";
				break;
			case Tile.GHOSTSPAWN:
				statestring = "Ghost spawn";
				break;
			case Tile.POWERPELLET:
				statestring = "Power pellet";
				break;
			}
			
		} catch(Exception e) {
			//prevents the program from drawing a nonexistent statestring
			repaint();
		}
		
		g.drawString(statestring, 50, SCREENY - 50);
		
		repaint();
		
	}
	
	//double buffering
	public void paint(Graphics g) {
		
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paintComponent(dbg);
		g.drawImage(dbImage, 0, 0, this);
	}
		
}
