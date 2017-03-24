package com.catandroid.app.common.players;

import java.util.Vector;

import android.content.Context;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.CityImprovement;
import com.catandroid.app.common.components.board_pieces.Knight;
import com.catandroid.app.common.components.board_pieces.ProgressCard.ProgressCardType;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_pieces.Resource.ResourceType;
import com.catandroid.app.R;
import com.catandroid.app.CatAndroidApp;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Vertex;

public class Player {

	private static boolean FREE_BUILD = false;

	private static final String[] EVENT_ROLL_STRINGS = { "", "☠", "☠", "☠", "Trade", "Science", "Politics" };

	public static final int MAX_SETTLEMENTS = 5;
	public static final int MAX_CITIES = 4;
	public static final int MAX_CITY_WALLS = 3;
	public static final int MAX_ROADS = 15;
	public static final int MAX_SHIPS = 15;
	public static final int MAX_BASIC_KNIGHTS = 2;
	public static final int MAX_STRONG_KNIGHTS = 2;
	public static final int MAX_MIGHTY_KNIGHTS = 2;
	public static final int MAX_TOTAL_KNIGHTS = 6;

	public static final int[] ROAD_COST = { 1, 0, 0, 1, 0, 0 };
	public static final int[] SETTLEMENT_COST = { 1, 1, 1, 1, 0, 0 };
	public static final int[] CITY_COST = { 0, 0, 2, 0, 3, 0 };

	private String googlePlayParticipantId;
	private int playerNumber;
	private Color color;
	private String playerName;
	protected int numOwnedSettlements;
	protected int numOwnedCities;
	protected int numOwnedCityWalls;
	protected int numTotalOwnedKnights;
	protected int numOwnedBasicKnights;
	protected int numOwnedStrongKnights;
	protected int numOwnedMightyKnights;
	protected Vector<Integer> ownedCommunityIds, reachingVertexIds;
	protected Vector<Integer> roadIds, shipIds;
	protected Vector<Integer> myKnightIds;
	private int defenderOfCatan = 0;
	private int playerType, privateVictoryPointsCount,
			tradeValue, myLongestTradeRouteLength, latestBuiltCommunityId;
	private int[] countPerResource, countPerProgressCard;
	private int[] cityImprovementLevels = {0, 0, 0};
	private boolean[] harbors;
	//TODO: should this be an integer?
	private Vector<ProgressCardType> hand;
	private Vector<ProgressCardType> newCards;
	private boolean usedCardThisTurn;
	private boolean shipWasMovedThisTurn;
	public int metropolisTypeToBuild;
	public boolean gotResourcesSinceLastTurn = false;
	private String actionLog;

	protected transient Board board;

	public enum Color {
		RED, BLUE, GREEN, YELLOW, SELECTING, NONE
	}

	public static final int PLAYER_HUMAN = 0;
	public static final int PLAYER_BOT = 1;
	public static final int PLAYER_ONLINE = 2;

	/**
	 * Initialize player object
	 *
	 * @param board
	 *            board costs_reference
	 * @param playerName
	 *            player name
	 * @param playerType
	 *            PLAYER_HUMAN, PLAYER_BOT, or PLAYER_ONLINE
	 */
	public Player(Board board, int playerNumber, String googlePlayParticipantId,
				  Color color, String playerName, int playerType) {
		this.board = board;
		this.googlePlayParticipantId = googlePlayParticipantId;
		this.color = color;
		this.playerName = playerName;
		this.playerType = playerType;
		this.playerNumber = playerNumber;

		numOwnedSettlements = 0;
		numOwnedCities = 0;
		numOwnedCityWalls = 0;
		hand = new Vector<>();
		numTotalOwnedKnights = 0;
		numOwnedBasicKnights = 0;
		numOwnedStrongKnights = 0;
		numOwnedMightyKnights = 0;
		myLongestTradeRouteLength = 0;
		privateVictoryPointsCount = 0;
		tradeValue = 4;
		usedCardThisTurn = false;
		shipWasMovedThisTurn = false;
		actionLog = "";
		latestBuiltCommunityId = -1;
		metropolisTypeToBuild = -1;

		hand = new Vector<ProgressCardType>();

		ownedCommunityIds = new Vector<Integer>();
		reachingVertexIds = new Vector<Integer>();
		roadIds = new Vector<Integer>();
		shipIds = new Vector<Integer>();
        myKnightIds = new Vector<Integer>();

		countPerResource = new int[Resource.RESOURCE_TYPES.length];
		harbors = new boolean[Resource.ResourceType.values().length];
		for (int i = 0; i < countPerResource.length; i++) {
			//everyone starts with 2 gold coins
			//everyone has hold harbour by default
			if(Resource.RESOURCE_TYPES[i] == Resource.ResourceType.GOLD){
				harbors[i] = true;
				countPerResource[i] = 2;
			} else {
				harbors[i] = false;
				countPerResource[i] = 0;
			}


		}
	}

	/**
	 * Set the board
	 *
	 * @param board
	 *
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Roll the dice
	 *
	 * @return the result of dice roll
	 */
	public int rollDice() {
		return rollDice((int) (Math.random() * 6) + 1,
				(int) (Math.random() * 6) + 1 , (int) (Math.random() * 6) + 1);
	}

	/**
	 * Roll the dice with a predefined result
	 *
	 * @param redDie
	 * @param yellowDie
	 * @param eventDie
	 * @return the result of the dice roll
	 */
	public int rollDice(int yellowDie, int redDie, int eventDie) {
		String eventText = "";
		if(eventDie < 4){
			eventText = "barbarian";
		} else{
			eventText = EVENT_ROLL_STRINGS[eventDie];
		}

		appendAction(R.string.player_roll, Integer.toString(redDie+yellowDie));
		appendAction("\t\tRed Die: " + redDie);
		appendAction("Event Rolled: " + eventText);
		board.executeDiceRoll(redDie, yellowDie, eventDie);

		return redDie + yellowDie;
	}

	/**
	 * Function called at the beginning of the turn
	 */
	public void beginTurn() {
		// clear the action log
		actionLog = "";
	}

	/**
	 * Function called at the end of the trun (after build phase finishes)
	 */
	public void endTurn() {
		//TODO: track player's hand
//		// add new progress cards to the set of usable cards
//		for (int i = 0; i < newCards.size(); i++)
//		{
//			countPerProgressCard[newCards.get(i).ordinal()] += 1;
//		}
//
//		newCards.clear();
//		usedCardThisTurn = false;
		shipWasMovedThisTurn = false;

		appendAction(R.string.player_ended_turn);
	}

	/**
	* Attempt to build a road on an edge. Returns true on success
	*
	* @param edge
	*            road destination
	* @return
	*/
	public boolean buildRoad(Edge edge) {
		if (edge == null || !canBuildRoad(edge))
		{
			return false;
		}

		// check resources
		//TODO: progress card effect?
		boolean free = board.isSetupPhase();
		if (!free && !canAffordToBuildRoad())
		{
			return false;
		}

		if (!edge.buildRoad(this))
		{
			return false;
		}

		if (!free) {
			useResources(Resource.ResourceType.BRICK, 1);
			useResources(Resource.ResourceType.LUMBER, 1);
		}

		appendAction(R.string.player_road);

		boolean hadLongest = (board.getLongestTradeRouteOwner() == this);
		board.updateLongestTradeRoute();

		if (!hadLongest && board.getLongestTradeRouteOwner() == this)
		{
			appendAction(R.string.player_longest_road);
		}

		roadIds.add(edge.getId());

		int vertexId = edge.getV0Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		vertexId = edge.getV1Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		return true;
	}

