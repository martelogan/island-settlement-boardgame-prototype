package com.catandroid.app.common.components.board_positions;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;

public class Vertex {

	public static final int NONE = 0;
	public static final int SETTLEMENT = 1;
	public static final int CITY = 2;
	public static final int CITY_WALL = 3;
	public static final int TRADE_METROPOLIS = 4;
	public static final int SCIENCE_METROPOLIS = 5;
	public static final int POLITICS_METROPOLIS = 6;
	public static final int WALLED_TRADE_METROPOLIS = 7;
	public static final int WALLED_SCIENCE_METROPOLIS = 8;
	public static final int WALLED_POLITICS_METROPOLIS = 9;

	private int id;
	private int curUnitType;

	private int ownerPlayerNumber;

	private int[] edgeIds;
	private int[] hexagonIds;
	private Harbor harbors;

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

		edgeIds = new int[3];
		edgeIds[0] = edgeIds[1] = edgeIds[2] = -1;

		hexagonIds = new int[3];
		hexagonIds[0] = hexagonIds[1] = hexagonIds[2] = -1;
		setHarbor(null);
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
	 * Associate an hexagonIds with vertex
	 *
	 * @param hexId
	 *            id of the hexagon to add (ignored if already associated)
	 */
	public void addHexagonById(int hexId) {
		for (int i = 0; i < 3; i++) {
			if (hexagonIds[i] == -1) {
				hexagonIds[i] = hexId;
				return;
			} else if (hexagonIds[i] == hexId) {
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
	 * Check if vertex has a building for any player
	 * 
	 * @return true if there is a settlement or city for any player
	 */
	public boolean hasBuilding() {
		return (curUnitType != NONE);
	}

	/**
	 * Check if vertex has a building for a player
	 * 
	 * @param player
	 *            the player to check for
	 * @return true if player has a building on the vertexS
	 */
	public boolean hasBuilding(int player) {
		return (board.getPlayerById(ownerPlayerNumber) == board.getPlayerById(player));
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
	 * Get the type of building at vertex
	 *
	 * @return the type of building at the vertex (equal to the number of
	 *         points)
	 */
	public void setCurUnitType(int unitType) {
		curUnitType = unitType;
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

		//determine how many resources to distribute
		if(curUnitType == 1){
			numToGive = 1;
		} else if(curUnitType == 2 || curUnitType == 3){
			numToGive = 2;
		}

		if (ownerPlayerNumber == -1)
		{
			return;
		}

		if (resourceType != null) {
			//Gold gets two times more on distribution (2 for settlement, 4 for city)
			if(resourceType == Resource.ResourceType.GOLD){
				board.getPlayerById(ownerPlayerNumber).addResources(resourceType, numToGive*2);
				board.getPlayerById(ownerPlayerNumber).gotResourcesSinceLastTurn = true;
			} else {
				board.getPlayerById(ownerPlayerNumber).addResources(resourceType, numToGive);
				board.getPlayerById(ownerPlayerNumber).gotResourcesSinceLastTurn = true;
			}
		}
	}

	/**
	 * Ensure that this vertex is available for vertexUnits
	 * 
	 * @return true iff the vertex is connected to at least one land hex
	 * and there are no adjacent cities/settlements
	 */
	public boolean couldBuild() {
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
				if(board.getVertexById(adjVertexId).hasBuilding()) {
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
	 *            setup condition allows player to build without a road
	 * @return true if player can build at vertex
	 */
	public boolean canBuild(Player player, int type, boolean setup) {

		if (!couldBuild()) {
			return false;
		}

		//TODO: change this to cities & knights version
		// only allow building settlements
		if (setup) {
			return (board.getPlayerById(ownerPlayerNumber) == null);
		}

		// check if owner has road to vertex
		if (!this.connectedToEdgeUnitOwnedBy(player)) {
			return false;
		}

		// can build settlement
		if (board.getPlayerById(ownerPlayerNumber) == null && type == SETTLEMENT) {
			return true;
		}
		// can build city
		else if(board.getPlayerById(ownerPlayerNumber) != null && type == CITY){
			return board.getPlayerById(ownerPlayerNumber) == player && curUnitType == SETTLEMENT;
		}
		//can build a wall
		else if(board.getPlayerById(ownerPlayerNumber) != null && type == CITY_WALL){
			boolean isCityType = curUnitType == CITY || curUnitType == TRADE_METROPOLIS
					|| curUnitType == SCIENCE_METROPOLIS || curUnitType == POLITICS_METROPOLIS;

			return board.getPlayerById(ownerPlayerNumber) == player && isCityType;
		}
		//can build a metropolis
		else if(board.getPlayerById(ownerPlayerNumber) != null && (type == TRADE_METROPOLIS
				|| type == SCIENCE_METROPOLIS || type == POLITICS_METROPOLIS)){

			boolean isCityType = curUnitType == CITY_WALL || curUnitType == CITY;
			return board.getPlayerById(ownerPlayerNumber) == player && isCityType;
		}
		else{
			return false;
		}
	}

	/**
	 * Simple version of canBuild(player, setup) where setup is false
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
	public boolean build(Player player, int type, boolean setup) {
		if (!this.canBuild(player, type, setup))
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
				curUnitType = board.isBuildMetropolisPhase() ? player.metropolisTypeToBuild : CITY_WALL;
				break;
			case CITY_WALL:
				//determine which type of metropolis we build
				switch(type){
					case TRADE_METROPOLIS:
						curUnitType = WALLED_TRADE_METROPOLIS;
						break;
					case SCIENCE_METROPOLIS:
						curUnitType = WALLED_SCIENCE_METROPOLIS;
						break;
					case POLITICS_METROPOLIS:
						curUnitType = WALLED_POLITICS_METROPOLIS;
						break;
				}
				break;
			case SCIENCE_METROPOLIS:
				curUnitType = WALLED_SCIENCE_METROPOLIS;
				break;
			case TRADE_METROPOLIS:
				curUnitType = WALLED_TRADE_METROPOLIS;
				break;
			case POLITICS_METROPOLIS:
				curUnitType = WALLED_TRADE_METROPOLIS;
				break;
		}

		if (harbors != null)
		{
			player.setTradeValue(harbors.getResourceType());
		}

		return true;
	}

	public void setHarbor(Harbor harbor) {
		this.harbors = harbor;
	}

	public Harbor getHarbor() {
		return harbors;
	}

	/**
	 * Find the longest road passing through this vertex for the given player
	 * 
	 * @param player
	 *            the player
	 * @param omit
	 *            omit an edgeIds already considered
	 * @return the road length
	 */
	public int getRoadLength(Player player, Edge omit, int countId) {
		int longest = 0;

		// FIXME: if two road paths diverge and re-converge, the result may be
		// calculated with whichever happens to be picked first

		// another player's road breaks the road chain
		if (board.getPlayerById(ownerPlayerNumber) != null && board.getPlayerById(ownerPlayerNumber) != player)
		{
			return 0;
		}

		// find the longest road aside from one passing through the given edgeIds
		for (int i = 0; i < 3; i++) {
			if (edgeIds[i] == -1 || edgeIds[i] == omit.getId())
			{
				continue;
			}

			int length = board.getEdgeById(edgeIds[i]).getRoadLength(player, this, countId);
			if (length > longest)
			{
				longest = length;
			}
		}

		return longest;
	}
}
