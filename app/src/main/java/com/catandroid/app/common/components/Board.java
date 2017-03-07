package com.catandroid.app.common.components;

import com.catandroid.app.R;
import com.catandroid.app.common.components.board_pieces.ProgressCard;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Harbor;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.components.utilities.BoardUtils;
import com.catandroid.app.common.players.AutomatedPlayer;
import com.catandroid.app.common.players.BalancedAI;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

public class Board {

	private transient ActiveGameFragment activeGameFragment;

	private ArrayList<String> gameParticipantIds;

    public final static int[] COUNT_PER_DICE_SUM = { 0, 0, 2, 3, 3, 3, 3, 0, 3, 3, 3, 3, 2 };

	private final HashMap<Hexagon.TerrainType, Integer> terrainTypeToCountMap;
	private HashMap<Hexagon.TerrainType, Integer> initTerrainTypeToCountMap(int boardSize)
	{
		HashMap<Hexagon.TerrainType, Integer> terrainTypeToCountMap =
				new HashMap<Hexagon.TerrainType, Integer>();
		terrainTypeToCountMap.put(Hexagon.TerrainType.DESERT, 2);
		terrainTypeToCountMap.put(Hexagon.TerrainType.GOLD_FIELD, 2);
		terrainTypeToCountMap.put(Hexagon.TerrainType.HILLS, 3);
		terrainTypeToCountMap.put(Hexagon.TerrainType.FOREST, 4);
		terrainTypeToCountMap.put(Hexagon.TerrainType.PASTURE, 4);
		terrainTypeToCountMap.put(Hexagon.TerrainType.MOUNTAINS, 4);
		terrainTypeToCountMap.put(Hexagon.TerrainType.FIELDS, 5);
		switch (boardSize) {
			case 0:
				terrainTypeToCountMap.put(Hexagon.TerrainType.SEA, 13);
				break;
			case 1:
				terrainTypeToCountMap.put(Hexagon.TerrainType.SEA, 37);
				break;
		}
		return terrainTypeToCountMap;
	}

	public Integer getTerrainCount(Hexagon.TerrainType terrainType) {
		Integer count = terrainTypeToCountMap.get(terrainType);
		if (count == null) {
			return 0;
		}
		return count;
	}

	private enum Phase {
		SETUP_SETTLEMENT, SETUP_FIRST_R, SETUP_CITY, SETUP_SECOND_R,
		PRODUCTION, BUILD, PROGRESS_CARD_1, PROGRESS_CARD_2, ROBBER, DONE
	}

	private Phase phase, returnPhase;

	private Hexagon[] hexagons;
	private Vertex[] vertices;
	private Edge[] edges;
	private Player[] players;
	private int numPlayers;
	private Harbor[] harbors;
	private Stack<Player> playersYetToDiscard;
	private BoardGeometry boardGeometry;
	private HashMap<Long, Hexagon> hexMap;

	private Hexagon curRobberHex, prevRobberHex;
	private int turn, turnNumber, roadCountId, longestRoad,
			maxPoints, lastDiceRollNumber;
	private Player longestRoadOwner, winner;

	private boolean autoDiscard;

	/**
	 * Create new board layout
	 * 
	 * @param names
	 *            array of players names
	 * @param human
	 *            whether each players is human
	 */
	public Board(ArrayList<String> gameParticipantIds, String[] names, boolean[] human, int maxPoints, BoardGeometry boardGeometry,
				 boolean autoDiscard, ActiveGameFragment activeGameFragment) {
		this.maxPoints = maxPoints;
		this.boardGeometry = boardGeometry;
		this.terrainTypeToCountMap = initTerrainTypeToCountMap(boardGeometry.getBoardSize());
		this.gameParticipantIds = gameParticipantIds;
		this.activeGameFragment = activeGameFragment;
		this.numPlayers = gameParticipantIds.size();
		commonInit();

		this.autoDiscard = autoDiscard;

		// initialize players
		players = new Player[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			Player.Color color = Player.Color.values()[i];

			if (human[i]) {
				String participantId = gameParticipantIds.get(i);
				players[i] = new Player(this, i, participantId, color, names[i],
						Player.PLAYER_HUMAN);
			} else {
				players[i] = new BalancedAI(this, i, color, names[i]);
			}

		}
	}

	public void setActiveGameFragment(ActiveGameFragment activeGameFragment){
		this.activeGameFragment = activeGameFragment;
	}

