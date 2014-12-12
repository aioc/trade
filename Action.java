package games.ttd;

import core.server.ClientConnection;
import core.server.DisconnectedException;

public class Action {

    private Turn move;

    private Action(Turn move) {
        this.move = move;
    }

    public static Action getAction(ClientConnection c) throws BadProtocolException,
       DisconnectedException {
           String inputString;
           inputString = c.getStrInput();

           String[] tokens = inputString.split("\\s");
           Action finalA;
           if (tokens.length < 4) {
               throw new BadProtocolException("Getting action: Not enough arguments (got " + inputString + ")");
           } else if (tokens.length > 4) {
               throw new BadProtocolException("Getting action: Too many arguments (got " + inputString + ")");
           } else if (!tokens[0].equals("MOVE")) {
               throw new BadProtocolException("Getting action: Invalid identifier (got " + inputString + ")");
           } else {
               int row = Integer.parseInt(tokens[1]);
               int col = Integer.parseInt(tokens[2]);
               int dir = Integer.parseInt(tokens[3]);
               Turn move = Turn.findTurn(dir);
               if (move == Turn.INVALID) {
                   throw new BadProtocolException("Getting action: Invalid move character (got " + inputString + ")");
               }
               move.setSquare(row, col);
               finalA = new Action(move);
           }
           return finalA;
    }

    public static Action noAction() {
        Turn m = Turn.NOP;
        return new Action(m);
    }

    public Turn getMove() {
        return move;
    }

    @Override
    public String toString() {
        return move + "";
    }
}
