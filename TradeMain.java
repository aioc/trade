package games.trade;

import core.Config;
import core.Director;

public class TradeMain {

	public static void main(String[] args) {
		Config config = new Config();
		config.parseArgs(args);
		config.port = 12317;
		
		
		GameFactory f = new GameFactory();
		config.gameCommands.put("PARAMS", new GameParamsCommand(f));
		new Director(new PlayerFactory(), f).run(config);
	}
	
}
