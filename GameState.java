package games.ttd;

import java.util.ArrayList;
import java.util.List;

public class GameState {

	private int numPlayers;
	private int boardSize;
    private int numTypes;
    private int numProducers;
    private int numConsumers;
	private GamePerson[] allPlayers;
	private List<Integer> killStart;
	private List<Producer> producers;
	private List<Consumer> consumers;
	
	private GameVisualiser visualReport;

	public GameState(int numPlayers, int boardSize, int numTypes, int numProducers, int numConsumers, GameVisualiser reportTo) {
		this.numPlayers = numPlayers;
		this.boardSize = boardSize;
		this.numTypes = numTypes;
		this.numProducers = numProducers;
		this.numConsumers = numConsumers;
		this.visualReport = reportTo;
		allPlayers = new GamePerson[numPlayers];
		killStart = new ArrayList<Integer>();
		
		// Testing generation.
		for (int i = 0; i < numProducers; i++) {
			producers.add(new Producer(boardSize/numProducers*i, 0, i, i+10));
		}
		for (int i = 0; i < numConsumers; i++) {
			consumers.add(new Consumer(boardSize/numProducers*i, boardSize-1, i, i+10));
		}
	}

	public void setPlayersAction(int playerID, Action a) {
		allPlayers[playerID].action = a;
	}

	public void implementMoves() {
        // TODO implementMoves()

        //for (each move) {
		//	visualReport.addStateToVisualise(allPlayers, events);
		//}
		//for (int i = 0; i < numPlayers; i++) {
		//	allPlayers[i].preAction = allPlayers[i].action;
		//	allPlayers[i].action = Action.noAction(maxHealth);
		//}
	}

	public List<Producer> getProducers() {
		return producers;
	}
	
	public List<Consumer> getConsumers() {
		return consumers;
	}
	
	public int getBoardSize() {
		return boardSize;
	}
    /* TODO  add some getters for the private members */
	
	public int getNumberPlayers() {
		return allPlayers.length;
	}

	public GamePerson getPerson(int playerID) {
		return allPlayers[playerID];
	}

	public void killPlayer(int playerID) {
		killStart.add(playerID);
	}

	public boolean isValidAction(int playerID, Action a) {
        // TODO fix this 
        return false;
	}

}

