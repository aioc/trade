package games.ttd;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
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

    private int board[][][][];
	
	private GameVisualiser visualReport;

	public GameState(int numPlayers, int boardSize, int numTypes, int numProducers, int numConsumers, int startMoney, GameVisualiser reportTo) {
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
        board = new int[boardSize][boardSize][4][numPlayers];
		allPlayers = new GamePerson[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i] = new GamePerson(startMoney);
		}
		killStart = new ArrayList<Integer>();
		producers = new ArrayList<Producer>();
		consumers = new ArrayList<Consumer>();
		
		nonbigotedGeneration(8, 0.05, 2, 0.05, 10, 15, 100);
		char[][] map = new char[boardSize][boardSize];
		for (int c = 0; c < boardSize; c++) {
			for (int r = 0; r < boardSize; r++) {
				map[c][r] = '.';
			}
		}
		for (int i = 0; i < numProducers; i++) {
			map[producers.get(i).c][producers.get(i).r] = 'P';
		}
		for (int i = 0; i < numConsumers; i++) {
			map[consumers.get(i).c][consumers.get(i).r] = 'C';
		}

		for (int r = 0; r < boardSize; r++) {
			for (int c = 0; c < boardSize; c++) {
				System.out.print(map[c][r]);
			}
			System.out.println();
		}
	}

    private List<Resource> changeToResourceList(List<? extends Resource> l) {
        ArrayList<Resource> ret = new ArrayList<Resource>();
        for (int i = 0; i < l.size(); i++) {
            ret.add((Resource)l.get(i));
        }
        return ret;
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
            producers.set(i, (Producer)doJump(producers.get(i)));
		}
		for (int i = 0; i < numConsumers; i++) {
            consumers.set(i, (Consumer)doJump(consumers.get(i)));
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
                producers.set(prodOrder.get(i), (Producer)tryJump(prodOrder.get(i), neighbourhoodRadius, sameIntolerence,
                        differentRequirement, nondifferentIntolerence, changeToResourceList(producers),
                        changeToResourceList(consumers)));
			}
			for (int i = 0; i < consOrder.size(); i++) {
                consumers.set(consOrder.get(i), (Consumer)tryJump(consOrder.get(i), neighbourhoodRadius, sameIntolerence,
                        differentRequirement, nondifferentIntolerence, changeToResourceList(consumers),
                        changeToResourceList(producers)));
			}
		}
	}

    private Resource tryJump(int index, int neighbourhoodRadius, double sameIntolerence, int differentRequirement, double nondifferentIntolerence, List<Resource> mainType, List<Resource> auxType) {
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
            return doJump(mainType.get(index));
        } else {
            return mainType.get(index);
        }
    }
    
    private Resource doJump(Resource resource) {
		Random rand = new Random();
		int newR;
		int newC;
        boolean bad = false;
	    do {
	    	bad = false;
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
        if (resource instanceof Producer) {
            Producer newProducer = new Producer((Producer)resource);
            newProducer.r = newR;
            newProducer.c = newC;
            return (Resource)newProducer;
        } else if (resource instanceof Consumer) {
            Consumer newConsumer = new Consumer((Consumer)resource);
            newConsumer.r = newR;
            newConsumer.c = newC;
            return (Resource)newConsumer;
        } else {
            assert 1 == 0;
            return null;
        }
        //resources.get(index).r = newR;
        //resources.get(index).c = newC;
    }

	public void setPlayersAction(int playerID, Action a) {
		allPlayers[playerID].action = a;
	}

    private Integer manhattanDist(Pair<Integer, Integer> a, Pair<Integer, Integer> b) {
        return Math.abs(a.getL() - b.getL()) + Math.abs(a.getR() - b.getR());
    }

    private Pair<Integer, List<Integer>> bfs(int producer, int player) {
        Queue<Pair<Pair<Integer, List<Integer>>, Pair<Integer, Integer>>> q = new LinkedList<Pair<Pair<Integer, List<Integer>>, Pair<Integer, Integer>>>();
        Pair<Integer, Integer> producerPoint = new Pair<Integer, Integer>(producers.get(producer).r, producers.get(producer).c);
        q.add(new Pair<Pair<Integer, List<Integer>>, Pair<Integer, Integer>>(new Pair<Integer, List<Integer>>(0, new LinkedList<Integer>()), producerPoint));
        Integer minDist = Integer.MAX_VALUE;
        boolean seen[][] = new boolean[boardSize][boardSize];
        List<Pair<Pair<Integer, List<Integer>>, Integer>> closeConsumers = new ArrayList<Pair<Pair<Integer, List<Integer>>, Integer>>();
        while (q.size() > 0) {
            Pair<Pair<Integer, List<Integer>>, Pair<Integer, Integer>> current = q.remove();
            if (manhattanDist(current.getR(), producerPoint) > minDist) continue;
            Pair<Integer, Integer> point = current.getR();
            for (int i = 0; i < consumers.size(); i++) {
                if (consumers.get(i).r == point.getL() && consumers.get(i).c == point.getR()) {
                    minDist = Math.min(manhattanDist(producerPoint, point), minDist);
                    closeConsumers.add(new Pair<Pair<Integer, List<Integer>>, Integer>(current.getL(), i));
                    break;
                }
            }

            seen[point.getL()][point.getR()] = true;

            final int dr[] = {-1, 0, 1, 0};
            final int dc[] = {0, -1, 0, 1};
            for (int k = 0; k < 4; k++) {
                if (board[point.getL()][point.getR()][k][player] > 0) {
                    int newR = point.getL() + dr[k];
                    int newC = point.getR() + dc[k];
                    int numTracks = 0;
                    int minTime = Integer.MAX_VALUE;
                    List<Integer> owners = new LinkedList<Integer>();
                    for (int i = 0; i < numPlayers; i++) {
                        if (board[point.getL()][point.getR()][k][i] > 0 && board[point.getL()][point.getR()][k][i] != tick
                                && board[point.getL()][point.getR()][k][i] <= minTime) {
                            if (board[point.getL()][point.getR()][k][i] == minTime) {
                                numTracks++;
                                owners.add(i);
                            } else {
                                minTime = board[point.getL()][point.getR()][k][i];
                                owners.clear();
                                owners.add(i);
                                numTracks = 1;
                            }
                        }
                    }
                    if (newC >= 0 && newC < boardSize && newR >= 0 && newR < boardSize && !seen[newR][newC]) {
                        List<Integer> newOwners = current.getL().getR();
                        newOwners.addAll(owners);
                        q.add(new Pair<Pair<Integer, List<Integer>>, Pair<Integer, Integer>>(new Pair<Integer, List<Integer>>(current.getL().getL() + numTracks, newOwners), new Pair<Integer, Integer>(newR, newC)));
                    }
                }
            }
        }
        Pair<Integer, List<Integer>> ret = new Pair<Integer, List<Integer>>(Integer.MAX_VALUE, new ArrayList<Integer>());
        for (int i = 0; i < closeConsumers.size();) {
            Pair<Integer, Integer> consumerPoint = new Pair<Integer, Integer>(consumers.get(closeConsumers.get(i).getR()).r, consumers.get(closeConsumers.get(i).getR()).c);
            if (manhattanDist(producerPoint, consumerPoint) > minDist) closeConsumers.remove(i);
            else {
                if (closeConsumers.get(i).getL().getL() < ret.getL())
                    ret = closeConsumers.get(i).getL();
                i++;
            }
        }
        return ret;
    }

	public void implementMoves() {
        this.tick++;
        // TODO implementMoves()
        List<GameEvent> events = new ArrayList<GameEvent>();
        // First of all, apply all moves.
        for (int i = 0; i < numPlayers; i++) {
            GamePerson newP = allPlayers[i].action.getMove().applyToPlayer(allPlayers[i], tick);
            Track newTrack = newP.tracks.get(newP.tracks.size() - 1);
            int newR = newTrack.c + newTrack.dr;
            int newC = newTrack.r + newTrack.dc;
            boolean goodTrack = true;
            if (newR < 0 || newR >= boardSize || newC < 0 || newC >= boardSize || board[newTrack.r][newTrack.c][newTrack.toDir()][i] != 0) {
                newP = allPlayers[i];
                allPlayers[i].lastTurn = Turn.NOP;
                goodTrack = false;
            }
            if (goodTrack) {
                board[newTrack.r][newTrack.c][newTrack.toDir()][i] = tick;
                board[newR][newC][(newTrack.toDir() + 2) % 4][i] = tick;
                newP.money--;
            }

            allPlayers[i] = newP;
        }
        for (int i = 0; i < numProducers; i++) {
            for (int j = 0; j < numPlayers; j++) {
                if (paidOut[i][j]) continue;
                // TODO check if this producer should pay out.
                // check if there's a path from producer i using player j's
                // tracks
                Pair<Integer, List<Integer>> path = bfs(i, j);
                if (path.getL() >= 0) {
                    // payout P - path
                    allPlayers[i].money = producers.get(i).payoff * (Math.abs(producers.get(i).r - consumers.get(path.getL()).r)
                                                                        + Math.abs(producers.get(i).c - consumers.get(path.getL()).c))
                                             - path.getR().size();
                    for (int k = 0; k < path.getR().size(); k++) {
                        allPlayers[path.getR().get(k)].money++;
                    }
                    paidOut[i][j] = true;
                }
            }
        }
		visualReport.addStateToVisualise(allPlayers, events);
		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i].preAction = allPlayers[i].action;
			allPlayers[i].action = Action.noAction();
		}
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

