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

int haveConnected[MAX_PRODUCERS];

// Data to keep track of the current turn.
int curTurn;


void clientRegister(void) {
	setName("dah-terminator");
	setColour(255, 128, 0);
}

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
}

void clientEntityInfo(int numProducers, int numConsumers, struct producer_info *producers, struct consumer_info *consumers) {
	numProd = numProducers;
	numCons = numConsumers;
	int i;
	for (i = 0; i < numProd; i++) {
		allProducers[i] = producers[i];
		haveConnected[i] = FALSE;
	}
	for (i = 0; i < numCons; i++) {
		allConsumers[i] = consumers[i];
	}
}

void clientPlayerUpdate(int pid, int newMoney) {
	curMoney[pid] = newMoney;	
}

void clientPlayerMoved(int pid, int moveMade, int r, int c, int d) {
	doesOwn[pid][r][c][d] = TRUE;
	turnBuilt[pid][r][c][d] = curTurn;
}

void clientDoTurn(void) {
	// 
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
	curTurn++;
}