	/**
	 * Attempt to build a ship on edge. Returns true on success
	 *
	 * @param edge
	 *            ship destination
	 * @return
	 */
	public boolean buildShip(Edge edge) {
		if (edge == null || !canBuildShip(edge))
		{
			return false;
		}

		// check resources
		//TODO: progress card effect?
		boolean free = board.isSetupPhase();
		if (!free && !canAffordToBuildShip())
		{
			return false;
		}

		if (!edge.buildShip(this))
		{
			return false;
		}

		if (!free) {
			useResources(Resource.ResourceType.LUMBER, 1);
			useResources(Resource.ResourceType.WOOL, 1);
		}

		//TODO: longest trade route (extend with road)
//		appendAction(R.string.player_ship);
//
//		boolean hadLongest = (board.getLongestTradeRouteOwner() == this);
//		board.updateLongestTradeRoute();
//
//		if (!hadLongest && board.getLongestTradeRouteOwner() == this)
//		{
//			appendAction(R.string.player_longest_road);
//		}

		shipIds.add(edge.getId());

		int vertexId = edge.getV0Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		vertexId = edge.getV1Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		return true;
	}


	/**
	 * Attempt to remove a ship from an edge. Returns true on success
	 *
	 * @param edge
	 *            current location of ship to move
	 * @return
	 */
	public boolean removeShipFrom(Edge edge) {
		if (edge == null || !canRemoveShipFrom(edge))
		{
			return false;
		}

		if (!edge.removeShipFromHere(this))
		{
			return false;
		}

		// update board to intermediately moving the edge
		board.startMovingShipPhase(edge);

		// remove vertex ids it was reaching (unless reached by another edge)

		int vertexId = edge.getV0Clockwise().getId();
		Vertex v = board.getVertexById(vertexId);
		Edge otherEdge;
		boolean reachedVfromAnotherEdgeUnit = false;
		int i = 0;
		for (i = 0; i < 3; i++) {
			otherEdge = v.getEdge(i);
			if  (otherEdge != null && otherEdge != edge
					&& (roadIds.contains(otherEdge.getId()) || shipIds.contains(otherEdge.getId())))
            { // one of our other roads/ships is reaching v
				reachedVfromAnotherEdgeUnit = true;
				break;
            }
		}
		if (!reachedVfromAnotherEdgeUnit) { // we are moving the only edge that reached this vertex
			// remove vertex from the ones we can reach
			reachingVertexIds.removeElement(vertexId);
		}

		vertexId = edge.getV1Clockwise().getId();
		v = board.getVertexById(vertexId);
		reachedVfromAnotherEdgeUnit = false;
		for (i = 0; i < 3; i++) {
			otherEdge = v.getEdge(i);
			if  (otherEdge != null && otherEdge != edge
					&& (roadIds.contains(otherEdge) || shipIds.contains(otherEdge)))
			{ // one of our other roads/ships is reaching v
				reachedVfromAnotherEdgeUnit = true;
				break;
			}
		}
		if (!reachedVfromAnotherEdgeUnit) { // we are moving the only edge that reached this vertex
			// remove vertex from the ones we can reach
			reachingVertexIds.removeElement(vertexId);
		}

		//TODO: longest trade route (extend with road)
//		appendAction(R.string.player_ship);
//
//		boolean hadLongest = (board.getLongestTradeRouteOwner() == this);
//		board.updateLongestTradeRoute();
//
//		if (!hadLongest && board.getLongestTradeRouteOwner() == this)
//		{
//			appendAction(R.string.player_longest_road);
//		}

		return true;
	}


	/**
	 * Attempt to move a ship to a different edge. Returns true on success
	 *
	 * @param edge
	 *            target destination of ship
	 * @return
	 */
	public boolean moveShipTo(Edge edge) {
		if (edge == null || !canMoveShipTo(edge))
		{
			return false;
		}

		if (!edge.moveShipToHere(this))
		{
			return false;
		}

		// remove vertex ids it was reaching (unless reached by another edge)

		int vertexId = edge.getV0Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		vertexId = edge.getV1Clockwise().getId();
		if (!reachingVertexIds.contains(vertexId))
		{
			reachingVertexIds.add(vertexId);
		}

		//TODO: longest trade route (extend with road)
//		appendAction(R.string.player_ship);
//
//		boolean hadLongest = (board.getLongestTradeRouteOwner() == this);
//		board.updateLongestTradeRoute();
//
//		if (!hadLongest && board.getLongestTradeRouteOwner() == this)
//		{
//			appendAction(R.string.player_longest_road);
//		}

		shipWasMovedThisTurn = true;

		return true;
	}



	/**
	 * Attempt to build an establishment on vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex to build on
	 * @param unitType
	 * 			  type of vertexUnit to build
	 * @return
	 */
	public boolean buildVertexUnit(Vertex vertex, int unitType) {
		if (vertex == null || !canBuildVertexUnit(vertex, unitType))
		{
			return false;
		}

		boolean setup = board.isSetupPhase();

		// check resources based on type we want to build
		if (unitType == Vertex.SETTLEMENT) {
			if (!setup && !canAffordToBuildSettlement())
			{
				return false;
			}
		} else if (unitType == Vertex.CITY) {
			if (!setup && !canAffordToBuildCity())
			{
				return false;
			}
		} else if (unitType == Vertex.CITY_WALL) {
			if (!setup && !canAffordToBuildCityWall())
			{
				return false;
			}
		} else if(metropolisTypeToBuild != -1){
			if (!board.isBuildMetropolisPhase()){
				return false;
			}
		}
		else {
			// invalid type
			return false;
		}

		if (!vertex.build(this, unitType, setup))
		{
			return false;
		}

		// deduct resources based on type and fix count/owner
		if (vertex.getCurUnitType() == Vertex.SETTLEMENT) {
			if (!setup) {
				useResources(Resource.ResourceType.BRICK, 1);
				useResources(Resource.ResourceType.LUMBER, 1);
				useResources(Resource.ResourceType.GRAIN, 1);
				useResources(Resource.ResourceType.WOOL, 1);
			}
			numOwnedSettlements += 1;
			ownedCommunityIds.add(vertex.getId());
			board.updateLongestTradeRoute();
		} else if(vertex.getCurUnitType() == Vertex.CITY){ // city
			if (!setup) {
				useResources(Resource.ResourceType.GRAIN, 2);
				useResources(Resource.ResourceType.ORE, 3);
				numOwnedSettlements -= 1;
			}
			else {
				ownedCommunityIds.add(vertex.getId());
			}
			numOwnedCities += 1;
		} else if(vertex.getCurUnitType() == Vertex.CITY_WALL
				|| vertex.getCurUnitType() == Vertex.WALLED_POLITICS_METROPOLIS
				|| vertex.getCurUnitType() == Vertex.WALLED_SCIENCE_METROPOLIS
				|| vertex.getCurUnitType() == Vertex.WALLED_TRADE_METROPOLIS){
			if (!setup && metropolisTypeToBuild == -1) {
				useResources(Resource.ResourceType.BRICK, 2);
				numOwnedCityWalls += 1;
			}
		}
		//no need to do anything for metropolis since the cost and tracking is taken care by
		//the CityImprovementFragment

		//append to the turn log
		switch(unitType) {
			case Vertex.SETTLEMENT:
				appendAction(R.string.player_settlement);
				break;
			case Vertex.CITY:
				appendAction(R.string.player_city);
				break;
			case Vertex.CITY_WALL:
				appendAction(R.string.player_city_wall);
				break;
			case Vertex.TRADE_METROPOLIS:
				appendAction("Built a trade metropolis");
				break;
			case Vertex.SCIENCE_METROPOLIS:
				appendAction("Built a science metropolis");
				break;
			case Vertex.POLITICS_METROPOLIS:
				appendAction("Built a politics metropolis");
				break;
			default:
				break;
		}



		// TODO: does settlement vs. city matter?
		// collect resources for city during setup
		Resource.ResourceType resourceType;
		if (board.isSetupPhase2()) {
			for (int i = 0; i < 3; i++) {
				Hexagon curHex = vertex.getHexagon(i);
				Hexagon.TerrainType terrainType;
				if (curHex != null) {
					terrainType = curHex.getTerrainType();
					if (terrainType != Hexagon.TerrainType.DESERT
							&& terrainType != Hexagon.TerrainType.SEA
							&& terrainType != Hexagon.TerrainType.GOLD_FIELD) {
						// collect resource for hex adjacent to city
						resourceType = curHex.getResourceType();
						addResources(resourceType, 200);
						appendAction(R.string.player_received_x_resources,
								Integer.toString(2) + " " + Resource.toRString(resourceType));
					} else if(terrainType == Hexagon.TerrainType.GOLD_FIELD){
						// collect 4 gold coins for gold hex adjacent to city at start
						resourceType = curHex.getResourceType();
						addResources(resourceType, 400);
						appendAction(R.string.player_received_x_resources,
								Integer.toString(4) + " " + Resource.toRString(resourceType));
					}
				}
			}
		}

		latestBuiltCommunityId = vertex.getId();

		return true;
	}

