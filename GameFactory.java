package games.ttd;

import java.util.List;
import java.util.Random;

import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.DisconnectedException;

public class GameFactory implements GameBuilder {

    private int boardSize;
    private int numTypes;
    private int numProducers;
    private int numConsumers;
    
    public GameFactory(int n, int c, int p, int cc) {
        boardSize = n;
        numTypes = c;
        numProducers = p;
        numConsumers = cc;
    }

	@Override
	public GameInstance createGameInstance(List<PersistentPlayer> players) {
        /* Why did we implement Fisher-Yates bullshit?
         * Java's Collections.shuffle does this for us...
         */
        for (int i = 0; i < players.size(); i++) {
            int swap = i + (new Random().nextInt(players.size() - i));
            PersistentPlayer p = players.get(i);
            players.set(i, players.get(swap));
            players.set(swap, p);
        }
		for (int i = 0; i < players.size(); i++) {
			PersistentPlayer p = players.get(i);
			String toSend = "NEWGAME " + players.size() + " " + i + " " + boardSize + " " + numTypes + " " + numProducers + " " + numConsumers;
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
		return new GameRunner(players, boardSize, numTypes, numProducers, numConsumers);

    }
}

