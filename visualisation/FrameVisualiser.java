package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.Producer;
import games.ttd.Statistics;
import games.ttd.Track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.List;

import core.visualisation.FrameVisualisationHandler;
import core.visualisation.VisualGameEvent;

public class FrameVisualiser implements FrameVisualisationHandler<VisualGameState> {

	private static final int TRACKFRAMES = 1;
	private static final int BORDER_SIZE = 10;
	private static final int SQUARE_BORDER_DIVSOR = 20;
	// Render-specific helpers
	private int sizeBoard;
	private int sizeSquare;
	private int sizeRectWidth;
	private int sizeRectHeight;
	private int borderSquareSize;
	Rectangle boardBox;
	Rectangle paintBox;
	Color statBoxColour = null;
	
	@Override
	public void generateBackground(VisualGameState s, int sWidth, int sHeight, Graphics2D g) {
		paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight
				- (2 * BORDER_SIZE));
		sizeBoard = Math.min(paintBox.height - (2 * BORDER_SIZE), (3 * (paintBox.width - (2 * BORDER_SIZE))) / 5);
		if (sizeBoard < 0) {
			sizeBoard = 0;
		}
		int rectWidth = (17 * (paintBox.width - (2 * BORDER_SIZE))) / 20,
			rectHeight = paintBox.height + BORDER_SIZE;
		boardBox = new Rectangle(paintBox.x + BORDER_SIZE, paintBox.y + BORDER_SIZE, rectWidth, rectHeight);
		sizeRectWidth = rectWidth / s.boardSize;
		sizeRectHeight = rectHeight / s.boardSize;
		sizeSquare = sizeBoard / s.boardSize;
		borderSquareSize = (sizeSquare / SQUARE_BORDER_DIVSOR) + 1;
		// Draw background
		g.setColor(Color.BLACK);
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		g.setColor(Color.LIGHT_GRAY.brighter());
		for (int i = 0; i < s.boardSize; i++) {
			for (int j = 0; j < s.boardSize; j++) {
				g.fillRect(
					boardBox.x + (j * sizeRectWidth),
					boardBox.y + (i * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
			}
		}
		for (Producer p : s.producers) {
			g.setColor(p.getColour().darker().darker().darker());
			g.fillRect(
					boardBox.x + (p.c * sizeRectWidth),
					boardBox.y + (p.r * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
		}
		for (Consumer c : s.consumers) {
			g.setColor(c.getColour().brighter().brighter().brighter().brighter());
			g.fillRect(
					boardBox.x + (c.c * sizeRectWidth),
					boardBox.y + (c.r * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
		}
	}

	@Override
	public void generateState(VisualGameState state, int sWidth, int sHeight, Graphics2D g) {
		boolean board[][][][] = state.getBoard();
		final Stroke currentStroke = g.getStroke();
		Stroke newStroke = new BasicStroke(3.0f);
		g.setStroke(newStroke);
		final int numPlayers = state.names.length;
		for (int i = 0; i < state.boardSize; i++) {
			for (int j = 0; j < state.boardSize; j++) {
				for (int k = 0; k < numPlayers; k++) {
					for (int l = 0; l < 4; l++) {
						if (board[i][j][k][l]) {
							drawTrack(g, state.colours[k], new Track(i, j, -1, l), k, numPlayers);
						}
					}
				}
			}
		}
		g.setStroke(currentStroke);
	}

	@Override
	public void eventCreated(VisualGameEvent e) {
		if (e instanceof TTDGainedMoneyEvent) {
			TTDGainedMoneyEvent te = (TTDGainedMoneyEvent)e;
			te.setTotalFrames(TRACKFRAMES);
		} else if (e instanceof TTDLostMoneyEvent) {
			TTDLostMoneyEvent te = (TTDLostMoneyEvent)e;
			te.setTotalFrames(TRACKFRAMES);
		} else {
			TTDGameEvent te = (TTDGameEvent)e;
			te.setTotalFrames(TRACKFRAMES);
		}
	}

	@Override
	public void animateEvents(VisualGameState currentState, List<VisualGameEvent> events, int sWidth, int sHeight, Graphics2D g) {
		Rectangle textBox = new Rectangle(boardBox.x + boardBox.width + BORDER_SIZE, paintBox.y + BORDER_SIZE,
				paintBox.width - boardBox.width - 2 * BORDER_SIZE, paintBox.height - BORDER_SIZE);
		if (statBoxColour == null)
			statBoxColour = currentState.generateRandomColour(Color.WHITE);
		g.setColor(statBoxColour);
		g.fillRect(textBox.x, textBox.y, textBox.width, textBox.height);
		drawStatBoxes(g, currentState, textBox);
		for (int i = 0; i < events.size(); i++) {
			if (!(events.get(i) instanceof TTDGameEvent)) {
				continue;
			}
			TTDGameEvent te = (TTDGameEvent)events.get(i);
			for (Track track: te.tracks) {
				currentState.stats[te.player].tracksBought++;
				drawTrack(g, currentState.colours[te.player], track, te.player, currentState.names.length);
			}
		}
		
	}
	
	private void drawTrack(Graphics2D g, Color colour, Track track, int pid, int numPlayers) {
		g.setColor(colour);
		int playerDotW = sizeRectWidth * (pid + 1) / (numPlayers + 1);
		int playerDotH = sizeRectHeight * (pid + 1) / (numPlayers + 1);
		// We have numPlayers dots evenly spaced between the TL and BR
		// of the square. We are going to connect the ith player to the
		// ith of these dots.
		if (track.d == 1 || track.d == 3) {
			// d == 1 should be right, d == 3 should be left
			g.drawLine(boardBox.x + track.c*sizeRectWidth + playerDotW,
						boardBox.y + track.r*sizeRectHeight + playerDotH,
						boardBox.x + track.c*sizeRectWidth + playerDotW - (sizeRectWidth*(track.d - 2)),
						boardBox.y + track.r*sizeRectHeight + playerDotH
					);
		} else {
			// d == 0 should be up, d == 2 should be down.
			g.drawLine(boardBox.x + track.c*sizeRectWidth + playerDotW,
						boardBox.y + track.r*sizeRectHeight + playerDotH,
						boardBox.x + track.c*sizeRectWidth + playerDotW,
						boardBox.y + track.r*sizeRectHeight + playerDotH + (sizeRectHeight*(track.d - 1))
					);
		}
	}

	private void drawStatBoxes(Graphics2D g, VisualGameState state, Rectangle textBox) {
		final int numPlayers = state.names.length;
		
		Rectangle topBox = new Rectangle(textBox.x, textBox.y, textBox.width, textBox.height / 12);
		Rectangle middleBox = new Rectangle(textBox.x, topBox.y + topBox.height, textBox.width,
				(4 * textBox.height) / 6);
		
		int squareSize = (int) Math.ceil(Math.sqrt(numPlayers));
		int squareWidth = middleBox.width;
		int squareHeight = middleBox.height / squareSize;
		int strokeSize = (Math.min(squareWidth, squareHeight) / 80) + 1;
		squareWidth -= 2 * strokeSize;
		squareHeight -= 2 * strokeSize;
	
		
		for (int i = 0; i < numPlayers; i++) {
			Rectangle playerBox = new Rectangle(middleBox.x + strokeSize,
					middleBox.y + (i * (squareHeight + 2 * strokeSize)) + strokeSize, squareWidth, squareHeight);
			Color pColour = state.colours[i];
			Color nameColour = Color.WHITE;
			Color textColour = Color.WHITE;
			g.setColor(pColour);
			g.setStroke(new BasicStroke(strokeSize));
			g.drawRect(playerBox.x, playerBox.y, playerBox.width, playerBox.height);
			g.setStroke(new BasicStroke(1));
			// Write their name so that it's either 24 big, or just fits.
			Rectangle nameBox = new Rectangle(playerBox.x, playerBox.y, playerBox.width, playerBox.height / 5);
			Font f = getLargestFittingFont(g.getFont(), nameBox, g, state.names[i], 24);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D fR = fm.getStringBounds(state.names[i], g);
			g.fillRect(nameBox.x, nameBox.y, nameBox.width, nameBox.height);
			g.setFont(f);
			g.setColor(nameColour);
			g.drawString(state.names[i], nameBox.x, (nameBox.y + nameBox.height) - (8 * (int) (nameBox.height - fR.getHeight())) / 10);
		}
	}

	private Font getLargestFittingFont(Font f, Rectangle r, Graphics2D g, String s, int largestSize) {
		int minSize = 1;
		int maxSize = largestSize;
		while (minSize < maxSize) {
			int midSize = (minSize + maxSize) / 2;
			f = f.deriveFont(Font.PLAIN, midSize);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D fR = fm.getStringBounds(s, g);
			if (fR.getWidth() < r.width && fR.getHeight() < r.height) {
				minSize = midSize + 1;
			} else {
				maxSize = midSize - 1;
			}
		}
		return f.deriveFont(minSize);
	}
	
	@Override
	public void eventEnded(VisualGameEvent e, VisualGameState state) {
		if (e instanceof TTDGainedMoneyEvent) {
			TTDGainedMoneyEvent te = (TTDGainedMoneyEvent)e;
			state.money[te.player] += te.amountGained; 
		} else if (e instanceof TTDLostMoneyEvent) {
			TTDLostMoneyEvent te = (TTDLostMoneyEvent)e;
			state.money[te.player] -= te.amountLost;
		} else {
			TTDGameEvent te = (TTDGameEvent)e;
			for (Track t : te.tracks){ 
				state.trackPlaced(t.r, t.c, te.player, t.d);
			}
		}
	}

}