	/**
	 * Attempt to hire a knight to the vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex to dispatch knight
	 * @return
	 */
	public boolean hireKnightTo(Vertex vertex) {
		if (vertex == null || !canHireKnightTo(vertex))
		{
			return false;
		}

		if (!canAffordToHireKnight()) {
			return false;
		}

		Knight hiredKnight = board.getNextAvailableKnight();
		hiredKnight.setOwnerPlayerNumber(playerNumber);

		if (!vertex.placeKnight(this, hiredKnight))
		{
			return false;
		}

		// pay for the knight
		useResources(ResourceType.WOOL, 1);
		useResources(ResourceType.ORE, 1);
		// update our knights
		myKnightIds.add(hiredKnight.getId());
		numOwnedBasicKnights += 1;
		numTotalOwnedKnights += 1;
		// knight may have blocked a trade route
		board.updateLongestTradeRoute();

		// append to the turn log
		appendAction(R.string.player_place_knight);

		return true;
	}

	/**
	 * Attempt to activate a knight at this vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex of knight to activate
	 * @return
	 */
	public boolean activateKnightAt(Vertex vertex) {
		if (vertex == null || !canActivateKnightAt(vertex))
		{
			return false;
		}

		if (!canAffordToActivateKnight()) {
			return false;
		}

		if (!vertex.activateKnight(this))
		{
			return false;
		}

		// pay for the knight activation
		useResources(ResourceType.GRAIN, 1);

		// append to the turn log
		appendAction(R.string.player_activate_knight);

		return true;
	}

	// TODO: implement promote knight
	/**
	 * Attempt to promote a knight at the vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex of knight to promote
	 * @return
	 */
	public boolean promoteKnightAt(Vertex vertex) {
		if (vertex == null || !canPromoteKnightAt(vertex))
		{
			return false;
		}

		if (!canAffordToPromoteKnight()) {
			return false;
		}

		if (!vertex.promoteKnight(this))
		{
			return false;
		}

		// pay for the knight
		useResources(ResourceType.WOOL, 1);
		useResources(ResourceType.ORE, 1);
		// update our knight counts
		Knight promotedKnight = vertex.getPlacedKnight();
		switch(promotedKnight.getKnightRank()) {
			case BASIC_KNIGHT:
				// FIXME: dirty write transaction...
				return false;
			case STRONG_KNIGHT:
				numOwnedBasicKnights -= 1;
				numOwnedStrongKnights += 1;
			case MIGHTY_KNIGHT:
				numOwnedStrongKnights -= 1;
				numOwnedMightyKnights += 1;
		}
		numTotalOwnedKnights += 1;

		// append to the turn log
		appendAction(R.string.player_promote_knight);

		return true;
	}

	/**
	 * Attempt to chase the robber with the knight at this vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex of knight with which to chase the robber
	 * @return
	 */
	public boolean chaseRobberFrom(Vertex vertex) {
		if (vertex == null || !canChaseRobberFrom(vertex))
		{
			return false;
		}

		Knight toChaseRobber = vertex.getPlacedKnight();
		if(!toChaseRobber.chaseRobber()) {
			return false;
		}

		// append to the turn log
		appendAction(R.string.player_chase_robber);

		return true;
	}

	/**
	 * Attempt to chase the pirate with the knight at this vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex of knight with which to chase the pirate
	 * @return
	 */
	public boolean chasePirateFrom(Vertex vertex) {
		if (vertex == null || !canChasePirateFrom(vertex))
		{
			return false;
		}

		Knight toChasePirate = vertex.getPlacedKnight();
		if(!toChasePirate.chasePirate()) {
			return false;
		}

		// append to the turn log
		appendAction(R.string.player_chase_pirate);

		return true;
	}

	/**
	 * Attempt to move a knight away from this vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex of knight to move
	 * @return
	 */
	public boolean removeKnightFrom(Vertex vertex) {

		if (vertex == null || !canRemoveKnightFrom(vertex))
		{
			return false;
		}

		Knight toMove = vertex.getPlacedKnight();

		if(!vertex.removeKnightFromHere(this)) {
			return false;
		}

		// NOTE: knight will update board to intermediately moving the knight

		//TODO: we will update the longest trade route when knight is actually moved

		return true;
	}

	/**
	 * Attempt to move a knight to this vertex. Returns true on success
	 *
	 * @param target
	 *            target
	 * @return
	 */
	public boolean moveKnightTo(Vertex target) {

		if (target == null || !canMoveKnightTo(target))
		{
			return false;
		}

		Knight toMove = board.getCurrentlyMovingKnight();

		if(toMove == null || !target.moveKnightToHere(this, toMove)) {
			return false;
		}

		board.updateLongestTradeRoute();

		// NOTE: calling method must update the board phase on success

		return true;
	}

	/**
	 * Can you build an edge unit on this edge?
	 *
	 * @param edge
	 * @return
	 */
	public boolean canBuildEdgeUnit(Edge edge) {
		if (edge == null ||
				(roadIds.size() + shipIds.size() >= MAX_ROADS + MAX_SHIPS))
		{
			return false;
		}

		if (board.isSetupPhase()) {
			Vertex v;
			if (latestBuiltCommunityId == -1) {
				v = null;
			}
			else {
				v = board.getVertexById(latestBuiltCommunityId);
			}
			// check if the edge is adjacent to the last settlement built
			if (v != edge.getV0Clockwise() &&
					v != edge.getV1Clockwise())
			{
				return false;
			}
		}

		return edge.canBuildEdgeUnit(this);
	}

