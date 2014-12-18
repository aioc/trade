package games.ttd;

import games.ttd.visualisation.FrameVisualiser;
import games.ttd.visualisation.VisualGameState;

import java.util.List;
import java.util.Random;

import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.DisconnectedException;
import core.visualisation.EventBasedFrameVisualiser;

public class GameFactory implements GameBuilder {

    public int boardSize = 60;
    public int numTypes = 4;
    public int numProducers = 50;
    public int numConsumers = 25;
    public int initialMoney = 20;

	@Override
	public GameInstance createGameInstance(List<PersistentPlayer> players) {
		/*
		 * Why did we implement Fisher-Yates bullshit? Java's
		 * Collections.shuffle does this for us...
		 */
		for (int i = 0; i < players.size(); i++) {
			int swap = i + (new Random().nextInt(players.size() - i));
			PersistentPlayer p = players.get(i);
			players.set(i, players.get(swap));
			players.set(swap, p);
		}
		for (int i = 0; i < players.size(); i++) {
			PersistentPlayer p = players.get(i);
			String toSend = "NEWGAME " + players.size() + " " + boardSize + " " + numTypes + " " + numProducers + " "
					+ numConsumers + " " + initialMoney + " " + i;
			p.getConnection().sendInfo(toSend);

			try {
				String inputString = p.getConnection().getStrInput();
				String[] tokens = inputString.split("\\s");
				if (tokens.length != 1) {
					p.getConnection().disconnect();
					continue;
				} else if (!tokens[0].equals("READY")) {
					p.getConnection().disconnect();
					continue;
				}
			} catch (DisconnectedException de) {
				p.getConnection().disconnect();
			}
		}
		GameRunner gr = new GameRunner(players, boardSize, numTypes, numProducers, numConsumers, initialMoney);
		FrameVisualiser fv = new FrameVisualiser();
		EventBasedFrameVisualiser<VisualGameState> vis = new EventBasedFrameVisualiser<VisualGameState>(gr, fv, new VisualGameState());

		// Ok, we need to get the visualGameState set up, give the EBFV to the
		// gameState to report to, and to the gameRunner to know when to finish
		// Use this method
		gr.setEventVisualiser(vis);
		return vis;
	}
}
