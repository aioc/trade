package games.ttd;

/**
 * Represents the current state of a particular player.
 */
public class GamePerson {
	
	public int money;
	public Action action;
	public Action preAction;
	public Move lastMove;
	public Statistics stats;
	
	public GamePerson(Position p, Direction dir, int money) {
		this.money = money;
		lastMove = Move.NOP;
		action = Action.noAction(money);
		preAction = action;
		stats = new Statistics();
	}
	
	public GamePerson(GamePerson p) {
		money = p.money;
		action = p.action;
		preAction = p.preAction;
		lastMove = p.lastMove;
		stats = new Statistics(p.stats);
	}
	
	public String toString() {
		return money + " " + preAction;
	}
	
	public void fromGamePerson(GamePerson p) {
		money = p.money;
		action = p.action;
		preAction = p.preAction;
		lastMove = p.lastMove;
		stats = new Statistics(p.stats);
	}
}
