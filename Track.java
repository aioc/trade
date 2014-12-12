package games.ttd;

public class Track {
    public int r, c;
    public int placedTime;
    public int dr, dc;

    public Track(int r, int c, int placedTime, int dr, int dc) {
        this.r = r;
        this.c = c;
        this.placedTime = placedTime;
        this.dr = dr;
        this.dc = dc;
    }

    public Track(int r, int c, int placedTime, int dir) {
        final int[] dr = {-1, 0, 1, 0};
        final int[] dc = {0, 1, 0, -1};
        this.r = r;
        this.c = c;
        this.placedTime = placedTime;
        this.dr = dr[dir];
        this.dc = dc[dir];
    }

    public String toString() {
        return r + " " + c + " " + placedTime + " " + dr + " "  + dc;
    }
}