	private void commonInit() {
		turn = 0;
		turnNumber = 1;
		phase = Phase.SETUP_SETTLEMENT;
		roadCountId = 0;
		longestRoad = 4;
		longestRoadOwner = null;
		hexagons = null;
		winner = null;

		playersYetToDiscard = new Stack<Player>();
		hexMap = new HashMap<Long, Hexagon>();

		// randomly initialize hexagons
		hexagons = BoardUtils.initRandomHexes(this);
		harbors = BoardUtils.initRandomHarbors(this, boardGeometry.getHarborCount());
		vertices = BoardUtils.generateVertices(this, boardGeometry.getVertexCount());
		edges = BoardUtils.generateEdges(this, boardGeometry.getEdgeCount());

		// populate board map with starting parameters
		boardGeometry.populateBoard(hexagons, vertices, edges, harbors, hexMap);

		// assign number tokens randomly
		BoardUtils.assignRandomNumTokens(hexagons);
	}

	/**
	 * Get a costs_reference to the board's geometry
	 *
	 * @return the board's geometry
	 */
	public BoardGeometry getBoardGeometry() {
		if (boardGeometry == null) {
			return null;
		}

		return boardGeometry;
	}

	/**
	 * Get the total number of players in this game
	 *
	 * @return the total number of players
	 */
	public int getNumPlayers() {
		return numPlayers;
	}

	/**
	 * Get a costs_reference to the current players
	 * 
	 * @return the current players
	 */
	public Player getCurrentPlayer() {
		if (players == null)
			return null;

		return players[turn];
	}


	/**
	 * Get a player by player number
	 * 
	 * @param playerNumber
	 *            players playerNumber [0, 3]
	 * @return the players
	 */
	public Player getPlayer(int playerNumber) {
		if (playerNumber < 0 || playerNumber >= players.length)
		{
			return null;
		}
		return players[playerNumber];
	}

	/**
	 * Distribute resources for a given dice roll number
	 * 
	 * @param diceRollNumber
	 *            the dice roll number to execute
	 */
	public void executeDiceRoll(int diceRollNumber) {
		if (diceRollNumber == 7) {
			// reduce each players to 7 cards
			for (int i = 0; i < numPlayers; i++) {
				int cards = players[i].getResourceCount();
				int extra = cards > 7 ? cards / 2 : 0;

				if (extra == 0)
					continue;

				if (autoDiscard) {
					// discard_resources randomly
					for (int j = 0; j < extra; j++)
						players[i].discard(null);
				}
				if (players[i].isBot()) {
					// instruct the ai to discard_resources
					AutomatedPlayer bot = (AutomatedPlayer) players[i];
					bot.discard(extra);
				} else if (players[i].isHuman()) {
					// queue human players to discard_resources
					playersYetToDiscard.add(players[i]);
				}
			}

			// enter robberphase
			startRobberPhase();
		} else {
			// distribute resources
			for (int i = 0; i < hexagons.length; i++)
			{
				hexagons[i].distributeResources(diceRollNumber);
			}
		}

		lastDiceRollNumber = diceRollNumber;
	}

	/**
	 * Get the last dice roll
	 * 
	 * @return the last dice roll, or 0
	 */
	public int getLastDiceRollNumber() {
		if (isSetupPhase() || isProgressPhase())
		{
			return 0;
		}

		return lastDiceRollNumber;
	}

	/**
	 * Run the AI's robber methods
	 * 
	 * @param current
	 *            current ai players
	 */
	private void startAIRobberPhase(AutomatedPlayer current) {
		int hex = current.placeRobber(hexagons, prevRobberHex);
		setRobber(hex);

		int count = 0;
		for (int i = 0; i < numPlayers; i++)
		{
			if (players[i] != players[turn] && hexagons[hex].adjacentToPlayer(players[i]))
			{
				count++;
			}
		}

		if (count > 0) {
			Player[] stealList = new Player[count];
			for (int i = 0; i < numPlayers; i++)
				if (players[i] != players[turn]
						&& hexagons[hex].adjacentToPlayer(players[i]))
				{
					stealList[--count] = players[i];
				}

			int who = current.steal(stealList);
			players[turn].steal(stealList[who]);
		}

		phase = returnPhase;
	}

