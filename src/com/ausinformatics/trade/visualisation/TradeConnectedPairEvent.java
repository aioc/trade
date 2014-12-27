package com.ausinformatics.trade.visualisation;

import games.trade.Consumer;
import games.trade.Producer;
import core.visualisation.VisualGameEvent;

public class TradeConnectedPairEvent extends VisualGameEvent {
	public Producer producer;
	public Consumer consumer;
	public int player;

	public TradeConnectedPairEvent(Producer p, Consumer c, int pid) {
		producer = p;
		consumer = c;
		totalFrames = 50;
		player = pid;
	}
}
