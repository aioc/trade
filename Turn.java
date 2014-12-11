package games.ttd;

public enum Turn {
	NOP('-', 0, 0),
	UP('0', 0, -1),
	RIGHT('1', 1, 0),
	DOWN('2', 0, 1),
	LEFT('3', -1, 0),
	INVALID(' ', -1, -1);
	
	private char c;
    private int dx;
    private int dy;
    private int x, y;
	
	private Turn(char c, int dx, int dy) {
		this.c = c;
		this.dx = dx;
		this.dy = dy;
        x = y = -1;
	}

    public void setSquare(int x, int y) {
        this.x = x;
        this.y = y;
    }
	
	public static Turn findTurn(char c) {
		for (Turn m: Turn.values()) {
			if (m.c == c) {
				return m;
			}
		}
		return INVALID;
	}
	
	public char getChar() {
		return c;
	}
	
    //TODO work out what this function does and where it's called from
	public boolean isApplyable() {
        return false;
	}
	
	public GamePerson applyToPlayer(GamePerson p) {
        assert x != -1;
        assert y != -1;
		GamePerson gp = new GamePerson(p);
        gp.tracks.add(new Track(x, y, dx, dy));
		return gp;
	}
}
