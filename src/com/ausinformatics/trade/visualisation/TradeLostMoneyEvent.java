package com.ausinformatics.trade.visualisation;

import com.ausinformatics.phais.core.visualisation.VisualGameEvent;

public class TradeLostMoneyEvent extends VisualGameEvent {
	public int amountLost;
	public int player;

	public TradeLostMoneyEvent(int amount, int pid) {
		super();
		this.amountLost = amount;
		this.player = pid;
	}

	public void setTotalFrames(int frames) {
		this.totalFrames = frames;
	}
}
