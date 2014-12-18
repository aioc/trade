package games.ttd.visualisation;

import core.visualisation.VisualGameEvent;

public class TTDGainedMoneyEvent extends VisualGameEvent {
	public int amountGained;
	public int player;
	public TTDGainedMoneyEvent(int amount, int pid) {
		super();
		this.amountGained = amount;
		this.player = pid;
	}
	public void setTotalFrames(int frames) {
		this.totalFrames = frames;		
	}
}
