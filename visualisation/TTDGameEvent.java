package games.ttd.visualisation;

import core.visualisation.VisualGameEvent;
import games.ttd.Track;

import java.util.ArrayList;
import java.util.List;

public class TTDGameEvent extends VisualGameEvent {
	public List<Track> tracks;
	
	public TTDGameEvent() {
		tracks = new ArrayList<Track>();
	}
	
	public void addTrack(Track track) {
		tracks.add(track);
	}
}
