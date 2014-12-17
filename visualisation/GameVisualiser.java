package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.GameEvent;
import games.ttd.GamePerson;
import games.ttd.Pair;
import games.ttd.Player;
import games.ttd.Producer;
import games.ttd.ResourceManager;
import games.ttd.Track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.sound.sampled.Clip;

import core.interfaces.PersistentPlayer;

public class GameVisualiser {
	private static final int DEFAULT_FRAMES_PER_STATE = 5;
	private static final int SPECIAL_FRAMES_PER_STATE = 15;
	private static final int BORDER_SIZE = 10;
	private static final int SQUARE_BORDER_DIVSOR = 20;

	// Contains all drawing-relevant information at a given turn in the game.
	private static class GameStateInfo extends Pair<List<GamePerson>, List<GameEvent>> {
		GameStateInfo(List<GamePerson> gamePersons, List<GameEvent> gameEvents) {
			super(gamePersons, gameEvents);
		}
	}

	private int boardSize;
	private boolean isVisualising;
	private List<PersistentPlayer> players;
	private List<Producer> producers;
	private List<Consumer> consumers;
	private GameStateInfo previousState;
	private GameStateInfo currentState;
	private Integer framesPerThisState;
	private Queue<GameStateInfo> states;
	private List<String> gameEvents;
	private int curFrame; // INV: 0 <= curFrame < framesPerThisState
	private boolean waitingOnNextState; // queue is empty *and* we've rendered all frames already
	private int curRound;
	private Clip music;

	// Render-specific helpers
	BufferedImage prerenderedBackground;
	private int sizeBoard;
	private int sizeSquare;
	private int sizeRectWidth;
	private int sizeRectHeight;
	private int borderSquareSize;
	private Rectangle boardBox; // The bounding box of the game board.
	private Rectangle paintBox; // The portion of the screen inside the outer border.

	public GameVisualiser(List<PersistentPlayer> pl, int boardSize) {
		players = pl;
		this.boardSize = boardSize;
		states = new LinkedList<GameStateInfo>();
		gameEvents = new ArrayList<String>();
		isVisualising = false;
		waitingOnNextState = true;
		framesPerThisState = null;
		curFrame = 0;
		curRound = 0;
		previousState = currentState = null;
		music = null;
	}

	private int getFramesPerThisState() {
		if (framesPerThisState != null) return framesPerThisState;
		framesPerThisState = DEFAULT_FRAMES_PER_STATE;
		PersistentPlayer lastPlayer = players.get(players.size()-1);
        // TODO add in MINARWIN.
		/*if (Player.isMINARWIN(lastPlayer) && previousState != null) {
			GamePerson gP = previousState.getL().get(players.size()-1);
			boolean awesome = false;
			for (int i = 0; i < players.size(); i++) {
				if (i == players.size()-1) continue;
				GamePerson gP2 = currentState.getL().get(i);
				if (gP2.lastMove != Move.SHOOT) continue;
				if (gP2.dir == Direction.UP && gP2.position.c == gP.position.c && gP2.position.r > gP.position.r) awesome = true;
				if (gP2.dir == Direction.DOWN && gP2.position.c == gP.position.c && gP2.position.r < gP.position.r) awesome = true;
				if (gP2.dir == Direction.RIGHT && gP2.position.r == gP.position.r && gP2.position.c < gP.position.c) awesome = true;
				if (gP2.dir == Direction.LEFT && gP2.position.r == gP.position.r && gP2.position.c > gP.position.c) awesome = true;
			}
			if (gP.lastMove == Move.SHOOT) awesome = true;
			if (awesome) framesPerThisState = SPECIAL_FRAMES_PER_STATE;
		}*/
		return framesPerThisState;
	}

	public void addStateToVisualise(GamePerson[] newPeople, List<GameEvent> events) {
		List<GamePerson> newL = new ArrayList<GamePerson>();
		for (GamePerson p : newPeople) {
			newL.add(new GamePerson(p));
		}
		GameStateInfo newState = new GameStateInfo(newL, events);
		if (waitingOnNextState) {
			startRenderingState(newState);
		} else {
			states.add(newState);
		}
	}

