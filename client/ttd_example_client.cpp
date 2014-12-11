#include <cstdio>
#include <cstdlib>
#include "ttd.h"


/*
 *   This is called when your client connects to the server. You need to
 *   provide a name using setName and a colour with setColour.
 */
void clientRegister() {
	setName("Dumbo-2014");
	setColour(255, 0, 0);
}

// ******** These two functions are only called at the start of the game ********

/*
 *   This is called when the game is about to begin. It tells you the number
 *   of players in the game, how big the board is, the number of different
 *   resource types, the amount of money players start with, and your
 *   player id (0 <= pid < numPlayers).
 *
 *   You are not required to call anything in here. After this function is
 *   called, entityInfo will be called.
 */
void clientInit(int numPlayers, int boardSize, int numResourceTypes, int startMoney, int pid) {

}

/*
 *   This is called when the game is about to begin, after clientInit.
 *   It tells you the number of producers and consumers, then info about them.
 *
 *   You are not required to call anything in here.
 */
void clientEntityInfo(int numProducers, int numConsumers, struct producer_info *producers, struct consumer_info *consumers) {

}

// ******** These two functions will be called *BEFORE* players take their turns for the round ********

/*
 *   This is called once for *every* player in the game (including those 
 *   with <= 0 money), telling you the amount of money they now have. The player
 *   id is such that 0 <= pid < numPlayers.
 *
 *   You are not required to call anything in here.
 */
void clientPlayerUpdate(int pid, int newMoney) {

}

/*
 *   This is called once for all players who made a move last turn. This
 *   will only be called after every call to clientPlayerUpdate has been done.
 *
 *   This informs you of the move made by that player. 0 <= pid < numPlayers,
 *   0 <= r, c < boardSize, and 0 <= d < 4.
 *   This means a track was built or the rights bought between square (r,c)
 *   and the square in direction d from it (where the meaning of d is
 *   defined at the start of this file).
 *
 *   You are not required to call anything in here.
 */
void clientPlayerMoved(int pid, int r, int c, int d) {

}

// ******** This function will be called *WHEN* it is your turn ********

/*
 *   This is called when it is time for you to take your turn. While in the
 *   function, you must call makeMove once. If you make multiple calls to
 *   makeMove, only the final call will be considered.
 *
 *   If you have <= 0 money, then this function will *not* be called.
 */
void clientDoTurn() {

}
