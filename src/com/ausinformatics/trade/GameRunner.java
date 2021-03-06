package com.ausinformatics.trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ausinformatics.phais.core.interfaces.PersistentPlayer;
import com.ausinformatics.phais.core.server.ClientConnection;
import com.ausinformatics.phais.core.server.DisconnectedException;
import com.ausinformatics.phais.core.visualisation.EndGameEvent;
import com.ausinformatics.phais.core.visualisation.EventBasedFrameVisualiser;
import com.ausinformatics.phais.core.visualisation.GameHandler;
import com.ausinformatics.trade.visualisation.TradeWinnerEvent;
import com.ausinformatics.trade.visualisation.VisualGameState;

public class GameRunner implements GameHandler {

	private GameState state;
	private List<PersistentPlayer> players;
	private Map<PersistentPlayer, Integer> results;
	private EventBasedFrameVisualiser<VisualGameState> vis;

	private int[] finalRanks;

	public GameRunner(List<PersistentPlayer> players, int boardSize, int numTypes, int numProducers, int numConsumers,
			int startingMoney) {
		this.players = players;

		results = new HashMap<PersistentPlayer, Integer>();
		finalRanks = new int[players.size()];
		state = new GameState(players.size(), boardSize, numTypes, numProducers, numConsumers, startingMoney, players);
	}

	public void setEventVisualiser(EventBasedFrameVisualiser<VisualGameState> vis) {
		this.vis = vis;
		state.setUpForVisualisation(vis);
	}

	private boolean isFinished(int playerIndex) {
		return results.containsKey(players.get(playerIndex));
	}

	private void killPlayers(List<Integer> toKill) {
		for (Integer i : toKill) {
			finalRanks[i] = players.size() - results.size() - toKill.size() + 1;
		}
		for (Integer i : toKill) {
			results.put(players.get(i), getReward(finalRanks[i] - 1));
		}
	}

	@Override
	public void begin() {
		// INITIAL STATE
		List<Producer> prods = state.getProducers();
		List<Consumer> cons = state.getConsumers();
		for (int i = 0; i < players.size(); i++) {
			PersistentPlayer p = players.get(i);
			ClientConnection connection = p.getConnection();
			for (int j = 0; j < prods.size(); j++)
				connection.sendInfo("PROD " + prods.get(j));
			for (int j = 0; j < cons.size(); j++)
				connection.sendInfo("CONS " + cons.get(j));
		}

		while (results.size() < players.size() - 1 && !state.gameOver()) {
			for (int i = 0; i < players.size(); i++) {
				PersistentPlayer p = players.get(i);
				if (!p.getConnection().isConnected() || isFinished(i)) {
					continue;
				}
				ClientConnection connection = p.getConnection();
				// First, send the state to everyone
				for (int j = 0; j < players.size(); j++) {
					GamePerson gp = state.getPerson(j);
					connection.sendInfo("CASHMONEY " + j + " " + gp.money);
				}
				for (int j = 0; j < players.size(); j++) {
					GamePerson gp = state.getPerson(j);
					connection.sendInfo("INVEST " + j + " " + gp.lastTurn);
				}
			}
			for (int i = 0; i < players.size(); i++) {
				PersistentPlayer p = players.get(i);
				if (!p.getConnection().isConnected() || isFinished(i)) {
					state.setPlayersAction(i, Action.noAction());
					if (state.getPerson(i).money > 0 && !isFinished(i)) {
						state.killPlayer(i);
					}
					continue;
				}
				ClientConnection connection = p.getConnection();
				p.getConnection().sendInfo("YOURMOVE");
				boolean playerDied = false;
				try {
					Action a = Action.getAction(connection);
					// Some verification
					if (!state.isValidAction(i, a)) {
						playerDied = true;
						connection.sendInfo("BADPROT Invalid action " + a.toString());
					} else {
						state.setPlayersAction(i, a);
					}
				} catch (DisconnectedException ex) {
					playerDied = true;
				} catch (BadProtocolException ex) {
					connection.sendInfo("BADPROT Invalid action. " + ex.getExtraInfo());
					playerDied = true;
				}
				if (playerDied) {
					state.killPlayer(i);
					killPlayers(Arrays.asList(i));
					state.setPlayersAction(i, Action.noAction());
				}
			}
			state.implementMoves();
			for (int i = 0; i < players.size(); i++) {
				if (state.getPerson(i).lastTurn == Turn.INVALID) {
					state.killPlayer(i);
					killPlayers(Arrays.asList(i));
					state.getPerson(i).lastTurn = Turn.NOP;
				}
			}
		}
		while (results.size() < players.size()) {
			int minV = -1;
			List<Integer> curMin = null;
			for (int i = 0; i < players.size(); i++) {
				if (!isFinished(i) && (minV == -1 || (state.getPerson(i).money < state.getPerson(minV).money))) {
					minV = i;
					curMin = new ArrayList<Integer>();
					curMin.add(i);
				} else if (!isFinished(i) && (minV == -1 || (state.getPerson(i).money == state.getPerson(minV).money))) {
					curMin.add(i);
				}
			}
			killPlayers(curMin);
		}
		int amoWinners = 0;
		String name = "";
		for (int i = 0; i < players.size(); i++) {
			players.get(i).getConnection().sendInfo("GAMEOVER " + finalRanks[i]);
			if (finalRanks[i] == 1) {
				amoWinners++;
				name = players.get(i).getName();
				// state.setWinner(i);
			}
		}
		if (amoWinners > 1) {
			name = "";
		}
		vis.giveEvent(new TradeWinnerEvent(name));
		vis.giveEvent(new EndGameEvent());
		int round = 0;
		while (!vis.finishedVisualising() && vis.isVisualising() && round < 500) {
			try {
				round++;
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (vis.isVisualising()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public Map<PersistentPlayer, Integer> getResults() {
		return results;
	}

	public int getReward(int pos) {
		return 1 + (players.size() - pos - 1) * 10000;
	}

}
