package com.ausinformatics.trade.visualisation;

import java.util.ArrayList;
import java.util.List;

import com.ausinformatics.trade.Track;
import com.ausinformatics.trade.Turn;

import core.visualisation.VisualGameEvent;

public class TradeGameEvent extends VisualGameEvent {
	public List<Track> tracks;
	public int player;

	public TradeGameEvent() {
		super();
		tracks = new ArrayList<Track>();
		player = -1;
	}

	public TradeGameEvent(Turn lastTurn, int pid) {
		super();
		tracks = new ArrayList<Track>();
		player = pid;
		if (lastTurn != Turn.INVALID || lastTurn != Turn.NOP) {
			// I don't think we care about the time argument to Track here;
			// that's only useful for state
			// This should probably be properly reworked, then.
			tracks.add(new Track(lastTurn.r(), lastTurn.c(), -1, lastTurn.getDir()));
		}
	}

	public void addTrack(Track track) {
		tracks.add(track);
	}

	public void setTotalFrames(int tf) {
		this.totalFrames = tf;
	}
}
