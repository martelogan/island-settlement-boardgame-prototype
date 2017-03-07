package com.catandroid.app.common.players;

import java.util.Vector;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_positions.Edge;
import com.catandroid.app.common.components.board_positions.Hexagon;
import com.catandroid.app.common.components.board_pieces.Resource.ResourceType;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.components.board_positions.Vertex;

public class BalancedAI extends Player implements AutomatedPlayer {

    protected static final int[] preference = { 9, 8, 8, 10, 7, 8 };

    public BalancedAI(Board board, int index, Color color, String name) {
        super(board, index, "", color, name, Player.PLAYER_BOT);
    }

    @Override
    public void buildPhase() {
        boolean done = false;
        while (!done) {
            done = true;

            boolean hasLongest = board.getLongestRoadOwner() == this;
            boolean settlementPriority = settlements + cities < 4;
            boolean roadContender = board.getLongestRoad() - getRoadLength() <= 3;

            boolean canSettle = false;
            boolean canCity = false;

            // check if we have a location to build a settlement or city
            for (int i = 0; i < roads.size(); i++) {
                Vertex v1 = roads.get(i).getV0Clockwise();
                Vertex v2 = roads.get(i).getV1Clockwise();

                if (v1.canBuild(this, Vertex.SETTLEMENT)
                        || v2.canBuild(this, Vertex.SETTLEMENT))
                {
                    canSettle = true;
                }

                if (v1.canBuild(this, Vertex.CITY)
                        || v2.canBuild(this, Vertex.CITY))
                {
                    canCity = true;
                }
            }

            // don't uselessly expand roads until player has 4 settlements/cities
            boolean considerRoad = getNumRoads() < MAX_ROADS
                    && (!canSettle || (!hasLongest && roadContender && !settlementPriority));

            // try to build a settlement
            if (canSettle && affordSettlement()) {
                Vertex pick = pickSettlement();
                if (pick != null && build(pick, Vertex.SETTLEMENT))
                {
                    done = false;
                }
            }

            // try to build a city
            if (canCity && affordCity()) {
                for (int i = 0; i < settlementIds.size(); i++) {
                    Vertex settlement = board.getVertexById(settlementIds.get(i));
                    if (settlement.getBuilding() == Vertex.SETTLEMENT
                            && build(settlement, Vertex.CITY))
                    {
                        done = false;
                    }
                }
            }

            // try to build a road if we can afford it
            if (considerRoad && affordRoad()) {
                boolean builtRoad = false;

                // try to build towards somewhere nearby to settle
                if (settlementPriority) {
                    for (int i = 0; i < reachingIds.size(); i++) {
                        Vertex vertex = board.getVertexById(reachingIds.get(i));
                        for (int j = 0; j < 3; j++) {
                            Edge edge = vertex.getEdge(j);
                            if (edge == null || edge.hasRoad())
                            {
                                continue;
                            }

                            Vertex other = edge.getAdjacent(vertex);
                            if (!other.hasBuilding() && other.couldBuild()
                                    && build(edge)) {
                                builtRoad = true;
                                break;
                            }
                        }

                        if (builtRoad)
                        {
                            break;
                        }
                    }
                }

                // build off an existing road
                if (!builtRoad && reachingIds.size() > 0) {
                    for (int i = 0; i < 3; i++) {
                        Vertex lastRoadEnd = board.getVertexById(reachingIds.get(reachingIds.size() - 1));
                        Edge edge = lastRoadEnd.getEdge(i);
                        if (edge != null && build(edge))
                        {
                            builtRoad = true;
                        }
                    }
                }

                // try and addCubic to another recent road
                if (!builtRoad) {
                    for (int i = roads.size() - 1; i >= 0; i--) {
                        Edge road = roads.get(i);
                        Vertex v1 = road.getV0Clockwise();
                        Vertex v2 = road.getV1Clockwise();

                        for (int j = 0; j < 3; j++) {
                            Edge edge1 = v1.getEdge(j);
                            Edge edge2 = v2.getEdge(j);

                            if (build(edge1) || build(edge2))
                            {
                                done = false;
                            }
                        }
                    }
                }
            }

            // trade in order to buy something
            if (done) {

                //TODO: consider progress card options here

                // trade road road resources
                if (considerRoad && !affordRoad() && tradeFor(ROAD_COST)) {
                    if (affordRoad())
                        done = false;
                }

                // trade for settlement resources
                else if (canSettle && !affordSettlement() && tradeFor(SETTLEMENT_COST)) {
                    if (affordSettlement())
                        done = false;
                }

                // trade for city resources
                else if (canCity && !affordCity() && !settlementPriority
                        && tradeFor(CITY_COST)) {
                    if (affordCity())
                        done = false;
                }
            }
        }
    }

