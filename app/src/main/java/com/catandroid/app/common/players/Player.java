package com.catandroid.app.common.players;

import java.util.Vector;

import android.content.Context;

import com.catandroid.app.common.components.Board;
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

	public static final int MAX_SETTLEMENTS = 5;
	public static final int MAX_CITIES = 4;
	public static final int MAX_ROADS = 15;

	public static final int[] ROAD_COST = { 1, 0, 0, 1, 0, 0 };
	public static final int[] SETTLEMENT_COST = { 1, 1, 1, 1, 0, 0 };
	public static final int[] CITY_COST = { 0, 0, 2, 0, 3, 0 };
	public static final int[] CARD_COST = { 0, 1, 1, 0, 1, 0 };

	private String googlePlayParticipantId;
	private int playerNumber;
	private Color color;
	private String name;
	protected int settlements;
	protected int cities;
	private int knightsCount, privateVictoryPointsCount, tradeValue, roadLength;
	private int[] countPerResource, countPerProgressCard;
	private boolean[] harbors;
	private Vector<ProgressCardType> newCards;
	private boolean usedCard;
	private int type, lastVertexPieceId;
	private String actionLog;

	protected Vector<Integer> settlementIds, reachingIds;
	protected Vector<Edge> roads;

	protected transient Board board;

	public enum Color {
		RED, BLUE, GREEN, YELLOW, SELECT, NONE
	}

	public static final int PLAYER_HUMAN = 0;
	public static final int PLAYER_BOT = 1;
	public static final int PLAYER_ONLINE = 2;

	/**
	 * Initialize player object
	 *
	 * @param board
	 *            board costs_reference
	 * @param name
	 *            player name
	 * @param type
	 *            PLAYER_HUMAN, PLAYER_BOT, or PLAYER_ONLINE
	 */
	public Player(Board board, int playerNumber, String googlePlayParticipantId, Color color, String name, int type) {
		this.board = board;
		this.googlePlayParticipantId = googlePlayParticipantId;
		this.color = color;
		this.name = name;
		this.type = type;
		this.playerNumber = playerNumber;

		settlements = 0;
		cities = 0;
		knightsCount = 0;
		roadLength = 0;
		privateVictoryPointsCount = 0;
		tradeValue = 4;
		usedCard = false;
		actionLog = "";
		lastVertexPieceId = -1;

		newCards = new Vector<ProgressCardType>();

		settlementIds = new Vector<Integer>();
		reachingIds = new Vector<Integer>();
		roads = new Vector<Edge>();

		// initialise number of each kind of progress card
		//TODO: track player's hand
//		countPerProgressCard = new int[ProgressCard.ProgressCardType.values().length];
//		for (int i = 0; i < countPerProgressCard.length; i++)
//		{
//			countPerProgressCard[i] = 0;
//		}

		countPerResource = new int[Resource.RESOURCE_TYPES.length];
		harbors = new boolean[Resource.ResourceType.values().length];
		for (int i = 0; i < countPerResource.length; i++) {
			countPerResource[i] = 0;
			harbors[i] = false;
		}
	}

	/**
	 * Roll the dice
	 *
	 * @return the result of the executeDiceRoll
	 */
	public int roll() {
		return roll((int) (Math.random() * 6) + (int) (Math.random() * 6) + 2);
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
	 * Roll the dice with a predefined result
	 *
	 * @param roll
	 *            the desired executeDiceRoll
	 * @return the result of the executeDiceRoll
	 */
	public int roll(int roll) {
		appendAction(R.string.player_roll, Integer.toString(roll));
		board.executeDiceRoll(roll);

		return roll;
	}

	/**
	 * Function called at the beginning of the turn
	 */
	public void beginTurn() {
		// clear the action log
		actionLog = "";
	}

	/**
	 * Function called at the end of the build phase
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
//		usedCard = false;

		appendAction(R.string.player_ended_turn);
	}

	/**
	 * Attempt to build a road on edge. Returns true on success
	 *
	 * @param edge
	 *            edge to build on
	 * @return
	 */
	public boolean build(Edge edge) {
		if (edge == null || !canBuild(edge))
		{
			return false;
		}

		// check resources
		boolean free = board.isSetupPhase() || board.isProgressPhase();
		if (!free && !affordRoad())
		{
			return false;
		}

		if (!edge.build(this))
		{
			return false;
		}

		if (!free) {
			useResources(Resource.ResourceType.BRICK, 1);
			useResources(Resource.ResourceType.LUMBER, 1);
		}

		appendAction(R.string.player_road);

		boolean hadLongest = (board.getLongestRoadOwner() == this);
		board.checkLongestRoad();

		if (!hadLongest && board.getLongestRoadOwner() == this)
		{
			appendAction(R.string.player_longest_road);
		}

		roads.add(edge);

		int vertexId = edge.getV0Clockwise().getId();
		if (!reachingIds.contains(vertexId))
		{
			reachingIds.add(vertexId);
		}

		vertexId = edge.getV1Clockwise().getId();
		if (!reachingIds.contains(vertexId))
		{
			reachingIds.add(vertexId);
		}

		return true;
	}

	/**
	 * Attempt to build an establishment on vertex. Returns true on success
	 *
	 * @param vertex
	 *            vertex to build on
	 * @return
	 */
	public boolean build(Vertex vertex, int type) {
		if (vertex == null || !canBuild(vertex, type))
		{
			return false;
		}

		boolean setup = board.isSetupPhase();

		// check resources based on type we want to build
		if (type == Vertex.SETTLEMENT) {
			if (!setup && !affordSettlement())
			{
				return false;
			}
		} else if (type == Vertex.CITY) {
			if (!setup && !affordCity())
			{
				return false;
			}
		} else {
			// invalid type
			return false;
		}

		if (!vertex.build(this, type, setup))
		{
			return false;
		}

		// deduct resources based on type
		if (vertex.getBuilding() == Vertex.SETTLEMENT) {
			if (!setup) {
				useResources(Resource.ResourceType.BRICK, 1);
				useResources(Resource.ResourceType.LUMBER, 1);
				useResources(Resource.ResourceType.GRAIN, 1);
				useResources(Resource.ResourceType.WOOL, 1);
			}
			settlements += 1;
			settlementIds.add(vertex.getId());
			board.checkLongestRoad();
		} else { // city
			if (!setup) {
				useResources(Resource.ResourceType.GRAIN, 2);
				useResources(Resource.ResourceType.ORE, 3);
				settlements -= 1;
			}
			else {
				settlementIds.add(vertex.getId());
			}
			cities += 1;
		}

		appendAction(type == Vertex.SETTLEMENT ? R.string.player_settlement
				: R.string.player_city);

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
							&& terrainType != Hexagon.TerrainType.SEA) {
						// collect resource for hex adjacent to city
						resourceType = curHex.getResourceType();
						addResources(resourceType, 2);
						appendAction(R.string.player_received_x_resources,
								Integer.toString(2) + " " + Resource.toRString(resourceType));
					}
				}
			}
		}

		lastVertexPieceId = vertex.getId();

		return true;
	}

	/**
	 * Can you build on this edge? Maybe
	 *
	 * @param edge
	 * @return
	 */
	public boolean canBuild(Edge edge) {
		if (edge == null || roads.size() >= MAX_ROADS)
			return false;

		if (board.isSetupPhase()) {
			Vertex v;
			if (lastVertexPieceId == -1) {
				v = null;
			}
			else {
				v = board.getVertexById(lastVertexPieceId);
			}
			// check if the edge is adjacent to the last settlement built
			if (v != edge.getV0Clockwise() &&
					v != edge.getV1Clockwise())
				return false;
		}

		return edge.canBuild(this);
	}

	/**
	 * Can you build on this vertex?
	 *
	 * @param vertex
	 * @return
	 */
	public boolean canBuild(Vertex vertex, int type) {
		if (type == Vertex.SETTLEMENT && settlements >= MAX_SETTLEMENTS)
		{
			return false;
		}
		else if (type == Vertex.CITY && cities >= MAX_CITIES)
		{
			return false;
		}

		return vertex.canBuild(this, type, board.isSetupPhase());
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
		countPerResource[resourceType.ordinal()] += count;
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
			appendAction(R.string.player_stole_from, from.getName());
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

		appendAction(R.string.player_traded_with, player.getName());
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
	 * Determine if the player can build a road
	 *
	 * @return true if the player can build a road
	 */
	public boolean affordRoad() {
		return (FREE_BUILD || roads.size() < MAX_ROADS
				&& getResources(Resource.ResourceType.BRICK) >= 1
				&& getResources(Resource.ResourceType.LUMBER) >= 1);
	}

	/**
	 * Determine if a player can build a settlement
	 *
	 * @return true if the player can build a settlement
	 */
	public boolean affordSettlement() {
		return (FREE_BUILD || settlements < MAX_SETTLEMENTS
				&& getResources(Resource.ResourceType.BRICK) >= 1
				&& getResources(Resource.ResourceType.LUMBER) >= 1
				&& getResources(Resource.ResourceType.GRAIN) >= 1
				&& getResources(Resource.ResourceType.WOOL) >= 1);
	}

	/**
	 * Determine if the player can build a city
	 *
	 * @return true if the player can build a city
	 */
	public boolean affordCity() {
		return (FREE_BUILD || cities < MAX_CITIES
				&& getResources(Resource.ResourceType.GRAIN) >= 2 && getResources(Resource.ResourceType.ORE) >= 3);
	}

	/**
	 * Get the number of privateVictoryPointsCount points that are evident to other players
	 *
	 * @return the number of privateVictoryPointsCount points
	 */
	public int getPublicVictoryPoints() {
		int points = settlements + 2 * cities;

		//TODO: add other public vps
		if (board.hasLongestRoad(this))
		{
			points += 2;
		}

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
//		if (usedCard)
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
//			usedCard = false;
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
//		if (!hasCard(card) || usedCard)
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
//		usedCard = true;
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
//			Player player = board.getPlayer(i);
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
	 * Determine if a trade is valid
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
		return (type == PLAYER_HUMAN);
	}

	/**
	 * Determine if the player is a bot
	 *
	 * @return true if the player is a bot
	 */
	public boolean isBot() {
		return (type == PLAYER_BOT);
	}

	/**
	 * Determine if the player is an online player
	 *
	 * @return
	 */
	public boolean isOnline() {
		return (type == PLAYER_ONLINE);
	}

	/**
	 * Set the player's name
	 *
	 * @param name
	 *            the player's new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the player's name
	 *
	 * @return the player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Notify the player the the road length is being recalculated
	 */
	public void cancelRoadLength() {
		roadLength = 0;
	}

	/**
	 * Notify the player of a road length and update its longest road length if
	 * greater
	 *
	 * @param roadLength
	 *            the length of a road
	 */
	public void setRoadLength(int roadLength) {
		if (roadLength > this.roadLength)
			this.roadLength = roadLength;
	}

	/**
	 * Get the length of the player's longest road
	 *
	 * @return the longest road length
	 */
	public int getRoadLength() {
		return roadLength;
	}

	/**
	 * Get the player's settlements count
	 *
	 * @return the number of settlements owned by the player
	 */
	public int getNumSettlements() {
		return settlements;
	}

	/**
	 * Get the number of resource cards the player has
	 *
	 * @return the number of resource cards the player has
	 */
	public int getNumResources() {
		int count = 0;
		for (int i = 0; i < countPerResource.length; i++)
			count += countPerResource[i];

		return count;
	}

	/**
	 * Get the number of cities
	 *
	 * @return the number of cities the player has
	 */
	public int getNumCities() {
		return cities;
	}

	/**
	 * Get the number of roads built
	 *
	 * @return the number of roads the player built
	 */
	public int getNumRoads() {
		return roads.size();
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
	private void appendAction(int action) {
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
	private void appendAction(int action, String additional) {
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
	private void appendAction(int action, int additional) {
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