	/**
	 * Start a players's turn
	 */
	public void runTurn() {
		// process ai turn
		if (players[turn].isBot()) {
			AutomatedPlayer current = (AutomatedPlayer) players[turn];
			switch (phase) {

				case SETUP_SETTLEMENT:
					current.setupSettlement(vertices);
					break;
				case SETUP_CITY:
					current.setupCity(vertices);
					break;
				case SETUP_FIRST_R:
				case SETUP_SECOND_R:
					current.setupRoad(edges);
					break;

				case PRODUCTION:
					current.productionPhase();
					players[turn].roll();
					break;

				case BUILD:
					current.buildPhase();
					break;

				case PROGRESS_CARD_1:
					current.progressRoad(edges);
				case PROGRESS_CARD_2:
					current.progressRoad(edges);
					phase = returnPhase;
					return;

				case ROBBER:
					startAIRobberPhase(current);
					return;

				case DONE:
					return;

				}

			nextPhase();
		}
	}

	/**
	 * Proceed to the next phase or next turn
	 * 
	 * My initial reaction was to treat it as a state machine
	 */
	public boolean nextPhase() {
		boolean turnChanged = false;

		switch (phase) {
			case SETUP_SETTLEMENT:
				phase = Phase.SETUP_FIRST_R;
				break;
			case SETUP_FIRST_R:
				if (turn < numPlayers-1) {
					turn++;
					turnChanged = true;
					phase = Phase.SETUP_SETTLEMENT;
					if(players[turn].isHuman()) {
						activeGameFragment.mListener.endTurn(gameParticipantIds.get(turn), false);
					}
				} else {
					phase = Phase.SETUP_CITY;
				}
				break;
			case SETUP_CITY:
				phase = Phase.SETUP_SECOND_R;
				break;
			case SETUP_SECOND_R:
				if (turn > 0) {
					turn--;
					turnChanged = true;
					phase = Phase.SETUP_CITY;
					if(players[turn].isHuman()) {
						activeGameFragment.mListener.endTurn(gameParticipantIds.get(turn), false);
					}
				} else {
					phase = Phase.PRODUCTION;
				}
				break;
			case PRODUCTION:
				phase = Phase.BUILD;
				break;
			case BUILD:
				if (turn == numPlayers - 1)
					turnNumber += 1;
				players[turn].endTurn();
				phase = Phase.PRODUCTION;
				turn++;
				turn %= numPlayers;
				turnChanged = true;
				players[turn].beginTurn();
				lastDiceRollNumber = 0;
                if(players[turn].isHuman()) {
                    activeGameFragment.mListener.endTurn(gameParticipantIds.get(turn),false);
                }
				break;
			case PROGRESS_CARD_1:
				phase = Phase.PROGRESS_CARD_2;
				break;
			case PROGRESS_CARD_2:
				phase = returnPhase;
				break;
			case ROBBER:
				phase = returnPhase;
				break;
			case DONE:
				return false;
			}

		return turnChanged;
	}

	/**
	 * Enter progress phase 1 (road building)
	 */
	public void startProgressPhase1() {
		returnPhase = phase;
		phase = Phase.PROGRESS_CARD_1;
		runTurn();
	}

	/**
	 * Enter the robber placement phase
	 */
	public void startRobberPhase() {
		if(this.curRobberHex != null) {
			this.curRobberHex.removeRobber();
		}
		this.prevRobberHex= this.curRobberHex;
		this.returnPhase = phase;
		this.curRobberHex = null;
		phase = Phase.ROBBER;
		runTurn();
	}

	/**
	 * Determine if we're in a setup phase
	 * 
	 * @return true if the game is in setup phase
	 */
	public boolean isSetupPhase() {
		return (phase == Phase.SETUP_SETTLEMENT || phase == Phase.SETUP_FIRST_R
				|| phase == Phase.SETUP_CITY || phase == Phase.SETUP_SECOND_R);
	}

	public boolean isSetupSettlement() {
		return (phase == Phase.SETUP_SETTLEMENT);
	}

	public boolean isSetupCity() {
		return (phase == Phase.SETUP_CITY);
	}

	public boolean isSetupRoad() {
		return (phase == Phase.SETUP_FIRST_R || phase == Phase.SETUP_SECOND_R);
	}

	public boolean isSetupPhase2() {
		return (phase == Phase.SETUP_CITY || phase == Phase.SETUP_SECOND_R);
	}

	public boolean isRobberPhase() {
		return (phase == Phase.ROBBER);
	}

	public boolean isProduction() {
		return (phase == Phase.PRODUCTION);
	}

	public boolean isBuild() {
		return (phase == Phase.BUILD);
	}

	public boolean isProgressPhase() {
		return (phase == Phase.PROGRESS_CARD_1 || phase == Phase.PROGRESS_CARD_2);
	}

	public boolean isProgressPhase1() {
		return (phase == Phase.PROGRESS_CARD_1);
	}

	public boolean isProgressPhase2() {
		return (phase == Phase.PROGRESS_CARD_2);
	}

