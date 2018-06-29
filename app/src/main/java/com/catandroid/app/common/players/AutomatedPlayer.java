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
package com.catandroid.app.common.players;

import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Vertex;

public interface AutomatedPlayer {

	/**
	 * Select the location to build a settlement; Note: you must build there before
	 * returning
	 * 
	 * @param vertices
	 *            the vertex list
	 * @return the index of the vertex you built on
	 */
	int setupSettlement(Vertex[] vertices);

	/**
	 * Select the location to build a city; Note: you must build there before
	 * returning
	 *
	 * @param vertices
	 *            the vertex list
	 * @return the index of the vertex you built on
	 */
	int setupCity(Vertex[] vertices);

	/**
	 * Select the location to place a road; Note: you must build there before
	 * returning
	 * 
	 * @param edges
	 *            the edge list
	 * @return the index of the edge you built on
	 */
	int setupRoad(Edge[] edges);

	/**
	 * Select the location to place a road; Note: you must build there before
	 * returning
	 * 
	 * @note in the case where a road can't be built, return -1
	 * 
	 * @param edges
	 *            the edge list
	 * @return the index of the edge you built on or -1
	 */
	int progressRoad(Edge[] edges);

	/**
	 * Run production phase
	 */
	void productionPhase();

	/**
	 * Run build phase
	 */
	void buildPhase();

	/**
	 * Select a hexagon to place the robber
	 * 
	 * @param hexagons
	 *            the list of hexagons
	 * @param exception
	 *            forbidden location (where the robber came from)
	 * @return the index of the hexagon to place the robber on
	 */
	int placeRobber(Hexagon[] hexagons, Hexagon exception);

	/**
	 * Select a player to steal from
	 * 
	 * @param players
	 *            list of players that you could steal from
	 * @return the index of the player to steal from
	 */
	int steal(Player[] players);

	/**
	 * Consider trading "resourceType" to "player" for "offer"
	 * 
	 * @param player
	 *            the player proposing the trade
	 * @param resourceType
	 *            the resourceType that the player wants
	 * @param offer
	 *            how many of each resource the player is offering
	 * @return the offer (to accept), a counter-offer, or null (to reject)
	 */
	int[] offerTrade(Player player, Resource.ResourceType resourceType, int[] offer);

	/**
	 * Instruct the player to discard resources
	 * 
	 * @note use Player.discard() via super
	 * 
	 * @param quantity
	 *            the number of resources that must be discarded
	 */
	void discard(int quantity);
}
