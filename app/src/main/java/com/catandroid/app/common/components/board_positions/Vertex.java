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
	public static final int WALLED_CITY = 3;
	public static final int TRADE_METROPOLIS = 4;
	public static final int SCIENCE_METROPOLIS = 5;
	public static final int POLITICS_METROPOLIS = 6;
	public static final int WALLED_TRADE_METROPOLIS = 7;
	public static final int WALLED_SCIENCE_METROPOLIS = 8;
	public static final int WALLED_POLITICS_METROPOLIS = 9;

	// KNIGHT UNIT CONSTANT
	public static final int KNIGHT = 10;

	private int id;
	private int curUnitType;
	private int placedKnightId = -1;
	private boolean isAdjacentToRobber = false, isAdjacentToPirate = false;

	// TODO: this should be okay since its an enum constant...
	private Knight.KnightRank placedKnightRank;

	private int ownerPlayerNumber;

	private int[] edgeIds = {-1, -1, -1};
	private int[] hexagonIds = {-1, -1, -1};
	private int[] harborIds = {-1, -1};
    private int[] fishingGroundIds = {-1, -1};
    private boolean hasFishingGround = false;

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
	 * Associate a fishing ground with the vertex
	 *
	 * @param fishingGround
	 *            the fishing ground to add (ignored if already associated)
	 */
	public void addFishingGround(FishingGround fishingGround) {
		for (int i = 0; i < 3; i++) {
			if (fishingGroundIds[i] == -1) {
                hasFishingGround = true;
				fishingGroundIds[i] = fishingGround.getId();
				return;
			} else if (fishingGroundIds[i] == fishingGround.getId()) {
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
	 * Get the hexagon at the given index of fishingGroundIds
	 *
	 * @param index
	 *            the fishingGroundIds index (0, 1, or 2)
	 * @return the fishingGround (or null)
	 * */
	public FishingGround getFishingGround(int index) {
		return board.getFishingGroundById(fishingGroundIds[index]);
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
	 * Check if vertex is currently owned by another player
	 *
	 * @param me
	 *            the player to check for
	 * @return true iff the vertex is either owned another player
	 */
	public boolean isOwnedByAnotherPlayer(Player me) {
		return this.hasVertexUnitPlacedHere() && this.getOwnerPlayer() != me;
	}

	/**
	 * Check if vertex has a community placed here
	 *
	 * @return true iff there is a community built here by the player here
	 * */
	public boolean hasCommunity() {
		switch(curUnitType) {
			case SETTLEMENT:
			case CITY:
			case WALLED_CITY:
			case TRADE_METROPOLIS:
			case SCIENCE_METROPOLIS:
			case POLITICS_METROPOLIS:
			case WALLED_TRADE_METROPOLIS:
			case WALLED_SCIENCE_METROPOLIS:
			case WALLED_POLITICS_METROPOLIS:
				return true;
			default: // e.g. a knight of player
				return false;
		}
	}

	/**
	 * Check if vertex has a community placed here by the given player
	 *
	 * @param player
	 *            the player to check for
	 * @return true iff there is a community built here by the player
	 */
	public boolean hasCommunityOf(Player player) {
		return hasCommunity() && (board.getPlayerById(ownerPlayerNumber) == player);
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
	 * Check if the vertex is currently adjacent to the robber
	 *
	 * @return true iff the vertex is currently adjacent to the robber
	 */
	public boolean isAdjacentToRobber() {
		return (this.isAdjacentToRobber);
	}

    /**
     * Check if the vertex is connected to a fishing ground
     *
     * @return true iff the vertex is currently connected to a fishing ground
     */
    public boolean hasFishingGround() {
        return (this.hasFishingGround);
    }

	/**
	 * Robber is adjacent to this vertex
	 *
	 * @return true if the vertex is now adjacent to the robber
	 */
	public boolean setRobber() {
		this.isAdjacentToRobber = true;
		return true;
	}

	/**
	 * Remove the robber from adjacency to this vertex
	 *
	 * @return true iff robber was indeed removed
	 */
	public boolean removeRobber() {
		if (this.isAdjacentToRobber) {
			this.isAdjacentToRobber = false;
			return true;
		}
		// robber is no longer adjacent to vertex
		return false;
	}

	/**
	 * Check if the vertex is currently adjacent to the pirate
	 *
	 * @return true iff the vertex is currently adjacent to the pirate
	 */
	public boolean isAdjacentToPirate() {
		return (this.isAdjacentToPirate);
	}

	/**
	 * Pirate is adjacent to this vertex
	 *
	 * @return true if the vertex is now adjacent to the pirate
	 */
	public boolean setPirate() {
		this.isAdjacentToPirate = true;
		return true;
	}

	/**
	 * Remove the pirate from adjacency to this vertex
	 *
	 * @return true iff pirate was indeed removed
	 */
	public boolean removePirate() {
		if (this.isAdjacentToPirate) {
			this.isAdjacentToPirate = false;
			return true;
		}
		// pirate is no longer adjacent to vertex
		return false;
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
	public boolean isConnectedToEdgeUnitOwnedBy(Player player) {
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
	public boolean isConnectedToRoadOwnedBy(Player player) {
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
	public boolean isConnectedToShipOwnedBy(Player player) {
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
	public boolean isConnectedToShipOwnedBy(Player player, Edge edgeToIgnore) {
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

	/**
	 * Check that vertex is connected directly to an unclaimed vertex by one of player's edge units
	 *
	 * @param player
	 *            the player to check for
	 * @return true iff one of the adjacent vertices is unclaimed and connected to here via
	 * 		   an edge unit owned by the player
	 */
	public boolean isConnectedToUnclaimedVertexByEdgeUnitOf(Player player) {
		Edge curEdge = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == player
					&& (curEdge.getV0Clockwise().getCurUnitType() == Vertex.NONE
						|| curEdge.getV1Clockwise().getCurUnitType() == Vertex.NONE))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Check that this vertex can connect to an unclaimed vertex via some trade route of the given player
	 *
	 * @param tradeRouteOwner
	 *            player that seeks to connect to unclaimed vertex
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean isConnectedToUnclaimedVertexByTradeRouteOf(Player tradeRouteOwner) {
		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == tradeRouteOwner &&
					curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.getCurUnitType() == Vertex.NONE) { // base case
					return true;
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute =
							neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
									tradeRouteOwner, curEdge);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
										tradeRouteOwner, curEdge);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
										tradeRouteOwner, curEdge);
					}
				}
				if(foundValidTradeRoute) {
					break;
				}
			}
		}

		return foundValidTradeRoute;
	}

	/**
	 * Check that this vertex can connect to an unclaimed vertex via some trade route of the given player
	 *
	 * @param tradeRouteOwner
	 *            player that seeks to connect to unclaimed vertex
	 * @param toIgnore
	 *            ignore previously considered edge
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean isConnectedToUnclaimedVertexByTradeRouteOf(Player tradeRouteOwner,
															  Edge toIgnore) {
		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge != toIgnore
					&& curEdge.getOwnerPlayer() == tradeRouteOwner
					&& curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.getCurUnitType() == Vertex.NONE) { // base case
					return true;
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute =
							neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
									tradeRouteOwner, curEdge);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
										tradeRouteOwner, curEdge);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToUnclaimedVertexByTradeRouteOf(
										tradeRouteOwner, curEdge);
					}
				}
				if(foundValidTradeRoute) {
					break;
				}
			}
		}

		return foundValidTradeRoute;
	}

	/**
	 * Check that this vertex can connect to a displaceable knight via some trade route of the given player
	 *
	 * @param tradeRouteOwner
	 *            player that seeks to connect to unclaimed vertex
	 * @param attackerKnight
	 *			 the knight intending to displace opponent knight
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean isConnectedToDisplaceableKnightByTradeRouteOf(Player tradeRouteOwner,
															  Knight attackerKnight) {
		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == tradeRouteOwner &&
					curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.getCurUnitType() == Vertex.KNIGHT) { // base case
					return attackerKnight.canDisplace(neighborVertex.getPlacedKnight());
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute =
							neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
									tradeRouteOwner, attackerKnight, curEdge);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
										tradeRouteOwner, attackerKnight, curEdge);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
										tradeRouteOwner, attackerKnight, curEdge);
					}
				}
				if(foundValidTradeRoute) {
					break;
				}
			}
		}

		return foundValidTradeRoute;
	}

	/**
	 * Check that this vertex can connect to a displaceable knight via some trade route of the given player
	 *
	 * @param tradeRouteOwner
	 *            player that seeks to connect to unclaimed vertex
	 * @param attackerKnight
	 *			 the knight intending to displace opponent knight
	 * @param toIgnore
	 *            ignore previously considered edge
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean isConnectedToDisplaceableKnightByTradeRouteOf(Player tradeRouteOwner,
																 Knight attackerKnight,
																 Edge toIgnore) {

		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge != toIgnore
					&& curEdge.getOwnerPlayer() == tradeRouteOwner
					&& curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.getCurUnitType() == Vertex.KNIGHT) { // base case
					return attackerKnight.canDisplace(neighborVertex.getPlacedKnight());
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute =
							neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
									tradeRouteOwner, attackerKnight, curEdge);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
										tradeRouteOwner, attackerKnight, curEdge);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.isConnectedToDisplaceableKnightByTradeRouteOf(
										tradeRouteOwner, attackerKnight, curEdge);
					}
				}
				if(foundValidTradeRoute) {
					break;
				}
			}
		}

		return foundValidTradeRoute;
	}

	/**
	 * Check that this vertex can connect to target via a trade route of the given player
	 *
	 * @param target
	 * 			  destination vertex of desired trade route
	 * @param tradeRouteOwner
	 *            player that seeks to connect to target
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean hasTradeRouteTo(Vertex target, Player tradeRouteOwner, boolean isPeaceful) {
		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge.getOwnerPlayer() == tradeRouteOwner &&
					curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.isOwnedByAnotherPlayer(tradeRouteOwner)) { // negative base case
					// are we an aggressive knight attempting displacement
					if(!isPeaceful && neighborVertex == target
							&& neighborVertex.getCurUnitType() == Vertex.KNIGHT) {
						// WARNING: need to check elsewhere that we can actually displace opponent
						return true;
					}
					// can't pass through vertex owned by another player
					continue;
				}
				if (neighborVertex == target) { // positive base case
					return true;
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute = neighborVertex.hasTradeRouteTo(target,
							tradeRouteOwner, curEdge, isPeaceful);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.hasTradeRouteTo(target,
										tradeRouteOwner, curEdge, isPeaceful);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.hasTradeRouteTo(target,
										tradeRouteOwner, curEdge, isPeaceful);
					}
				}
				if(foundValidTradeRoute) {
					return true;
				}
			}
		}

		return foundValidTradeRoute;
	}

	/**
	 * Check that this vertex can connect to target via a trade route of the given player
	 *
	 * @param target
	 * 			  destination vertex of desired trade route
	 * @param tradeRouteOwner
	 *            player that seeks to connect to target
	 * @param toIgnore
	 *            ignore previously considered edge
	 * @return true iff there is a path to the target vertex via edge units owned by the given player
	 */
	public boolean hasTradeRouteTo(Vertex target, Player tradeRouteOwner,
								   Edge toIgnore, boolean isPeaceful) {
		boolean foundValidTradeRoute = false;
		Edge curEdge = null;
		Vertex neighborVertex = null;
		for (int i = 0; i < 3; i++) {
			curEdge = edgeIds[i] != -1 ? board.getEdgeById(edgeIds[i]) : null;
			if (curEdge != null && curEdge!= toIgnore && curEdge.getOwnerPlayer() == tradeRouteOwner
					&& curEdge.getCurUnitType() != Edge.NONE)
			{
				neighborVertex = curEdge.getAdjacent(this);
				if (neighborVertex.isOwnedByAnotherPlayer(tradeRouteOwner)) { // negative base case
					// are we an aggressive knight attempting displacement
					if(!isPeaceful && neighborVertex == target
							&& neighborVertex.getCurUnitType() == Vertex.KNIGHT) {
						// WARNING: need to check elsewhere that
						// we can actually displace opponent
						return true;
					}
					// can't pass through vertex owned by another player
					continue;
				}
				if (neighborVertex == target) { // base case
					return true;
				}
				else if(neighborVertex.hasCommunityOf(tradeRouteOwner)) {
					foundValidTradeRoute = neighborVertex.hasTradeRouteTo(target,
							tradeRouteOwner, curEdge, isPeaceful);
				}
				else if(curEdge.getCurUnitType() == Edge.ROAD) {
					if(isConnectedToRoadOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.hasTradeRouteTo(target,
										tradeRouteOwner, curEdge, isPeaceful);
					}
				}
				else if(curEdge.getCurUnitType() == Edge.SHIP) {
					if(isConnectedToShipOwnedBy(tradeRouteOwner)) {
						foundValidTradeRoute =
								neighborVertex.hasTradeRouteTo(target,
										tradeRouteOwner, curEdge, isPeaceful);
					}
				}
				if(foundValidTradeRoute) {
					return true;
				}
			}
		}

		return false;
	}

	public void distributeResources(Resource.ResourceType resourceType) {

		if (ownerPlayerNumber == -1)
		{
			return;
		}

		if (resourceType != null) {
			board.getPlayerById(ownerPlayerNumber).addResources(resourceType, curUnitType, true);
			board.getPlayerById(ownerPlayerNumber).gotResourcesSinceLastTurn = true;
		}
	}

	public void distributeFish(int diceRoll) {
		if (!(Hexagon.getFishingLakeNumbersSet().contains(diceRoll)
				|| FishingGround.getFishingGroundNumbersSet().contains(diceRoll))) {
			return;
		}

		if (ownerPlayerNumber == -1)
		{
			return;
		}

		if (Hexagon.getFishingLakeNumbersSet().contains(diceRoll)) {
			Hexagon curHex = null;
			for(int i = 0; i < hexagonIds.length; i++) {
				curHex = getHexagon(i);
				if(curHex != null && curHex.getTerrainType() == Hexagon.TerrainType.FISH_LAKE) {
					board.getPlayerById(ownerPlayerNumber).addFish(curUnitType);
				}
			}
		}
		else if (FishingGround.getFishingGroundNumbersSet().contains(diceRoll)) {
			FishingGround curFishingGround = null;
			for(int i = 0; i < fishingGroundIds.length; i++) {
				curFishingGround = getFishingGround(i);
				if(curFishingGround != null && curFishingGround.getNumberToken().getTokenNum() == diceRoll) {
					board.getPlayerById(ownerPlayerNumber).addFish(curUnitType);
				}
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
				if(board.getVertexById(adjVertexId).hasCommunity()) {
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
		if (!this.isConnectedToEdgeUnitOwnedBy(player)) {
			return false;
		}

		if (board.getPlayerById(ownerPlayerNumber) == null && vertexUnitType == SETTLEMENT) {
			// player can build a settlement here
			return true;
		}
		else if(board.getPlayerById(ownerPlayerNumber) != null && vertexUnitType == CITY) {
			// player can build a city here
			return board.getPlayerById(ownerPlayerNumber) == player
					&& vertexUnitType == CITY && curUnitType == SETTLEMENT;
		}
		else if(board.getPlayerById(ownerPlayerNumber) != null && vertexUnitType == WALLED_CITY) {
			boolean isCityType = curUnitType == CITY || curUnitType == TRADE_METROPOLIS
				|| curUnitType == SCIENCE_METROPOLIS || curUnitType == POLITICS_METROPOLIS;
			// player can build a city wall here
			return board.getPlayerById(ownerPlayerNumber) == player && isCityType;
		}
		else if(board.getPlayerById(ownerPlayerNumber) != null && (vertexUnitType == TRADE_METROPOLIS
				|| vertexUnitType == SCIENCE_METROPOLIS || vertexUnitType == POLITICS_METROPOLIS)) {
			// player can build a metropolis here
			boolean isCityType = curUnitType == WALLED_CITY || curUnitType == CITY;
			return board.getPlayerById(ownerPlayerNumber) == player && isCityType;
		}
		else{
			return false;
		}
	}

	/**
	 * Check if player can place a new knight at an unclaimed vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can introduce a knight at this vertex
	 */
	public boolean canPlaceNewKnightHere(Player player) {

		/* NOTE: unlike a buildable vertex unit, we can place a knight on any vertex
		*  connected to one of the player's edge units (even if the vertex is adjacent
		*  to another player's community or at sea but reachable by ship. So, there is
		*  no need to call canPlaceBuildableVertexUnitHere() when placing a knight.
		 */

		// ensure that the owner has an edge connected to this vertex
		if (!this.isConnectedToEdgeUnitOwnedBy(player)) {
			return false;
		}
		if (board.getPlayerById(ownerPlayerNumber) == null && curUnitType == NONE) {
			// player can place a new knight at this vertex
			return true;
		}
		else {
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
	 * Check if the player can chase the robber with a knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can chase the robber with a knight from this vertex
	 */
	public boolean canChaseRobberFromHere(Player player) {

		if (curUnitType == KNIGHT && board.getPlayerById(ownerPlayerNumber) == player) {
			return isAdjacentToRobber && getPlacedKnight().canMakeMove();
		}
		else{
			return false;
		}
	}

	/**
	 * Check if the player can chase the pirate with a knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can chase the pirate with a knight from this vertex
	 */
	public boolean canChasePirateFromHere(Player player) {

		if (curUnitType == KNIGHT && board.getPlayerById(ownerPlayerNumber) == player) {
			return isAdjacentToPirate && getPlacedKnight().canMakeMove();
		}
		else{
			return false;
		}
	}

	/**
	 * Check if the player can move the knight away from this vertex
	 *
	 * @param player
	 *            player to check for
	 * @return true iff player can move his/her knight away from this vertex
	 */
	public boolean canRemoveKnightFromHere(Player player) {

		if (curUnitType == KNIGHT && board.getPlayerById(ownerPlayerNumber) == player) {
			return getPlacedKnight().canStartMoving()
					&& (this.isConnectedToUnclaimedVertexByTradeRouteOf(player) ||
					this.isConnectedToDisplaceableKnightByTradeRouteOf(player, getPlacedKnight()));
		}
		else {
			return false;
		}
	}

	/**
	 * Check if the player can place the currently moving knight at this vertex
	 *
	 * @param player
	 *            player to check for
	 * @param toPlace
	 * 			  the knight to check for
	 * @return true iff player can move his/her knight to this vertex
	 */
	public boolean canMoveKnightToHere(Player player, Knight toPlace, boolean isPeaceful) {
		if (player == null || toPlace == null) {
			return false;
		}

		if (player != board.getPlayerOfCurrentGameTurn() || toPlace != board.getCurrentlyMovingKnight()) {
			return false;
		}

		if(!(curUnitType == NONE) || (ownerPlayerNumber != -1)) {
			if(!isPeaceful && canDisplaceKnightFromHere(player, toPlace)) {
				return true;
			}
			else {
				return false;
			}
		}

		return toPlace.canMoveTo(this, isPeaceful);
	}

	/**
	 * Check if the player can place the currently moving knight peacefully at this vertex
	 * (following displacement)
	 *
	 * @param player
	 *            player to check for
	 * @param toPlace
	 * 			  the knight to check for
	 * @return true iff player can move his/her knight to this vertex (following displacement)
	 */
	public boolean canDisplaceKnightToHere(Player player, Knight toPlace) {
		if (player == null || toPlace == null) {
			return false;
		}

		if ((player != board.getPlayerOfCurrentGameTurn() && !board.isMyPseudoTurn())
				|| toPlace != board.getCurrentlyMovingKnight()) {
			return false;
		}

		if(!(curUnitType == NONE) || (ownerPlayerNumber != -1)) {
			return false;
		}

		return toPlace.canDisplaceKnightTo(this);
	}

	/**
	 * Check if the attacking player can displace a knight at this vertex
	 *
	 * @param attackerPlayer
	 *            which player intends to displace an opponent knight
	 * @param attackerKnight
	 *            the knight intending to displace opponent knight
	 */
	public boolean canDisplaceKnightFromHere(Player attackerPlayer, Knight attackerKnight) {
		if (attackerPlayer == null || attackerKnight == null) {
			return false;
		}

		if (attackerPlayer != board.getPlayerOfCurrentGameTurn()
				|| attackerKnight != board.getCurrentlyMovingKnight()) {
			return false;
		}

		if(!(curUnitType == KNIGHT) || (ownerPlayerNumber == -1)) {
			return false;
		}

		return attackerKnight.canDisplace(getPlacedKnight());
	}

	public boolean canDisplaceKnightFromHere(Player attackerPlayer) {
		if (attackerPlayer == null) {
			return false;
		}

		if (attackerPlayer != board.getPlayerOfCurrentGameTurn()) {
			return false;
		}

		if(!(curUnitType == KNIGHT) || (ownerPlayerNumber == -1)) {
			return false;
		}

		return true;
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
				curUnitType = board.isBuildMetropolisPhase() ? player.metropolisTypeToBuild : WALLED_CITY;
				break;
			case WALLED_CITY:
				//determine which type of metropolis we build
				switch(vertexUnitType) {
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
	 * Attempt to place a new knight at this vertex for player
	 *
	 * @param player
	 *            which player intends to place a knight here
	 * @param toPlace
	 *            the knight to place
	 */
	public boolean placeNewKnight(Player player, Knight toPlace) {
		if (!this.canPlaceNewKnightHere(player))
		{
			return false;
		}

		placedKnightId = toPlace.getId();
		placedKnightRank = toPlace.getKnightRank();
		ownerPlayerNumber = player.getPlayerNumber();
		curUnitType = KNIGHT;
		toPlace.setCurrentVertexLocation(this);

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

	/**
	 * Remove a knight at this vertex for player
	 *
	 * @param player
	 *            which player intends to remove a knight from here
	 */
	public boolean removeKnightFromHere(Player player) {
		if (!this.canRemoveKnightFromHere(player))
		{
			return false;
		}

		if(!getPlacedKnight().startMoving()) {
			return false;
		}

		// remove knight from this vertex
		placedKnightId = -1;
		placedKnightRank = null;
		ownerPlayerNumber = -1;
		curUnitType = NONE;
		return true;
	}


	/**
	 * Return a knight back to stationed vertex
	 *
	 */
	public boolean moveKnightBackHere() {

		return placeNewKnight(board.getPlayerOfCurrentGameTurn(), board.getCurrentlyMovingKnight());
	}

	/**
	 * Move a knight peacefully to this vertex
	 *
	 * @param player
	 *            which player intends to place a knight here
	 * @param toPlace
	 *            the knight to place
	 */
	public boolean moveKnightPeacefullyToHere(Player player, Knight toPlace) {
		if (player == null || toPlace == null) {
			return false;
		}

		if (player != board.getPlayerOfCurrentGameTurn()
				|| toPlace != board.getCurrentlyMovingKnight()) {
			return false;
		}

		if(!canMoveKnightToHere(player, toPlace, true)) {
			return false;
		}

		if(placeNewKnight(player, toPlace)) {
			toPlace.deactivate();
			return true;
		}

		return false;
	}


	/**
	 * Move a knight peacefully to this vertex FOLLOWING DISPLACEMENT
	 *
	 * @param player
	 *            which player intends to place a knight here
	 * @param toPlace
	 *            the knight to place
	 */
	public boolean displaceKnightToHere(Player player, Knight toPlace) {
		if (player == null || toPlace == null) {
			return false;
		}

		if ((player != board.getPlayerOfCurrentGameTurn() && !board.isMyPseudoTurn())
				|| toPlace != board.getCurrentlyMovingKnight()) {
			return false;
		}

		// WARNING: returns false if you are trying to displace a knight
		if(!canDisplaceKnightToHere(player, toPlace)) {
			return false;
		}

		if(placeNewKnight(player, toPlace)) {
			return true;
		}

		return false;
	}


	/**
	 * Displace the knight at this vertex by the attacking knight
	 *
	 * @param attackerPlayer
	 *            which player intends to displace an opponent knight
	 * @param attackerKnight
	 *            the knight intending to displace opponent knight
	 */
	public boolean displaceKnightFromHere(Player attackerPlayer, Knight attackerKnight) {
		if (attackerPlayer == null || attackerKnight == null) {
			return false;
		}

		if (attackerPlayer != board.getPlayerOfCurrentGameTurn()
				|| attackerKnight != board.getCurrentlyMovingKnight()) {
			return false;
		}

		if(!canDisplaceKnightFromHere(attackerPlayer, attackerKnight)) {
			return false;
		}

		// DISPLACE THE KNIGHT STATIONED AT THIS VERTEX
		placedKnightId = -1;
		placedKnightRank = null;
		ownerPlayerNumber = -1;
		curUnitType = NONE;

		// PLACE THE ATTACKING KNIGHT AT THIS VERTEX
		if(placeNewKnight(attackerPlayer, attackerKnight)) {
			attackerKnight.deactivate();
			return true;
		}

		return false;
	}


	public Harbor[] getHarbors() {
		Harbor[] myHarbors = new Harbor[2];
		myHarbors[0] = board.getHarborById(harborIds[0]);
		myHarbors[0] = board.getHarborById(harborIds[1]);
		return myHarbors;
	}

	public FishingGround[] getFishingGrounds() {
		FishingGround[] myFishingGrounds = new FishingGround[2];
		myFishingGrounds[0] = board.getFishingGroundById(fishingGroundIds[0]);
		myFishingGrounds[0] = board.getFishingGroundById(fishingGroundIds[1]);
		return myFishingGrounds;
	}

	//FIXME: WARNING...trade route logic is only half-working (considers roads only + minor bug)

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
