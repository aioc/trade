package games.trade;

import games.trade.visualisation.TradeConnectedPairEvent;
import games.trade.visualisation.TradeGainedMoneyEvent;
import games.trade.visualisation.TradeGameEvent;
import games.trade.visualisation.TradeLostMoneyEvent;
import games.trade.visualisation.VisualGameState;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import core.interfaces.PersistentPlayer;
import core.visualisation.EventBasedFrameVisualiser;

public class GameState {

	private int numPlayers;
	private int boardSize;
	private int numTypes;
	private int numProducers;
	private int numConsumers;
	private int tick;
	private boolean[][] paidOut;
	private GamePerson[] allPlayers;
	private List<PersistentPlayer> pplayers;
	private List<Integer> killStart;
	private List<Producer> producers;
	private List<Consumer> consumers;

	// Stores the turn of when it was built
	private int board[][][][];

	private EventBasedFrameVisualiser<VisualGameState> vis;

	public GameState(int numPlayers, int boardSize, int numTypes, int numProducers, int numConsumers, int startMoney,
			List<PersistentPlayer> pplayers) {
		this.tick = 0;
		this.numPlayers = numPlayers;
		this.boardSize = boardSize;
		this.numTypes = numTypes;
		this.numProducers = numProducers;
		this.numConsumers = numConsumers;
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
		this.pplayers = pplayers;

		nonbigotedGeneration();
	}

	public void setUpForVisualisation(EventBasedFrameVisualiser<VisualGameState> vis) {
		this.vis = vis;

		VisualGameState s = vis.getCurState();
		s.producers = producers;
		s.consumers = consumers;
		s.boardSize = boardSize;
		s.money = new int[allPlayers.length];
		s.colours = new Color[allPlayers.length];
		s.names = new String[allPlayers.length];
		for (int i = 0; i < allPlayers.length; i++) {
			s.money[i] = allPlayers[i].money;
			s.names[i] = ((Player) pplayers.get(i)).getName();
			int red = (((Player) pplayers.get(i)).getColour() >> 16) & 0xFF;
			int green = (((Player) pplayers.get(i)).getColour() >> 8) & 0xFF;
			int blue = ((Player) pplayers.get(i)).getColour() & 0xFF;
			s.colours[i] = new Color(red, green, blue);
			s.stats = new Statistics[allPlayers.length];
		}
		s.winner = null;
		s.init();
	}

	private List<Resource> changeToResourceList(List<? extends Resource> l) {
		ArrayList<Resource> ret = new ArrayList<Resource>();
		for (int i = 0; i < l.size(); i++) {
			ret.add((Resource) l.get(i));
		}
		return ret;
	}

	private void nonbigotedGeneration() {
		final int minPayoff = 2;
		final int maxPayoff = 5;
		final int iterations = 100;

		Random rand = new Random();
		for (int i = 0; i < numProducers; i++) {
			producers.add(new Producer(0, 0, rand.nextInt(numTypes), rand.nextInt(maxPayoff - minPayoff + 1)
					+ minPayoff));
		}
		for (int i = 0; i < numConsumers; i++) {
			consumers.add(new Consumer(0, 0, rand.nextInt(numTypes)));
		}
		for (int i = 0; i < numProducers; i++) {
			producers.set(i, (Producer) doJump(producers.get(i)));
		}
		for (int i = 0; i < numConsumers; i++) {
			consumers.set(i, (Consumer) doJump(consumers.get(i)));
		}

		for (int iter = 0; iter < iterations; iter++) {
			ArrayList<Integer> prodOrder = new ArrayList<Integer>();
			ArrayList<Integer> consOrder = new ArrayList<Integer>();
			for (int i = 0; i < Math.max(numProducers, numConsumers); i++) {
				if (i < numProducers)
					prodOrder.add(i);
				if (i < numConsumers)
					consOrder.add(i);
			}
			Collections.shuffle(prodOrder);
			Collections.shuffle(consOrder);
			for (int i = 0; i < prodOrder.size(); i++) {
				producers.set(
						prodOrder.get(i),
						(Producer) tryJump(prodOrder.get(i), changeToResourceList(producers),
								changeToResourceList(consumers)));
			}
			for (int i = 0; i < consOrder.size(); i++) {
				consumers.set(
						consOrder.get(i),
						(Consumer) tryJump(consOrder.get(i), changeToResourceList(consumers),
								changeToResourceList(producers)));
			}
		}
	}

