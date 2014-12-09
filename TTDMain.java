package games.ttd;

import core.Config;

public class TTDMain {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		System.out.println("TTD Will not start!");
		//new Director(new PlayerFactory(), new GameFactory()).run(config);
	}

}
