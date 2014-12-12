#ifndef __PHAIS_H
#define __PHAIS_H

#ifdef __cplusplus
extern "C" {
#endif
	/////////////////////////////////////////////////////////////////////

	// Constants used
#define TRUE                    1
#define FALSE                   0

	// Limits of certain restrictions
#define MAX_NUM_PLAYERS         16
#define MAX_BOARD_SIZE          128
#define MAX_RESOURCE_TYPES      8
#define MAX_NAME_LENGTH         16
#define MAX_PRODUCERS			1024
#define MAX_CONSUMERS			1024

	// Directions
#define UP                      0
#define RIGHT                   1
#define DOWN                    2
#define LEFT                    3
#define NUM_DIRECTIONS			4
	/*
	 *   Use:
	 *   int dr[] = {-1, 0, 1, 0};
	 *   int dc[] = {0, 1, 0, -1};
	 *   to deal with directions.
	 *   These directions are definied in such a way, that if you are at square
	 *   (r,c), the square in direction d from it has the coordinates
	 *   (r + dr[d], c + dc[d]).
	 */


	/* All of these use:
	 * 0 <= r, c < boardSize
	 * 0 <= type < numResourceTypes
	 */

	struct consumer_info {
		int r;
		int c;
		int type;
	};

	struct producer_info {
		int r;
		int c;
		int type;
		int value; // This is value per distance
	};

	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////

	// The following must be implemented by the client:

	/*
	 *   This is called when your client connects to the server. You need to
	 *   provide a name using setName and a colour with setColour.
	 */
	void clientRegister(void);

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
	void clientInit(int numPlayers, int boardSize, int numResourceTypes, int startMoney, int pid);

	/*
	 *   This is called when the game is about to begin, after clientInit.
	 *   It tells you the number of producers and consumers, then info about them.
	 *
	 *   You are not required to call anything in here.
	 */
	void clientEntityInfo(int numProducers, int numConsumers, struct producer_info *producers, struct consumer_info *consumers);

	// ******** These two functions will be called *BEFORE* players take their turns for the round ********

	/*
	 *   This is called once for *every* player in the game (including those 
	 *   with <= 0 money), telling you the amount of money they now have. The player
	 *   id is such that 0 <= pid < numPlayers.
	 *
	 *   You are not required to call anything in here.
	 */
	void clientPlayerUpdate(int pid, int newMoney);

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
	void clientPlayerMoved(int pid, int r, int c, int d);

	// ******** This function will be called *WHEN* it is your turn ********

	/*
	 *   This is called when it is time for you to take your turn. While in the
	 *   function, you must call makeMove once. If you make multiple calls to
	 *   makeMove, only the final call will be considered.
	 *
	 *   If you have <= 0 money, then this function will *not* be called.
	 */
	void clientDoTurn(void);


	/////////////////////////////////////////////////////////////////////
	// The following are available to the client:

	/*
	 *   This will send to the server what you want your name to be. It must
	 *   only contain A-Z, a-z, 1-9 and _. The length of the name should be at
	 *   most 16 characters (not including the null terminating byte).
	 *
	 *   This can only be called in clientRegister.
	 */
	void setName(const char* name);

	/*
	 *   This will send to the server what colour you want to be. The values
	 *   must be 0-255.
	 *
	 *   This can only be called in clientRegister.
	 */
	void setColour(int r, int g, int b);

	/*   
	 *   This will tell the server you wish the build or buy the rights to the
	 *   track between square (r,c) and the square in direction d from it (where the
	 *   meaning of d is definied at the start of this file).
	 *
	 *   This can only be called in clientDoTurn.
	 */
	void makeMove(int r, int c, int d);

	/////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif

#endif
