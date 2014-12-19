package games.trade;

public class Statistics {

	public int tracksBought;
	public int moneyMade;

	public Statistics() {
		tracksBought = 0;
		moneyMade = 0;
	}

	public Statistics(Statistics s) {
		tracksBought = s.tracksBought;
		moneyMade = s.moneyMade;
	}

}
