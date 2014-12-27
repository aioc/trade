package com.ausinformatics.trade.visualisation;

import com.ausinformatics.phais.core.visualisation.VisualGameEvent;
import com.ausinformatics.trade.Consumer;
import com.ausinformatics.trade.Producer;

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
