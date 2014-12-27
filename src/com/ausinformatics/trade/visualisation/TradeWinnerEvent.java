package com.ausinformatics.trade.visualisation;

import core.visualisation.VisualGameEvent;

public class TradeWinnerEvent extends VisualGameEvent {
	public String playerName;

	public TradeWinnerEvent(String name) {
		super();
		playerName = name;
	}
}
