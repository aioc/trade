package games.ttd;

public class Consumer extends Resource {
    public int r, c;
    public int colour;

    public Consumer(int r, int c, int colour) {
        this.r = r;
        this.c = c;
        this.colour = colour;
    }

    public Consumer(Consumer p) {
        r = p.r;
        c = p.c;
        colour = p.colour;
    }

    public String toString() {
        return r + " " + c + " " + colour;
    }
}