	private Resource tryJump(int index, List<Resource> mainType, List<Resource> auxType) {

		final int neighbourhoodRadius = 8;

		// Same type, different colour
		final int sameLowA = 1;
		final double sameLowIntolerenceA = 0.00;
		final int sameHighA = 3;
		final double sameHighIntolerenceA = 0.8;
		// Different type, different colour
		final int differentLowA = 0;
		final double differentLowIntolerenceA = 0.0;
		final int differentHighA = 0;
		final double differentHighIntolerenceA = 0.0;

		// Same type, same colour
		final int sameLowC = 0;
		final double sameLowIntolerenceC = 0.05;
		final int sameHighC = 0;
		final double sameHighIntolerenceC = 0.8;
		// Different type, same colour
		final int differentLowC = 0;
		final double differentLowIntolerenceC = 0.05;
		final int differentHighC = 0;
		final double differentHighIntolerenceC = 1.00;

		double jumpChance = 0.0;

		int numSameDiffColour = 0;
		int numSameSameColour = 0;
		for (int i = 0; i < mainType.size(); i++) {
			if (Math.abs(mainType.get(i).r - mainType.get(index).r) <= neighbourhoodRadius
					&& Math.abs(mainType.get(i).c - mainType.get(index).c) <= neighbourhoodRadius) {
				if (mainType.get(i).colour == mainType.get(index).colour) {
					numSameSameColour++;
				} else {
					numSameDiffColour++;
				}
			}
		}
		int numDifferentDiffColour = 0;
		int numDifferentSameColour = 0;
		for (int i = 0; i < auxType.size(); i++) {
			if (Math.abs(auxType.get(i).r - mainType.get(index).r) <= neighbourhoodRadius
					&& Math.abs(auxType.get(i).c - mainType.get(index).c) <= neighbourhoodRadius) {
				if (auxType.get(i).colour == mainType.get(index).colour) {
					numDifferentSameColour++;
				} else {
					numDifferentDiffColour++;
				}
			}
		}
		// jumpChance += nondifferentIntolerence * Math.max(differentRequirement
		// - numDifferent, 0);
		if (numSameSameColour < sameLowC) {
			jumpChance += sameLowIntolerenceC * (sameLowC - numSameSameColour);
		} else if (numSameSameColour > sameHighC) {
			jumpChance += sameHighIntolerenceC * (numSameSameColour - sameHighC);
		}
		if (numSameDiffColour < sameLowA) {
			jumpChance += sameLowIntolerenceA * (sameLowA - numSameDiffColour);
		} else if (numSameDiffColour > sameHighA) {
			jumpChance += sameHighIntolerenceA * (numSameDiffColour - sameHighA);
		}

		if (numDifferentSameColour < differentLowC) {
			jumpChance += differentLowIntolerenceC * (differentLowC - numDifferentSameColour);
		} else if (numDifferentSameColour > differentHighC) {
			jumpChance += differentHighIntolerenceC * (numDifferentSameColour - differentHighC);
		}
		if (numDifferentDiffColour < differentLowA) {
			jumpChance += differentLowIntolerenceA * (differentLowA - numDifferentDiffColour);
		} else if (numDifferentDiffColour > differentHighA) {
			jumpChance += differentHighIntolerenceA * (numDifferentDiffColour - differentLowA);
		}

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
				if (newR == producers.get(i).r && newC == producers.get(i).c) {
					bad = true;
				}
			}
			for (int i = 0; !bad && i < numConsumers; i++) {
				if (newR == consumers.get(i).r && newC == consumers.get(i).c) {
					bad = true;
				}
			}
		} while (bad);
		if (resource instanceof Producer) {
			Producer newProducer = new Producer((Producer) resource);
			newProducer.r = newR;
			newProducer.c = newC;
			return (Resource) newProducer;
		} else if (resource instanceof Consumer) {
			Consumer newConsumer = new Consumer((Consumer) resource);
			newConsumer.r = newR;
			newConsumer.c = newC;
			return (Resource) newConsumer;
		} else {
			assert 1 == 0;
			return null;
		}
	}

	public void setPlayersAction(int playerID, Action a) {
		allPlayers[playerID].action = a;
	}

	private Integer manhattanDist(Resource r1, Resource r2) {
		return Math.abs(r1.r - r2.r) + Math.abs(r1.c - r2.c);
	}

	private Pair<Consumer, List<GamePerson>> pathFind(Producer p, int player) {
		PriorityQueue<DijkstraState> pq = new PriorityQueue<>();

		boolean seen[][] = new boolean[boardSize][boardSize];
		DijkstraState bestForSquare[][] = new DijkstraState[boardSize][boardSize];
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				bestForSquare[i][j] = new DijkstraState(i, j, Integer.MAX_VALUE, new ArrayList<GamePerson>());
			}
		}
		bestForSquare[p.r][p.c].cost = 0;
		pq.add(bestForSquare[p.r][p.c]);

		while (pq.size() > 0) {
			DijkstraState cur = pq.remove();
			while (seen[cur.r][cur.c] && pq.size() > 0) {
				cur = pq.remove();
			}
			if (seen[cur.r][cur.c]) {
				break;
			}
			seen[cur.r][cur.c] = true;
			for (int k = 0; k < 4; k++) {
				if (board[cur.r][cur.c][k][player] > 0) {
					int newR = cur.r + Turn.dr[k];
					int newC = cur.c + Turn.dc[k];
					// Do we add stuff on?
					if (board[cur.r][cur.c][k][player] == 0)
						continue;
					List<GamePerson> newOwners = new ArrayList<>();
					int bestTurn = board[cur.r][cur.c][k][player] - 1;
					for (int i = 0; i < numPlayers; i++) {
						if (board[cur.r][cur.c][k][i] < bestTurn && board[cur.r][cur.c][k][i] > 0) {
							newOwners = new ArrayList<>();
							newOwners.add(allPlayers[i]);
							bestTurn = board[cur.r][cur.c][k][i];
						} else if (board[cur.r][cur.c][k][i] == bestTurn && board[cur.r][cur.c][k][i] > 0) {
							newOwners.add(allPlayers[i]);
						}
					}
					int newCost = cur.cost + newOwners.size();
					if (newCost < bestForSquare[newR][newC].cost) {
						DijkstraState newState = new DijkstraState(newR, newC, newCost, new ArrayList<GamePerson>(
								cur.otherTracksUsed));
						newState.otherTracksUsed.addAll(newOwners);
						bestForSquare[newR][newC] = newState;
						pq.add(newState);
					}
				}
			}
		}
		// Now to find the best consumer. It is the one with the least manhattan
		// distance, then
		// least cost
		int bestCost = Integer.MAX_VALUE;
		int bestDis = Integer.MAX_VALUE;
		Consumer curCons = null;
		List<GamePerson> curTracks = new ArrayList<>();

		for (Consumer c : consumers) {
			int manDis = manhattanDist(c, p);
			int cost = bestForSquare[c.r][c.c].cost;
			if (seen[c.r][c.c] && (manDis < bestDis || (manDis == bestDis && cost < bestCost))) {
				bestDis = manDis;
				bestCost = cost;
				curCons = c;
				curTracks = bestForSquare[c.r][c.c].otherTracksUsed;
			}
		}
		return new Pair<Consumer, List<GamePerson>>(curCons, curTracks);
	}

	public void implementMoves() {
		this.tick++;
		// First of all, apply all moves.
		for (int i = 0; i < numPlayers; i++) {
			GamePerson newP = allPlayers[i].action.getMove().applyToPlayer(allPlayers[i], tick);
			if (newP.lastTurn == Turn.NOP) {
				allPlayers[i] = newP;
				continue;
			}
			Turn t = newP.lastTurn;
			int newR = t.r() + Turn.dr[t.getDir()];
			int newC = t.c() + Turn.dc[t.getDir()];
			if (newR < 0 || newR >= boardSize || newC < 0 || newC >= boardSize
					|| board[t.r()][t.c()][t.getDir()][i] != 0 || newP.money <= 0) {
				newP = allPlayers[i];
				allPlayers[i].lastTurn = Turn.INVALID;
			} else {
				board[t.r()][t.c()][t.getDir()][i] = tick;
				board[newR][newC][(t.getDir() + 2) % 4][i] = tick;
				newP.money--;
				vis.giveEvent(new TradeLostMoneyEvent(1, i));
			}
			allPlayers[i] = newP;
			if (allPlayers[i].lastTurn != Turn.INVALID) {
				vis.giveEvent(new TradeGameEvent(newP.lastTurn, i));
			}
		}
		for (int i = 0; i < numProducers; i++) {
			for (int j = 0; j < numPlayers; j++) {
				if (paidOut[i][j])
					continue;
				Pair<Consumer, List<GamePerson>> path = pathFind(producers.get(i), j);
				if (path.getL() != null) {
					int amountGained = producers.get(i).payoff * manhattanDist(producers.get(i), path.getL())
							- path.getR().size();
					allPlayers[j].money += amountGained;
					vis.giveEvent(new TradeConnectedPairEvent(producers.get(i), path.getL(), j));
					vis.giveEvent(new TradeGainedMoneyEvent(amountGained, j));
					for (GamePerson p : path.getR()) {
						int player = 0;
						for (; player < allPlayers.length; player++)
							if (p == allPlayers[player])
								break;
						vis.giveEvent(new TradeGainedMoneyEvent(1, player));
						p.money++;
					}
					paidOut[i][j] = true;
				}
			}
		}
		for (Integer i : killStart) {
			allPlayers[i].money = -9001;
			// TODO: Send event
		}
		for (int i = 0; i < numPlayers; i++) {
			allPlayers[i].preAction = allPlayers[i].action;
			allPlayers[i].action = Action.noAction();
		}
	}

	public boolean gameOver() {
		// The game is over if:
		// There are no players with > 0 money, or
		// There are no more producers to hook up, or
		// Both players send null moves on the previous turn, or
		// Only one player with any money is left
		int haveMoney = 0;
		for (int i = 0; i < numPlayers && haveMoney <= 1; i++) {
			if (allPlayers[i].money > 0) {
				haveMoney++;
			}
		}
		if (haveMoney <= 1)
			return true;
		boolean noProd = true;
		for (int i = 0; i < numProducers && noProd; i++) {
			for (int j = 0; j < numPlayers; j++) {
				if (!paidOut[i][j]) {
					noProd = false;
				}
			}
		}
		if (noProd)
			return true;
		boolean allNull = tick > 1;
		for (int i = 0; i < numPlayers && allNull; i++) {
			if (allPlayers[i].lastTurn != Turn.NOP) {
				allNull = false;
			}
		}
		if (allNull)
			return true;
		return false;
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

	public int getNumberPlayers() {
		return allPlayers.length;
	}

	public GamePerson getPerson(int playerID) {
		return allPlayers[playerID];
	}

	public void killPlayer(int playerID) {
		killStart.add(playerID);
		allPlayers[playerID].lastTurn = Turn.NOP;
	}

	public boolean isValidAction(int playerID, Action a) {
		return a.getMove() != Turn.INVALID
				&& (a.getMove().r() < boardSize && a.getMove().c() < boardSize && a.getMove().r() >= 0 && a.getMove()
						.c() >= 0);
	}
}
