package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.Producer;

import java.awt.Color;
import java.util.List;

public class VisualGameState {
	public int boardSize = 0;
	public List<Producer> producers;
	public List<Consumer> consumers;
	public int[] money;
	public String[] names;
	public Color[] colours;
	
	private boolean board[][][][];
	
	public void initBoard() {
		this.board = new boolean[boardSize][boardSize][money.length][4];
	}

	public boolean[][][][] getBoard() {
		return board;
	}
	
	public void trackPlaced(int r, int c, int pid, int d) {
		board[r][c][pid][d] = true;
	}
}
