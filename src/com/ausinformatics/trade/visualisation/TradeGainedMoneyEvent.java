package com.ausinformatics.trade.visualisation;

import com.ausinformatics.phais.core.visualisation.VisualGameEvent;

public class TradeGainedMoneyEvent extends VisualGameEvent {
	public int amountGained;
	public int player;

	public TradeGainedMoneyEvent(int amount, int pid) {
		super();
		this.amountGained = amount;
		this.player = pid;
	}

	public void setTotalFrames(int frames) {
		this.totalFrames = frames;
	}
}
