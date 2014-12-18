package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.Producer;
import games.ttd.Statistics;

import java.awt.Color;
import java.util.List;
import java.util.Random;

public class VisualGameState {
	public int boardSize = 0;
	public List<Producer> producers;
	public List<Consumer> consumers;
	public int[] money;
	public String[] names;
	public Color[] colours;
    public Statistics[] stats;
	
	private boolean board[][][][];
	
	public void init() {
		this.board = new boolean[boardSize][boardSize][money.length][4];
		final int numPlayers = colours.length;
		for (int i = numPlayers - 1; i >= 0; i--) {
			stats[i] = new Statistics();
			for (int j = i + 1; j < numPlayers; j++) {
				if (colourDistance(colours[i], colours[j]) < 100) {
					colours[i] = generateRandomColour(Color.WHITE);
				}
			}
		}
	}
	
	public double colourDistance(Color c1, Color c2) {
	    double rmean = (c1.getRed() + c2.getRed()) / 2;
	    int r = c1.getRed() - c2.getRed();
	    int g = c1.getGreen() - c2.getGreen();
	    int b = c1.getBlue() - c2.getBlue();
	    double weightR = 2 + rmean/256;
	    double weightG = 4.0;
	    double weightB = 2 + (255-rmean)/256;
	    return Math.sqrt(weightR*r*r + weightG*g*g + weightB*b*b);
	} 
	
	public Color generateRandomColour(Color mixer) {
	    Random random = new Random();
	    int red = random.nextInt(256);
	    int green = random.nextInt(256);
	    int blue = random.nextInt(256);

	    // mix the color
	    if (mixer != null) {
	        red = (red + mixer.getRed()) / 2;
	        green = (green + mixer.getGreen()) / 2;
	        blue = (blue + mixer.getBlue()) / 2;
	    }

	    Color color = new Color(red, green, blue);
	    return color;
	}

	public boolean[][][][] getBoard() {
		return board;
	}
	
	public void trackPlaced(int r, int c, int pid, int d) {
		board[r][c][pid][d] = true;
	}
}