	/**
	 * Can you build a road on this edge?
	 *
	 * @param edge
	 * @return
	 */
	public boolean canBuildRoad(Edge edge) {
		if (edge == null || roadIds.size() >= MAX_ROADS)
		{
			return false;
		}

		if (board.isSetupPhase()) {
			Vertex v;
			if (latestBuiltCommunityId == -1) {
				v = null;
			}
			else {
				v = board.getVertexById(latestBuiltCommunityId);
			}
			// check if the edge is adjacent to the last settlement built
			if (v != edge.getV0Clockwise() &&
					v != edge.getV1Clockwise())
			{
				return false;
			}
		}

		return edge.canBuildRoad(this);
	}

	/**
	 * Can you build a ship on this edge?
	 *
	 * @param edge
	 * @return
	 */
	public boolean canBuildShip(Edge edge) {
		if (edge == null || shipIds.size() >= MAX_SHIPS)
		{
			return false;
		}

		if (board.isSetupPhase()) {
			Vertex v;
			if (latestBuiltCommunityId == -1) {
				v = null;
			}
			else {
				v = board.getVertexById(latestBuiltCommunityId);
			}
			// check if the edge is adjacent to the last settlement built
			if (v != edge.getV0Clockwise() &&
					v != edge.getV1Clockwise())
			{
				return false;
			}
		}

		return edge.canBuildShip(this);
	}

	/**
	 * Can you build on this vertex?
	 *
	 * @param vertex
	 *            vertex to build on
	 * @param unitType
	 * 			  type of vertexUnit to build
	 * @return
	 */
	public boolean canBuildVertexUnit(Vertex vertex, int unitType) {
		if (unitType == Vertex.SETTLEMENT && numOwnedSettlements >= MAX_SETTLEMENTS)
		{
			return false;
		}
		else if (unitType == Vertex.CITY && numOwnedCities >= MAX_CITIES)
		{
			return false;
		}
		else if (unitType == Vertex.CITY_WALL && numOwnedCityWalls >= MAX_CITY_WALLS)
		{
			return false;
		}

		return vertex.canBuild(this, unitType, board.isSetupPhase());
	}

	/**
	 * Can you hire knight to this vertex?
	 *
	 * @param vertex
	 *            vertex to build on
	 * @return
	 */
	public boolean canHireKnightTo(Vertex vertex) {
		if (numOwnedBasicKnights >= MAX_BASIC_KNIGHTS
				|| numTotalOwnedKnights >= MAX_TOTAL_KNIGHTS)
		{
			return false;
		}

		return vertex.canPlaceKnightHere(this);
	}

	/**
	 * Can you activate a knight at this vertex?
	 *
	 * @param vertex
	 *            vertex with knight to activate
	 * @return
	 */
	public boolean canActivateKnightAt(Vertex vertex) {
		return vertex.canActivateKnightHere(this);
	}

	/**
	 * Can you promote a knight at this vertex?
	 *
	 * @param vertex
	 *            vertex with knight to activate
	 * @return
	 */
	public boolean canPromoteKnightAt(Vertex vertex) {
		if (numTotalOwnedKnights >= MAX_TOTAL_KNIGHTS)
		{
			return false;
		}

		return vertex.canPromoteKnightHere(this);
	}

	/**
	 * Can you chase the robber with one of your knights?
	 *
	 * @return
	 */
	public boolean canChaseRobber() {
		boolean canChase = false;
		Knight curKnight;
		Vertex curKnightVertexLocation;
		for (int i = 0; i < myKnightIds.size(); i++) {
			curKnight = getKnightAddedAtIndex(i);
			curKnightVertexLocation = curKnight.getCurrentVertexLocation();
			if(curKnight.canMakeMove() && curKnightVertexLocation.isAdjacentToRobber()) {
				canChase = true;
				break;
			}
		}
		return canChase;
	}

	/**
	 * Can you chase the pirate with one of your knights?
	 *
	 * @return
	 */
	public boolean canChasePirate() {
		boolean canChase = false;
		Knight curKnight;
		Vertex curKnightVertexLocation;
		for (int i = 0; i < myKnightIds.size(); i++) {
			curKnight = getKnightAddedAtIndex(i);
			curKnightVertexLocation = curKnight.getCurrentVertexLocation();
			if(curKnight.canMakeMove() && curKnightVertexLocation.isAdjacentToPirate()) {
				canChase = true;
				break;
			}
		}
		return canChase;
	}


	/**
	 * Can you chase the robber with a knight from this vertex?
	 * @param vertex
	 *            vertex from which to chase the robber
	 * @return
	 */
	public boolean canChaseRobberFrom(Vertex vertex) {
		return vertex.canChaseRobberFromHere(this);
	}

	/**
	 * Can you chase the pirate with a knight from this vertex?
	 * @param vertex
	 *            vertex from which to chase the pirate
	 * @return
	 */
	public boolean canChasePirateFrom(Vertex vertex) {
		return vertex.canChasePirateFromHere(this);
	}

	/**
	 * Can you move at least one of your knights?
	 *
	 * @return
	 */
	public boolean canMoveSomeKnight() {
		boolean canMove = false;
		Knight curKnight;
		Vertex curKnightVertexLocation;
		for (int i = 0; i < myKnightIds.size(); i++) {
			curKnight = getKnightAddedAtIndex(i);
			curKnightVertexLocation = curKnight.getCurrentVertexLocation();
			if(curKnight.canMakeMove() && curKnightVertexLocation.canRemoveKnightFromHere(this)) {
				canMove = true;
				break;
			}
		}
		return canMove;
	}

	/**
	 * Can you move the player's knight away from this vertex?
	 * @param vertex
	 *            vertex from which to remove a knight
	 * @return
	 */
	public boolean canRemoveKnightFrom(Vertex vertex) {
		return vertex.canRemoveKnightFromHere(this);
	}

	/**
	 * Can you place the currently moving knight at this vertex?
	 *
	 * @param vertex
	 * @return
	 */
	public boolean canMoveKnightTo(Vertex vertex) {
		if (vertex == null)
		{
			return false;
		}

		Knight currentlyMovingKnight = board.getCurrentlyMovingKnight();

		if(!isMyKnight(currentlyMovingKnight)) {
			return false;
		}

		return vertex.canMoveKnightToHere(this, currentlyMovingKnight);
	}

	/**
	 * Can you remove a ship from this edge?
	 *
	 * @param edge
	 * @return
	 */
	public boolean canRemoveShipFrom(Edge edge) {
		if (edge == null || shipWasMovedThisTurn)
		{
			return false;
		}

		return edge.canRemoveShipFromHere(this);
	}

	/**
	 * Can you move a ship to this edge?
	 *
	 * @param edge
	 * @return
	 */
	public boolean canMoveShipTo(Edge edge) {
		if (edge == null || shipWasMovedThisTurn)
		{
			return false;
		}

		return edge.canMoveShipToHere(this);
	}

	/**
	 * Was a ship moved by this player on current turn?
	 *
	 * @return
	 */
	public boolean movedShipThisTurn() {
		return shipWasMovedThisTurn;
	}

	/**
	 * Returns the number of cards in the players hand
	 *
	 * @return sum of player's resources
	 */
	public int getResourceCount() {
		int sum = 0;
		for (int i = 0; i < countPerResource.length; i++)
			sum += countPerResource[i];
		return sum;
	}

