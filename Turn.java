package games.ttd;

public enum Turn {
	UP(0, 0, -1),
	RIGHT(1, 1, 0),
	DOWN(2, 0, 1),
	LEFT(3, -1, 0),
	NOP(4, 0, 0),
	INVALID(-1, -1, -1);
	

    public static final int[] dr = {-1, 0, 1, 0};
    public static final int[] dc = {0, 1, 0, -1};
	
	private int dir;
    private int r, c;
	
	private Turn(int dir, int r, int c) {
		this.dir = dir;
        this.r = r;
        this.c = c;
	}

    public void setSquare(int r, int c) {
        this.r = r;
        this.c = c;
    }

    public int r() {
        return this.r;
    }

    public int c() {
        return this.c;
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
        assert r != -1;
        assert c != -1;
		GamePerson gp = new GamePerson(p);
		if (this != NOP) { 
			gp.tracks.add(new Track(r, c, time, dir));
		}
		gp.lastTurn = this;
		return gp;
	}

    public String toString() {
        return r + " " + c + " " + dir;
    }

}
