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

int haveTarget;

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
	haveTarget = FALSE;
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


int absV(int n) {return n < 0 ? -n : n;}

int manDis(struct producer_info p, struct consumer_info c) {
	return absV(p.r - c.r) + absV(p.c - c.c);
}


struct producer_info targetFrom;
struct consumer_info targetTo;
int place;

void calcPlace(int *r, int *c, int *d) {
	int disR = absV(targetFrom.r - targetTo.r);
	if (place < disR) {
		if (targetFrom.r < targetTo.r) {
			*r = targetFrom.r + place;
			*d = DOWN;
		} else {
			*r = targetFrom.r - place;
			*d = UP;
		}
		*c = targetFrom.c;
	} else {
		if (targetFrom.c < targetTo.c) {
			*c = targetFrom.c + place - disR;
			*d = RIGHT;
		} else {
			*c = targetFrom.c - place + disR;
			*d = LEFT;
		}
		*r = targetTo.r;
	}
	place++;
}

int aquireTarget(void) {
	int i, j;
	int bestMoney = -1;
	for (i = 0; i < numProd; i++) {
		if (haveConnected[i]) continue;
		for (j = 0; j < numCons; j++) {
			if (allProducers[i].type != allConsumers[j].type) continue;
			int dis = manDis(allProducers[i], allConsumers[j]);
			if (dis <= curMoney[myId]) {
				int gain = dis * allProducers[i].value;
				if (gain > bestMoney) {
					bestMoney = gain;
					place = 0;
					targetFrom = allProducers[i];
					targetTo = allConsumers[j];
				}
			}
		}
	}
	return bestMoney != -1;
}

void finishedConnect(struct producer_info p) {
	int i;
	for (i = 0; i < numProd; i++) {
		if (p.r == allProducers[i].r && p.c == allProducers[i].c) {
			haveConnected[i] = TRUE;
		}
	}
}

void clientDoTurn(void) {
	int shouldMove = TRUE;
	int r, c, d;
	if (!haveTarget) {	
		if (!aquireTarget()) {
			shouldMove = FALSE;
		}
	}
	if (shouldMove) {
		calcPlace(&r, &c, &d);
		while (doesOwn[myId][r][c][d] && shouldMove) {
			if (place == manDis(targetFrom, targetTo)) {
				finishedConnect(targetFrom);
				if (!aquireTarget()) {
					shouldMove = FALSE;
					break;
				}
			}
			calcPlace(&r, &c, &d);
		}
	}
	if (shouldMove) {

		makeMove(r, c, d);
	} else {
		makeNoMove();
	}
	curTurn++;
}
