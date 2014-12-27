package com.ausinformatics.trade;

public class Producer extends Resource {
	public int payoff;

	public Producer(int r, int c, int colour, int p) {
		super(r, c, colour);
		this.r = r;
		this.c = c;
		this.colour = colour;
		this.payoff = p;
	}

	public Producer(Producer p) {
		super(p.r, p.c, p.colour);
		payoff = p.payoff;
	}

	public String toString() {
		return r + " " + c + " " + colour + " " + payoff;
	}
}
