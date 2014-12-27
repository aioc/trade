package com.ausinformatics.trade;

public class Consumer extends Resource {

	public Consumer(int r, int c, int colour) {
		super(r, c, colour);
	}

	public Consumer(Consumer p) {
		super(p.r, p.c, p.colour);
	}

	public String toString() {
		return r + " " + c + " " + colour;
	}
}
