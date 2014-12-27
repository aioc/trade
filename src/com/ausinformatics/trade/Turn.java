package com.ausinformatics.trade;

public class Turn {

	public static final Turn NOP = new Turn(4, 0, 0);
	public static final Turn INVALID = new Turn(-1, -1, -1);

	public static final int[] dr = { -1, 0, 1, 0 };
	public static final int[] dc = { 0, 1, 0, -1 };

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
		if (0 <= dir && dir < 4) {
			return new Turn(dir, 0, 0);
		}
		return INVALID;
	}

	public int getDir() {
		return dir;
	}

	// TODO work out what this function does and where it's called from
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
		if (this == NOP) {
			return "";
		}
		return r + " " + c + " " + dir;
	}

}
