package games.ttd;

public class Consumer extends Resource {
    public int x, y;
    public int colour;

    public Consumer(int x, int y, int c) {
        this.x = x;
        this.y = y;
        this.colour = c;
    }

    public Consumer(Consumer p) {
        x = p.x;
        y = p.y;
        colour = p.colour;
    }

    public String toString() {
        return x + " " + y + " " + colour;
    }
}