	/**
	 * Add resources to the player
	 *
	 * @param resourceType
	 *            resourceType of resources to add
	 * @param count
	 *            number of that resource to add
	 */
	public void addResources(Resource.ResourceType resourceType, int count) {
		//distribute the commodities when its a city, city+wall, metropolis
		boolean isCity = (count == 2 || count == 3 || count == 4 || count == 4
				|| count == 5 ||count == 6 || count == 7 || count == 8 || count == 9);
		switch(resourceType) {
			case LUMBER:
				if(isCity){
					countPerResource[Resource.toResourceIndex(ResourceType.PAPER)] += 1;
					countPerResource[resourceType.ordinal()] += 1;
				} else {
					countPerResource[resourceType.ordinal()] += count;
				}
				break;
			case WOOL:
				if(isCity){
					//@TODO REMOVE
					countPerResource[Resource.toResourceIndex(ResourceType.CLOTH)] += 1;
					countPerResource[resourceType.ordinal()] += 1;
				} else {
					countPerResource[resourceType.ordinal()] += count;
				}
				break;
			case ORE:
				if(isCity){
					countPerResource[Resource.toResourceIndex(ResourceType.COIN)] += 1;
					countPerResource[resourceType.ordinal()] += 1;
				} else {
					countPerResource[resourceType.ordinal()] += count;
				}
				break;
			case GRAIN:
				countPerResource[resourceType.ordinal()] += count;
				break;
			case BRICK:
				countPerResource[resourceType.ordinal()] += count;
				break;
			case GOLD:
				countPerResource[resourceType.ordinal()] += count;
				break;
			case PAPER:
				countPerResource[resourceType.ordinal()] += count;
				break;
			case COIN:
				countPerResource[resourceType.ordinal()] += count;
				break;
			case CLOTH:
				countPerResource[resourceType.ordinal()] += count;
				break;
			default:
				break;
		}


	}

	/**
	 * Get the number of resources a player has of a given resourceType
	 *
	 * @param resourceType
	 *            the resourceType
	 * @return the number of resources
	 */
	public int getResources(Resource.ResourceType resourceType) {
		return countPerResource[resourceType.ordinal()];
	}

	/**
	 * Get a copy of the player's resource list
	 *
	 * @return an editable copy of the player's resource list
	 */
	public int[] getCountPerResource() {
		int[] list = new int[countPerResource.length];
		for (int i = 0; i < countPerResource.length; i++) {
			list[i] = countPerResource[i];
		}

		return list;
	}

	/**
	 * Consume resources of a given resourceType
	 *
	 * @param resourceType
	 *            the resourceType to use
	 * @param count
	 *            the number to use
	 */
	public void useResources(Resource.ResourceType resourceType, int count) {
		countPerResource[resourceType.ordinal()] -= count;
	}

	/**
	 * Pick a random resource and deduct it from this player
	 *
	 * @return the type stolen
	 */
	private Resource.ResourceType stealResource() {
		int count = getResourceCount();
		if (count <= 0)
			return null;

		// pick random card
		int select = (int) (Math.random() * count);
		for (int i = 0; i < countPerResource.length; i++) {
			if (select < countPerResource[i]) {
				useResources(Resource.ResourceType.values()[i], 1);
				return Resource.ResourceType.values()[i];
			}

			select -= countPerResource[i];
		}

		return null;
	}

	/**
	 * Steal a resource from another player
	 *
	 * @param from
	 *            the player to steal from
	 * @return the type of resource stolen
	 */
	public Resource.ResourceType steal(Player from) {
		Resource.ResourceType resourceType = from.stealResource();
		return steal(from, resourceType);
	}

	/**
	 * Steal a resource from another player
	 *
	 * @param from
	 *            the player to steal from
	 * @param resourceType
	 *            the resourceType of card to be stolen
	 * @return the resourceType of resource stolen
	 */
	public Resource.ResourceType steal(Player from, Resource.ResourceType resourceType) {
		if (resourceType != null) {
			addResources(resourceType, 1);
			appendAction(R.string.player_stole_from, from.getPlayerName());
		}

		return resourceType;
	}

	/**
	 * DiscardResourcesFragment one resource of a given resourceType
	 *
	 * @param resourceType
	 *            or null for random
	 */
	public void discard(Resource.ResourceType resourceType) {
		Resource.ResourceType choice = resourceType;

		// pick random resourceType if none is specified
		if (choice == null) {
			while (true) {
				int pick = (int) (Math.random() * Resource.RESOURCE_TYPES.length);
				if (countPerResource[pick] > 0) {
					choice = Resource.RESOURCE_TYPES[pick];
					break;
				}
			}
		}

		useResources(choice, 1);

		int res = Resource.toRString(choice);
		appendAction(R.string.player_discarded, res);
	}

	/**
	 * Trade with another player
	 *
	 * @param player
	 *            the player to trade with
	 * @param resourceType
	 *            resourceType of resource to trade for
	 * @param trade
	 *            the resources to give the player
	 */
	public void trade(Player player, Resource.ResourceType resourceType, int[] trade) {
		//player is the person  that accepts the trade (loses resourceType, gains trade[]
		addResources(resourceType, 1);
		player.useResources(resourceType, 1);

		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {
			if (trade[i] <= 0)
			{
				continue;
			}

			useResources(Resource.RESOURCE_TYPES[i], trade[i]);
			player.addResources(Resource.RESOURCE_TYPES[i], trade[i]);

			for (int j = 0; j < trade[i]; j++) {
				appendAction(R.string.player_traded_away, Resource
						.toRString(Resource.RESOURCE_TYPES[i]));
			}
		}

		appendAction(R.string.player_traded_with, player.getPlayerName());
		appendAction(R.string.player_received_resource, Resource
				.toRString(resourceType));
	}

	/**
	 * Get the player's Color
	 *
	 * @return the player's Color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Get the player's playerNumber number
	 *
	 * @return the playerNumber number [0, 3]
	 */
	public int getPlayerNumber() {
		return playerNumber;
	}

	/**
	 * Returns the player's participant id
	 *
	 * @return googlePlayParticipantId
	 */
	public String getGooglePlayParticipantId() {
		return googlePlayParticipantId;
	}

	/**
	 * Set the player's participant id
	 */
	public void setGooglePlayParticipantId(String googlePlayParticipantId) {
		this.googlePlayParticipantId = googlePlayParticipantId;
	}

	/**
	 * Determine if the player can afford to build an edge unit
	 *
	 * @return true if the player can afford to build a unit
	 */
	public boolean canAffordToBuildSomeEdgeUnit() {
		return (FREE_BUILD || (roadIds.size() + shipIds.size())< MAX_ROADS + MAX_SHIPS
				&& getResources(Resource.ResourceType.LUMBER) >= 1
				&& (getResources(Resource.ResourceType.BRICK) >= 1
				|| getResources(Resource.ResourceType.WOOL) >= 1));
	}

	/**
	 * Determine if the player can afford to build a road
	 *
	 * @return true if the player can afford to build a road
	 */
	public boolean canAffordToBuildRoad() {
		return (FREE_BUILD || roadIds.size() < MAX_ROADS
				&& getResources(Resource.ResourceType.LUMBER) >= 1
				&& getResources(Resource.ResourceType.BRICK) >= 1);
	}

