package games.ttd.visualisation;

import games.ttd.Consumer;
import games.ttd.Producer;
import core.visualisation.VisualGameEvent;

public class TTDConnectedPairEvent extends VisualGameEvent {
	public Producer producer;
	public Consumer consumer;
	public int player;
	
	public TTDConnectedPairEvent(Producer p, Consumer c, int pid) {
		producer = p;
		consumer = c;
		totalFrames = 50;
		player = pid;
	}
}
