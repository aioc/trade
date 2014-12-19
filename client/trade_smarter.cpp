#include <cstdio>
#include <cstdlib>
#include <ctime>
#include "trade.h"

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

#define BLANK			0
#define CONSUMER		1
#define PRODUCER		2

int onSquare[MAX_BOARD_SIZE][MAX_BOARD_SIZE];

int myId;

int haveConnected[MAX_PRODUCERS];

// Data to keep track of the current turn.
int curTurn;

int haveTarget;

void clientRegister(void) {
	setName("dah-smarter");
	setColour(56, 78, 90);
}

void clientInit(int numPlayers, int boardSize, int numResourceTypes, int startMoney, int pid) {
	srand(time(NULL));
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
	for (i = 0; i < boardSize; i++) {
		for (j = 0; j < boardSize; j++) {
			onSquare[i][j] = BLANK;
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
		onSquare[allProducers[i].r][allProducers[i].c] = PRODUCER;
		haveConnected[i] = FALSE;
	}
	for (i = 0; i < numCons; i++) {
		allConsumers[i] = consumers[i];
		onSquare[allConsumers[i].r][allConsumers[i].c] = CONSUMER;
	}
}

void clientPlayerUpdate(int pid, int newMoney) {
	curMoney[pid] = newMoney;	
}

void clientPlayerMoved(int pid, int moveMade, int r, int c, int d) {
	if (moveMade) {
		doesOwn[pid][r][c][d] = TRUE;
		doesOwn[pid][r + dr[d]][c + dc[d]][(d + 2) % 4] = TRUE;
		turnBuilt[pid][r][c][d] = curTurn;
		turnBuilt[pid][r + dr[d]][c + dc[d]][(d + 2) % 4] = curTurn;
	}
}

typedef struct _point point;

struct _point {
	int r, c;
	int dis;
};

int heapB;
point heap[10 * MAX_BOARD_SIZE * MAX_BOARD_SIZE];

void push(point p) {
	heap[heapB] = p;
	int at = heapB++;
	while (at > 1 && heap[at].dis < heap[at / 2].dis) {
		point temp = heap[at];
		heap[at] = heap[at / 2];
		heap[at / 2] = temp;
		at /= 2;
	}
}

point pop(void) {
	point ret = heap[1];
	heap[1] = heap[--heapB];
	int at = 1;
	while (2 * at + 1 < heapB && (heap[at].dis > heap[at * 2].dis || heap[at].dis > heap[at * 2 + 1].dis)) {
		if (heap[at * 2].dis < heap[at * 2 + 1].dis) {
			point temp = heap[at];
			heap[at] = heap[at * 2];
			heap[at * 2] = temp;
			at *= 2;
		} else {
			point temp = heap[at];
			heap[at] = heap[at * 2 + 1];
			heap[at * 2 + 1] = temp;
			at *= 2;
			at++;
		}
	}
	if (2 * at < heapB && heap[at].dis > heap[at * 2].dis) {
		point temp = heap[at];
		heap[at] = heap[at * 2];
		heap[at * 2] = temp;
	}
	return ret;
}



int absV(int n) {return n < 0 ? -n : n;}

int manDis(struct producer_info p, struct consumer_info c) {
	return absV(p.r - c.r) + absV(p.c - c.c);
}

int manDisP(struct producer_info p, point pp) {
	return absV(p.r - pp.r) + absV(p.c - pp.c);
}


int preDir[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
int dis[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
int seen[MAX_BOARD_SIZE][MAX_BOARD_SIZE];

int bestGain;
int pathLength;
point pathToBuy[MAX_BOARD_SIZE * MAX_BOARD_SIZE * 4];
int bestProd;


void findBestCons(int p) {
	heapB = 1;
	point start;
	start.r = allProducers[p].r;
	start.c = allProducers[p].c;
	start.dis = 0;
	push(start);
	preDir[allProducers[p].r][allProducers[p].c] = -1;
	dis[allProducers[p].r][allProducers[p].c] = 0;


	point bestP = {};

	int foundBest = FALSE;
	int i, j;
	for (i = 0; i < size; i++) {
		for (j = 0; j < size; j++) {
			dis[i][j] = 10000000;
			seen[i][j] = FALSE;
		}
	}
	while (heapB > 1) {
		point c = pop();
		while (seen[c.r][c.c] && heapB > 1) {
			c = pop();
		}
		if (seen[c.r][c.c]) {
			break;
		}
		seen[c.r][c.c] = TRUE;
		if (onSquare[c.r][c.c] == CONSUMER) {
			// Work out the gain we get
			int gain = manDisP(allProducers[p], c) * allProducers[p].value - c.dis;
			if (gain > bestGain && c.dis <= curMoney[myId]) {
				bestGain = gain;
				foundBest = TRUE;
				bestP = c;
			}
			continue;
		}
		if (onSquare[c.r][c.c] == PRODUCER && (c.r != allProducers[p].r || c.c != allProducers[p].c)) {
			continue;
		}
		int offset = rand() % 4;
		for (i = 0; i < 4; i++) {
			int ddd = (i + offset) % 4;
			point nP;
			nP.r = c.r + dr[ddd];
			nP.c = c.c + dc[ddd];
			if (nP.r < 0 || nP.c < 0 || nP.r >= size || nP.c >= size) continue;
			if (seen[nP.r][nP.c]) continue;
			int newDis = c.dis;
			if (!doesOwn[myId][c.r][c.c][ddd]) {
				newDis++;
				for (j = 0; j < numPl; j++) {
					if (doesOwn[j][c.r][c.c][ddd]) {
						newDis++;
					}
				}
			} else {
				for (j = 0; j < numPl; j++) {
					if (doesOwn[j][c.r][c.c][ddd] && turnBuilt[j][c.r][c.c][ddd] < turnBuilt[myId][c.r][c.c][ddd]) {
						newDis++;
					}
				}
			}
			if (newDis < dis[nP.r][nP.c]) {
				dis[nP.r][nP.c] = newDis;
				preDir[nP.r][nP.c] = ddd;
				nP.dis = newDis;
				push(nP);
			}
		}
	}
	if (!foundBest) return;
	// Ok, now put the path on
	point c = bestP;
	pathLength = 0;
	while (preDir[c.r][c.c] != -1) {
		point nP;
		nP.r = c.r - dr[preDir[c.r][c.c]];
		nP.c = c.c - dc[preDir[c.r][c.c]];
		nP.dis = preDir[c.r][c.c];
		if (!doesOwn[myId][nP.r][nP.c][nP.dis]) {
			pathToBuy[pathLength++] = nP;
		}
		c = nP;
	}
	bestProd = p;
	printf("From %d %d, to %d %d, %d\n", allProducers[p].r, allProducers[p].c, bestP.r, bestP.c, pathLength);
}


int findTarget(void) {
	// We do a dfs from each producer, to see what the maximum amount
	// of money we can make is
	int i;
	bestGain = -1;
	for (i = 0; i < numProd; i++) {
		// Find best consumer for this
		if (!haveConnected[i]) {
			findBestCons(i);	
		}
	}
	if (bestGain == -1) {
		return FALSE;
	}
	haveTarget = TRUE;
	return TRUE;
}



void clientDoTurn(void) {
	int shouldMove = TRUE; 
	if (!haveTarget) {
		shouldMove = findTarget();
	}
	if (shouldMove) {
		point p = pathToBuy[--pathLength];
		if (pathLength == 0) {
			haveTarget = FALSE;
			haveConnected[bestProd] = TRUE;
		}
		makeMove(p.r, p.c, p.dis);
	} else {
		makeNoMove();
	}
	curTurn++;
}
