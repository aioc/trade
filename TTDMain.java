package games.ttd;

import core.Config;
import core.Director;
import core.commander.EmptyGameCommandHandler;

public class TTDMain {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
        /* yolo hard code, should make these into options later */
        int boardSize = 10;
        int numTypes = 5;
        int numProducers = 15;
        int numConsumers = 45;
        int initialMoney = 20;
        new Director(new PlayerFactory(), new GameFactory(boardSize, numTypes, numProducers, numConsumers, initialMoney), new EmptyGameCommandHandler()).run(config);
	}

}
