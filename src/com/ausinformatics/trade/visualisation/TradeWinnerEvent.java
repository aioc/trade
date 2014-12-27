package com.ausinformatics.trade.visualisation;

import com.ausinformatics.phais.core.visualisation.VisualGameEvent;

public class TradeWinnerEvent extends VisualGameEvent {
	public String playerName;

	public TradeWinnerEvent(String name) {
		super();
		playerName = name;
	}
}
