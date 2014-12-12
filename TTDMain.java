package games.ttd;

import core.Config;
import core.Director;

public class TTDMain {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		GameFactory f = new GameFactory();
        new Director(new PlayerFactory(), f).run(config);
	}

}