	/**
	 * Determine if the player can afford to build a ship
	 *
	 * @return true if the player can afford to build a ship
	 */
	public boolean canAffordToBuildShip() {
		return (FREE_BUILD || shipIds.size() < MAX_SHIPS
				&& getResources(Resource.ResourceType.LUMBER) >= 1
				&& getResources(Resource.ResourceType.WOOL) >= 1);
	}

	/**
	 * Determine if a player can afford to build a settlement
	 *
	 * @return true if the player can afford to build a settlement
	 */
	public boolean canAffordToBuildSettlement() {
		return (FREE_BUILD || numOwnedSettlements < MAX_SETTLEMENTS
				&& getResources(Resource.ResourceType.BRICK) >= 1
				&& getResources(Resource.ResourceType.LUMBER) >= 1
				&& getResources(Resource.ResourceType.GRAIN) >= 1
				&& getResources(Resource.ResourceType.WOOL) >= 1);
	}

	/**
	 * Determine if the player can afford to build a city
	 *
	 * @return true if the player can afford to build a city
	 */
	public boolean canAffordToBuildCity() {
		return (FREE_BUILD || numOwnedCities < MAX_CITIES
				&& getResources(Resource.ResourceType.GRAIN) >= 2 && getResources(Resource.ResourceType.ORE) >= 3);
	}

	/**
	 * Determine if the player can afford to build a city wall
	 *
	 * @return true if the player can afford to build a city wall
	 */
	public boolean canAffordToBuildCityWall() {
		return (FREE_BUILD || numOwnedCityWalls < MAX_CITY_WALLS
				&& getResources(Resource.ResourceType.BRICK) >= 2);
	}

	/**
	 * Determine if the player can afford to hire a knight
	 *
	 * @return true if the player can afford to hire a knight
	 */
	public boolean canAffordToHireKnight() {
		return (FREE_BUILD || (numOwnedBasicKnights < MAX_BASIC_KNIGHTS
				&& numTotalOwnedKnights < MAX_TOTAL_KNIGHTS
				&& getResources(Resource.ResourceType.WOOL) >= 1
				&& getResources(Resource.ResourceType.ORE) >= 1));
	}

	/**
	 * Determine if the player can afford to activate a knight
	 *
	 * @return true if the player can afford to activate a knight
	 */
	public boolean canAffordToActivateKnight() {
		return (FREE_BUILD || getResources(ResourceType.GRAIN) >= 1);
	}

	/**
	 * Determine if the player can afford to promote a knight
	 *
	 * @return true if the player can afford to promote a knight
	 */
	public boolean canAffordToPromoteKnight() {
		return (FREE_BUILD || (numTotalOwnedKnights < MAX_TOTAL_KNIGHTS
				&& getResources(Resource.ResourceType.WOOL) >= 1
				&& getResources(Resource.ResourceType.ORE) >= 1));
	}


	/**
	 * Get the number of privateVictoryPointsCount points that are evident to other players
	 *
	 * @return the number of privateVictoryPointsCount points
	 */
	public int getPublicVictoryPoints() {
		int points = numOwnedSettlements + 2 * numOwnedCities;

		//TODO: add other public vps
		if (board.hasLongestTradeRoute(this))
		{
			points += 2;
		}
		if(board.getMetropolisOwners()[0] == getPlayerNumber()){
			points += 2;
		}
		if(board.getMetropolisOwners()[1] == getPlayerNumber()){
			points += 2;
		}
		if(board.getMetropolisOwners()[2] == getPlayerNumber()){
			points += 2;
		}
		points += defenderOfCatan;

		return points;
	}

	/**
	 * Return player's current total privateVictoryPointsCount points
	 *
	 * @return the number of privateVictoryPointsCount points
	 */
	public int getVictoryPoints() {
		return getPublicVictoryPoints() + privateVictoryPointsCount;
	}

	/**
	 * Return player's current trade discipline level
	 *
	 * @return the player's current trade discipline level
	 */
	public int getTradeLevel() {
		return cityImprovementLevels[CityImprovement.toCityImprovementIndex(
				CityImprovement.CityImprovementType.TRADE)];
	}

	/**
	 * Return player's current science discipline level
	 *
	 * @return the player's current science discipline level
	 */
	public int getScienceLevel() {
		return cityImprovementLevels[CityImprovement.toCityImprovementIndex(
				CityImprovement.CityImprovementType.SCIENCE)];
	}

	/**
	 * Return player's current politics discipline level
	 *
	 * @return the player's current politics discipline level
	 */
	public int getPoliticsLevel() {
		return cityImprovementLevels[CityImprovement.toCityImprovementIndex(
				CityImprovement.CityImprovementType.POLITICS)];
	}

	/**
	 * Return player's nth hired knight
	 * @param knightOrdinalIndex
	 * 			ordinal index of knight hired by the player
	 * @return the player's nth hired knight
	 */
	public Knight getKnightAddedAtIndex(int knightOrdinalIndex) {
		return board.getKnightById(myKnightIds.get(knightOrdinalIndex));
	}

	/**
	 * Does this knight belong to the player?
	 * @param toCheck
	 * 			knight for which to check ownership
	 * @return true iff the toCheck belongs to this player
	 */
	public boolean isMyKnight(Knight toCheck) {
		if (toCheck.getOwnerPlayer() != this) {
			return false;
		} else {
			Knight curKnight;
			for (int i = 0; i < myKnightIds.size(); i++) {
				curKnight = getKnightAddedAtIndex(i);
				if (curKnight == toCheck) {
					return true;
				}
			}
		}
		return false;
	}

	 /* Pillage one city to a settlement
	 * @return the player's nth hired knight
	 */
	public void pillageCity() {

		boolean pillagedWall = board.pillageCity(playerNumber);
		if(pillagedWall){
			numOwnedCities--;
			numOwnedCityWalls--;
		} else{
			numOwnedCities--;
		}
		numOwnedSettlements++;
	}

