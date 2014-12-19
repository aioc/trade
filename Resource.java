package games.trade;

import java.awt.Color;

class Resource {
	public int r, c;
	public int colour;

	public Resource(int r, int c, int colour) {
		this.r = r;
		this.c = c;
		this.colour = colour;
	}

	public Color getColour() {
		final Color[] colours = new Color[8];
		colours[0] = Color.BLUE;
		colours[1] = Color.CYAN;
		colours[2] = Color.GREEN;
		colours[3] = Color.MAGENTA;
		colours[4] = Color.ORANGE;
		colours[5] = Color.PINK;
		colours[6] = Color.RED;
		colours[7] = Color.YELLOW;
		return colours[colour];
	}
}