    private Resource.ResourceType pickResourceType() {
        ResourceType pick = Resource.ResourceType.BRICK;
        int min = 100;

        // pick the resource that the player has the least of
        for (int i = 0; i < Resource.RESOURCE_TYPES.length; i++) {
            Resource.ResourceType resourceType = Resource.RESOURCE_TYPES[i];
            int number = getResources(resourceType);

            if (number < min) {
                min = number;
                pick = resourceType;
            }
        }

        return pick;
    }

    @Override
    public void productionPhase() {
        //TODO: implement any pre-roll behaviour
    }

    @Override
    public int setupRoad(Edge[] edges) {
        // build from random settlement and build in a random direction
        while (true) {
            Vertex vertex = board.getVertexById(settlementIds.get(
                    (int) (Math.random() * settlementIds.size())));
            int pick = (int) (Math.random() * 3);
            Edge edge = vertex.getEdge(pick);
            if (edge != null && build(edge))
            {
                return edge.getId();
            }
        }
    }

    @Override
    public int progressRoad(Edge[] edges) {
        Edge road = pickRoad();
        if (road == null)
            return -1;

        Vertex from = road.getV0Clockwise();
        if (from.getOwner() == this || from.hasRoad(this) && build(road))
            return road.getId();

        from = road.getV1Clockwise();
        if (from.getOwner() == this || from.hasRoad(this) && build(road))
            return road.getId();

        return -1;
    }

