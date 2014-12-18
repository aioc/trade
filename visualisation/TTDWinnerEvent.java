package games.ttd.visualisation;

import core.visualisation.VisualGameEvent;

public class TTDWinnerEvent extends VisualGameEvent {
	public String playerName;
	public TTDWinnerEvent(String name) {
		super();
		playerName = name;
	}
}
