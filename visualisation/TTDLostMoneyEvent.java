package games.ttd.visualisation;

import core.visualisation.VisualGameEvent;

public class TTDLostMoneyEvent extends VisualGameEvent {
	public int amountLost;
	public int player;
	public TTDLostMoneyEvent(int amount, int pid) {
		super();
		this.amountLost = amount;
		this.player = pid;
	}
	public void setTotalFrames(int frames) {
		this.totalFrames = frames;		
	}
}
