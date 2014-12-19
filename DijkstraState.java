package games.trade;

import java.util.ArrayList;
import java.util.List;

public class DijkstraState implements Comparable<DijkstraState> {

	public int r, c, cost;
	public List<GamePerson> otherTracksUsed;

	public DijkstraState(int r, int c, int cost, List<GamePerson> otherTracksUsed) {
		this.r = r;
		this.c = c;
		this.cost = cost;
		this.otherTracksUsed = otherTracksUsed;
	}

	public DijkstraState(DijkstraState d) {
		r = d.r;
		c = d.c;
		cost = d.cost;
		this.otherTracksUsed = new ArrayList<>(d.otherTracksUsed);
	}

	@Override
	public int compareTo(DijkstraState o) {
		return cost - o.cost;
	}

}
