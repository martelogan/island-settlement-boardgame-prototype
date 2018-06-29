/*
 * island-settlement-boardgame-prototype
 * Copyright (C) 2017, Logan Martel, Frederick Parsons
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.catandroid.app.common.components.board_positions;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.NumberToken;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.utilities.hex_grid_utils.AxialHexLocation;
import com.catandroid.app.common.players.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Hexagon {

	private NumberToken numberToken;
	private Resource resourceProduced;
	private TerrainType terrainType;
	private int[] vertexIds = {-1, -1, -1, -1, -1, -1};
	private int[] edgeIds = {-1, -1, -1, -1, -1, -1};
	private AxialHexLocation coord;
	private boolean hasRobber = false, hasPirate = false, hasMerchant = false;
	private int id;
	private transient Board board;

	public enum TerrainType {
		FOREST, PASTURE, FIELDS, HILLS, MOUNTAINS, DESERT, FISH_LAKE, SEA, GOLD_FIELD
	}
	private static final Integer[] FISHING_LAKE_NUMBERS = new Integer[] {2,3,11,12};
	private static final Set<Integer> FISHING_LAKE_NUMBERS_SET =
			new HashSet<Integer>(Arrays.asList(FISHING_LAKE_NUMBERS));

	public static Set<Integer> getFishingLakeNumbersSet() {
		return FISHING_LAKE_NUMBERS_SET;
	}
	/**
	 * Initialize the hexagon with a resource resourceType and numberToken number
	 *
	 * @param terrainType
	 *            terrainType of hexagon
	 * @param id
	 *            id number for the hexagon
	 */
	public Hexagon(Board board, TerrainType terrainType, int id) {
		this.terrainType = terrainType;
		this.resourceProduced = getResource(terrainType);
		this.numberToken = new NumberToken(0);
		this.id = id;
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
	 * Set a vertex of the hexagon
	 *
	 * @param vDirect
	 *            the vertex direction on hexagon
	 * @param v
	 * 			  the vertex to set
	 * @return
	 */
	public void setVertex(Vertex v, int vDirect) {
		vertexIds[vDirect] = v.getId();
		v.addHexagon(this);
	}

	/**
	 * Set a vertex of the hexagon
	 *
	 * @param vDirect
	 *            the vertex direction on hexagon
	 * @param vertexId
	 * 			  id of the vertex to set
	 * @return
	 */
	public void setVertexId(int vertexId, int vDirect) {
		vertexIds[vDirect] = vertexId;
		board.getVertexById(vertexId).addHexagon(this);
	}

	/**
	 * Get a vertex at vDirect on the hexagon
	 *
	 * @param vDirect
	 *            the vertex direction on hexagon
	 * @return the vertex at vDirect on hexagon
	 */
	public Vertex getVertex(int vDirect) {
		return board.getVertexById(vertexIds[vDirect]);
	}

	/**
	 * Get the vertex direction in vertexIds
	 *
	 * @param v
	 *            the vertex to find
	 * @return vDirect of vertex on hexagon
	 */
	public int findVdirect(Vertex v) {
		for (int i = 0; i < 6; i++) {
			if (board.getVertexById(vertexIds[i]) == v) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the vertex direction in vertexIds
	 *
	 * @param vertexId
	 *            the id of vertex to find
	 * @return vDirect of vertex on hexagon
	 */
	public int findVdirectById(int vertexId) {
		for (int i = 0; i < 6; i++) {
			if (vertexIds[i] == vertexId) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the edge direction in edgeIds
	 *
	 * @param e
	 *            the edge to find
	 * @return vDirect of edge on hexagon
	 */
	public int findEdgeDirect(Edge e) {
		for (int i = 0; i < 6; i++) {
			if (board.getEdgeById(edgeIds[i]) == e) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the edge direction in edgeIds
	 *
	 * @param edgeId
	 *            the id of the edge to find
	 * @return vDirect of edge on hexagon
	 */
	public int findEdgeDirectById(int edgeId) {
		for (int i = 0; i < 6; i++) {
			if (edgeIds[i] == edgeId) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the hexagon's produced resource
	 *
	 * @return the hexagon's produced resource
	 */
	public Resource getResource() {
		return resourceProduced;
	}

	/**
	 * Get the hexagon's produced resourceType
	 *
	 * @return the hexagon's produced resourceType
	 */
	public Resource.ResourceType getResourceType()
	{
		if (resourceProduced == null) {
			return null;
		}
		return resourceProduced.getResourceType();
	}

	/**
	 * Get the hexagon's terrainType
	 *
	 * @return the hexagon's terrainType
	 */
	public TerrainType getTerrainType() { return terrainType; }

	/**
	 * Get an edge of the hexagon
	 *
	 * @param direction
	 *            the direction of the edge on the hexagon
	 * @return the edge
	 */
	public Edge getEdge(int direction) {
		return board.getEdgeById(edgeIds[direction]);
	}

	/**
	 * Set an edge of the hexagon
	 *
	 * @param direction
	 *            the direction of the edge on the hexagon
	 * @param edge
	 * 			  the edge to set
	 * @return
	 */
	public void setEdge(Edge edge, int direction) {
		edge.setOriginHexDirect(direction);
		edgeIds[direction] = edge.getId();
	}

	/**
	 * Get integer representation of the number token
	 * currently placed on this hexagon
	 *
	 * @return integer representation of number token
	 */
	public int getNumberTokenAsInt() {
		return this.numberToken.getTokenNum();
	}

	/**
	 * Get the number token object placed on this hexagon
	 *
	 * @return number token currently placed on this hexagon
	 */
	public NumberToken getNumberTokenAsObject() {
		return this.numberToken;
	}

	/**
	 * Place number token on this hexagon (pass by int)
	 *
	 * @param tokenNum
	 *            integer rep of number token to place
	 */
	public void placeNumberToken(int tokenNum) {
		this.numberToken = new NumberToken(tokenNum);
	}

	/**
	 * Place number token on this hexagon (pass by obect)
	 *
	 * @param token
	 *            object rep of number token to place
	 */
	public void placeNumberToken(NumberToken token) {
		this.numberToken = token;
	}

	/**
	 * Set hexagon's axial coordinate
	 *
	 * @param coord
	 *            axial coordinate to set
	 * @return
	 */
	public void setCoord(AxialHexLocation coord) {
		this.coord = coord;
	}

	/**
	 * Get a hexagon's axial coordinate
	 *
	 * @param
	 * @return the axial coordinate of the hexagon
	 */
	public AxialHexLocation getCoord() {
		return coord;
	}

	/**
	 * Distribute resources from this hexagon
	 *
	 * @param diceRoll
	 *            the current dice sum
	 */
	public void distributeResources(int diceRoll) {
		if (diceRoll != this.numberToken.getTokenNum() || (hasRobber() && !board.isRobberDisabled())) {
			return;
		}

		for (int i = 0; i < 6; i++)
		{
			board.getVertexById(vertexIds[i]).distributeResources(resourceProduced.getResourceType());
		}
	}

	/**
	 * Distribute fish from this hexagon
	 *
	 * @param diceRoll
	 *            the current dice sum
	 */
	public void distributeFish(int diceRoll) {
		if (!(FISHING_LAKE_NUMBERS_SET.contains(diceRoll)
				|| FishingGround.getFishingGroundNumbersSet().contains(diceRoll))) {
			return;
		}

		for (int i = 0; i < 6; i++)
		{
			board.getVertexById(vertexIds[i]).distributeFish(diceRoll);
		}
	}

	/**
	 * Check if a given player owns a vertex unit adjacent to the hexagon
	 *
	 * @param player
	 *            the player to check
	 * @return true iff player has a vertex unit adjacent to the hexagon
	 */
	public boolean adjacentToVertexUnitOwnedBy(Player player) {
		for (int i = 0; i < 6; i++) {
			if (board.getVertexById(vertexIds[i]).getOwnerPlayer() == player) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a given player owns a ship adjacent to the hexagon
	 *
	 * @param player
	 *            the player to check
	 * @return true iff player has a ship adjacent to the hexagon
	 */
	public boolean adjacentToShipOwnedBy(Player player) {
		Edge curEdge = null;
		for (int i = 0; i < 6; i++) {
			curEdge = board.getEdgeById(edgeIds[i]);
			if (curEdge.getCurUnitType() == Edge.SHIP
					&& curEdge.getOwnerPlayer() == player) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all players owning a settlement adjacent to the hexagon
	 *
	 * @return a vector of players
	 */
	public Vector<Player> getPlayers() {
		Vector<Player> players = new Vector<Player>();
		for (int i = 0; i < 6; i++) {
			Player owner = board.getVertexById(vertexIds[i]).getOwnerPlayer();
			if (owner != null && !players.contains(owner)) {
				players.add(owner);
			}
		}

		return players;
	}

	/**
	 * Check if this hexagon is adjacent to a given hexagon
	 *
	 * @param hexagon
	 *            the hexagon to check for
	 * @return true if hexagon is adjacent to this hexagon
	 */
	public boolean isAdjacent(Hexagon hexagon) {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				Hexagon adjacent = board.getVertexById(vertexIds[i]).getHexagon(j);
				if (adjacent == null || adjacent.getId() == this.id) {
					continue;
				}
				else if (hexagon == adjacent) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Set the robber on this hexagon
	 *
	 * @return true if the hexagon now has the robber
	 */
	public boolean setRobber() {
		if (this.terrainType != TerrainType.SEA) {
			Vertex curVertex;
			for (int i = 0; i < 6; i++) {
				curVertex = board.getVertexById(vertexIds[i]);
				// TODO: technically a dirty write
				if(curVertex != null && !curVertex.setRobber()) {
					// something went wrong
					return false;
				}
			}
			this.hasRobber = true;
			return true;
		}
		return false;
	}

	/**
	 * Set the robber on this hexagon
	 *
	 * @return true if the hexagon now has the robber
	 */
	public boolean setMerchant() {
		if (this.terrainType != TerrainType.SEA) {
			this.hasMerchant = true;
			return true;
		}
		return false;
	}

	/**
	 * Set the pirate on this hexagon
	 *
	 * @return true if the hexagon now has the pirate
	 */
	public boolean setPirate() {
		if (this.terrainType == TerrainType.SEA) {
			Edge curEdge;
			Vertex curVertex;
			for (int i = 0; i < 6; i++) {
				curEdge = board.getEdgeById(edgeIds[i]);
				curVertex = board.getVertexById(vertexIds[i]);
				// TODO: technically dirty writes
				if(curEdge != null && !curEdge.setBlockedByPirate()) {
					// something went wrong
					return false;
				}
				if(curVertex != null && !curVertex.setPirate()) {
					// something went wrong
					return false;
				}
			}
			this.hasPirate = true;
			return true;
		}
		return false;
	}

	/**
	 * Remove the robber from this hexagon
	 *
	 * @return true iff robber was indeed removed
	 */
	public boolean removeRobber() {
		if (this.hasRobber) {
			Vertex curVertex;
			for (int i = 0; i < 6; i++) {
				curVertex = board.getVertexById(vertexIds[i]);
				// TODO: technically a dirty write
				if(curVertex != null && !curVertex.removeRobber()) {
					// something went wrong
					return false;
				}
			}
			this.hasRobber = false;
			return true;
		}
		// robber is not on this hex
		return false;
	}
	/**
	 * Remove the merchant from this hexagon
	 *
	 * @return true iff merchant was indeed removed
	 */
	public boolean removeMerchant() {
		if (this.hasMerchant) {
			this.hasMerchant = false;
			return true;
		}
		// robber is not on this hex
		return false;
	}

	/**
	 * Remove the pirate from this hexagon
	 *
	 * @return true iff pirate was indeed removed
	 */
	public boolean removePirate() {
		if (this.hasPirate) {
			Edge curEdge;
			Vertex curVertex;
			for (int i = 0; i < 6; i++) {
				curEdge = board.getEdgeById(edgeIds[i]);
				curVertex = board.getVertexById(vertexIds[i]);
				// TODO: technically a dirty write
				if(curEdge != null && !curEdge.removePirate()) {
					// something went wrong
					return false;
				}
				if(curVertex != null && !curVertex.removePirate()) {
					// something went wrong
					return false;
				}
			}
			this.hasPirate = false;
			return true;
		}
		// pirate is not on this hex
		return false;
	}

	/**
	 * Check if the hexagon has the robber
	 *
	 * @return true iff the hexagon has the robber
	 */
	public boolean hasRobber() {
		return (this.hasRobber);
	}

	/**
	 * Check if the hexagon has the merchant
	 *
	 * @return true iff the hexagon has the merchant
	 */
	public boolean hasMerchant() {
		return (this.hasMerchant);
	}

	/**
	 * Check if the hexagon has the pirate
	 *
	 * @return true iff the hexagon has the pirate
	 */
	public boolean hasPirate() {
		return (this.hasPirate);
	}

	/**
	 * Get the hexagon id
	 *
	 * @return the hexagon id number
	 */
	public int getId() {
		return id;
	}

	private static final HashMap<TerrainType, Resource> terrainTypeToResourceMap =
			initTerrainTypeToResourceMap();
	private static HashMap<TerrainType, Resource> initTerrainTypeToResourceMap()
	{
		HashMap<TerrainType, Resource> terrainToResourceMap =
				new HashMap<TerrainType, Resource>();
		Resource lumber, wool, grain, brick, ore, gold;
		lumber = new Resource(Resource.ResourceType.LUMBER);
		wool = new Resource(Resource.ResourceType.WOOL);
		grain = new Resource(Resource.ResourceType.GRAIN);
		brick = new Resource(Resource.ResourceType.BRICK);
		ore = new Resource(Resource.ResourceType.ORE);
		gold = new Resource(Resource.ResourceType.GOLD);
		terrainToResourceMap.put(TerrainType.FOREST, lumber);
		terrainToResourceMap.put(TerrainType.PASTURE, wool);
		terrainToResourceMap.put(TerrainType.FIELDS, grain);
		terrainToResourceMap.put(TerrainType.HILLS, brick);
		terrainToResourceMap.put(TerrainType.MOUNTAINS, ore);
		terrainToResourceMap.put(TerrainType.GOLD_FIELD, gold);
		return terrainToResourceMap;
	}

	public static Resource getResource(TerrainType terrainType) {
		return terrainTypeToResourceMap.get(terrainType);
	}

	/**
	 * Get the marginal X sign of vDirect on Hexagon
	 * @param vDirect
	 *            the direction of the edge on the hexagon
	 * @return the marginal X sign of the vDirect
	 */
	public static int getVDirectXsign(int vDirect) {
		switch(vDirect) {
			case 0:
				return 1;
			case 1:
				return 1;
			case 2:
				return 0;
			case 3:
				return -1;
			case 4:
				return -1;
			case 5:
				return 0;
			default:
				return Integer.MIN_VALUE;
		}
	}

	/**
	 * Get the marginal Y sign of vDirect on Hexagon
	 * @param vDirect
	 *            the direction of the edge on the hexagon
	 * @return the marginal Y sign of the vDirect
	 */
	public static int getVDirectYsign(int vDirect) {
		switch(vDirect) {
			case 0:
				return 1;
			case 1:
				return -1;
			case 2:
				return -1;
			case 3:
				return -1;
			case 4:
				return 1;
			case 5:
				return 1;
			default:
				return Integer.MIN_VALUE;
		}
	}

}
