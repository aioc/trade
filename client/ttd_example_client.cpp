#include <cstdio>
#include <cstdlib>
#include "ttd.h"

int dr[] = {-1, 0, 1, 0};
int dc[] = {0, 1, 0, -1};

// The number of players
int numPl;
// The board size
int size;
int numTypes;
// Does player own the track coming out of that square
int doesOwn[MAX_NUM_PLAYERS][MAX_BOARD_SIZE][MAX_BOARD_SIZE][NUM_DIRECTIONS];

// What turn the track was built coming out of that square
int turnBuilt[MAX_NUM_PLAYERS][MAX_BOARD_SIZE][MAX_BOARD_SIZE][NUM_DIRECTIONS];

// How much money each player has
int curMoney[MAX_NUM_PLAYERS];

// Producer and consumer info
int numProd, numCons;
struct producer_info allProducers[MAX_PRODUCERS];
struct consumer_info allConsumers[MAX_CONSUMERS];

int myId;

// Data to keep track of the current turn.
// The idea is that we can assume that each turn, clientPlayerUpdate will be called at least once,
// then clientPlayerMoved will be called at least once. This means that a new turn occurs when
// a clientPlayerUpdate is called after a clientPlayerMoved (ignore that clientDoTurn could
// be called inbetween).
int curTurn;
int clientPlayerMovedLastCall;


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
	numPl = numPlayers;
	size = boardSize;
	numTypes = numResourceTypes;
	myId = pid;
	int i, j, k, l;
	for (i = 0; i < numPl; i++) {
		curMoney[i] = startMoney;
		for (j = 0; j < boardSize; j++) {
			for (k = 0; k < boardSize; k++) {
				for (l = 0; l < NUM_DIRECTIONS; l++) {
					doesOwn[i][j][k][l] = FALSE;
					turnBuilt[i][j][k][l] = -1;
				}
			}
		}
	}
	curTurn = 0;
	clientPlayerMovedLastCall = FALSE;
}

/*
 *   This is called when the game is about to begin, after clientInit.
 *   It tells you the number of producers and consumers, then info about them.
 *
 *   You are not required to call anything in here.
 */
void clientEntityInfo(int numProducers, int numConsumers, struct producer_info *producers, struct consumer_info *consumers) {
	numProd = numProducers;
	numCons = numConsumers;
	int i;
	for (i = 0; i < numProd; i++) {
		allProducers[i] = producers[i];
	}
	for (i = 0; i < numCons; i++) {
		allConsumers[i] = consumers[i];
	}
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
	// Handle turn logic. See comment at start of file for more information
	if (clientPlayerMovedLastCall) {
		curTurn++;
	}
	clientPlayerMovedLastCall = FALSE;

	curMoney[pid] = newMoney;	
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
	// Handle turn logic. See comment at start of file for more information
	clientPlayerMovedLastCall = TRUE;

	doesOwn[pid][r][c][d] = TRUE;
	turnBuilt[pid][r][c][d] = curTurn;
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
	// Idea: Randomly pick a track to build :)
	int r = rand() % size;
	int c = rand() % size;
	int d = rand() % NUM_DIRECTIONS;
	while (TRUE) {
		// Check if it is valid
		if (r + dr[d] >= 0 && r + dr[d] < size && c + dc[d] >= 0 && c + dc[d] < size) {
			if (!doesOwn[myId][r][c][d]) {
				break;
			}
		}
		r = rand() % size;
		c = rand() % size;
		d = rand() % NUM_DIRECTIONS;
	}
	makeMove(r, c, d);
}
