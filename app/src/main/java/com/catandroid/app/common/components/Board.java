package com.catandroid.app.common.components;

import android.widget.Toast;

import com.catandroid.app.R;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.Knight;
import com.catandroid.app.common.components.board_pieces.ProgressCard;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.FishingGround;
import com.catandroid.app.common.components.board_positions.Harbor;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_positions.Vertex;
import com.catandroid.app.common.components.utilities.BoardUtils;
import com.catandroid.app.common.logistics.multiplayer.TradeProposal;
import com.catandroid.app.common.players.AutomatedPlayer;
import com.catandroid.app.common.players.BalancedAI;
import com.catandroid.app.common.players.Player;
import com.catandroid.app.common.ui.fragments.ActiveGameFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

public class Board {

	private transient ActiveGameFragment activeGameFragment;

	private ArrayList<String> gameParticipantIds;

    public final static int[] COUNT_PER_DICE_SUM = { 0, 0, 1, 2, 2, 3, 3, 0, 3, 3, 2, 2, 1 };

	private final HashMap<Hexagon.TerrainType, Integer> terrainTypeToCountMap;
	private HashMap<Hexagon.TerrainType, Integer> initTerrainTypeToCountMap(int boardSize)
	{
		HashMap<Hexagon.TerrainType, Integer> terrainTypeToCountMap =
				new HashMap<Hexagon.TerrainType, Integer>();
		terrainTypeToCountMap.put(Hexagon.TerrainType.FISH_LAKE, 1);
		terrainTypeToCountMap.put(Hexagon.TerrainType.DESERT, 1);
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


	public enum Phase {
		SETUP_SETTLEMENT, SETUP_EDGE_UNIT_1, SETUP_CITY, SETUP_EDGE_UNIT_2,
		PRODUCTION, PLAYER_TURN, MOVING_SHIP, MOVING_KNIGHT, DISPLACING_KNIGHT,
		PROGRESS_CARD_STEP_1, PROGRESS_CARD_STEP_2,	BUILD_METROPOLIS,
		CHOOSE_ROBBER_PIRATE, MOVING_ROBBER, MOVING_PIRATE, TRADE_PROPOSED, TRADE_RESPONDED,
		DEFENDER_OF_CATAN, PLACE_MERCHANT, DONE
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public Phase getPhase() {
		return phase;
	}

	private Phase phase;

	private int barbarianPosition;

	public void setReturnPhase(Phase returnPhase) {
		this.returnPhase = returnPhase;
	}

	private Phase returnPhase;

	private Hexagon[] hexagons;
	private Vertex[] vertices;
	private Edge[] edges;
	private Player[] players;
	private int nextAvailableKnightId = -1;
	private Knight[] knights;
	private int numPlayers;
	private int numTotalPlayableKnights;
	private Harbor[] harbors;
    private FishingGround[] fishingGrounds;
	private Stack<Integer> playerIdsYetToAct;
	private BoardGeometry boardGeometry;
	private HashMap<Long, Integer> hexIdMap;

	private ArrayList<ProgressCard.ProgressCardType> tradeDeck;
	private ArrayList<ProgressCard.ProgressCardType> scienceDeck;
	private ArrayList<ProgressCard.ProgressCardType> politicsDeck;

	public TradeProposal getTradeProposal() {
		return tradeProposal;
	}

	public void setTradeProposal(TradeProposal tradeProposal) {
		this.tradeProposal = tradeProposal;
	}

	private TradeProposal tradeProposal = null;

	private Integer curRobberHexId = null, prevRobberHexId = null,
			curPirateHexId = null, prevPirateHexId = null;
	private int curPlayerNumber, gameTurnNumber, gameRoundNumber, numberOfLongestTradeRouteUpdates,
			longestTradeRouteLength, maxPoints, lastDiceRollNumber;
	private Integer longestTradeRouteOwnerId = null, winnerId = null;
	private int latestPlayerChoice = -1;
    private int tempEdgeIdMemory = -1, tempVertexIdMemory = -1,
			tempKnightIdMemory = -1, tempPlayerNumberMemory = -1;

	private int[] metropolisOwners = {-1, -1, -1};



    private int merchantOwner = -1;
    private int curMerchantHexId = -1;
    Resource.ResourceType merchantType = null;

	private boolean autoDiscard;

	public int playerNumBootOwner = -1;

	boolean pirateDisabled = false;

	boolean robberDisabled = false;

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
		this.numTotalPlayableKnights = numPlayers * 6;
		nextAvailableKnightId = 0;

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

		commonInit();

		this.autoDiscard = autoDiscard;
	}

	public void setActiveGameFragment(ActiveGameFragment activeGameFragment){
		this.activeGameFragment = activeGameFragment;
	}

	private void commonInit() {
		curPlayerNumber = 0;
		gameRoundNumber = 1;
		gameTurnNumber = 1;
		phase = Phase.SETUP_SETTLEMENT;
		numberOfLongestTradeRouteUpdates = 0;
		longestTradeRouteLength = 4;
		longestTradeRouteOwnerId = null;
		hexagons = null;
		winnerId = null;

		playerIdsYetToAct = new Stack<Integer>();

		//TODO: move to boardUtils
		// initialize 6 knights per player
		knights = new Knight[numPlayers * 6];
		for (int i = 0; i < (numPlayers * 6); i++) {
			knights[i] = new Knight(i);
		}


		hexIdMap = new HashMap<Long, Integer>();

		// randomly initialize board positions
		vertices = BoardUtils.generateVertices(this, boardGeometry.getVertexCount());
		edges = BoardUtils.generateEdges(this, boardGeometry.getEdgeCount());
		hexagons = BoardUtils.initRandomHexes(this);
		harbors = BoardUtils.initRandomHarbors(this, boardGeometry.getHarborCount());
		Integer[] fishingGroundNumbers = {4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10};
		fishingGrounds = BoardUtils.generateFishingGrounds(this,
				fishingGroundNumbers);

		// populate board map with starting parameters
		boardGeometry.populateBoard(hexagons, vertices, edges,
				harbors, fishingGrounds, hexIdMap);

		// assign number tokens randomly
		BoardUtils.assignRandomNumTokens(hexagons);

		//TODO: remove robber reset when adding fishermen rules
		// reset the robber hex now that the edges & verticesare ready
		getCurRobberHex().setRobber();
		// reset the pirate hex now that the edges & vertices are ready
		getCurPirateHex().setPirate();
		//generate progress card decks
		progressCardInit();
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
	 * Get a reference to the player for current game turn
	 * 
	 * @return the player of the current game turn
	 */
	public Player getPlayerOfCurrentGameTurn() {
		if (players == null)
		{
			return null;
		}

		return players[curPlayerNumber];
	}


	/**
	 * Get a reference to the player looking at screen
	 *
	 * @return the player currently looking at device
	 */
	public Player getActiveFragmentPlayer() {
		if (players == null)
		{
			return null;
		}

		return getPlayerFromParticipantId(activeGameFragment.myParticipantId);
	}

	/**
	 * Get a player by player number
	 * 
	 * @param playerNumber
	 *            players playerNumber [0, 3]
	 * @return the players
	 */
	public Player getPlayerById(int playerNumber) {
		if (playerNumber < 0 || playerNumber >= players.length)
		{
			return null;
		}
		return players[playerNumber];
	}

	/**
	 * Get a reference to the currently moving ship
	 *
	 * @return the currently moving ship (or null)
	 */
	public Edge getCurrentlyMovingShip() {
		if (phase != Phase.MOVING_SHIP) {
			return null;
		}

		return getEdgeById(tempEdgeIdMemory);
	}

	/**
	 * Get a reference to the currently moving knight
	 *
	 * @return the currently moving knight (or null)
	 */
	public Knight getCurrentlyMovingKnight() {
		if (phase != Phase.MOVING_KNIGHT && phase != Phase.DISPLACING_KNIGHT) {
			return null;
		}

		return getKnightById(tempKnightIdMemory);
	}

	/**
	 * Get a reference to the player that needs to move their displaced knight
	 *
	 * @return the player that needs to move their displaced knight
	 */
	public Player getPlayerToDisplaceKnight() {
		if (phase != Phase.DISPLACING_KNIGHT) {
			return null;
		}

		return getPlayerById(tempPlayerNumberMemory);
	}

	/**
	 * Get a reference to the old location of the currently moving knight
	 *
	 * @return previous location of the currently moving knight
	 */
	public Vertex getStartLocationOfMovingKnight() {
		if (phase != Phase.MOVING_KNIGHT && phase != Phase.DISPLACING_KNIGHT) {
			return null;
		}

		return getVertexById(tempVertexIdMemory);
	}

	/**
	 * Distribute resources for a given dice roll number
	 *  @param diceRollNumber1
	 *            the dice roll number to execute
	 * @param diceRollNumber2
	 * @param eventRoll
	 */
	public void executeDiceRoll(int diceRollNumber1, int diceRollNumber2, int eventRoll) {
		//TODO: remove debugging
		int diceRollNumber = diceRollNumber1 + diceRollNumber2;
//		int diceRollNumber = diceRollNumber1 + diceRollNumber2;
		CityImprovement.CityImprovementType disciplineRolled;
		switch(eventRoll){
			case 4:
				disciplineRolled = CityImprovement.CityImprovementType.TRADE;
				break;
			case 5:
				disciplineRolled = CityImprovement.CityImprovementType.SCIENCE;
				break;
			case 6:
				disciplineRolled = CityImprovement.CityImprovementType.POLITICS;
				break;
			default:
				disciplineRolled = null;
		}

		//@TODO
		//check if we distribute progress cards
		//diceRollNumber2 is the die that we use for the number
		for(int i = 0; i < players.length; i++){
			getPlayerById(i).distributeProgressCard(diceRollNumber2, disciplineRolled);
		}

		//@TODO
		//resolve the barbarian
		if(eventRoll == 1 || eventRoll == 2 || eventRoll == 3){
			resolveBarbarians();
		}


		if (diceRollNumber == 7) {
			// reduce each players to 7 cards
			robberDisabled = false;
			pirateDisabled = false;
			for (int i = 0; i < numPlayers; i++) {
				int cards = players[i].getResourceCount();
				int walls = getPlayerById(i).getNumOwnedCityWalls();
				int extra = cards > (7 + walls * 2) ? cards / 2 : 0;

				if (extra == 0)
					continue;

				if (autoDiscard) {
					// discard_resources randomly
					for (int j = 0; j < extra; j++){
						players[i].discard(null);
					}
				}
				 else if (players[i].isBot()) {
					// instruct the ai to discard_resources
					AutomatedPlayer bot = (AutomatedPlayer) players[i];
					bot.discard(extra);
				} else if (players[i].isHuman()) {
					// queue human players to discard_resources
					playerIdsYetToAct.add(players[i].getPlayerNumber());
				}
			}

			// enter ChooseRobberPiratePhase
			startChooseRobberPiratePhase();
		} else {
			// distribute resources and fish
			for (int i = 0; i < hexagons.length; i++)
			{
				hexagons[i].distributeResources(diceRollNumber);
				hexagons[i].distributeFish(diceRollNumber);
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
		int hexId = current.placeRobber(hexagons,
				prevRobberHexId != null ? hexagons[prevRobberHexId] : null);
        Hexagon hex = hexagons[hexId];
		setCurRobberHex(hex);

		int count = 0;
		for (int i = 0; i < numPlayers; i++)
		{
			if (players[i] != players[curPlayerNumber] && hex.adjacentToVertexUnitOwnedBy(players[i]))
			{
				count++;
			}
		}

		if (count > 0) {
			Player[] stealList = new Player[count];
			for (int i = 0; i < numPlayers; i++)
				if (players[i] != players[curPlayerNumber]
						&& hex.adjacentToVertexUnitOwnedBy(players[i]))
				{
					stealList[--count] = players[i];
				}

			int who = current.steal(stealList);
			players[curPlayerNumber].steal(stealList[who]);
		}

		phase = returnPhase;
	}

	/**
	 * Start a players's turn
	 */
	public void runAITurn() {
		// process ai turn
		if (players[curPlayerNumber].isBot()) {
			AutomatedPlayer current = (AutomatedPlayer) players[curPlayerNumber];
			switch (phase) {

				case SETUP_SETTLEMENT:
					current.setupSettlement(vertices);
					break;
				case SETUP_CITY:
					current.setupCity(vertices);
					break;
				case SETUP_EDGE_UNIT_1:
				case SETUP_EDGE_UNIT_2:
					current.setupRoad(edges);
					break;

				case PRODUCTION:
					current.productionPhase();
					players[curPlayerNumber].rollDice();
					break;

				case PLAYER_TURN:
					current.buildPhase();
					break;

                case MOVING_SHIP:
					//TODO: automate
					break;

				case MOVING_KNIGHT:
					//TODO: automate
					break;

				case PROGRESS_CARD_STEP_1:
					current.progressRoad(edges);
				case PROGRESS_CARD_STEP_2:
					current.progressRoad(edges);
					phase = returnPhase;
					return;

				case CHOOSE_ROBBER_PIRATE:
				case MOVING_ROBBER:
				case MOVING_PIRATE:
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
	 */
	public boolean nextPhase() {
		boolean turnChanged = false;

		switch (phase) {
			case SETUP_SETTLEMENT:
				phase = Phase.SETUP_EDGE_UNIT_1;
				break;
			case SETUP_EDGE_UNIT_1:
				if (curPlayerNumber < numPlayers-1) {
					curPlayerNumber++;
					gameTurnNumber++;
					turnChanged = true;
					phase = Phase.SETUP_SETTLEMENT;
					if(players[curPlayerNumber].isHuman()) {
						activeGameFragment.mListener.endTurn(gameParticipantIds.get(curPlayerNumber), false);
					}
				} else {
					phase = Phase.SETUP_CITY;
				}
				break;
			case SETUP_CITY:
				phase = Phase.SETUP_EDGE_UNIT_2;
				break;
			case SETUP_EDGE_UNIT_2:
				if (curPlayerNumber > 0) {
					curPlayerNumber--;
                    gameTurnNumber++;
					turnChanged = true;
					phase = Phase.SETUP_CITY;
					if(players[curPlayerNumber].isHuman()) {
						activeGameFragment.mListener.endTurn(gameParticipantIds.get(curPlayerNumber), false);
					}
				} else {
					phase = Phase.PRODUCTION;
				}
				break;
			case PRODUCTION:
				phase = Phase.PLAYER_TURN;
				break;
			case PLAYER_TURN:
				if (curPlayerNumber == numPlayers - 1)
				{
					gameRoundNumber += 1;
				}
				players[curPlayerNumber].endTurn();
				phase = Phase.PRODUCTION;
				curPlayerNumber++;
				gameTurnNumber++;
				curPlayerNumber %= numPlayers;
				turnChanged = true;
				players[curPlayerNumber].beginTurn();
				lastDiceRollNumber = 0;
				if(players[curPlayerNumber].isHuman()) {
					activeGameFragment.mListener.endTurn(gameParticipantIds.get(curPlayerNumber),false);
				}
				break;
            case MOVING_SHIP:
                phase = returnPhase;
				tempEdgeIdMemory = -1;
				break;
			case MOVING_KNIGHT:
				// TODO: what else when ending knight movement?
				phase = returnPhase;
				tempKnightIdMemory = -1;
				tempVertexIdMemory = -1;
				break;
			case DISPLACING_KNIGHT:
				// TODO: what else when ending knight displacement?
				phase = returnPhase;
				tempKnightIdMemory = -1;
				tempVertexIdMemory = -1;
				tempPlayerNumberMemory = -1;
				// pass the turn back to the active turn player
				activeGameFragment.mListener.endTurn(
						getPlayerOfCurrentGameTurn().getGooglePlayParticipantId(), false);
				break;
			case PROGRESS_CARD_STEP_1:
				phase = Phase.PROGRESS_CARD_STEP_2;
				break;
			case PROGRESS_CARD_STEP_2:
				phase = returnPhase;
				break;
			case MOVING_ROBBER:
				phase = returnPhase;
				break;
			case MOVING_PIRATE:
				phase = returnPhase;
				break;
			case TRADE_PROPOSED:
				if(!tradeProposal.isTradeReplied()){
					//we did not accept or counteroffer. we should pass to the next player in the queue to propse to
					int nextPlayerToRespondNum = 0;
					if(tradeProposal.hasNextPlayerToPropose()){
						Player nextPlayerToRespond = tradeProposal.getNextPlayerToPropose();
						nextPlayerToRespondNum = nextPlayerToRespond.getPlayerNumber();
						tradeProposal.setCurrentPlayerToProposeId(nextPlayerToRespondNum);
						activeGameFragment.mListener.endTurn(gameParticipantIds.get(nextPlayerToRespondNum),false);

					} else {
						startTradeRespondedPhase();
					}

				} else {
					startTradeRespondedPhase();
				}
				break;
			case TRADE_RESPONDED:
				//reset the board to state before trade
				setTradeProposal(null);
				phase = Phase.PLAYER_TURN;
				break;
			case BUILD_METROPOLIS:
				phase = Phase.PLAYER_TURN;
				getPlayerOfCurrentGameTurn().metropolisTypeToBuild = -1;
				break;
			case DEFENDER_OF_CATAN:
				//pass to the next player according to stack
				if(!playerIdsYetToAct.isEmpty()){
					int nextPlayerToRespondNum = playerIdsYetToAct.peek();
					activeGameFragment.mListener.endTurn(gameParticipantIds.get(nextPlayerToRespondNum),false);
				} else {
					phase = Phase.PLAYER_TURN;
					activeGameFragment.mListener.endTurn(gameParticipantIds.get(curPlayerNumber),false);
				}
            case PLACE_MERCHANT:
                phase = Phase.PLAYER_TURN;
			case DONE:
				return false;
		}

		return turnChanged;
	}

	/**
	 * Proceed to the next phase or next turn
	 *
	 */
	public boolean nextPhase(int choice) {
		boolean turnChanged = false;

		switch (phase) {
			case CHOOSE_ROBBER_PIRATE:
				if (choice == 0) {
					startRobberPhase();
				}
				else if (choice == 1) {
					startPiratePhase();
				}
				else {
					return false;
				}
				break;
			case DONE:
				return false;
		}

		return turnChanged;
	}

	/**
	 * Enter progress card phase 1
	 */
	public void startProgressPhase1() {
		returnPhase = phase;
		phase = Phase.PROGRESS_CARD_STEP_1;
		runAITurn();
	}

    /**
     * Enter moving ship phase
     */
    public void startMovingShipPhase(Edge prevShipLocation) {
        returnPhase = phase;
        phase = Phase.MOVING_SHIP;
        tempEdgeIdMemory = prevShipLocation.getId();
        runAITurn();
    }

	/**
	 * Cancel moving ship phase
	 */
	public void cancelMovingShipPhase() {
		Edge prevShipLocation = getEdgeById(tempEdgeIdMemory);
		prevShipLocation.moveShipToHere(getPlayerOfCurrentGameTurn());
		nextPhase();
	}

	/**
	 * Enter moving knight phase
	 */
	public void startMovingKnightPhase(Knight toMove) {
		returnPhase = phase;
		phase = Phase.MOVING_KNIGHT;
		tempKnightIdMemory = toMove.getId();
		tempVertexIdMemory = toMove.getCurrentVertexLocation().getId();
		runAITurn();
	}

	/**
	 * Cancel moving knight phase
	 */
	public void cancelMovingKnightPhase() {
		Vertex prevKnightLocation = getVertexById(tempVertexIdMemory);
		prevKnightLocation.moveKnightBackHere();
		nextPhase();
	}

	/**
	 * Enter the trade proposal phase
	 */
	public void startTradeProposedPhase() {
		this.setPhase(Board.Phase.TRADE_PROPOSED);
		int playerPropose = tradeProposal.getCurrentPlayerToProposeId();
		activeGameFragment.mListener.endTurn(this.getPlayerById(playerPropose).getGooglePlayParticipantId(), false);
	}

	/**
	 * Enter the knight displacement phase
	 */
	public void startKnightDisplacementPhase(Knight toDisplace) {
		//TODO: what else when moving knight?
		// set return phase to the player turn of current active player
		returnPhase = phase;
		phase = Phase.DISPLACING_KNIGHT;
		tempKnightIdMemory = toDisplace.getId();
		tempVertexIdMemory = toDisplace.getCurrentVertexLocation().getId();
		Player displacedKnightOwner = toDisplace.getOwnerPlayer();
		tempPlayerNumberMemory = displacedKnightOwner.getPlayerNumber();
		toDisplace.displaceFromPost();
		if(displacedKnightOwner.isBot()) {
			runAITurn();
		}
		else {
			activeGameFragment.mListener.endTurn(
					displacedKnightOwner.getGooglePlayParticipantId(), false);
		}
		toast("Your turn will resume when your opponent moves the displaced knight! Check back later.");
	}

	/**
	 * Enter the trade Responded phase
	 */
	public void startTradeRespondedPhase() {
		this.setPhase(Phase.TRADE_RESPONDED);
		activeGameFragment.mListener.endTurn(getPlayerById(
				tradeProposal.getTradeCreatorPlayerId()).getGooglePlayParticipantId(), false);
	}

	public void resolveBarbarians(){
		if(barbarianPosition >= 7){
			//the barbarians attack!!
			toast("The barbarians attack!");
			players[curPlayerNumber].appendAction(R.string.player_barbarians_attack);
			int barbarianStrength = 0;
			for(int i = 0; i < numPlayers; i++){
				barbarianStrength += players[i].getNumOwnedCities();
			}

			int strengthCatan = 0;
			Knight currentKnight;
			int[] playerStrength = new int[numPlayers];
			Arrays.fill(playerStrength, 0);
			for(int i = 0; i < knights.length; i++){
				currentKnight = knights[i];
				if(currentKnight.isActive()){
					switch(currentKnight.getKnightRank()){
						case BASIC_KNIGHT:
							strengthCatan += 1;
							playerStrength[currentKnight.getOwnerPlayer().getPlayerNumber()] += 1;
							break;
						case STRONG_KNIGHT:
							strengthCatan += 2;
							playerStrength[currentKnight.getOwnerPlayer().getPlayerNumber()] += 2;
							break;
						case MIGHTY_KNIGHT:
							strengthCatan += 3;
							playerStrength[currentKnight.getOwnerPlayer().getPlayerNumber()] += 3;
							break;
						default:
							break;

					}
				}
			}
			boolean barbariansWin = (barbarianStrength > strengthCatan);
			//performAttack();
			if(barbariansWin){
				players[curPlayerNumber].appendAction(R.string.player_barbarians_won);
				int minValue = Integer.MAX_VALUE;
				ArrayList<Player> losers = new ArrayList<>();
				for(int i=0; i < numPlayers; i++){
					int playerMetropolisOwned = 0;
					if(metropolisOwners[0] == i) playerMetropolisOwned++;
					if(metropolisOwners[1] == i) playerMetropolisOwned++;
					if(metropolisOwners[2] == i) playerMetropolisOwned++;
					int playerCitiesOwned = players[i].getNumOwnedCities();

					//dont count players that don't have cities or only metropolis
					if(playerCitiesOwned > 0 && (playerMetropolisOwned != playerCitiesOwned)){
						if(playerStrength[i] < minValue){
							losers.clear();
							losers.add(players[i]);
							minValue = playerStrength[i];
						} else if(playerStrength[i] == minValue){
							losers.add(players[i]);
						}
					}
				}

				for(Player loser : losers){
					loser.pillageCity();
				}

			} else{
				players[curPlayerNumber].appendAction(R.string.player_barbarians_lose);
				int maxValue = Integer.MIN_VALUE;
				ArrayList<Player> winners = new ArrayList<>();
				for(int i=0; i < numPlayers; i++){
					if(playerStrength[i] > maxValue){
						winners.clear();
						winners.add(players[i]);
						maxValue = playerStrength[i];
					} else if(playerStrength[i] == maxValue){
						winners.add(players[i]);
					}
				}

				if(winners.size() == 1){
					players[curPlayerNumber].appendAction(R.string.player_barbarians_defender, winners.get(0).getPlayerName());
					winners.get(0).wonDefenderOfCatan();
				} else if(winners.size() > 1){
					Player currentPlayer = players[curPlayerNumber];
					boolean currentPlayerDefended = false;
					if(winners.contains(currentPlayer)){
						//dont pass turn to us after
						winners.remove(currentPlayer);
						currentPlayerDefended = true;
						players[curPlayerNumber].appendAction(R.string.player_barbarians_tie, currentPlayer.getPlayerName());
					}
					for(Player winner : winners){
						playerIdsYetToAct.add(winner.getPlayerNumber());
						players[curPlayerNumber].appendAction(R.string.player_barbarians_tie, winner.getPlayerName());
					}

					if(currentPlayerDefended) playerIdsYetToAct.add(curPlayerNumber);

					this.setPhase(Board.Phase.DEFENDER_OF_CATAN);
					nextPhase();
				}

			}
			for(int i = 0; i < knights.length; i ++){
				knights[i].deactivate();
			}
			barbarianPosition = 0;
		}
	}
	/**
	 * Determine if we're in a setup phase
	 * 
	 * @return true if the game is in setup phase
	 */
	public boolean isSetupPhase() {
		return (phase == Phase.SETUP_SETTLEMENT || phase == Phase.SETUP_EDGE_UNIT_1
				|| phase == Phase.SETUP_CITY || phase == Phase.SETUP_EDGE_UNIT_2);
	}

	public boolean isSetupSettlement() {
		return (phase == Phase.SETUP_SETTLEMENT);
	}

	public boolean isSetupCity() {
		return (phase == Phase.SETUP_CITY);
	}

	public boolean isSetupRoadOrShip() {
		return (phase == Phase.SETUP_EDGE_UNIT_1 || phase == Phase.SETUP_EDGE_UNIT_2);
	}

	public boolean isSetupPhase2() {
		return (phase == Phase.SETUP_CITY || phase == Phase.SETUP_EDGE_UNIT_2);
	}

	public boolean isChooseRobberPiratePhase() {
		return (phase == Phase.CHOOSE_ROBBER_PIRATE);
	}

	public boolean isRobberPhase() {
		return (phase == Phase.MOVING_ROBBER);
	}

	public boolean isPiratePhase() {
		return (phase == Phase.MOVING_PIRATE);
	}

	public boolean isProduction() {
		return (phase == Phase.PRODUCTION);
	}

	public boolean isPlayerTurnPhase() {
		return (phase == Phase.PLAYER_TURN);
	}

    public boolean isMovingShipPhase() {
		return (phase == Phase.MOVING_SHIP);
	}

	public boolean isMovingKnightPhase() {
		return (phase == Phase.MOVING_KNIGHT);
	}

	public boolean isKnightDisplacementPhase() { return (phase == Phase.DISPLACING_KNIGHT);}

	public boolean isProgressPhase() {
		return (phase == Phase.PROGRESS_CARD_STEP_1 || phase == Phase.PROGRESS_CARD_STEP_2);
	}

	public boolean isProgressPhase1() {
		return (phase == Phase.PROGRESS_CARD_STEP_1);
	}

	public boolean isProgressPhase2() {
		return (phase == Phase.PROGRESS_CARD_STEP_2);
	}

	public boolean isTradeProposedPhase() { return (phase == Phase.TRADE_PROPOSED);}

	public boolean isTradeRespondedPhase() { return (phase == Phase.TRADE_RESPONDED);}

	public boolean isBuildMetropolisPhase() { return (phase == Phase.BUILD_METROPOLIS);}

    public boolean isPlaceMerchantPhase() { return (phase == Phase.PLACE_MERCHANT);}

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
		{
			return null;
		}

		return harbors[harborId];
	}

	/**
	 * Get a given fishing ground by id
	 *
	 * @param fishingGroundId
	 *            the id of the fishing ground
	 * @return the fishing ground with fishingGroundId(or null)
	 */
	public FishingGround getFishingGroundById(int fishingGroundId) {
		if (fishingGroundId < 0 || fishingGroundId >= boardGeometry.getFishingGroundCount())
		{
			return null;
		}

		return fishingGrounds[fishingGroundId];
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

	public int[] getMetropolisOwners() {
		return metropolisOwners;
	}

	public void setMetropolisOwners(int[] metropolisOwners) {
		this.metropolisOwners = metropolisOwners;
	}

	/**
	 * Get the knight with the given id
	 *
	 * @param knightId
	 *            the id of the knight
	 * @return the knight
	 */
	public Knight getKnightById(int knightId) {
		if (knightId < 0 || knightId >= knights.length)
		{
			return null;
		}

		return knights[knightId];
	}

	public Knight getNextAvailableKnight() {
		if (this.nextAvailableKnightId < 0 ||
				this.nextAvailableKnightId >= knights.length)
		{
			return null;
		}
		Knight nextAvailableKnight = knights[this.nextAvailableKnightId];
		this.nextAvailableKnightId += 1;
		return nextAvailableKnight;
	}

	public Knight[] getKnights() {
		return knights;
	}

	public boolean isPirateDisabled() {
		return pirateDisabled;
	}

	public void setPirateDisabled(boolean pirateDisabled) {
		this.pirateDisabled = pirateDisabled;
	}
	public boolean isRobberDisabled() {
		return robberDisabled;
	}

	public void setRobberDisabled(boolean robberDisabled) {
		this.robberDisabled = robberDisabled;
	}

	public void startChooseRobberPiratePhase() {
		this.returnPhase = phase;
		this.phase = Phase.CHOOSE_ROBBER_PIRATE;
		//if there are other to discard, pass the turn to them
		//the turn passing then occurs in the turnHandler for each player.
		//the turn is passed in the discardResourcesFragment
		//when the stack is empty, the turn is passed back to the player that rolled 7
		if(!playerIdsYetToAct.isEmpty()){
			activeGameFragment.mListener.endTurn(
					getPlayerById(playerIdsYetToAct.peek()).getGooglePlayParticipantId(), false);
			toast("Your turn will resume when all players have discarded! Check back later.");
		}
		// run AI's turn
		runAITurn();
	}

	/**
	 * Enter the robber placement phase
	 */
	public void  startRobberPhase() {
		if(this.curRobberHexId != null) {
			hexagons[this.curRobberHexId].removeRobber();
		}
		this.prevRobberHexId = this.curRobberHexId;
		this.curRobberHexId = null;
		phase = Phase.MOVING_ROBBER;
		// run AI's turn
		runAITurn();
	}

	/**
	 * Enter the pirate placement phase
	 */
	public void startPiratePhase() {
		if(this.curPirateHexId != null) {
			hexagons[this.curPirateHexId].removePirate();
		}
		this.prevPirateHexId = this.curPirateHexId;
		this.curPirateHexId = null;
		phase = Phase.MOVING_PIRATE;
		// run AI's turn
		runAITurn();
	}

	/**
	 * Get the hexagon with the robber
	 *
	 * @return the hexagon with the robber
	 */
	public Hexagon getCurRobberHex() {
		return curRobberHexId != null ? hexagons[curRobberHexId] : null;
	}

	/**
	 * Get the hexagon with the pirate
	 *
	 * @return the hexagon with the pirate
	 */
	public Hexagon getCurPirateHex() {
		return curPirateHexId != null ? hexagons[curPirateHexId] : null;
	}

	/**
	 * If the robber is being moved, return the last hexagons where it last
	 * resided, or otherwise the current location
	 *
	 * @return the last location of the robber
	 */
	public Hexagon getPrevRobberHex() {
		int hexCount = this.boardGeometry.getHexCount();
		int curRobberId = this.curRobberHexId != null ? hexagons[this.curRobberHexId].getId() : -1;
		int prevRobberId = this.prevRobberHexId != null ? hexagons[this.prevRobberHexId].getId() : -1;
		if (this.phase == Phase.MOVING_ROBBER && prevRobberId >= 0 && prevRobberId < hexCount)
		{
			return hexagons[this.prevRobberHexId];
		}
		else if (curRobberId >= 0 && curRobberId < hexCount) {
			return hexagons[this.curRobberHexId];
		}
		else {
			return null;
		}
	}

    /**
     * If the pirate is being moved, return the last hexagons where it last
     * resided, or otherwise the current location
     *
     * @return the last location of the pirate
     */
    public Hexagon getPrevPirateHex() {
        int hexCount = this.boardGeometry.getHexCount();
        int curPirateHexId = this.curPirateHexId != null ?
                hexagons[this.curPirateHexId].getId() : -1;
        int prevPirateHexId = this.prevPirateHexId != null
                ? hexagons[this.prevPirateHexId].getId() : -1;
        if (this.phase == Phase.MOVING_PIRATE && prevPirateHexId >= 0 && prevPirateHexId < hexCount)
        {
            return hexagons[this.prevPirateHexId];
        }
        else if (curPirateHexId >= 0 && curPirateHexId < hexCount) {
            return hexagons[this.curPirateHexId];
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
		if (this.curRobberHexId != null && hexagons != null) {
			hexagons[curRobberHexId].removeRobber();
		}
		this.curRobberHexId = curRobberHex.getId();
		curRobberHex.setRobber();
		return true;
	}

    /**
     * Set the current robber hexagon
     *
     * @param curPirateHex
     *            current robber hexagon
     * @return true iff the currebt robber hex was set
     */
    public boolean setCurPirateHex(Hexagon curPirateHex) {
        if (this.curPirateHexId != null && hexagons != null) {
            hexagons[curPirateHexId].removePirate();
        }
        this.curPirateHexId = curPirateHex.getId();
        curPirateHex.setPirate();
        return true;
    }

    /**
     * Set the current merchant hexagon
     *
     * @param curMerchantHex
     *            current merchant hexagon
     * @return true iff the currebt merchant hex was set
     */
    public boolean setCurMerchantHex(Hexagon curMerchantHex) {
        if (this.curMerchantHexId != -1 && hexagons != null) {
            hexagons[curMerchantHexId].removeMerchant();
            merchantType = null;
        }
        this.curMerchantHexId = curMerchantHex.getId();
        curMerchantHex.setMerchant();
        merchantType = curMerchantHex.getResourceType();
        return true;
    }

    public int getMerchantOwner() {
        return merchantOwner;
    }

    public void setMerchantOwner(int merchantOwner) {
        this.merchantOwner = merchantOwner;
    }

    public Resource.ResourceType getMerchantType() {
        return merchantType;
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
	 * Get the barbarian position
	 *
	 * @return barbarian position
	 */
	public int getBarbarianPosition() {
		return barbarianPosition;
	}

	/**
	 * Pillage one city to a settlement
	 *
	 * @return true if we destroyed a wall, false if it was city
	 */
	public boolean pillageCity(int playerNumber)
	{
		for(int i = 0; i < vertices.length; i++){
			boolean isPillageableCity = vertices[i].getCurUnitType() == Vertex.CITY;
			boolean isPillageableWall = vertices[i].getCurUnitType() == Vertex.WALLED_CITY;

			if(vertices[i].getOwnerPlayer() != null && vertices[i].getOwnerPlayer().getPlayerNumber() == playerNumber
					&& isPillageableCity){
				vertices[i].setCurUnitType(Vertex.SETTLEMENT);
				return false;
			} else if(vertices[i].getOwnerPlayer() != null && vertices[i].getOwnerPlayer().getPlayerNumber() == playerNumber
					&& isPillageableWall){
				vertices[i].setCurUnitType(Vertex.SETTLEMENT);
				return true;
			}
		}
		return false;
	}

	//TODO: adapt to fit ship requirements
	/**
	 * Update the longest trade route length and owner
	 */
	public void updateLongestTradeRoute() {
		Player previousOwner = longestTradeRouteOwnerId != null ? players[longestTradeRouteOwnerId] : null;

		// reset trade route length in case a path was broken
		longestTradeRouteLength = 4;
		longestTradeRouteOwnerId = null;

		// temporarily reset players' trade route lengths to 0
		for (int i = 0; i < numPlayers; i++)
		{
			players[i].cancelMyLongestTradeRouteLength();
		}

		// find longest trade route
		int i, longestTradeRouteFromCurEdge;
		Edge curEdge = null;
		Player curEdgeOwner;
		for (i = 0; i < edges.length; i++) {
			curEdge = edges[i];
			if (curEdge.hasEdgeUnit()) {
				longestTradeRouteFromCurEdge = curEdge.getLongestTradeRouteLengthFromHere(
						++numberOfLongestTradeRouteUpdates);

				curEdgeOwner = curEdge.getOwnerPlayer();
				curEdgeOwner.notifyTradeRouteLength(longestTradeRouteFromCurEdge);
				if (longestTradeRouteFromCurEdge > longestTradeRouteLength) {
					longestTradeRouteLength = longestTradeRouteFromCurEdge;
					longestTradeRouteOwnerId = curEdgeOwner.getPlayerNumber();
				}
			}
		}

		// barring a change, the previous title holder retains the achievement
		if (previousOwner != null
				&& previousOwner.getMyLongestTradeRouteLength() == longestTradeRouteLength)
		{
			longestTradeRouteOwnerId = previousOwner.getPlayerNumber();
		}
	}

	/**
	 * Determine if a player currently has the longest trade route
	 * 
	 * @param player
	 *            the player
	 * @return true iff player currently has the longest trade route
	 */
	public boolean hasLongestTradeRoute(Player player) {
		return (longestTradeRouteOwnerId != null
				&& player.getPlayerNumber() == longestTradeRouteOwnerId);
	}

	/**
	 * Get the length of the longest road
	 * 
	 * @return the length of the longest road
	 */
	public int getLongestTradeRouteLength() {
		return longestTradeRouteLength;
	}

	/**
	 * Get the current owner of the longest trade route
	 * 
	 * @return the current owner of the longest trade route
	 */
	public Player getLongestTradeRouteOwner() {
		return longestTradeRouteOwnerId != null ? players[longestTradeRouteOwnerId] : null;
	}

	/**
	 * Check if any players need to discard_resources
	 * 
	 * @return true if one or more players need to discard_resources
	 */
	public boolean hasPlayersYetToAct() {
		return !playerIdsYetToAct.empty();
	}

	/**
	 * Get the next players queued for discarding
	 *
	 * @return a players or null
	 */
	public Player checkNextPlayerToAct() {
		try {
			Integer playerId = playerIdsYetToAct.peek();
			return playerId != null ? players[playerId] : null;
		} catch (EmptyStackException e) {
			return null;
		}
	}

	/**
	 * Get the next players queued for discarding
	 * 
	 * @return a players or null
	 */
	public Player getPlayerToAct() {
		try {
			Integer playerId = playerIdsYetToAct.pop();
			return playerId != null ? players[playerId] : null;
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
			case SETUP_EDGE_UNIT_1:
				return R.string.phase_first_edge_unit;
			case SETUP_CITY:
				return R.string.phase_first_city;
			case SETUP_EDGE_UNIT_2:
				return R.string.phase_second_edge_unit;
			case PRODUCTION:
				return R.string.phase_your_turn;
			case PLAYER_TURN:
                return R.string.phase_player_turn;
            case MOVING_SHIP:
                return R.string.phase_move_ship;
			case MOVING_KNIGHT:
				return R.string.phase_move_knight;
			case DISPLACING_KNIGHT:
				return R.string.phase_displacing_knight;
			case PROGRESS_CARD_STEP_1:
				// TODO: progress card step 1
				return 0;
			case PROGRESS_CARD_STEP_2:
				// TODO: progress card step 2
				return 0;
			case CHOOSE_ROBBER_PIRATE:
				return R.string.phase_make_choice;
			case MOVING_ROBBER:
				return R.string.phase_move_robber;
			case MOVING_PIRATE:
				return R.string.phase_move_pirate;
			case TRADE_PROPOSED:
				return R.string.waiting_trade_proposed;
			case TRADE_RESPONDED:
				return R.string.waiting_trade_responded;
			case BUILD_METROPOLIS:
				return R.string.game_build_metropolis;
			case DEFENDER_OF_CATAN:
				return R.string.game_defended_catan_wait_pick_card;
			case PLACE_MERCHANT:
				return R.string.game_place_merchant;
			case DONE:
				return R.string.phase_game_over;
			}

		return 0;
	}

	public boolean itsMyTurn(String myParticipantId){
		String s = this.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId();
		return (this.getPlayerOfCurrentGameTurn().getGooglePlayParticipantId().equals(myParticipantId));
	}

	/**
	 * Get the global turn number
	 *
	 * @return the turn number (one per player turn, starting at 1)
	 */
	public int getGameTurnNumber() {
		return gameTurnNumber;
	}

	/**
	 * Get the global round number
	 * 
	 * @return the round number (one round per full set of player turns, starting at 1)
	 */
	public int getGameRoundNumber() {
		return gameRoundNumber;
	}

	/**
	 * Get the winner
	 * 
	 * @return the winning player or null
	 */
	public Player getWinner() {
		// winnerId already found or we just want to check what was already found
		if (winnerId != null)
		{
			return players[winnerId];
		}

		// check for winnerId
		for (int i = 0; i < numPlayers; i++) {
			int hasBoot = 0;
			if(playerNumBootOwner == players[i].getPlayerNumber()) hasBoot = 1;
			if (players[i].getVictoryPoints() >= (maxPoints+hasBoot)) {
				winnerId = players[i].getPlayerNumber();
				if(phase != phase.DONE){
					//we need to tell google the game is done
					activeGameFragment.mListener.endTurn(players[winnerId].getGooglePlayParticipantId(),true);
				}
				phase = Phase.DONE;
				break;
			}
		}

		return winnerId != null ? players[winnerId] : null;
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
		for (FishingGround fishingGround : fishingGrounds) {
			fishingGround.setBoard(this);
		}
		for (Player player : players) {
			player.setBoard(this);
		}
		for(Knight knight : knights) {
			knight.setBoard(this);
		}
	}

	public void reinitPlayers(String[] names, ArrayList<String> ids){
        for(int i = 0; i < players.length; i++){
            players[i].setPlayerName(names[i]);
            players[i].setGooglePlayParticipantId(ids.get(i));
        }
    }


	public ProgressCard.ProgressCardType pickNewProgressCard(CityImprovement.CityImprovementType type){
		switch(type){
			case TRADE:
				return tradeDeck.get(0);
			case SCIENCE:
				return scienceDeck.get(0);
			case POLITICS:
				return politicsDeck.get(0);
			default:
				return null;
		}
	}

	public void returnProgressCard(ProgressCard.ProgressCardType card){

		switch(ProgressCard.getDisciplineFromCard(card)){
			case TRADE:
				tradeDeck.add(card);
				break;
			case SCIENCE:
				scienceDeck.add(card);
				break;
			case POLITICS:
				politicsDeck.add(card);
				break;
			default:
				break;
		}
	}

	public boolean progressCardStackEmpty(CityImprovement.CityImprovementType type){
		switch(type){
			case TRADE:
				return tradeDeck.isEmpty();
			case POLITICS:
				return politicsDeck.isEmpty();
			case SCIENCE:
				return scienceDeck.isEmpty();
		}
		return true;
	}

	public void progressCardInit(){

		tradeDeck = new ArrayList<>();
		scienceDeck = new ArrayList<>();
		politicsDeck = new ArrayList<>();

		//@TODO for testing purposes ONLY
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);

		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		scienceDeck.add(ProgressCard.ProgressCardType.MERCHANT);

		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);
		politicsDeck.add(ProgressCard.ProgressCardType.MERCHANT);


		//@TODO Implement all these progress cards
//		tradeDeck.add(ProgressCard.ProgressCardType.COMMERCIAL_HARBOR);
//		tradeDeck.add(ProgressCard.ProgressCardType.COMMERCIAL_HARBOR);
//		tradeDeck.add(ProgressCard.ProgressCardType.MASTER_MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MASTER_MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT_FLEET);
//		tradeDeck.add(ProgressCard.ProgressCardType.MERCHANT_FLEET);
//		tradeDeck.add(ProgressCard.ProgressCardType.RESOURCE_MONOPOLY);
//		tradeDeck.add(ProgressCard.ProgressCardType.RESOURCE_MONOPOLY);
//		tradeDeck.add(ProgressCard.ProgressCardType.RESOURCE_MONOPOLY);
//		tradeDeck.add(ProgressCard.ProgressCardType.RESOURCE_MONOPOLY);
//		tradeDeck.add(ProgressCard.ProgressCardType.TRADE_MONOPOLY);
//		tradeDeck.add(ProgressCard.ProgressCardType.TRADE_MONOPOLY);
//
//		scienceDeck.add(ProgressCard.ProgressCardType.ALCHEMIST);
//		scienceDeck.add(ProgressCard.ProgressCardType.ALCHEMIST);
//		scienceDeck.add(ProgressCard.ProgressCardType.CRANE);
//		scienceDeck.add(ProgressCard.ProgressCardType.CRANE);
//		scienceDeck.add(ProgressCard.ProgressCardType.ENGINEER);
//		scienceDeck.add(ProgressCard.ProgressCardType.INVENTOR);
//		scienceDeck.add(ProgressCard.ProgressCardType.INVENTOR);
//		scienceDeck.add(ProgressCard.ProgressCardType.IRIGIATION);
//		scienceDeck.add(ProgressCard.ProgressCardType.IRIGIATION);
//		scienceDeck.add(ProgressCard.ProgressCardType.MEDICINE);
//		scienceDeck.add(ProgressCard.ProgressCardType.MEDICINE);
//		scienceDeck.add(ProgressCard.ProgressCardType.MINING);
//		scienceDeck.add(ProgressCard.ProgressCardType.MINING);
//		scienceDeck.add(ProgressCard.ProgressCardType.PRINTER);
//		scienceDeck.add(ProgressCard.ProgressCardType.ROAD_BUILLDING);
//		scienceDeck.add(ProgressCard.ProgressCardType.ROAD_BUILLDING);
//		scienceDeck.add(ProgressCard.ProgressCardType.SMITH);
//		scienceDeck.add(ProgressCard.ProgressCardType.SMITH);
//
//		politicsDeck.add(ProgressCard.ProgressCardType.BISHOP);
//		politicsDeck.add(ProgressCard.ProgressCardType.CONSTITUTION);
//		politicsDeck.add(ProgressCard.ProgressCardType.DESERTER);
//		politicsDeck.add(ProgressCard.ProgressCardType.DESERTER);
//		politicsDeck.add(ProgressCard.ProgressCardType.DIPLOMAT);
//		politicsDeck.add(ProgressCard.ProgressCardType.DIPLOMAT);
//		politicsDeck.add(ProgressCard.ProgressCardType.INTRIGUE);
//		politicsDeck.add(ProgressCard.ProgressCardType.INTRIGUE);
//		politicsDeck.add(ProgressCard.ProgressCardType.SABOTEUR);
//		politicsDeck.add(ProgressCard.ProgressCardType.SABOTEUR);
//		politicsDeck.add(ProgressCard.ProgressCardType.SPY);
//		politicsDeck.add(ProgressCard.ProgressCardType.SPY);
//		politicsDeck.add(ProgressCard.ProgressCardType.SPY);
//		politicsDeck.add(ProgressCard.ProgressCardType.WARLORD);
//		politicsDeck.add(ProgressCard.ProgressCardType.WARLORD);
//		politicsDeck.add(ProgressCard.ProgressCardType.WEDDING);
//		politicsDeck.add(ProgressCard.ProgressCardType.WEDDING);

		Collections.shuffle(tradeDeck);
		Collections.shuffle(scienceDeck);
		Collections.shuffle(politicsDeck);

	}

	public boolean isMyPseudoTurn(){
		//TODO: is this dangerous for complete onlooker players?

		//check if the trade proposal is currently proposed to me (my Pseudo turn)
		if(tradeProposal != null) {
			return (getPlayerById(
			        tradeProposal.getCurrentPlayerToProposeId()
            ).getGooglePlayParticipantId().equals(activeGameFragment.myParticipantId));
		}
		else if(hasPlayersYetToAct()){
			return true;
		}
        else if(phase == Phase.CHOOSE_ROBBER_PIRATE &&
                !getPlayerOfCurrentGameTurn().getGooglePlayParticipantId().equals(
                        activeGameFragment.myParticipantId)) {
            return true;
        }
        else if(phase == Phase.DISPLACING_KNIGHT &&
				getPlayerToDisplaceKnight().getGooglePlayParticipantId().equals(
						activeGameFragment.myParticipantId)) {
			return true;
		}
		return false;
	}

	public Player getPlayerFromParticipantId(String pId){
		return players[gameParticipantIds.indexOf(pId)];
	}

    private void toast(String message) {
        Toast.makeText(activeGameFragment.getActivity().getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

}
