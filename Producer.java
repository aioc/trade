package games.ttd;

public class Producer extends Resource {
    public int x, y;
    public int colour;
    public int payoff;

    public Producer(int x, int y, int c, int p) {
        this.x = x;
        this.y = y;
        this.colour = c;
        this.payoff = p;
    }

    public Producer(Producer p) {
        x = p.x;
        y = p.y;
        colour = p.colour;
        payoff = p.payoff;
    }

    public String toString() {
        return x + " " + y + " " + colour + " " + payoff;
    }
}