	/**
	 * Increment defender of catan
	 * @return void
	 */
	public void wonDefenderOfCatan(){
		defenderOfCatan++;
	}

//TODO: see how we can use this similar code for progress cards

//	/**
//	 * Get the player's progress cards
//	 *
//	 * @return an array with the number of each type of card
//	 */
//	public int[] getCards() {
//		return cards;
//	}
//
//	/**
//	 * Get the number of a given progress card type that a player has
//	 *
//	 * @param card
//	 *            the card type
//	 * @return the number of that card type including new cards
//	 */
//	public int getNumProgressCardType(ProgressCardType card) {
//		int count = 0;
//		for (int i = 0; i < newCards.size(); i++) {
//			if (newCards.get(i) == card)
//			{
//				count += 1;
//			}
//		}
//
//		return cards[card.ordinal()] + count;
//	}
//
//	/**
//	 * Get the number of privateVictoryPointsCount point cards
//	 *
//	 * @return the number of privateVictoryPointsCount point cards the player has
//	 */
//	public int getVictoryCards() {
//		return privateVictoryPointsCount;
//	}
//
//	/**
//	 * Determine if the player has a card to use
//	 *
//	 * @return true if the player is allowed to use a card
//	 */
//	public boolean canUseCard() {
//		if (usedCardThisTurn)
//			return false;
//
//		for (int i = 0; i < cards.length; i++) {
//			if (cards[i] > 0)
//			{
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	/**
//	 * Add a development card of the given type
//	 *
//	 * @param card
//	 *            the card type
//	 */
//	public void addCard(ProgressCardType card, boolean canUse) {
//		if (canUse) {
//			cards[card.ordinal()] += 1;
//			usedCardThisTurn = false;
//		} else {
//			newCards.add(card);
//		}
//	}
//
//	/**
//	 * Determine if the player has a particular card
//	 *
//	 * @param card
//	 *            the card type to check for
//	 * @return true if the player has this card type
//	 */
//	public boolean hasCard(ProgressCardType card) {
//		return (cards[card.ordinal()] > 0);
//	}
//
//	/**
//	 * Use a card
//	 *
//	 * @param card
//	 *            the card type to use
//	 * @return true if the card was used successfully
//	 */
//	public boolean useCard(ProgressCardType card) {
//		if (!hasCard(card) || usedCardThisTurn)
//			return false;
//
//		switch (card) {
//			case PROGRESS:
//				board.startProgressPhase1();
//				break;
//			case VICTORY:
//				return false;
//			default:
//				break;
//		}
//
//		cards[card.ordinal()] -= 1;
//		usedCardThisTurn = true;
//
//		appendAction(R.string.player_played_card, ProgressCard
//				.getCardStringResource(card));
//
//		return true;
//	}

//	/**
//	 * Steal all resources of a given resourceType from the other players
//	 *
//	 * @param resourceType
//	 */
//	public int monopoly(Resource.ResourceType resourceType) {
//		appendAction(R.string.to_remove_str, Resource
//				.toRString(resourceType));
//
//		int total = 0;
//
//		for (int i = 0; i < board.getNumPlayers(); i++) {
//			Player player = board.getPlayerById(i);
//			int count = player.getResources(resourceType);
//
//			if (player == this || count <= 0)
//				continue;
//
//			player.useResources(resourceType, count);
//			addResources(resourceType, count);
//			total += count;
//
//			appendAction(R.string.player_stole_from, player.getName());
//		}
//
//		return total;
//	}
//
//	/**
//	 * Get 2 free resources
//	 *
//	 * @param resourceType1
//	 *            first resource type
//	 * @param resourceType2
//	 *            second resource type
//	 */
//	public void harvest(Resource.ResourceType resourceType1, ResourceType resourceType2) {
//		addResources(resourceType1, 1);
//		addResources(resourceType2, 1);
//
//		appendAction(R.string.player_received_resource, Resource
//				.toRString(resourceType1));
//		appendAction(R.string.player_received_resource, Resource
//				.toRString(resourceType2));
//	}
//
// 	/**
//	 * Get the number of development cards the player has
//	 *
//	 * @return the number of development cards the player has
//	 */
//	public int getNumProgressCards() {
//		int count = 0;
//		for (int i = 0; i < cards.length; i++)
//		{
//			count += cards[i];
//		}
//
//		return count + newCards.size();
//	}

	/**
	 * Get the number of resources that are required to trade for 1 resource
	 *
	 * @return the number of resources needed
	 */
	public int getTradeValue() {
		return tradeValue;
	}

	/**
	 * Determine if the player has a particular harbor resourceType
	 *
	 * @param resourceType
	 *            the resource resourceType, or null for 3:1 harbor
	 * @return
	 */
	public boolean hasHarbor(Resource.ResourceType resourceType) {
		if (resourceType == null) {
			return (tradeValue == 3);
		}

		return harbors[resourceType.ordinal()];
	}

	/**
	 * Add a harbor
	 *
	 * @param resourceType
	 *            the harbor resourceType
	 */
	public void setTradeValue(Resource.ResourceType resourceType) {
		// 3:1 harbor
		if (resourceType == ResourceType.ANY) {
			tradeValue = 3;
			return;
		}

		// specific harbor
		if (resourceType != null)
		{
			harbors[resourceType.ordinal()] = true;
		}
	}

	/**
	 * Determine if a trade is valid with baml
	 *
	 * @param resourceType
	 *            the resourceType to trade for
	 * @param trade
	 *            an array of the number of each card resourceType offered
	 * @return true if the trade is valid
	 */
	public boolean canTrade(Resource.ResourceType resourceType, int[] trade) {
		int value = 0;
		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {

			// check for specific 2:1 harbor
			if (hasHarbor(Resource.RESOURCE_TYPES[i])
					&& getResources(Resource.RESOURCE_TYPES[i]) >= 2 && trade[i] >= 2)
			{
				return true;
			}

			// deduct from number of resource cards needed
			int number = getResources(Resource.RESOURCE_TYPES[i]);
			if (number >= trade[i])
			{
				value += trade[i];
			}
		}

		return (value >= tradeValue);
	}

	/**
	 * Trade for a resource
	 *
	 * @param resourceType
	 *            the resourceType to trade for
	 * @param trade
	 *            an array of the number of each card resourceType offered
	 * @return true if the trade was performed successfully
	 */
	public boolean trade(Resource.ResourceType resourceType, int[] trade) {
		// validate trade
		if (!canTrade(resourceType, trade))
		{
			return false;
		}

		// check for 2:1 harbor
		for (int i = 0; i < trade.length; i++) {

			// check for specific 2:1 harbor
			if (hasHarbor(Resource.RESOURCE_TYPES[i])
					&& getResources(Resource.RESOURCE_TYPES[i]) >= 2 && trade[i] >= 2) {
				addResources(resourceType, 1);
				useResources(Resource.RESOURCE_TYPES[i], 2);
				return true;
			}
		}

		// normal 4:1 or 3:1 trade
		int value = tradeValue;
		for (int i = 0; i < trade.length; i++) {

			int number = getResources(Resource.RESOURCE_TYPES[i]);

			// deduct from number of resource cards needed
			if (trade[i] >= value && number >= value) {
				useResources(Resource.RESOURCE_TYPES[i], value);
				addResources(resourceType, 1);

				appendAction(R.string.player_traded_for, Resource
						.toRString(resourceType));

				for (int j = 0; j < value; j++) {
					appendAction(R.string.player_traded_away, Resource
							.toRString(Resource.RESOURCE_TYPES[i]));
				}

				return true;
			} else if (trade[i] > 0 && number >= trade[i]) {
				useResources(Resource.RESOURCE_TYPES[i], trade[i]);
				value -= trade[i];
			}
		}

		// this shouldn't happen
		return false;
	}

	/**
	 * Determine if a trade is valid
	 *
	 *
	 * @param trade
	 *            an array of the number of each card resourceType offered
	 * @return true if the trade is valid
	 */
	public boolean canTradePlayer(int[] trade) {
		boolean canTrade = true;
		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {

			// deduct from number of resource cards needed
			int number = getResources(Resource.RESOURCE_TYPES[i]);
			if (number < trade[i])
			{
				canTrade = false;
			}
		}

		return canTrade;
	}

	/**
	 * Find all possible trade combinations
	 *
	 * @param want
	 *            the type of resource to trade for
	 * @return a Vector of arrays of the number of each card type to offer
	 */
	public Vector<int[]> findTrades(Resource.ResourceType want) {
		Vector<int[]> offers = new Vector<int[]>();

		// generate trades for 2:1 harbors
		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {
			if (Resource.RESOURCE_TYPES[i] == want
					|| !hasHarbor(Resource.RESOURCE_TYPES[i]))
			{
				continue;
			}

			int[] trade = new int[Resource.RESOURCE_TYPES.length];
			trade[i] = 2;

			if (canTrade(want, trade))
			{
				offers.add(trade);
			}
		}

		// generate 3:1 or 4:1 trades
		for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {
			if (Resource.RESOURCE_TYPES[i] == want
					|| hasHarbor(Resource.RESOURCE_TYPES[i]))
			{
				continue;
			}

			int[] trade = new int[Resource.RESOURCE_TYPES.length];
			trade[i] = tradeValue;

			if (canTrade(want, trade))
			{
				offers.add(trade);
			}
		}

		return offers;
	}