    @Override
    public int setupSettlement(Vertex[] vertices) {
        int highest = 0;
        int index = 0;

        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].getOwner() != null)
            {
                continue;
            }

            int score = vertexValue(vertices[i], preference);
            if (score > highest && canBuild(vertices[i], Vertex.SETTLEMENT)) {
                highest = score;
                index = i;
            }
        }

        build(vertices[index], Vertex.SETTLEMENT);
        reachingIds.add(index);
        return index;
    }

    @Override
    public int setupCity(Vertex[] vertices) {
        int highest = 0;
        int index = 0;

        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].getOwner() != null)
            {
                continue;
            }

            int score = vertexValue(vertices[i], preference);
            if (score > highest && canBuild(vertices[i], Vertex.CITY)) {
                highest = score;
                index = i;
            }
        }

        build(vertices[index], Vertex.CITY);
        reachingIds.add(index);
        return index;
    }


    @Override
    public int placeRobber(Hexagon[] hexagons, Hexagon exception) {
        int best = 0, highest = 0;
        for (int i = 0; i < hexagons.length; i++) {
            int value = hexagonValue(hexagons[i], preference);
            if (value <= highest || hexagons[i] == exception)
                continue;

            Vector<Player> players = hexagons[i].getPlayers();
            boolean canSteal = false;
            for (int j = 0; j < players.size(); j++) {
                if (players.get(j) == this)
                {
                    value = 1;
                }
                else if (players.get(j).getResourceCount() > 0)
                {
                    canSteal = true;
                }
            }

            if (canSteal && value > highest) {
                best = i;
                highest = value;
            }
        }

        return best;
    }

    @Override
    public int steal(Player[] players) {
        // steal from the first player that has resources
        for (int i = 0; i < players.length; i++) {
            if (players[i].getResourceCount() > 0)
            {
                return i;
            }
        }

        return 0;
    }

    protected Vertex pickSettlement() {
        if (!affordSettlement())
        {
            return null;
        }

        Vertex best = null;
        int highest = 0;

        for (int i = 0; i < roads.size(); i++) {
            Vertex v1 = roads.get(i).getV0Clockwise();
            Vertex v2 = roads.get(i).getV1Clockwise();

            int value1 = v1.canBuild(this, Vertex.SETTLEMENT) ? vertexValue(v1,
                    preference) : 0;
            int value2 = v2.canBuild(this, Vertex.SETTLEMENT) ? vertexValue(v2,
                    preference) : 0;

            if (value1 > value2 && value1 > highest) {
                highest = value1;
                best = v1;
            } else if (value2 > highest) {
                highest = value2;
                best = v2;
            }
        }

        return best;
    }

    protected Edge pickRoad() {
        // build off an existing road
        if (reachingIds.size() > 0) {
            Vertex lastRoadEnd = board.getVertexById(reachingIds.get(reachingIds.size() - 1));
            for (int i = 0; i < 3; i++) {
                Edge edge = lastRoadEnd.getEdge(i);
                if (edge != null && canBuild(edge))
                    return edge;
            }
        }

        // try and addCubic to another recent road
        for (int i = roads.size() - 1; i >= 0; i--) {
            Edge road = roads.get(i);
            Vertex v1 = road.getV0Clockwise();
            Vertex v2 = road.getV1Clockwise();

            for (int j = 0; j < 3; j++) {
                // null checking is in canBuild

                Edge edge1 = v1.getEdge(j);
                if (canBuild(edge1))
                {
                    return edge1;
                }

                Edge edge2 = v2.getEdge(j);
                if (canBuild(edge2))
                {
                    return edge2;
                }
            }
        }

        return null;
    }

    protected int hexagonValue(Hexagon hexagon, int[] factors) {
        if (factors != null && hexagon.getTerrainType() != Hexagon.TerrainType.DESERT
                && hexagon.getTerrainType() != Hexagon.TerrainType.SEA) {
            return factors[hexagon.getResourceType().ordinal()] * hexagon.getNumberTokenAsObject().getTotalWaysToSum();
        }
        else {
            return hexagon.getNumberTokenAsObject().getTotalWaysToSum();
        }
    }

    protected int vertexValue(Vertex vertex, int[] factors) {
        int value = 1;
        for (int i = 0; i < 3; i++) {
            Hexagon hexagon = vertex.getHexagon(i);
            if (hexagon != null)
            {
                value += hexagonValue(hexagon, factors);
            }
        }

        return value;
    }

    private boolean tradeFor(int[] want) {
        // copy list of resource we have
        int[] have = new int[Resource.RESOURCE_TYPES.length];
        for (int i = 0; i < have.length; i++)
        {
            have[i] = getResources(Resource.RESOURCE_TYPES[i]);
        }

        // create list of resources we need
        int[] need = new int[Resource.RESOURCE_TYPES.length];
        for (int i = 0; i < need.length; i++)
        {
            need[i] = want[i] - have[i];
        }

        Vector<Resource.ResourceType> resourceTypes = new Vector<Resource.ResourceType>();
        Vector<int[]> trades = new Vector<int[]>();

        // for each resource resourceTypes we need
        for (int i = 0; i < need.length; i++) {
            // for the number of that resource we need
            for (int j = 0; j < need[i]; j++) {
                Vector<int[]> offers = findTrades(Resource.RESOURCE_TYPES[i]);
                for (int k = 0; k < offers.size(); k++) {
                    int[] offer = offers.get(k);

                    // check if it uses any resources that are wanted
                    // and that we still would have enough resources
                    boolean accept = true;
                    for (int l = 0; l < offer.length; l++) {
                        if (offer[l] > 0 && want[l] > 0 || have[l] < offer[l])
                        {
                            accept = false;
                        }
                    }

                    // accept the first good offer
                    if (accept) {
                        // adjust balance
                        need[i] -= 1;
                        for (int l = 0; l < have.length; l++)
                        {
                            have[l] -= offer[l];
                        }

                        // addCubic to list of trades
                        resourceTypes.add(Resource.RESOURCE_TYPES[i]);
                        trades.add(offer);
                        break;
                    }
                }
            }
        }

        // check if the trades cover everything needed
        for (int i = 0; i < need.length; i++)
        {
            if (need[i] > 0)
            {
                return false;
            }
        }

        // run the trades
        for (int i = 0; i < trades.size(); i++) {
            // abort on first failing trade
            if (!trade(resourceTypes.get(i), trades.get(i)))
            {
                return false;
            }
        }

        return true;
    }

    private void addList(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++)
        {
            a[i] += b[i];
        }
    }

    private void subtractList(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++)
        {
            a[i] += b[i];
        }
    }

    private int compareList(int[] a, int[] b) {
        boolean greater = false;

        for (int i = 0; i < a.length; i++) {
            if (a[i] < b[i])
            {
                return -1;
            }
            else if (a[i] > b[i])
            {
                greater = true;
            }
        }

        if (greater)
        {
            return 1;
        }

        return 0;
    }

    @Override
    public int[] offerTrade(Player player, Resource.ResourceType resourceType, int[] offer) {
        // don't try to trade for a resource we don't have
        if (getResources(resourceType) <= 0)
        {
            return null;
        }

        // don't trade with players who may be about to win
        int points = player.getPublicVictoryPoints();
        int max = board.getMaxPoints();
        if (max - points <= 1)
        {
            return null;
        }

        // get a resource list
        int[] extra = getCountPerResource();

        // deduct anything that we can already build
        do {
            if (affordCity()) {
                subtractList(extra, CITY_COST);
                continue;
            } else if (affordSettlement()) {
                subtractList(extra, SETTLEMENT_COST);
                continue;
            } else if (affordRoad()) {
                subtractList(extra, ROAD_COST);
                continue;
            }
        } while (false);

        // don't trade if that resource is needed for something we can build
        if (extra[resourceType.ordinal()] <= 0)
        {
            return null;
        }

        // addCubic in the offer
        addList(extra, offer);

        // see if we can build anything new
        if ((compareList(extra, ROAD_COST) >= 0)
                || (compareList(extra, SETTLEMENT_COST) >= 0)
                || (compareList(extra, CITY_COST) >= 0)
                || (compareList(extra, CARD_COST) >= 0))
        {
            return offer;
        }

        return null;
    }

    @Override
    public void discard(int quantity) {
        // get a resource list
        int[] extra = getCountPerResource();
        int count = getResourceCount() - quantity;

        // deduct anything that we can already build
        do {
            if (affordCity() && count >= 5) {
                subtractList(extra, CITY_COST);
                count -= 5;
                continue;
            } else if (affordSettlement() && count >= 4) {
                subtractList(extra, SETTLEMENT_COST);
                count -= 4;
                continue;
            } else if (affordRoad() && count >= 2) {
                subtractList(extra, ROAD_COST);
                count -= 2;
                continue;
            }
        } while (false);

        // discard_resources 'quantity' resources
        for (int q = 0; q < quantity; q++) {

            // try to pick the most common resource
            int max = 0;
            Resource.ResourceType mostCommon = null;
            for (int i = 0; i < extra.length; i++) {
                if (extra[i] > max) {
                    max = extra[i];
                    mostCommon = Resource.RESOURCE_TYPES[i];
                }
            }

            if (mostCommon != null)
            {
                extra[mostCommon.ordinal()] -= 1;
            }

            // discard_resources the most common resource, or a random resource
            super.discard(mostCommon);
        }
    }
}
