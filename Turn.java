package games.ttd;

public enum Turn {
	NOP(0, 0, 0),
	UP(0, 0, -1),
	RIGHT(1, 1, 0),
	DOWN(2, 0, 1),
	LEFT(3, -1, 0),
	INVALID(-1, -1, -1);
	
	private int dir;
    private int x, y;
	
	private Turn(int dir, int dx, int dy) {
		this.dir = dir;
        x = y = -1;
	}

    public void setSquare(int x, int y) {
        this.x = x;
        this.y = y;
    }
	
	public static Turn findTurn(int dir) {
		for (Turn m: Turn.values()) {
			if (m.dir == dir) {
				return m;
			}
		}
		return INVALID;
	}
	
	public int getDir() {
		return dir;
	}
	
    //TODO work out what this function does and where it's called from
	public boolean isApplyable() {
        return false;
	}
	
	public GamePerson applyToPlayer(GamePerson p, int time) {
        assert x != -1;
        assert y != -1;
		GamePerson gp = new GamePerson(p);
        gp.tracks.add(new Track(x, y, time, dir));
		return gp;
	}

    public String toString() {
        return x + " " + y + " " + dir;
    }

}