	/**
	 * Get the dice number token value for a hexagons
	 * 
	 * @param id
	 *            the id of the hexagons
	 * @return the number token value
	 */
	public int getNumberTokenByHexId(int id) {
		return hexagons[id].getNumberTokenAsInt();
	}

	/**
	 * Get the resource produced by a particular hexagon
	 * 
	 * @param id
	 *            the id of the hexagon
	 * @return the resource produced by that hexagon
	 */
	public Resource getResourceByHexId(int id) {
		return hexagons[id].getResource();
	}

	/**
	 * Get indexed hexToTerrainTypes mapping
	 * 
	 * @return array of terrain type ordinals
	 * @note this is intended only to be used to stream out the board layout
	 */
	public int[] getHexToTerrainTypesMapping() {
		int hexMapping[] = new int[hexagons.length];
		for (int i = 0; i < hexagons.length; i++) {
			hexMapping[i] = hexagons[i].getResourceType().ordinal();
		}

		return hexMapping;
	}

	/**
	 * Get a given hexagon by id
	 * 
	 * @param hexId
	 *            the id of the hexagon
	 * @return the hexagon with hexId (or null)
	 */
	public Hexagon getHexagonById(int hexId) {
		if (hexId < 0 || hexId >= boardGeometry.getHexCount())
		{
			return null;
		}

		return hexagons[hexId];
	}
	
	public Hexagon[] getHexagons() {
		return hexagons;
	}

	/**
	 * Get a given harbor by id
	 * 
	 * @param harborId
	 *            the id of the harbor
	 * @return the harbor with harborId (or null)
	 */
	public Harbor getHarborById(int harborId) {
		if (harborId < 0 || harborId >= boardGeometry.getHarborCount())
			return null;

		return harbors[harborId];
	}

	/**
	 * Get the edge with the given id
	 * 
	 * @param edgeId
	 *            the id of the edge
	 * @return the edge with edgeId (or null)
	 */
	public Edge getEdgeById(int edgeId) {
		if (edgeId < 0 || edgeId >= boardGeometry.getEdgeCount())
        {
            return null;
        }

		return edges[edgeId];
	}
	
	public Edge[] getEdges() {
		return edges;
	}

    /**
     * Get the vertex with the given id
     *
     * @param vertexId
     *            the id of the vertex
     * @return the vertex
     */
	public Vertex getVertexById(int vertexId) {
		if (vertexId < 0 || vertexId >= boardGeometry.getEdgeCount())
        {
            return null;
        }

		return vertices[vertexId];
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}

	/**
	 * Get a progress card
	 *
	 * @return the type of progress card or null if that stack is empty
	 */
	public ProgressCard.ProgressCardType getRandomProgressCard() {
		//TODO: implement progress cards
		ProgressCard.ProgressCardType card =null;
		return card;
	}

	/**
	 * Get the hexagons with the robberIndex
	 *
	 * @return the hexagons with the robberIndex
	 */
	public Hexagon getCurRobberHex() {
		return curRobberHex;
	}

	/**
	 * If the robber is being moved, return the last hexagons where it last
	 * resided, or otherwise the current location
	 *
	 * @return the last location of the robber
	 */
	public Hexagon getPrevRobberHex() {
		int hexCount = this.boardGeometry.getHexCount();
		int curRobberId = this.curRobberHex != null ? this.curRobberHex.getId() : -1;
		int prevRobberId = this.prevRobberHex != null ? this.prevRobberHex.getId() : -1;
		if (this.phase == Phase.ROBBER && prevRobberId >= 0 && prevRobberId < hexCount)
			return this.prevRobberHex;
		else if (curRobberId >= 0 && curRobberId < hexCount) {
			return this.curRobberHex;
		}
		else {
			return null;
		}
	}

	/**
	 * Set the current robber hexagon
	 *
	 * @param curRobberHex
	 *            current robber hexagon
	 * @return true iff the currebt robber hex was set
	 */
	public boolean setCurRobberHex(Hexagon curRobberHex) {
		if (this.curRobberHex != null) {
			this.curRobberHex.removeRobber();
		}
		this.curRobberHex = curRobberHex;
		this.curRobberHex.setRobber();
		return true;
	}

	/**
	 * Set the index for the robber
	 *
	 * @param robberIndex
	 *            id of the hexagon with the robber
	 * @return true if the robber was placed
	 */
	public boolean setRobber(int robberIndex) {
		if (this.curRobberHex != null) {
			this.curRobberHex.removeRobber();
		}
		this.curRobberHex = this.hexagons[robberIndex];
		this.curRobberHex.setRobber();
		return true;
	}

