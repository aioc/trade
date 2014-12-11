package games.ttd;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the current state of a particular player.
 */
public class GamePerson {

    public int money;
    public List<Track> tracks;
    public Action action;
    public Action preAction;
    public Turn lastTurn;
    public Statistics stats;

    public GamePerson(int money) {
        this.money = money;
        tracks = new ArrayList<Track>();
        lastTurn = Turn.NOP;
        action = Action.noAction();
        preAction = action;
        stats = new Statistics();
    }

    public GamePerson(GamePerson p) {
        money = p.money;
        action = p.action;
        preAction = p.preAction;
        lastTurn = p.lastTurn;
        stats = new Statistics(p.stats);
    }

    public String toString() {
        return money + " " + preAction;
    }

    public void fromGamePerson(GamePerson p) {
        money = p.money;
        action = p.action;
        preAction = p.preAction;
        lastTurn = p.lastTurn;
        stats = new Statistics(p.stats);
    }
}
