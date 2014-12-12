package games.ttd;

import java.util.List;
import java.util.Random;

import core.interfaces.GameBuilder;
import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.DisconnectedException;

public class GameFactory implements GameBuilder {

    public int boardSize = 10;
    public int numTypes = 5;
    public int numProducers = 15;
    public int numConsumers = 45;
    public int initialMoney = 20;

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
			String toSend = "NEWGAME " + players.size() + " " + boardSize + " " + numTypes + " " + numProducers + " " + numConsumers + " " + initialMoney + " " + i;
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

