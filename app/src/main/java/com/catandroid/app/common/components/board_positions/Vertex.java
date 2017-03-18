package com.catandroid.app.common.components.board_positions;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.Knight;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;

public class Vertex {

	public static final int NONE = 0;

	//TODO: refactor these constants to more sensible enums/objects

	// BUILDABLE VERTEX UNIT CONSTANTS
	public static final int SETTLEMENT = 1;
	public static final int CITY = 2;
	public static final int CITY_WALL = 3;

	// KNIGHT UNIT CONSTANT
	public static final int KNIGHT = 10;

	private int id;
	private int curUnitType;
	private int placedKnightId = -1;

	// TODO: this should be okay since its an enum constant...
	private Knight.KnightRank placedKnightRank;

	private int ownerPlayerNumber;

	private int[] edgeIds = {-1, -1, -1};
	private int[] hexagonIds = {-1, -1, -1};
	private int[] harborIds = {-1, -1};

	private transient Board board;

	/**
	 * Initialize a vertex with edgeIds set to -1
	 * 
	 * @param id
	 *            the vertex id for drawing
	 */
	public Vertex(Board board, int id) {
		this.id = id;
		ownerPlayerNumber = -1;
		curUnitType = NONE;
		this.board = board;
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
	 * Associate an edge with vertex
	 * 
	 * @param e
	 *            the edge to add (ignored if already associated)
	 */
	public void addEdge(Edge e) {
		for (int i = 0; i < 3; i++) {
			if (edgeIds[i] == -1) {
				edgeIds[i] = e.getId();
				return;
			} else if (edgeIds[i] == e.getId()) {
				return;
			}
		}
	}

	/**
	 * Associate a hexagon with vertex
	 * 
	 * @param hex
	 *            the hexagon to add (ignored if already associated)
	 */
	public void addHexagon(Hexagon hex) {
		for (int i = 0; i < 3; i++) {
			if (hexagonIds[i] == -1) {
				hexagonIds[i] = hex.getId();
				return;
			} else if (hexagonIds[i] == hex.getId()) {
				return;
			}
		}
	}

	/**
	 * Associate a harbor with the vertex
	 *
	 * @param harbor
	 *            the harbor to add (ignored if already associated)
	 */
	public void addHarbor(Harbor harbor) {
		for (int i = 0; i < 3; i++) {
			if (harborIds[i] == -1) {
				harborIds[i] =harbor.getId();
				return;
			} else if (harborIds[i] == harbor.getId()) {
				return;
			}
		}
	}

	/**
	 * Get the hexagon at the given index of hexagonIds
	 * 
	 * @param index
	 *            the hexagonIds index (0, 1, or 2)
	 * @return the hexagon (or null)
	 * */
	public Hexagon getHexagon(int index) {
		return board.getHexagonById(hexagonIds[index]);
	}

	/**
	 * Get the hexagonIds's id for drawing
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the edge at the given index of edgeIds
	 *
	 * @param index
	 *            the edgeIds index (0, 1, or 2)
	 * @return the edge (or null)
	 * */
	public Edge getEdge(int index) {
		return board.getEdgeById(edgeIds[index]);
	}

	/**
	 * Check if vertex has a current unit from any player
	 * 
	 * @return true if there is any vertex unit placed here
	 */
	public boolean hasVertexUnitPlacedHere() {
		return (curUnitType != NONE);
	}

	/**
	 * Check if vertex has a unit placed here by the given player
	 * 
	 * @param player
	 *            the player to check for
	 * @return true iff there is a vertex unit placed here by the player
	 */
	public boolean hasVertexUnitPlacedBy(Player player) {
		return (board.getPlayerById(ownerPlayerNumber) == player);
	}

	/**
	 * Get the type of building at vertex
	 * 
	 * @return the type of building at the vertex (equal to the number of
	 *         points)
	 */
	public int getCurUnitType() {
		return curUnitType;
	}

	/**
	 * Get the owner of any unit at this vertex
	 * 
	 * @return the Player that owns it, or null
	 */
	public Player getOwnerPlayer() {
		return board.getPlayerById(ownerPlayerNumber);
	}

	/**
	 * Get the knight placed at this vertex (if it exists)
	 *
	 * @return the knight currently placed at this vertex
	 */
	public Knight getPlacedKnight() {
		if(curUnitType != KNIGHT || placedKnightId == -1) {
			return null;
		}

		return board.getKnightById(placedKnightId);
	}

	/**
	 * Check for adjacent edgeUnit
	 *
	 * @param player
	 *            the player to check for
	 * @return true if one of the adjacent edges has an edgeUnit of player
	 */
	public boolean connectedToEdgeUnitOwnedBy(Player player) {
		Edge curEdge = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == player)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Check for adjacent road
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if one of the adjacent edges has a road of player
	 */
	public boolean connectedToRoadOwnedBy(Player player) {
		Edge curEdge = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == player
					&& curEdge.getCurUnitType() == Edge.ROAD)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Check for adjacent ship
	 *
	 * @param player
	 *            the player to check for
	 * @return true if one of the adjacent edges has a ship of player
	 */
	public boolean connectedToShipOwnedBy(Player player) {
		Edge curEdge = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == player
					&& curEdge.getCurUnitType() == Edge.SHIP)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Check for adjacent ship
	 *
	 * @param player
	 *            the player to check for
	 * @param edgeToIgnore
	 *            do not count this edge in the check
	 * @return true if one of the adjacent edges has a ship of player
	 */
	public boolean connectedToShipOwnedBy(Player player, Edge edgeToIgnore) {
		Edge curEdge = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge != edgeToIgnore && curEdge.getOwnerPlayer() == player
					&& curEdge.getCurUnitType() == Edge.SHIP)
			{
				return true;
			}
		}

