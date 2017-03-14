package com.catandroid.app.common.components.board_positions;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.players.Player;

public class Edge {

	public static final int NONE = 0;
	public static final int ROAD = 1;
	public static final int SHIP = 2;

	private int id;
	private int curUnitType;
	private int[] vertexIds;
	private int ownerPlayerNumber = -1;
	private int lastRoadCountId;
	private int originHexId;
    private int originHexDirect;
	private int neighborHexId = -1;
    private int portHexId = -1;
	private int portHexDirect = -1;
    private int myHarborId = -1;
	private boolean isBorderingSea = false;
	private transient Board board;

	/**
	 * Initialize edge with vertexIds set to null
	 */
	public Edge(Board board, int id) {
		this.id = id;
		curUnitType = NONE;
		vertexIds = new int[2];
		vertexIds[0] = vertexIds[1] = -1;
		ownerPlayerNumber = -1;
		lastRoadCountId = 0;
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
	 * Set vertices for the edge
	 *
	 * @param v0
	 *            the first vertex
	 * @param v1
	 *            the second vertex
	 */
	public void setVertices(Vertex v0, Vertex v1) {
		vertexIds[0] = v0.getId();
		vertexIds[1] = v1.getId();
		v0.addEdge(this);
		v1.addEdge(this);
	}

	/**
	 * Check if the edge has a given vertexI
	 * 
	 * @param v
	 *            the vertex to check for
	 * @return true if v is associated with the edge
	 */
	public boolean hasVertex(int v) {
		return (vertexIds[0] == v || vertexIds[1] == v);
	}

	/**
	 * Get the other vertex associated with edge
	 * 
	 * @param v
	 *            one vertex
	 * @return the other associated vertex or null if not completed
	 */
	public Vertex getAdjacent(Vertex v) {
		if (board.getVertexById(vertexIds[0]) == v) {
			return board.getVertexById(vertexIds[1]);
		}
		else if (board.getVertexById(vertexIds[1]) == v) {
			return board.getVertexById(vertexIds[0]);
		}

		return null;
	}

	/**
	 * Check if an edgeUnit has been build at the edge
	 * 
	 * @return true if an edgeUnit was built
	 */
	public boolean hasEdgeUnit() {
		return (ownerPlayerNumber != -1);
	}

	/**
	 * Get current unit type on this ede
	 *
	 * @return int rep. of current unit type on this edge
	 */
	public int getCurUnitType() {return  curUnitType;}

	/**
	 * Get the owner of this edge
	 * 
	 * @return null or the owner of this edge
	 */
	public Player getOwnerPlayer() {
		return board.getPlayerById(ownerPlayerNumber);
	}

	/**
	 * Determine if the player can build an edgeUnit on this edge
	 * 
	 * @param player
	 *            the player to check for
	 * @return 	true iff the player has an edgeUnit to an unoccupied vertex
	 *			or the player has an adjacent vertexUnit
	 */
	public boolean canBuildEdgeUnit(Player player) {
		if (ownerPlayerNumber != -1) {
			return false;
		}

		// check for edgeUnits between each vertex
		for (int i = 0; i < 2; i++) {
			// the player has an edgeUnit to an unoccupied vertex
			// or the player has an adjacent building
			if (board.getVertexById(vertexIds[i]).connectedToEdgeUnitOwnedBy(player) &&
					!board.getVertexById(vertexIds[i]).hasBuilding()
					|| board.getVertexById(vertexIds[i]).hasBuilding(player.getPlayerNumber())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine if the player can build a road on this edge
	 *
	 * @param player
	 *            the player to check for
	 * @return true if the player can build a road on this edge
	 */
	public boolean canBuildRoad(Player player) {
		if (ownerPlayerNumber != -1) {
			return false;
		}

		// check for edgeUnits between each vertex
		for (int i = 0; i < 2; i++) {
			// the player has a road to an unoccupied vertex
			// or the player has an adjacent building
			if (board.getVertexById(vertexIds[i]).connectedToRoadOwnedBy(player) &&
					!board.getVertexById(vertexIds[i]).hasBuilding()
					|| board.getVertexById(vertexIds[i]).hasBuilding(player.getPlayerNumber())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine if the player can build a ship on this edge
	 *
	 * @param player
	 *            the player to check for
	 * @return true if the player can build a ship on this edge
	 */
	public boolean canBuildShip(Player player) {
		if (ownerPlayerNumber != -1 || !isAvailableForShip()) {
			return false;
		}

		// check for ships between each vertex
		for (int i = 0; i < 2; i++) {
			// the player has a ship to an unoccupied vertex
			// or the player has an adjacent building
			if (board.getVertexById(vertexIds[i]).connectedToShipOwnedBy(player) &&
					!board.getVertexById(vertexIds[i]).hasBuilding()
					|| board.getVertexById(vertexIds[i]).hasBuilding(player.getPlayerNumber())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Set the port hexagon
	 * @param h
	 *            the hex to set
	 * @return
	 */
	public void setPortHex(Hexagon h) {

		this.portHexId = h.getId();
		this.portHexDirect = h.findEdgeDirect(this);
	}

	/**
	 * Remove port hexagon (candidacy lost)
	 * @return
	 */
	public void removePortHex() {

		this.portHexId = -1;
		this.portHexDirect = -1;
		this.isBorderingSea = false;
	}

	public void setBorderingSea(boolean borderingSea) {
		isBorderingSea = borderingSea;
	}

	public boolean isBorderingSea() {
		return isBorderingSea;
	}

	public boolean isAvailableForShip() {
		if (ownerPlayerNumber != -1) {
			return false;
		}
		Hexagon.TerrainType originTerrain = originHexId != -1 ?
                board.getHexagonById(originHexId).getTerrainType() : null;
        Hexagon.TerrainType neighborTerrain = neighborHexId != -1 ?
                board.getHexagonById(neighborHexId).getTerrainType() : null;
		return originTerrain == Hexagon.TerrainType.SEA
				|| neighborTerrain	== Hexagon.TerrainType.SEA;
	}

	/**
	 * Get the port hexagon
	 *
	 * @return the port hexagon
	 */
	public Hexagon getPortHex() {
		return board.getHexagonById(portHexId);
	}

	/**
	 * Get the port hexagon direction
	 *
	 * @return the port hexagon direction
	 */
	public int getPortHexDirect() {
		return this.portHexDirect;
	}

	/**
	 * Get the port hexagon id
	 *
	 * @return the port hexagon id
	 */
	public int getPortHexId() {
		return portHexId;
	}

    public int getDirectTowardsSea_X() {
		return Hexagon.getVDirectXsign(this.portHexDirect);
    }

    public int getDirectTowardsSea_Y() {
		return Hexagon.getVDirectYsign(this.portHexDirect);
    }

    /**
     * Set the origin hexagon
     * @param h
     *            the hex to set
     * @return
     */
    public void setOriginHex(Hexagon h) {
        this.originHexId = h.getId();
    }

    /**
     * Get the origin hexagon
     *
     * @return the origin hexagon
     */
    public Hexagon getOriginHex() {
        return board.getHexagonById(originHexId);
    }

    /**
     * Get the origin hexagon id
     *
     * @return the origin hexagon id
     */
    public int getOriginHexId() {
        return originHexId;
    }

    /**
     * Set the origin hexagon direction
     * @param direct
     *            the edge direction on origin hexagon
     * @return
     */
    public void setOriginHexDirect(int direct) {
        this.originHexDirect = direct;
    }

    /**
     * Get the origin hexagon direction
     * @return the origin hexagon direction
     */
    public int getOriginHexDirect() {
        return this.originHexDirect;
    }

    /**
     * Get the marginal X sign of the origin hexagon direction
     * @return the marginal X sign of the origin hexagon direction
     */
    public int getOriginHexDirectXsign() {
        return Hexagon.getVDirectXsign(this.originHexDirect);
    }

	/**
	 * Get the marginal Y sign of the origin hexagon direction
	 * @return the marginal Y sign of the origin hexagon direction
	 */
	public int getOriginHexDirectYsign() {
		return Hexagon.getVDirectYsign(this.originHexDirect);
	}

	/**
	 * Set the neighbor hexagon
	 * @param h
	 *            the hex to set
	 * @return
	 */
	public void setNeighborHex(Hexagon h) {
		this.neighborHexId = h.getId();
	}

	/**
	 * Get the neighbor hexagon
	 *
	 * @return the neighbor hexagon
	 */
	public Hexagon getNeighborHex() {
		return board.getHexagonById(neighborHexId);
	}

	/**
	 * Get the neighbor hexagon direction of this edge
	 * @return the neighbor hexagon direction of this edge
	 */
	public int getNeighborHexDirect() {
		return board.getHexagonById(neighborHexId).findEdgeDirect(this);
	}

	/**
	 * Set a harbor on this edge
	 * @param harbor
	 *            the harbor to set
	 * @return
	 */
	public void setMyHarbor(Harbor harbor) {
		harbor.setEdgeById(this.getId());
		this.myHarborId = harbor.getId();
	}

    /**
     * Get the harbor on this edge
     * @return the harbor
     */
    public Harbor getMyHarbor() {
        return board.getHarborById(this.myHarborId);
    }

	/**
	 * Get the first vertex
	 * 
	 * @return the first vertex
	 */
	public Vertex getV0Clockwise() {

		return board.getVertexById(vertexIds[0]);
	}

	/**
	 * Get the second vertex
	 * 
	 * @return the second vertex
	 */
	public Vertex getV1Clockwise() {
		return board.getVertexById(vertexIds[1]);
	}

	/**
	 * Get the id of this edge
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Build a road on edge
	 * 
	 * @param player
	 *            the edgeUnit ownerPlayerNumber
	 * @return true if player can build an edgeUnit on edge
	 */
	public boolean buildRoad(Player player) {
		if (!canBuildRoad(player)) {
			return false;
		}

		ownerPlayerNumber = player.getPlayerNumber();
		curUnitType = ROAD;
		return true;
	}

	/**
	 * Build an edgeUnit on edge
	 *
	 * @param player
	 *            the edgeUnit ownerPlayerNumber
	 * @return true if player can build an edgeUnit on edge
	 */
	public boolean buildShip(Player player) {
		if (!canBuildShip(player)) {
			return false;
		}

		ownerPlayerNumber = player.getPlayerNumber();
		curUnitType = SHIP;
		return true;
	}

	/**
	 * Get the road length through this edge
	 * 
	 * @param player
	 *            player to measure for
	 * @param from
	 *            where we are measuring from
	 * @param countId
	 *            unique id for this count iteration
	 * @return the road length
	 */
	public int getRoadLength(Player player, Vertex from, int countId) {
		if (ownerPlayerNumber != player.getPlayerNumber() || lastRoadCountId == countId) {
			return 0;
		}

		// this ensures that that road isn't counted multiple times (cycles)
		lastRoadCountId = countId;

		// find other vertexIds
		int to = (from.getId() == vertexIds[0] ? vertexIds[1] : vertexIds[0]);

		// return road length
		return board.getVertexById(to).getRoadLength(player, this, countId) + 1;
	}

	/**
	 * Get the longest road length through this edge
	 * 
	 * @param countId
	 *            unique id for this count iteration
	 * @return the road length
	 */
	public int getRoadLength(int countId) {
		Player ownerPlayer = board.getPlayerById(ownerPlayerNumber);
		if (ownerPlayer == null) {
			return 0;
		}

		// this ensures that that road isn't counted multiple times (cycles)
		lastRoadCountId = countId;

		int length1 = board.getVertexById(vertexIds[0]).getRoadLength(ownerPlayer, this, countId);
		int length2 = board.getVertexById(vertexIds[1]).getRoadLength(ownerPlayer, this, countId);
		return length1 + length2 + 1;
	}
}
