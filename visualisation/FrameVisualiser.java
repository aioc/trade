package games.trade.visualisation;

import games.trade.Consumer;
import games.trade.Producer;
import games.trade.Track;

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
	Color textColour = null;

	Color winnerColor = null;

	@Override
	public void generateBackground(VisualGameState s, int sWidth, int sHeight, Graphics2D g) {
		paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight - (2 * BORDER_SIZE));
		sizeBoard = Math.min(paintBox.height - (2 * BORDER_SIZE), (3 * (paintBox.width - (2 * BORDER_SIZE))) / 5);
		if (sizeBoard < 0) {
			sizeBoard = 0;
		}
		int rectWidth = (17 * (paintBox.width - (2 * BORDER_SIZE))) / 20, rectHeight = paintBox.height + BORDER_SIZE;
		boardBox = new Rectangle(paintBox.x + BORDER_SIZE, paintBox.y + BORDER_SIZE, rectWidth, rectHeight);
		sizeRectWidth = rectWidth / s.boardSize;
		sizeRectHeight = rectHeight / s.boardSize;
		sizeSquare = sizeBoard / s.boardSize;
		borderSquareSize = (sizeSquare / SQUARE_BORDER_DIVSOR) + 1;
		// Draw background
		g.setColor(Color.WHITE);
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		g.setColor(Color.LIGHT_GRAY.brighter());
		for (int i = 0; i < s.boardSize; i++) {
			for (int j = 0; j < s.boardSize; j++) {
				/*g.fillRect(boardBox.x + (j * sizeRectWidth), boardBox.y + (i * sizeRectHeight), sizeRectWidth
						- borderSquareSize, sizeRectHeight - borderSquareSize);*/
			}
		}
		for (Producer p : s.producers) {
			g.setColor(p.getColour().darker().darker().darker());
			g.fillRect(boardBox.x + (p.c * sizeRectWidth), boardBox.y + (p.r * sizeRectHeight), sizeRectWidth
					- borderSquareSize, sizeRectHeight - borderSquareSize);
		}
		for (Consumer c : s.consumers) {
			g.setColor(c.getColour().brighter().brighter().brighter().brighter());
			g.fillRect(boardBox.x + (c.c * sizeRectWidth), boardBox.y + (c.r * sizeRectHeight), sizeRectWidth
					- borderSquareSize, sizeRectHeight - borderSquareSize);
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
		if (state.winner != null) {
			drawWinnerBox(g, state, sWidth, sHeight, state.winner);
		}
		g.setStroke(currentStroke);
	}

	@Override
	public void eventCreated(VisualGameEvent e) {
		if (e instanceof TradeGainedMoneyEvent) {
			TradeGainedMoneyEvent te = (TradeGainedMoneyEvent) e;
			te.setTotalFrames(TRACKFRAMES);
		} else if (e instanceof TradeLostMoneyEvent) {
			TradeLostMoneyEvent te = (TradeLostMoneyEvent) e;
			te.setTotalFrames(TRACKFRAMES);
		} else if (e instanceof TradeConnectedPairEvent) {} else if (e instanceof TradeWinnerEvent) {
			((TradeWinnerEvent) e).totalFrames = 60;
		} else {
			TradeGameEvent te = (TradeGameEvent) e;
			te.setTotalFrames(TRACKFRAMES);
		}
	}

	@Override
	public void animateEvents(VisualGameState currentState, List<VisualGameEvent> events, int sWidth, int sHeight,
			Graphics2D g) {
		Rectangle textBox = new Rectangle(boardBox.x + boardBox.width + BORDER_SIZE, paintBox.y + BORDER_SIZE,
				paintBox.width - boardBox.width - 2 * BORDER_SIZE, paintBox.height - BORDER_SIZE);
		if (statBoxColour == null)
			statBoxColour = currentState.generateRandomColour(Color.WHITE);
		g.setColor(statBoxColour);
		g.fillRect(textBox.x, textBox.y, textBox.width, textBox.height);
		drawStatBoxes(g, currentState, textBox);
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i) instanceof TradeConnectedPairEvent) {
				Consumer c = ((TradeConnectedPairEvent) events.get(i)).consumer;
				Producer p = ((TradeConnectedPairEvent) events.get(i)).producer;
				Color playerColour = currentState.colours[((TradeConnectedPairEvent) events.get(i)).player];
				Color colour = c.getColour().brighter().brighter().brighter().brighter();
				colour = new Color((colour.getRed() + playerColour.getRed()) / 2,
						(colour.getGreen() + playerColour.getGreen()) / 2,
						(colour.getBlue() + playerColour.getBlue()) / 2);
				g.setColor(colour);
				g.fillRect(boardBox.x + (c.c * sizeRectWidth) - 5, boardBox.y + (c.r * sizeRectHeight) - 5,
						sizeRectWidth - borderSquareSize + 10, sizeRectHeight - borderSquareSize + 10);
				colour = p.getColour().darker().darker().darker();
				colour = new Color((colour.getRed() + playerColour.getRed()) / 2,
						(colour.getGreen() + playerColour.getGreen()) / 2,
						(colour.getBlue() + playerColour.getBlue()) / 2);
				g.setColor(colour);
				g.fillRect(boardBox.x + (p.c * sizeRectWidth) - 5, boardBox.y + (p.r * sizeRectHeight) - 5,
						sizeRectWidth - borderSquareSize + 10, sizeRectHeight - borderSquareSize + 10);
				continue;
			} else if (events.get(i) instanceof TradeWinnerEvent) {
				drawWinnerBox(g, currentState, sWidth, sHeight, ((TradeWinnerEvent) events.get(i)).playerName);
				continue;
			} else if (!(events.get(i) instanceof TradeGameEvent)) {
				continue;
			}
			TradeGameEvent te = (TradeGameEvent) events.get(i);
			for (Track track : te.tracks) {
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
			g.drawLine(boardBox.x + track.c * sizeRectWidth + playerDotW, boardBox.y + track.r * sizeRectHeight
					+ playerDotH, boardBox.x + track.c * sizeRectWidth + playerDotW - (sizeRectWidth * (track.d - 2)),
					boardBox.y + track.r * sizeRectHeight + playerDotH);
		} else {
			// d == 0 should be up, d == 2 should be down.
			g.drawLine(boardBox.x + track.c * sizeRectWidth + playerDotW, boardBox.y + track.r * sizeRectHeight
					+ playerDotH, boardBox.x + track.c * sizeRectWidth + playerDotW, boardBox.y + track.r
					* sizeRectHeight + playerDotH + (sizeRectHeight * (track.d - 1)));
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
			Rectangle playerBox = new Rectangle(middleBox.x + strokeSize, middleBox.y
					+ (i * (squareHeight + 2 * strokeSize)) + strokeSize, squareWidth, squareHeight);
			Color pColour = state.colours[i];
			Color nameColour = Color.WHITE;
			while (textColour == null || state.colourDistance(textColour, statBoxColour) < 200) {
				textColour = state.generateRandomColour(Color.WHITE);
			}
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
			g.drawString(state.names[i], nameBox.x + (nameBox.width - (int) fR.getWidth()) / 2,
					(nameBox.y + nameBox.height) - (8 * (int) (nameBox.height - fR.getHeight())) / 10);
			Rectangle playerBoxInterior = new Rectangle(playerBox.x + strokeSize, playerBox.y + strokeSize,
					playerBox.width - 2 * strokeSize, playerBox.height - 2 * strokeSize);
			g.setColor(textColour);
			String s = "Money: $" + state.money[i];
			f = getLargestFittingFont(g.getFont(), playerBoxInterior, g, s, 24);
			g.setFont(f);
			fm = g.getFontMetrics(f);
			fR = fm.getStringBounds(s, g);
			g.drawString(s, playerBoxInterior.x, playerBoxInterior.y + (2 * playerBoxInterior.height) / 5);

			s = "Tracks bought: " + state.stats[i].tracksBought;
			f = getLargestFittingFont(g.getFont(), playerBoxInterior, g, s, 24);
			g.setFont(f);
			// fm = g.getFontMetrics(f);
			// fR = fm.getStringBounds(s, g);
			g.drawString(s, playerBoxInterior.x,
					playerBoxInterior.y + (2 * playerBoxInterior.height) / 5 + (int) fR.getHeight() + 10);
		}
	}

	private void drawWinnerBox(Graphics2D g, VisualGameState state, int sWidth, int sHeight, String winner) {
		String text = "";
		if (winner.equals("")) {
			text = "The game was a draw";
		} else {
			text = winner + " was the winner!";
		}
		Color c = Color.BLACK;
		for (int i = 0; i < state.names.length; i++) {
			if (state.names[i].equals(winner)) {
				c = state.colours[i];
			}
		}
		Rectangle winnerRect = new Rectangle(boardBox.x + boardBox.width / 4, boardBox.y + boardBox.height / 3,
				boardBox.width / 2, boardBox.height / 3);
		g.setColor(c);
		g.fillRect(winnerRect.x, winnerRect.y, winnerRect.width, winnerRect.height);

		while (winnerColor == null || state.colourDistance(c, winnerColor) < 200) {
			winnerColor = state.generateRandomColour(Color.WHITE);
		}
		Rectangle nameBox = new Rectangle(winnerRect.x + (winnerRect.width / 10), winnerRect.y
				+ (winnerRect.height / 10), (winnerRect.width * 4) / 5, (winnerRect.height * 4) / 5);
		Font f = getLargestFittingFont(g.getFont(), nameBox, g, text, 180);
		g.setStroke(new BasicStroke(1));
		FontMetrics fm = g.getFontMetrics(f);
		Rectangle2D fR = fm.getStringBounds(text, g);
		g.setFont(f);
		g.setColor(winnerColor);
		g.drawString(text, nameBox.x + (nameBox.width - (int) fR.getWidth()) / 2, nameBox.y
				+ (nameBox.height - (int) fR.getHeight() + fm.getHeight()) / 2);
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
		if (e instanceof TradeGainedMoneyEvent) {
			TradeGainedMoneyEvent te = (TradeGainedMoneyEvent) e;
			state.money[te.player] += te.amountGained;
		} else if (e instanceof TradeLostMoneyEvent) {
			TradeLostMoneyEvent te = (TradeLostMoneyEvent) e;
			state.money[te.player] -= te.amountLost;
		} else if (e instanceof TradeConnectedPairEvent) {} else if (e instanceof TradeWinnerEvent) {
			state.winner = ((TradeWinnerEvent) e).playerName;
		} else {
			TradeGameEvent te = (TradeGameEvent) e;
			for (Track t : te.tracks) {
				state.trackPlaced(t.r, t.c, te.player, t.d);
			}
		}
	}

}
