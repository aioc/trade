package games.ttd;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		producers = new ArrayList<Producer>();
		consumers = new ArrayList<Consumer>();
		
		// Placeholder generation.
		/*
		for (int i = 0; i < numProducers; i++) {
			producers.add(new Producer(boardSize/numProducers*i, 0, i, i+10));
		}
		for (int i = 0; i < numConsumers; i++) {
			consumers.add(new Consumer(boardSize/numProducers*i, boardSize-1, i));
		}
		*/
		nonbigotedGeneration(8, 0.05, 2, 0.05, 10, 15, 100);
	}

	private void nonbigotedGeneration(int neighbourhoodRadius, double sameIntolerence, int differentRequirement, double nondifferentIntolerence, int minPayoff, int maxPayoff, int iterations) {
		Random rand = new Random();
		for (int i = 0; i < numProducers; i++) {
			producers.add(new Producer(0, 0, rand.nextInt(numTypes), rand.nextInt(maxPayoff-minPayoff+1)+minPayoff));
		}
		for (int i = 0; i < numConsumers; i++) {
			consumers.add(new Consumer(0, 0, rand.nextInt(numTypes)));
		}
		for (int i = 0; i < numProducers; i++) {
			jumpProducer(i);
		}
		for (int i = 0; i < numConsumers; i++) {
			jumpConsumer(i);
		}
		// TODO: jump people in random order. (java.util.Collections.shuffle() a range of indices)
		for (int iter = 0; iter < iterations; iter++) {
			for (int i = 0; i < numProducers; i++) {
				tryJumpProducer(i, neighbourhoodRadius, sameIntolerence, differentRequirement, nondifferentIntolerence);
			}
			for (int i = 0; i < numConsumers; i++) {
				tryJumpConsumer(i, neighbourhoodRadius, sameIntolerence, differentRequirement, nondifferentIntolerence);
			}
		}
	}
	
	private boolean tryJumpProducer(int index, int neighbourhoodRadius, double sameIntolerence, int differentRequirement, double nondifferentIntolerence) {
		double jumpChance = 0.0;
		for (int i = 0; i < numProducers; i++) {
			if (Math.abs(producers.get(i).x - producers.get(index).x) <= neighbourhoodRadius && 
					Math.abs(producers.get(i).y - producers.get(index).y) <= neighbourhoodRadius) {
				jumpChance += sameIntolerence;
			}
		}
		int numDifferent = 0;
		for (int i = 0; i < numConsumers; i++) {
			if (Math.abs(consumers.get(i).x - producers.get(index).x) <= neighbourhoodRadius && 
					Math.abs(consumers.get(i).y - producers.get(index).y) <= neighbourhoodRadius) {
				numDifferent += 1;
				
			}
		}
		jumpChance += nondifferentIntolerence*Math.max(differentRequirement - numDifferent, 0);
		Random rand = new Random();
		if (rand.nextDouble() < jumpChance) {
			jumpProducer(index);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean tryJumpConsumer(int index, int neighbourhoodRadius, double sameIntolerence, int differentRequirement, double nondifferentIntolerence) {
		double jumpChance = 0.0;
		for (int i = 0; i < numConsumers; i++) {
			if (Math.abs(consumers.get(i).x - consumers.get(index).x) <= neighbourhoodRadius && 
					Math.abs(consumers.get(i).y - consumers.get(index).y) <= neighbourhoodRadius) {
				jumpChance += sameIntolerence;
			}
		}
		int numDifferent = 0;
		for (int i = 0; i < numProducers; i++) {
			if (Math.abs(producers.get(i).x - consumers.get(index).x) <= neighbourhoodRadius && 
					Math.abs(producers.get(i).y - consumers.get(index).y) <= neighbourhoodRadius) {
				numDifferent += 1;
			}
		}
		jumpChance += nondifferentIntolerence*Math.max(differentRequirement - numDifferent, 0);
		Random rand = new Random();
		if (rand.nextDouble() < jumpChance) {
			jumpConsumer(index);
			return true;
		} else {
			return false;
		}
	}

	private void jumpProducer(int index) {
		Random rand = new Random();
		int newX;
		int newY;
		tryagain:
		while (true) {
			newX = rand.nextInt(boardSize);
			newY = rand.nextInt(boardSize);
			for (int i = 0; i < numProducers; i++) {
				if (newX == producers.get(i).x &&
						newY == producers.get(i).y) {
					continue tryagain;
				}
			}
			for (int i = 0; i < numConsumers; i++) {
				if (newX == consumers.get(i).x &&
						newY == consumers.get(i).y) {
					continue tryagain;
				}
			}
			break;
		}
		Producer newProducer = new Producer(producers.get(index));
		newProducer.x = newX;
		newProducer.y = newY;
		producers.set(index, newProducer);
	}
	
	private void jumpConsumer(int index) {
		Random rand = new Random();
		int newX;
		int newY;
		tryagain:
		while (true) {
			newX = rand.nextInt(boardSize);
			newY = rand.nextInt(boardSize);
			for (int i = 0; i < numProducers; i++) {
				if (newX == producers.get(i).x &&
						newY == producers.get(i).y) {
					continue tryagain;
				}
			}
			for (int i = 0; i < numConsumers; i++) {
				if (newX == consumers.get(i).x &&
						newY == consumers.get(i).y) {
					continue tryagain;
				}
			}
			break;
		}
		Consumer newConsumer = new Consumer(consumers.get(index));
		newConsumer.x = newX;
		newConsumer.y = newY;
		consumers.set(index, newConsumer);
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
        return a.getMove() != Turn.INVALID && (a.getMove().x() < boardSize && a.getMove().y() < boardSize && a.getMove().x() >= 0 && a.getMove().y() >= 0);
	}

}

