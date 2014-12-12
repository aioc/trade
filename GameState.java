package games.ttd;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameState {

	private int numPlayers;
	private int boardSize;
    private int numTypes;
    private int numProducers;
    private int numConsumers;
    private int tick;
    private boolean[][] paidOut;
	private GamePerson[] allPlayers;
	private List<Integer> killStart;
	private List<Producer> producers;
	private List<Consumer> consumers;
	
	private GameVisualiser visualReport;

	public GameState(int numPlayers, int boardSize, int numTypes, int numProducers, int numConsumers, GameVisualiser reportTo) {
        this.tick = 0;
		this.numPlayers = numPlayers;
		this.boardSize = boardSize;
		this.numTypes = numTypes;
		this.numProducers = numProducers;
		this.numConsumers = numConsumers;
		this.visualReport = reportTo;
        paidOut = new boolean[numProducers][numPlayers];
        for (int i = 0; i < numProducers; i++) {
            for (int j = 0; j < numPlayers; j++) {
                paidOut[i][j] = false;
            }
        }
		allPlayers = new GamePerson[numPlayers];
		killStart = new ArrayList<Integer>();
		producers = new ArrayList<Producer>();
		consumers = new ArrayList<Consumer>();

		nonbigotedGeneration(8, 0.05, 2, 0.05, 10, 15, 100);
		char[][] map = new char[boardSize][boardSize];
		for (int x = 0; x < boardSize; x++) {
			for (int y = 0; y < boardSize; y++) {
				map[x][y] = '.';
			}
		}
		for (int i = 0; i < numProducers; i++) {
			map[producers.get(i).x][producers.get(i).y] = 'P';
		}
		for (int i = 0; i < numConsumers; i++) {
			map[consumers.get(i).x][consumers.get(i).y] = 'C';
		}

		for (int y = 0; y < boardSize; y++) {
			for (int x = 0; x < boardSize; x++) {
				System.out.print(map[x][y]);
			}
			System.out.println();
		}
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
            doJump(i, producers);
		}
		for (int i = 0; i < numConsumers; i++) {
            doJump(i, consumers);
		}
		for (int iter = 0; iter < iterations; iter++) {
            ArrayList<Integer> prodOrder = new ArrayList<Integer>();
            ArrayList<Integer> consOrder = new ArrayList<Integer>();
            for (int i = 0; i < Math.max(numProducers, numConsumers); i++) {
                if (i < numProducers) prodOrder.add(i);
                if (i < numConsumers) consOrder.add(i);
            }
            Collections.shuffle(prodOrder);
            Collections.shuffle(consOrder);
			for (int i = 0; i < prodOrder.size(); i++) {
                tryJump(prodOrder.get(i), neighbourhoodRadius, sameIntolerence,
                        differentRequirement, nondifferentIntolerence, producers,
                        consumers);
			}
			for (int i = 0; i < consOrder.size(); i++) {
                tryJump(consOrder.get(i), neighbourhoodRadius, sameIntolerence,
                        differentRequirement, nondifferentIntolerence, consumers,
                        producers);
			}
		}
	}

    private boolean tryJump(int index, int neighbourhoodRadius, double sameIntolerence, int differentRequirement, double nondifferentIntolerence, List<? extends Resource> mainType, List<? extends Resource> auxType) {
        double jumpChance = 0.0;
        for (int i = 0; i < mainType.size(); i++) {
            if (Math.abs(mainType.get(i).r - mainType.get(index).r) <= neighbourhoodRadius
                    && Math.abs(mainType.get(i).c - mainType.get(index).c) <= neighbourhoodRadius) {
                jumpChance += sameIntolerence;
            }
        }
        int numDifferent = 0;
        for (int i = 0; i < auxType.size(); i++) {
            if (Math.abs(auxType.get(i).r - mainType.get(index).r) <= neighbourhoodRadius
                    && Math.abs(auxType.get(i).c - mainType.get(index).c) <= neighbourhoodRadius) {
                numDifferent += 1;
            }
        }
        jumpChance += nondifferentIntolerence*Math.max(differentRequirement - numDifferent, 0);
        Random rand = new Random();
        if (rand.nextDouble() < jumpChance) {
            doJump(index, mainType);
            return true;
        } else {
            return false;
        }
    }
	
    private void doJump(int index, List<? extends Resource> resources) {
		Random rand = new Random();
		int newR;
		int newC;
        boolean bad = false;
	    do {
			newR = rand.nextInt(boardSize);
			newC = rand.nextInt(boardSize);
			for (int i = 0; !bad && i < numProducers; i++) {
				if (newR == producers.get(i).r &&
						newC == producers.get(i).c) {
                    bad = true;
				}
			}
			for (int i = 0; !bad && i < numConsumers; i++) {
				if (newR == consumers.get(i).r &&
						newC == consumers.get(i).c) {
                    bad = true;
				}
			}
		} while (bad);
        resources.get(index).r = newR;
        resources.get(index).c = newC;
    }

	public void setPlayersAction(int playerID, Action a) {
		allPlayers[playerID].action = a;
	}

	public void implementMoves() {
        // TODO implementMoves()
        List<GameEvent> events = new ArrayList<GameEvent>();
        // First of all, apply all moves.
        for (int i = 0; i < numPlayers; i++) {
            GamePerson newP = allPlayers[i].action.getMove().applyToPlayer(allPlayers[i], tick);
            Track newTrack = newP.tracks.get(newP.tracks.size() - 1);
            int newR = newTrack.c + newTrack.dr;
            int newC = newTrack.r + newTrack.dc;
            if (newR < 0 || newR >= boardSize || newC < 0 || newC >= boardSize) {
                newP = allPlayers[i];
                allPlayers[i].lastTurn = Turn.NOP;
            }
            allPlayers[i] = newP;
        }
        for (int i = 0; i < numProducers; i++) {
            for (int j = 0; j < numPlayers; j++) {
                if (paidOut[i][j]) continue;
                // TODO check if this producer should pay out.
            }
        }
		visualReport.addStateToVisualise(allPlayers, events);
		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i].preAction = allPlayers[i].action;
			allPlayers[i].action = Action.noAction();
		}
        this.tick++;
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
        return a.getMove() != Turn.INVALID && (a.getMove().r() < boardSize && a.getMove().c() < boardSize && a.getMove().r() >= 0 && a.getMove().c() >= 0);
	}

}

