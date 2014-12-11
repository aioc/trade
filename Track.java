package games.ttd;

public class Track {
    public int r, c;
    public int placedTime;
    public int dx, dy;

    public Track(int r, int c, int placedTime, int dx, int dy) {
        this.r = r;
        this.c = c;
        this.placedTime = placedTime;
        this.dx = dx;
        this.dy = dy;
    }

    public Track(int r, int c, int placedTime, int dir) {
        final int[] dx = {-1, 0, 1, 0};
        final int[] dy = {0, 1, 0, -1};
        this.r = r;
        this.c = c;
        this.placedTime = placedTime;
        this.dx = dx[dir];
        this.dy = dy[dir];
    }

    public String toString() {
        return r + " " + c + " " + placedTime + " " + dx + " "  + dy;
    }
}
