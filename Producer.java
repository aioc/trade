package games.ttd;

public class Producer extends Resource {
    public int r, c;
    public int colour;
    public int payoff;

    public Producer(int r, int c, int colour, int p) {
        this.r = r;
        this.c = c;
        this.colour = colour;
        this.payoff = p;
    }

    public Producer(Producer p) {
        r = p.r;
        c = p.c;
        colour = p.colour;
        payoff = p.payoff;
    }

    public String toString() {
        return r + " " + c + " " + colour + " " + payoff;
    }
}