	/**
	 * Does all the bookkeeping required so that the next render shows a new state.
	 * Pushes game events to the queue, resets curFrame, increments curRound.
	 */
	private void startRenderingState(GameStateInfo newState) {
		previousState = currentState;
		currentState = newState;
		curFrame = 0;
		curRound++;
		framesPerThisState = null;
		addEvents(currentState.getR());
		waitingOnNextState = false;
	}

	public void handleWindowResize(int width, int height) {
		prepareBackground(width, height);
	}
	
	public void reportProducers(List<Producer> p) {
		producers = new ArrayList<Producer>(p.size());
		for (Producer pp : p) {
			producers.add(new Producer(pp));
		}
	}

	public void reportConsumers(List<Consumer> p) {
		consumers = new ArrayList<Consumer>(p.size());
		for (Consumer pp : p) {
			consumers.add(new Consumer(pp));
		}
	}

	/**
	 * Recalculates all 'constants' dependent on window size (e.g. board width in pixels);
	 * redraws the pre-drawn background.
	 */
	private void prepareBackground(int sWidth, int sHeight) {
		paintBox = new Rectangle(BORDER_SIZE, BORDER_SIZE, sWidth - (2 * BORDER_SIZE), sHeight
				- (2 * BORDER_SIZE));
		sizeBoard = Math.min(paintBox.height - (2 * BORDER_SIZE), (3 * (paintBox.width - (2 * BORDER_SIZE))) / 5);
		if (sizeBoard < 0) {
			sizeBoard = 0;
		}
		int rectWidth = (17 * (paintBox.width - (2 * BORDER_SIZE))) / 20,
			rectHeight = paintBox.height - (2 * BORDER_SIZE);
		boardBox = new Rectangle(paintBox.x + BORDER_SIZE, paintBox.y + BORDER_SIZE, rectWidth, rectHeight);
		sizeRectWidth = rectWidth / boardSize;
		sizeRectHeight = rectHeight / boardSize;
		sizeSquare = sizeBoard / boardSize;
		borderSquareSize = (sizeSquare / SQUARE_BORDER_DIVSOR) + 1;
		// Draw background
		prerenderedBackground = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) prerenderedBackground.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(paintBox.x, paintBox.y, paintBox.width, paintBox.height);
		g.setColor(Color.WHITE);
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				g.fillRect(
					boardBox.x + (j * sizeRectWidth),
					boardBox.y + (i * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
			}
		}
		for (Producer p : producers) {
			g.setColor(p.getColour().darker().darker().darker());
			g.fillRect(
					boardBox.x + (p.c * sizeRectWidth),
					boardBox.y + (p.r * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
		}
		for (Consumer c : consumers) {
			g.setColor(c.getColour().brighter().brighter().brighter().brighter());
			g.fillRect(
					boardBox.x + (c.c * sizeRectWidth),
					boardBox.y + (c.r * sizeRectHeight),
					sizeRectWidth - borderSquareSize,
					sizeRectHeight - borderSquareSize);
		}
	}
	
	/**
	 * Renders a single frame of animation; updates the frame counter.
	 */
	public void visualise(Graphics2D g, int sWidth, int sHeight) {
		if (!isVisualising) {
			isVisualising = true;
			// Set up the background and bounding box sizes for the first time.
			prepareBackground(sWidth, sHeight);
		}
		if (currentState == null) {
			return;
		}
		List<GamePerson> curPeople = currentState.getL();
		if (paintBox.isEmpty()) {
			return;
		}
		// Draw back
		g.drawImage(prerenderedBackground, 0, 0, null);
		Rectangle textBox = new Rectangle(boardBox.x + boardBox.width + BORDER_SIZE, paintBox.y + BORDER_SIZE,
				paintBox.width - boardBox.width - 2 * BORDER_SIZE, paintBox.height - 2 * BORDER_SIZE);
		g.setColor(Color.RED.darker());
		g.fillRect(textBox.x, textBox.y, textBox.width, textBox.height);
		drawStatBoxes(g, textBox, curPeople);
        // TODO add in drawing code here!
		for (int i = 0; i < currentState.getL().size(); i++) {
			GamePerson person = currentState.getL().get(i);
			int red = (((Player)players.get(i)).getColour() >> 16) & 0xFF;
			int green = (((Player)players.get(i)).getColour() >> 8) & 0xFF;
			int blue = ((Player)players.get(i)).getColour() & 0xFF;
			g.setColor(new Color(red, green, blue));
			for (Track track : person.tracks) {
				int playerDotW = sizeRectWidth * (i + 1) / (players.size() + 1);
				int playerDotH = sizeRectHeight * (i + 1) / (players.size() + 1);
				// We have numPlayers dots evenly spaced between the TL and BR
				// of the square. We are going to connect the ith player to the
				// ith of these dots.
				if (track.d == 1 || track.d == 3) {
					g.drawLine(track.c + playerDotW,
								track.r + playerDotH,
								track.c + playerDotW + sizeRectWidth,
								track.r + playerDotH
							);
				} else {
					g.drawLine(track.c + playerDotW,
								track.r + playerDotH,
								track.c + playerDotW,
								track.r + playerDotH + sizeRectHeight
							);
				}

			}
		}
		/*
		 * BEGIN HACKING CODE
		 */
		/*if (players.get(players.size() - 1).getConnection() instanceof MINARWIN) {
			if (music == null) {
				music = MINARWIN.getMusic();
				new Thread(
			            new Runnable() {
			                public void run() {
			                	music.start();
			                }
			            }
			        ).start();
			}
 			if (false) {
				BufferedImage img = null;
				try {
					img = ImageIO.read(this.getClass().getResource(
							"/resources/evgeny.png"));
				} catch (IOException e) {
					// This ain't going to happen.
				}
 			}
		}*/
		/*
		 * END HACKING CODE
		 */
		curFrame++;
		if (curFrame == getFramesPerThisState()) {
			if (states.size() > 0) {
				startRenderingState(states.remove());
			} else {
				curFrame--;
				waitingOnNextState = true;
			}
		}
		if (curFrame >= getFramesPerThisState() && states.size() > 1) {
			curFrame = 0;
			curRound++;
		}
	}

	private void drawRectangle(Graphics2D g, Rectangle r) {
		g.drawRect(r.x, r.y, r.width, r.height);
	}

	/**
	 * Draw player names, health, shots, etc.
	 */
	private void drawStatBoxes(Graphics2D g, Rectangle textBox, List<GamePerson> curPeople) {
		// Divide into 3 areas: Top (title + turn stuff),
		// Middle (player score cards)
		// Bot (player events)
		Rectangle topBox = new Rectangle(textBox.x, textBox.y, textBox.width, textBox.height / 12);
		Rectangle middleBox = new Rectangle(textBox.x, topBox.y + topBox.height, textBox.width,
				(4 * textBox.height) / 6);
		Rectangle botBox = new Rectangle(textBox.x, middleBox.y + middleBox.height, textBox.width,
				(3 * textBox.height) / 12);
		Font myFont = getFont();
		drawTitle(g, topBox, myFont);
		// Next, work out each player box and draw them.
		int squareSize = (int) Math.ceil(Math.sqrt(players.size()));
		int squareWidth = middleBox.width / squareSize;
		int squareHeight = middleBox.height / squareSize;
		int strokeSize = (Math.min(squareWidth, squareHeight) / 80) + 1;
		squareWidth -= 2 * strokeSize;
		squareHeight -= 2 * strokeSize;
		// Now, loop through all players assigning them a box and drawing them
		for (int i = 0; i < players.size(); i++) {
			int x = i % squareSize;
			int y = i / squareSize;
			Rectangle squarePos = new Rectangle(middleBox.x + (x * (squareWidth + 2 * strokeSize)) + strokeSize,
					middleBox.y + (y * (squareHeight + 2 * strokeSize)) + strokeSize, squareWidth, squareHeight);
			drawPlayerBox(g, myFont, squarePos, (Player) players.get(i), curPeople.get(i), strokeSize);
		}
		drawEvents(g, myFont, botBox);
	}

	private void drawTitle(Graphics2D g, Rectangle titleBox, Font f) {
		String toDraw1 = "Round " + ((curRound / 5) + 1);
		String toDraw2 = ":" + (curRound%5 + 1);
		String toDraw = toDraw1+toDraw2;
		// Make it as big as possible yet still fit.
		f = getLargestFittingFont(f, titleBox, g, toDraw, 256);
		FontMetrics fm = g.getFontMetrics(f);
		g.setFont(f);
		g.setColor(Color.WHITE);
		g.drawString(toDraw1, titleBox.x, titleBox.y + fm.getHeight());
		g.setColor(new Color(50,50,50));
		g.drawString(toDraw2, titleBox.x + ((int)fm.getStringBounds(toDraw1, g).getWidth()), titleBox.y + fm.getHeight());
	}

	private void drawPlayerBox(Graphics2D g, Font f, Rectangle playerBox, Player p, GamePerson gP, int strokeSize) {
		boolean isMINARWIN = false;// TODO fixme Player.isMINARWIN(p);
		Color pColour = isMINARWIN ? Color.WHITE : new Color(p.getColour());
		Color nameColour = isMINARWIN ? Color.RED : Color.WHITE;
		Color textColour = Color.WHITE;
		g.setColor(pColour);
		g.setStroke(new BasicStroke(strokeSize));
		drawRectangle(g, playerBox);
		g.setStroke(new BasicStroke(1));
		// Write their name so that it's either 24 big, or just fits.
		Rectangle nameBox = new Rectangle(playerBox.x, playerBox.y, playerBox.width, playerBox.height / 5);
		f = getLargestFittingFont(f, nameBox, g, p.getName(), 24);
		FontMetrics fm = g.getFontMetrics(f);
		Rectangle2D fR = fm.getStringBounds(p.getName(), g);
		g.fillRect(nameBox.x, nameBox.y, nameBox.width, nameBox.height);
		g.setFont(f);
		g.setColor(nameColour);
		g.drawString(p.getName(), nameBox.x, (nameBox.y + nameBox.height) - (8 * (int) (nameBox.height - fR.getHeight())) / 10);
		// Draw the interior of the box
		Rectangle playerBoxInterior = new Rectangle(playerBox.x + strokeSize, playerBox.y + strokeSize, playerBox.width - 2*strokeSize, playerBox.height - 2*strokeSize);
		// Now, we draw their hearts such that they fill the space (kinda)
		int heartSize = Math.min((playerBoxInterior.width / 5), playerBoxInterior.height / 5);
		int heartGap = heartSize / 10;
		heartSize -= heartGap;
		Image heart = getHeartImage(heartSize);
		Point curHeartP = new Point(playerBoxInterior.x + heartGap, nameBox.y + nameBox.height + heartGap);
		//for (int i = 0; i < gP.health; i++) {
		//	g.drawImage(heart, curHeartP.x, curHeartP.y, null);
		//	curHeartP.x += heartSize + heartGap;
		//}
		// Next, draw some stats
		Rectangle curStatWindow = new Rectangle(playerBoxInterior.x, playerBoxInterior.y + (2 * playerBoxInterior.height) / 5, playerBoxInterior.width, playerBoxInterior.height / 5);
		g.setColor(textColour);
		//String movesString = "Moves: " + gP.stats.movesMade;
		//String shotsString = "Shots: " + gP.stats.shotsFired;
		//String killsString = "Kills: " + gP.stats.killsDone;
		//g.setFont(getLargestFittingFont(f, curStatWindow, g, movesString, 24));
		//g.drawString(movesString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
		//curStatWindow.y += curStatWindow.height;
		//g.setFont(getLargestFittingFont(f, curStatWindow, g, shotsString, 24));
		//g.drawString(shotsString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
		//curStatWindow.y += curStatWindow.height;
		//g.setFont(getLargestFittingFont(f, curStatWindow, g, killsString, 24));
		//g.drawString(killsString, curStatWindow.x, curStatWindow.y + g.getFontMetrics().getHeight());
	}
	
	private void drawEvents(Graphics2D g, Font f, Rectangle eventBox) {
		// Draw the most recent 5.
		int start = gameEvents.size() - 5;
		if (start < 0) {
			start = 0;
		}
		Point drawPoint = new Point(eventBox.x, eventBox.y);
		Rectangle fitBox = new Rectangle(eventBox.x, eventBox.y, (9 * eventBox.width) / 10, eventBox.height / 5);
		float fontSize = 24f;
		for (int i = 0; i < 5 && i < gameEvents.size(); i++) {
			String text = gameEvents.get(start + i);
			f = getLargestFittingFont(f, fitBox, g, text, 20);
			fontSize = Math.min(fontSize, f.getSize2D());
		}
		g.setFont(f.deriveFont(fontSize));
		for (int i = 0; i < 5 && i < gameEvents.size(); i++) {
			String text = gameEvents.get(start + i);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D textRec = fm.getStringBounds(text, g);
			g.drawString(text, drawPoint.x, drawPoint.y + (int) textRec.getHeight());
			drawPoint.y += (int) textRec.getHeight() + 1;
		}
	}
 
	private Font getLargestFittingFont(Font f, Rectangle r, Graphics2D g, String s, int largestSize) {
		int minSize = 1;
		int maxSize = largestSize;
		while (minSize < maxSize) {
			int midSize = (minSize + maxSize + 1) / 2;
			f = f.deriveFont(Font.PLAIN, midSize);
			FontMetrics fm = g.getFontMetrics(f);
			Rectangle2D fR = fm.getStringBounds(s, g);
			if (fR.getWidth() < r.width && fR.getHeight() < r.height) {
				minSize = midSize;
			} else {
				maxSize = midSize - 1;
			}
		}
		return f.deriveFont(minSize);
	}

	private void addEvents(List<GameEvent> events) {
		for (GameEvent e : events) {
			gameEvents.add((gameEvents.size() + 1) + ": " + e.getRepresentation(players));
		}
	}

	public boolean stillHasStatesQueued() {
		return states.size() != 0;
	}

	public boolean hasVisualised() {
		return isVisualising;
	}

	private Font getFont() {
		return ResourceManager.getFont();
	}

	private BufferedImage getHeartImage(int size) {
		BufferedImage finalBi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) finalBi.getGraphics();
		//g.drawImage(ResourceManager.getImage(), 0, 0, size - 1, size - 1, 0, 0, 15, 15, null);
		double x1Points[] = {0.4, 0, 0.3, 0.5, 0.8, 1.0, 1.0};
		double y1Points[] = {1.0, 0.3, 0, 0.3, 0.0, 0.1, 0.4};
		int x[] = new int[x1Points.length];
		int y[] = new int[y1Points.length];
		for (int i = 0; i < x1Points.length; i++) {
			        x[i] = (int) (x1Points[i] * size);
			        y[i] = (int) (y1Points[i] * size);
		};
		g.setColor(Color.WHITE);
		g.fillPolygon(x, y, x.length);
		g.dispose();
		return finalBi;
	}

	private void drawStar(Graphics2D g, Point centre, int personSize) {
		int x[] = new int[10];
		int y[] = new int[10];
		for (int i = 0; i < x.length; i++) {
			double rad = (i % 2 == 0) ? ((double) personSize/4) : ((double) personSize/8);
			double angle = i * 2 * Math.PI / 10 - Math.PI/2;
			x[i] = (int) (centre.x + Math.cos(angle) * rad);
			y[i] = (int) (centre.y + Math.sin(angle) * rad);
		}
		g.setColor(Color.RED);
		g.fillPolygon(x, y, x.length);
	}

}