		return false;
	}

	public void distributeResources(Resource.ResourceType resourceType) {

		int numToGive = 0;

		// determine how many resources to distribute
		if(curUnitType == Vertex.SETTLEMENT){
			numToGive = 1;
		} else if(curUnitType == Vertex.CITY || curUnitType == Vertex.CITY_WALL){
			numToGive = 2;
		}

		if (ownerPlayerNumber == -1)
		{
			return;
		}

		if (resourceType != null) {
			// Gold gets two times more on distribution (2 for settlement, 4 for city)
			if(resourceType == Resource.ResourceType.GOLD){
				board.getPlayerById(ownerPlayerNumber).addResources(resourceType, numToGive*2);
			} else {
				board.getPlayerById(ownerPlayerNumber).addResources(resourceType, numToGive);
			}
		}
	}

	/**
	 * Ensure that this vertex is available for a vertexUnit buildable
	 * 
	 * @return true iff the vertex is connected to at least one land hex
	 * and there are no adjacent cities/settlements
	 */
	public boolean canPlaceBuildableVertexUnitHere() {
		int curEdgeId, curHexId, adjVertexId;
		boolean noAdjacentCommunity = true,
				isOnLand = false;
		Edge intersectingEdge;
		Hexagon adjacentHex;
		// for each connected edge and/or hexagon
		for (int i = 0; i < 3; i++) {
			curEdgeId = edgeIds[i];
			curHexId = hexagonIds[i];
			intersectingEdge = curEdgeId == -1 ? null : board.getEdgeById(curEdgeId);
			adjacentHex = curHexId == -1 ? null : board.getHexagonById(curHexId);
			if (intersectingEdge != null)
			{
				adjVertexId = intersectingEdge.getAdjacent(this).getId();
				if(board.getVertexById(adjVertexId).hasVertexUnitPlacedHere()) {
					// there is a nearby community and we cannot build here
					noAdjacentCommunity = false;
					break;
				}
			}
			if (adjacentHex != null) {
				if (adjacentHex.getTerrainType() != Hexagon.TerrainType.SEA) {
					// the vertex is indeed connected to land
					isOnLand = true;
				}
			}
		}

		return noAdjacentCommunity && isOnLand;
	}

	/**
	 * Check if player can build at vertex
	 * 
	 * @param player
	 *            player to check for
	 * @param setup
	 *            setup condition allows player to build without a connected edge unit
	 * @return true if player can build at vertex
	 */
	public boolean canBuild(Player player, int vertexUnitType, boolean setup) {
		if (!canPlaceBuildableVertexUnitHere()) {
			return false;
		}

		// only allow direct settlement/city placement during setup phase
		if (setup) {
			return (board.getPlayerById(ownerPlayerNumber) == null);
		}

		// ensure that the owner has an edge connected to this vertex
		if (!this.connectedToEdgeUnitOwnedBy(player)) {
			return false;
		}

		if (board.getPlayerById(ownerPlayerNumber) == null && vertexUnitType == SETTLEMENT) {
			// player can build a settlement here
			return true;
		}
		else if(board.getPlayerById(ownerPlayerNumber) != null && vertexUnitType == CITY){
			// player can build a city here
			return board.getPlayerById(ownerPlayerNumber) == player
					&& vertexUnitType == CITY && curUnitType == SETTLEMENT;
		}
		else if(board.getPlayerById(ownerPlayerNumber) != null && vertexUnitType == CITY_WALL){
			// player can build a city wall here
			return board.getPlayerById(ownerPlayerNumber) == player
					&& vertexUnitType == CITY_WALL && curUnitType == CITY;
		}
		else{
			return false;
		}
	}

	/**
	 * Check if player can place a knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can place a knight at this vertex
	 */
	public boolean canPlaceKnightHere(Player player) {

		/* NOTE: unlike a buildable vertex unit, we can place a knight on any vertex
		*  connected to one of the player's edge units (even if the vertex is adjacent
		*  to another player's community or at sea but reachable by ship. So, there is
		*  no need to call canPlaceBuildableVertexUnitHere() when placing a knight.
		 */

		// ensure that the owner has an edge connected to this vertex
		if (!this.connectedToEdgeUnitOwnedBy(player)) {
			return false;
		}

		if (board.getPlayerById(ownerPlayerNumber) == null && curUnitType == NONE) {
			// player can place a knight at this vertex
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Check if player can activate a knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can activate a knight at this vertex
	 */
	public boolean canActivateKnightHere(Player player) {

		if (curUnitType == KNIGHT && board.getPlayerById(ownerPlayerNumber) == player) {
			Knight toActivate = getPlacedKnight();
			return !toActivate.isActive();
		}
		else{
			return false;
		}
	}

	/**
	 * Check if player can promote a knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can promote a knight at this vertex
	 */
	public boolean canPromoteKnightHere(Player player) {

		if (curUnitType == KNIGHT && board.getPlayerById(ownerPlayerNumber) == player) {
			Knight toPromote = getPlacedKnight();
			Knight.KnightRank currentRank = toPromote.getKnightRank();
			switch (currentRank) {
				case BASIC_KNIGHT:
				case STRONG_KNIGHT:
					return toPromote.canPromote();
				default:
					return false;
			}
		}
		else{
			return false;
		}
	}

	/**
	 * Wrapper of canBuild(player, setup) where setup is false by default
	 * 
	 * @param player
	 *            player to check for
	 * @return true if player can build at vertex
	 */
	public boolean canBuild(Player player, int type) {
		return this.canBuild(player, type, false);
	}

	/**
	 * Build at vertex for player
	 * 
	 * @param player
	 *            which player intends to build
	 * @param setup
	 *            setup condition allows player to build without a road
	 */
	public boolean build(Player player, int vertexUnitType, boolean setup) {
		if (!this.canBuild(player, vertexUnitType, setup))
		{
			return false;
		}

		switch (curUnitType) {
			case NONE:
				ownerPlayerNumber = player.getPlayerNumber();
				curUnitType = board.isSetupPhase2() ? CITY : SETTLEMENT;
				break;
			case SETTLEMENT:
				curUnitType = CITY;
				break;
			case CITY:
				curUnitType = CITY_WALL;
				break;
			case CITY_WALL:
				return false;
		}

		int i, curId;
		for (i = 0; i < harborIds.length; i++) {
			curId = harborIds[i];
			if(curId != -1) {
				player.setTradeValue(board.getHarborById(curId).getResourceType());
			}
		}

		return true;
	}

	/**
	 * Place a knight at this vertex for player
	 *
	 * @param player
	 *            which player intends to place a knight here
	 * @param toPlace
	 *            the knight to place
	 */
	public boolean placeKnight(Player player, Knight toPlace) {
		if (!this.canPlaceKnightHere(player))
		{
			return false;
		}

		placedKnightId = toPlace.getId();
		placedKnightRank = toPlace.getKnightRank();
		ownerPlayerNumber = player.getPlayerNumber();
		curUnitType = KNIGHT;

		return true;
	}

	/**
	 * Activate a knight at this vertex for player
	 *
	 * @param player
	 *            which player intends to activate a knight here
	 */
	public boolean activateKnight(Player player) {
		if (!this.canActivateKnightHere(player))
		{
			return false;
		}

		return getPlacedKnight().activate();
	}

	/**
	 * Promote a knight at this vertex for player
	 *
	 * @param player
	 *            which player intends to promote a knight here
	 */
	public boolean promoteKnight(Player player) {
		if (!this.canPromoteKnightHere(player))
		{
			return false;
		}

		return getPlacedKnight().promote();
	}

	public Harbor[] getHarbors() {
		Harbor[] myHarbors = new Harbor[2];
		myHarbors[0] = board.getHarborById(harborIds[0]);
		myHarbors[0] = board.getHarborById(harborIds[1]);
		return myHarbors;
	}

	/**
	 * Find the longest trade route passing through this vertex for the given player
	 * 
	 * @param player
	 *            the player of interest
	 * @param toIgnore
	 *            ignore previously considered edge
	 * @param numberOfLongestTradeRouteUpdatesSoFar
	 *            number of times the longest trade route was recalculated through here
	 * @return the longest trade route length passing through this vertex for the given player
	 */
	public int getLongestTradeRouteLengthFromHere(Player player, Edge toIgnore,
												  int numberOfLongestTradeRouteUpdatesSoFar) {
		int longestTradeRouteLengthSoFar = 0;

		// FIXME: if two road paths diverge and re-converge, the result would be calculated with whichever happens to be picked first

		// another player's road breaks the road chain
		Player ourOwner = board.getPlayerById(ownerPlayerNumber);
		if (ourOwner != null && ourOwner != player)
		{
			return 0;
		}

		// find the longest road (ignoring previously included edge)
		Edge curEdge = null;
		int i= 0, longestRoadLengthFromCurAdjacentEdge = 0;
		for (i = 0; i < 3; i++) {
			curEdge = board.getEdgeById(edgeIds[i]);
			if (curEdge == null || curEdge == toIgnore)
			{
				continue;
			}

			longestRoadLengthFromCurAdjacentEdge = curEdge.getLongestTradeRouteLengthFromHere(player,
					this, numberOfLongestTradeRouteUpdatesSoFar);

			if (longestRoadLengthFromCurAdjacentEdge > longestTradeRouteLengthSoFar)
			{
				longestTradeRouteLengthSoFar = longestRoadLengthFromCurAdjacentEdge;
			}
		}

		return longestTradeRouteLengthSoFar;
	}
}
