package games.ttd;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.interfaces.GameInstance;
import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class GameRunner implements GameInstance {

    private GameState state;
    private List<PersistentPlayer> players;
    private Map<PersistentPlayer, Integer> results;
    private GameVisualiser visualiser;

    private int[] finalRanks;

    public GameRunner(List<PersistentPlayer> players, int boardSize, int numTypes, int numProducers, int numConsumers) {
        /*
         * BEGIN HACKING CODE
         */
        //for (int i = 0; i < players.size(); i++) {
        //	if (players.get(i).getConnection() instanceof MINARWIN) {
        //		// We want to push it to the end
        //		PersistentPlayer p = players.get(i);
        //		players.remove(i);
        //		players.add(p);
        //		break;
        //	}
        //}
        /*
         * END HACKING CODE
         */
        this.players = players;

        results = new HashMap<PersistentPlayer, Integer>();
        finalRanks = new int[players.size()];
        visualiser = new GameVisualiser(players, boardSize);
        state = new GameState(players.size(), boardSize, numTypes, numProducers, numConsumers, visualiser);
        /*
         * BEGIN HACKING CODE
         */
        //if (players.get(players.size() - 1).getConnection() instanceof MINARWIN) {
        //	// Send it our state
        //	((MINARWIN) players.get(players.size() - 1).getConnection()).giveMeAllYourStates(state, players);
        //}
        /*
         * END HACKING CODE
         */
    }

    private boolean isFinished(int playerIndex) {
        return results.containsKey(players.get(playerIndex));
    }

    private void killPlayers(List<Integer> toKill) {
        for (Integer i : toKill) {
            finalRanks[i] = players.size() - results.size();
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

        while (results.size() < players.size() - 1 /* && moves_exist() */) {
            for (int i = 0; i < players.size(); i++) {
                PersistentPlayer p = players.get(i);
                if (!p.getConnection().isConnected() && isFinished(i)) {
                    continue;
                }
                ClientConnection connection = p.getConnection();
                // First, send the state to everyone
                for (int j = 0; j < players.size(); j++) {
                    GamePerson gp = state.getPerson(i);
                    connection.sendInfo("CASHMONEY " + i + " " + gp.money);
                    if (gp.lastTurn != Turn.INVALID || gp.lastTurn != Turn.NOP)
                        connection.sendInfo("INVEST " + i + " " + gp.lastTurn);
                }
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
                    killPlayers(i);
                }
            }
            state.implementMoves();
        }
        /* The game is over. Check for money, fill in finalRanks and
         * results.put(players.get(i), getReward(finalRanks[i] - 1));
         */
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getConnection().sendInfo("GAMEOVER " + finalRanks[i]);
            if (finalRanks[i] == 1) {
                state.setWinner(i);
            }
        }
        while (visualiser.stillHasStatesQueued() && visualiser.hasVisualised()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (visualiser.hasVisualised()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getVisualisation(Graphics g, int width, int height) {
        visualiser.visualise((Graphics2D) g, width, height);
    }

    @Override
    public void handleWindowResize(int width, int height) {
        visualiser.handleWindowResize(width, height);
    }

    @Override
    public Map<PersistentPlayer, Integer> getResults() {
        return results;
    }

    public int getReward(int pos) {
        return 1 << (players.size() - pos - 1);
    }

}

