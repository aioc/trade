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
int doneGG;
int haveTarget;

int rep[MAX_BOARD_SIZE * MAX_BOARD_SIZE];



void clientRegister(void) {
	setName("dah-terminator");
	setColour(0, 0, 0);
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
			rep[i * boardSize + j] = i * boardSize + j;
		}
	}
	curTurn = 0;
	doneGG = FALSE;
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

int findRep(int n) {
	if (rep[n] == n) {
		return n;
	}
	return rep[n] = findRep(rep[n]);
}

void join(int a, int b) {
	rep[findRep(a)] = findRep(b);
}

int nn(int r, int c) {
	return r * size + c;
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
		
		// Check out if there are consumers
		int isCons = FALSE;
		int closeCon = 0;
		for (i = 0; i < numCons; i++) {
			if (findRep(nn(allConsumers[i].r, allConsumers[i].c)) == findRep(nn(c.r, c.c))) {
				if (!isCons || manDis(allProducers[p], allConsumers[i]) < closeCon) {
					isCons = TRUE;
					closeCon = manDis(allProducers[p], allConsumers[i]);
				}
			}
		}
		if (isCons) {
			// Work out the gain we get
			int gain = closeCon * allProducers[p].value - c.dis;
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

int atLineSq;

int findTarget(void) {
	// We do a dfs from each producer, to see what the maximum amount
	// of money we can make is
	int i;
	bestGain = -1;
	int amoLeft = 0;
	for (i = 0; i < numProd; i++) {
		if (!haveConnected[i]) {
			amoLeft++;
		}
	}
	if (amoLeft <= 1 && (!doneGG || atLineSq > 0) {
		return FALSE;
	}
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





int amoLineSquares;
point lineSquares[MAX_BOARD_SIZE * MAX_BOARD_SIZE * 10];

int amoInc;

void drawLine(int r1, int c1, int r2, int c2) {
	// Need to be straight
	c1 += amoInc;
	c2 += amoInc;
	int d = 0;
	if (c1 == c2) {
		if (r1 < r2) {
			d = DOWN;
		} else {
			d = UP;
		}
	} else {
		if (c1 < c2) {
			d = RIGHT;
		} else {
			d = LEFT;
		}
	}
	point p;
	p.r = r1;
	p.c = c1;
	p.dis = d;
	while (p.r != r2 || p.c != c2) {
		lineSquares[amoLineSquares++] = p;
		point oth = p;
		oth.r += dr[(p.dis - 1 + 4) % 4];
		oth.c += dc[(p.dis - 1 + 4) % 4];
		lineSquares[amoLineSquares++] = oth;

		oth = p;
		oth.dis = (p.dis - 1 + 4) % 4;
		lineSquares[amoLineSquares++] = oth;
		
		oth.r += dr[p.dis];
		oth.c += dc[p.dis];
		lineSquares[amoLineSquares++] = oth;

		p.r += dr[p.dis];
		p.c += dc[p.dis];
	}
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
		join(nn(p.r, p.c), nn(p.r + dr[p.dis], p.c + dc[p.dis]));
	} else {
		// Spell out GG
		if (!doneGG) {
			doneGG = TRUE;
			amoLineSquares = 0;
			atLineSq = 0;
			if (curMoney[myId] > 500) {
				amoInc = 0;
				drawLine(size / 4, size / 2, size / 4, size / 4);
				drawLine(size / 4, size / 4, (size * 3) / 4, size / 4);
				drawLine((size * 3) / 4, size / 4, (size * 3) / 4, (size * 7) / 16);
				drawLine((size * 3) / 4, (size * 7) / 16, size / 2, (size * 7) / 16);
				drawLine(size / 2, (size * 6) / 16, size / 2, (size * 8) / 16);
				
				amoInc = size / 4 + 1;
				drawLine(size / 4, size / 2, size / 4, size / 4);
				drawLine(size / 4, size / 4, (size * 3) / 4, size / 4);
				drawLine((size * 3) / 4, size / 4, (size * 3) / 4, (size * 7) / 16);
				drawLine((size * 3) / 4, (size * 7) / 16, size / 2, (size * 7) / 16);
				drawLine(size / 2, (size * 6) / 16, size / 2, (size * 8) / 16);
			}
		}
		point p = {};
		int foundMove = FALSE;
		while (atLineSq < amoLineSquares && !foundMove) {
			p = lineSquares[atLineSq++];
			if (!doesOwn[myId][p.r][p.c][p.dis]) {
				foundMove = TRUE;
			}
		}
		if (foundMove) {
			makeMove(p.r, p.c, p.dis);
		} else {
			makeNoMove();
		}
	}
	curTurn++;
}
