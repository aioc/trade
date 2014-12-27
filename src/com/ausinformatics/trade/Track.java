package com.ausinformatics.trade;

public class Track {
	public int r, c;
	public int d;
	public int placedTime;

	public Track(int r, int c, int placedTime, int dir) {
		this.r = r;
		this.c = c;
		this.placedTime = placedTime;
		d = dir;
	}

	public String toString() {
		return r + " " + c + " " + placedTime + " " + d;
	}
}