	/**
	 * Determine if the player is a human player on this device
	 *
	 * @return true if the player is human controlled on this device
	 */
	public boolean isHuman() {
		return (playerType == PLAYER_HUMAN);
	}

	/**
	 * Determine if the player is a bot
	 *
	 * @return true if the player is a bot
	 */
	public boolean isBot() {
		return (playerType == PLAYER_BOT);
	}

	/**
	 * Determine if the player is an online player
	 *
	 * @return
	 */
	public boolean isOnline() {
		return (playerType == PLAYER_ONLINE);
	}

	/**
	 * Set the player's name
	 *
	 * @param playerName
	 *            the player's new name
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * Get the player's name
	 *
	 * @return the player's name
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Notify the player that longest trade route lengths are being recalculated
	 */
	public void cancelMyLongestTradeRouteLength() {
		myLongestTradeRouteLength = 0;
	}

	/**
	 * Notify the player of one of their trade route lengths and update
	 * their longestTradeRoute length if needed
	 *
	 * @param tradeRouteLength
	 *            the length of a trade route
	 */
	public void notifyTradeRouteLength(int tradeRouteLength) {
		if (tradeRouteLength > this.myLongestTradeRouteLength)
		{
			this.myLongestTradeRouteLength = tradeRouteLength;
		}
	}

	/**
	 * Get the length of the player's longest trade route
	 *
	 * @return the player's longest trade route length
	 */
	public int getMyLongestTradeRouteLength() {
		return myLongestTradeRouteLength;
	}

	/**
	 * Get the player's settlements count
	 *
	 * @return the number of settlements owned by the player
	 */
	public int getNumOwnedSettlements() {
		return numOwnedSettlements;
	}

	/**
	 * Get the player's resources count
	 *
	 * @return the number of resources owned by the player
	 */
	public int getTotalNumOwnedResources() {
		int count = 0;
		for (int i = 0; i < countPerResource.length; i++)
		{
			count += countPerResource[i];
		}

		return count;
	}

	/**
	 * Get the number of cities owned by the player
	 *
	 * @return the number of city owned by the player
	 */
	public int getNumOwnedCities() {
		return numOwnedCities;
	}

	/**
	 * Get the number of city walls owned by the player
	 *
	 * @return the number of city walls owned by the player
	 */
	public int getNumOwnedCityWalls() {
		return numOwnedCityWalls;
	}

	/**
	 * Get the total number of knights owned by the player
	 *
	 * @return the total number of knights the player has
	 */
	public int getTotalNumOwnedKnights() {
		return numTotalOwnedKnights;
	}

	/**
	 * Get the number of basic knights owned by the player
	 *
	 * @return the number of basic knights the player has
	 */
	public int getNumOwnedBasicKnights() {
		return numOwnedBasicKnights;
	}

	/**
	 * Get the number of strong knights owned by the player
	 *
	 * @return the number of strong knights the player has
	 */
	public int getNumOwnedStrongKnights() {
		return numOwnedStrongKnights;
	}

	/**
	 * Get the number of mighty knights owned by the player
	 *
	 * @return the number of mighty knights the player has
	 */
	public int getNumOwnedMightyKnights() {
		return numOwnedMightyKnights;
	}

	/**
	 * Get the number of roads built
	 *
	 * @return the number of roads the player built
	 */
	public int getNumRoads() {
		return roadIds.size();
	}

	/**
	 * Get the number of ships built
	 *
	 * @return the number of ships the player built
	 */
	public int getNumShips() {
		return shipIds.size();
	}

	public void distributeProgressCard(int diceRollNumber2, CityImprovement.CityImprovementType type){
		boolean barbarianRolled = type == null;

		//if we rolled a cityimprovement, attempt to distribute progress
		if(!barbarianRolled){
			int playerDisciplineLevel = cityImprovementLevels[CityImprovement.toCityImprovementIndex(type)];
			boolean mustGiveProgresCard = playerDisciplineLevel != 0 && diceRollNumber2 <= playerDisciplineLevel+1;
			if(mustGiveProgresCard){
				switch(type){
					case TRADE:
						//distribute trade Progress Card
						hand.add(board.pickNewProgressCard(CityImprovement.CityImprovementType.TRADE));
						break;
					case SCIENCE:
						//distribute science Progress Card
						hand.add(board.pickNewProgressCard(CityImprovement.CityImprovementType.SCIENCE));
						break;
					case POLITICS:
						//distribute politics Progress Card
						hand.add(board.pickNewProgressCard(CityImprovement.CityImprovementType.POLITICS));
						break;
					default:
						//DO NOT GIVE THE PLAYER ANY PROGRESS CARDS
						break;
				}
			}
		}
	}

	/**
	 * Get the players hand
	 *
	 * @return the players hand
	 */
	public Vector<ProgressCardType> getHand(){
		return hand;
	}


	public int getDefenderOfCatan() {
		return defenderOfCatan;
	}

	/**
	 * Get the players cityImprovement levels
	 *
	 * @return the players city improvement levels
	 */
	public int[] getCityImprovementLevels() {
		return cityImprovementLevels;
	}

	/**
	 * Add an action to the turn log
	 *
	 * @param action
	 *            a string of the action
	 */
	private void appendAction(String action) {
		if (board.isSetupPhase())
		{
			return;
		}

		if (actionLog == "")
		{
			actionLog += "→ " + action;
		}
		else
		{
			actionLog += "\n" + "→ " + action;
		}

	}

	/**
	 * Add an action to the turn log using a resource string
	 *
	 * @param action
	 *            string resource playerNumber for action
	 */
	public void appendAction(int action) {
		Context context = CatAndroidApp.getInstance().getContext();
		appendAction(context.getString(action));
	}

	/**
	 * Add an action to the turn log using a resource string and supplementary
	 * string
	 *
	 * @param action
	 *            string resource playerNumber for action
	 * @param additional
	 *            string to substitute into %s in action
	 */
	public void appendAction(int action, String additional) {
		Context context = CatAndroidApp.getInstance().getContext();
		appendAction(String.format(context.getString(action), additional));
	}

	/**
	 * Add an action to the turn log using a resource string and supplementary
	 * string
	 *
	 * @param action
	 *            string resource playerNumber for action
	 * @param additional
	 *            string resource to substitute into %s in action
	 */
	public void appendAction(int action, int additional) {
		Context context = CatAndroidApp.getInstance().getContext();
		appendAction(String.format(context.getString(action), context
				.getString(additional)));
	}

	/**
	 * Get the action log
	 *
	 * @return a String containing the log
	 */
	public String getActionLog() {
		return actionLog;
	}

	/**
	 * Get the string resource for a color
	 *
	 * @param color
	 *            the color
	 * @return the string resource
	 */
	public static int getColorStringResource(Color color) {
		switch (color) {
			case RED:
				return R.string.red;
			case BLUE:
				return R.string.blue;
			case GREEN:
				return R.string.green;
			case YELLOW:
				return R.string.yellow;
			default:
				return R.string.empty_string;
		}
	}
}