	/**
	 * Get the number of points required to win
	 * 
	 * @return the number of points required to win
	 */
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * Update the longest road owner and length
	 */
	public void checkLongestRoad() {
		Player previousOwner = longestRoadOwner;

		// reset road length in case a road was split
		longestRoad = 4;
		longestRoadOwner = null;

		// reset players' road lengths to 0
		for (int i = 0; i < numPlayers; i++)
		{
			players[i].cancelRoadLength();
		}

		// find longest road
		for (int i = 0; i < edges.length; i++) {
			if (edges[i].hasRoad()) {
				int length = edges[i].getRoadLength(++roadCountId);

				Player owner = edges[i].getOwnerPlayer();
				owner.setRoadLength(length);
				if (length > longestRoad) {
					longestRoad = length;
					longestRoadOwner = owner;
				}
			}
		}

		// the same players keeps the longest road if length doesn't change
		if (previousOwner != null
				&& previousOwner.getRoadLength() == longestRoad)
		{
			longestRoadOwner = previousOwner;
		}
	}

	/**
	 * Determine if players has the longest road
	 * 
	 * @param player
	 *            the players
	 * @return true if players had the longest road
	 */
	public boolean hasLongestRoad(Player player) {
		return (longestRoadOwner != null && player == longestRoadOwner);
	}

	/**
	 * Get the length of the longest road
	 * 
	 * @return the length of the longest road
	 */
	public int getLongestRoad() {
		return longestRoad;
	}

	/**
	 * Get the owner of the longest road
	 * 
	 * @return the players with the longest road
	 */
	public Player getLongestRoadOwner() {
		return longestRoadOwner;
	}

	/**
	 * Check if any players need to discard_resources
	 * 
	 * @return true if one or more players need to discard_resources
	 */
	public boolean checkPlayerToDiscard() {
		return !playersYetToDiscard.empty();
	}

	/**
	 * Get the next players queued for discarding
	 * 
	 * @return a players or null
	 */
	public Player getPlayerToDiscard() {
		try {
			return playersYetToDiscard.pop();
		} catch (EmptyStackException e) {
			return null;
		}
	}

	/**
	 * Get an instruction string for the current phase
	 * 
	 * @return the instruction string resource or 0
	 */
	public int getPhaseResource() {
		switch (phase) {
			case SETUP_SETTLEMENT:
				return R.string.phase_first_settlement;
			case SETUP_FIRST_R:
				return R.string.phase_first_road;
			case SETUP_CITY:
				return R.string.phase_first_city;
			case SETUP_SECOND_R:
				return R.string.phase_second_road;
			case PRODUCTION:
				return R.string.phase_your_turn;
			case BUILD:
				return R.string.phase_build;
			case PROGRESS_CARD_1:
				// TODO: progress card step 1
				return 0;
			case PROGRESS_CARD_2:
				// TODO: progress card step 2
				return 0;
			case ROBBER:
				return R.string.phase_move_robber;
			case DONE:
				return R.string.phase_game_over;
			}

		return 0;
	}

	public boolean itsMyTurn(String myParticipantId){
		String s = this.getCurrentPlayer().getGooglePlayParticipantId();
		return (this.getCurrentPlayer().getGooglePlayParticipantId().equals(myParticipantId));
	}

	/**
	 * Get the global turn number
	 * 
	 * @return the turn number (starting at 1, after setup)
	 */
	public int getTurnNumber() {
		return turnNumber;
	}

	/**
	 * Get the winner
	 * 
	 * @return the winning players or null
	 */
	public Player getWinner() {
		// winner already found or we just want to check what was already found
		if (winner != null)
		{
			return winner;
		}

		// check for winner
		for (int i = 0; i < numPlayers; i++) {
			if (players[i].getVictoryPoints() >= maxPoints) {
				winner = players[i];
				if(phase != phase.DONE){
					//we need to tell google the game is done
					activeGameFragment.mListener.endTurn(winner.getGooglePlayParticipantId(),true);
				}
				phase = Phase.DONE;
				break;
			}
		}

		return winner;
	}

    public void reinitBoardOnDependents() {

		for (Hexagon hexagon : hexagons) {
			hexagon.setBoard(this);
		}
		for (Vertex vertex : vertices) {
			vertex.setBoard(this);
		}
		for (Edge edge : edges) {
			edge.setBoard(this);
		}
		for (Harbor harbor : harbors) {
			harbor.setBoard(this);
		}
		for (Player player : players) {
			player.setBoard(this);
		}
	}

}
