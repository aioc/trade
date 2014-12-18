package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.Producer;
import games.ttd.Track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import core.visualisation.FrameVisualisationHandler;
import core.visualisation.VisualGameEvent;

public class FrameVisualiser implements FrameVisualisationHandler<VisualGameState> {

	private static final int TRACKFRAMES = 1;
	private static final int BORDER_SIZE = 10;
	private static final int SQUARE_BORDER_DIVSOR = 20;
	// Render-specific helpers
	BufferedImage prerenderedBackground;
	private int sizeBoard;
	private int sizeSquare;
	private int sizeRectWidth;
	private int sizeRectHeight;
	private int borderSquareSize;
	Rectangle boardBox;
	Rectangle paintBox;
	
	@Override
	public void generateBackground(VisualGameState s, int sWidth, int sHeight, Graphics2D programGraphics) {
		paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight
				- (2 * BORDER_SIZE));
		sizeBoard = Math.min(paintBox.height - (2 * BORDER_SIZE), (3 * (paintBox.width - (2 * BORDER_SIZE))) / 5);
		if (sizeBoard < 0) {
			sizeBoard = 0;
		}
		int rectWidth = (17 * (paintBox.width - (2 * BORDER_SIZE))) / 20,
			rectHeight = paintBox.height - (2 * BORDER_SIZE);
		boardBox = new Rectangle(paintBox.x + BORDER_SIZE, paintBox.y + BORDER_SIZE, rectWidth, rectHeight);
		sizeRectWidth = rectWidth / s.boardSize;
		sizeRectHeight = rectHeight / s.boardSize;
		sizeSquare = sizeBoard / s.boardSize;
		borderSquareSize = (sizeSquare / SQUARE_BORDER_DIVSOR) + 1;
		// Draw background
		prerenderedBackground = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) prerenderedBackground.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		g.setColor(Color.WHITE);
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
		programGraphics.drawImage(prerenderedBackground, 0, 0, null);
	}

	@Override
	public void generateState(VisualGameState state, int sWidth, int sHeight, Graphics2D g) {
		state.initBoard();
	}

	@Override
	public void eventCreated(VisualGameEvent e) {
		TTDGameEvent te = (TTDGameEvent)e;
		te.setTotalFrames(TRACKFRAMES);
	}

	@Override
	public void animateEvents(VisualGameState currentState, List<VisualGameEvent> events, int sWidth, int sHeight, Graphics2D g) {
		Rectangle textBox = new Rectangle(boardBox.x + boardBox.width + BORDER_SIZE, paintBox.y + BORDER_SIZE,
				paintBox.width - boardBox.width - 2 * BORDER_SIZE, paintBox.height - 2 * BORDER_SIZE);
		g.setColor(Color.RED.darker());
		g.fillRect(textBox.x, textBox.y, textBox.width, textBox.height);
		drawStatBoxes(g, textBox);
		System.out.println("got " + events.size() + " events");
		for (int i = 0; i < events.size(); i++) {
			TTDGameEvent te = (TTDGameEvent)events.get(i);
			g.setColor(currentState.colours[te.player]);
			int playerDotW = sizeRectWidth * (te.player + 1) / (currentState.names.length + 1);
			int playerDotH = sizeRectHeight * (te.player + 1) / (currentState.names.length + 1);
			// We have numPlayers dots evenly spaced between the TL and BR
			// of the square. We are going to connect the ith player to the
			// ith of these dots.
			for (Track track : te.tracks) {
				if (track.d == 1 || track.d == 3) {
					// d == 1 should be right, d == 3 should be left
					g.drawLine(track.c + playerDotW,
								track.r + playerDotH,
								track.c + playerDotW - (sizeRectWidth*(track.d - 2)),
								track.r + playerDotH
							);
				} else {
					// d == 0 should be up, d == 2 should be down.
					g.drawLine(track.c + playerDotW,
								track.r + playerDotH,
								track.c + playerDotW,
								track.r + playerDotH - (sizeRectHeight*(track.d - 1))
							);
				}
			}
		}
		
	}

	private void drawStatBoxes(Graphics2D g, Rectangle textBox) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventEnded(VisualGameEvent e, VisualGameState state) {
		boolean board[][][][] = state.getBoard();
		
	}

}
