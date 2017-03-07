package com.catandroid.app.common.components.utilities;

import com.catandroid.app.common.components.board_pieces.OwnableUnit;
import com.catandroid.app.common.components.board_pieces.Resource;

import java.util.HashMap;

/**
 * Created by logan on 2017-02-28.
 */

public class CostManager {
    private static final HashMap<OwnableUnit, HashMap<Resource, Integer>> costToPlayUnitMap =
            initCostToPlayUnitMap();

    private static HashMap<OwnableUnit, HashMap<Resource, Integer>> initCostToPlayUnitMap()
    {
        HashMap<OwnableUnit, HashMap<Resource, Integer>> initCostToPlayUnitMap =
                new HashMap<OwnableUnit, HashMap<Resource, Integer>>();
//        Resource lumber, wool, grain, brick, ore;
//        lumber = new Resource(Resource.ResourceType.LUMBER);
//        wool = new Resource(Resource.ResourceType.WOOL);
//        grain = new Resource(Resource.ResourceType.GRAIN);
//        brick = new Resource(Resource.ResourceType.BRICK);
//        ore = new Resource(Resource.ResourceType.ORE);
//        initCostToPlayUnitMap.put(Hexagon.TerrainType.FOREST, lumber);
//        initCostToPlayUnitMap.put(Hexagon.TerrainType.PASTURE, wool);
//        initCostToPlayUnitMap.put(Hexagon.TerrainType.FIELDS, grain);
//        initCostToPlayUnitMap.put(Hexagon.TerrainType.HILLS, brick);
//        initCostToPlayUnitMap.put(Hexagon.TerrainType.MOUNTAINS, ore);
        return initCostToPlayUnitMap;
    }

    public static HashMap<Resource, Integer> getCostToPlayUnit(OwnableUnit unit) {
        return costToPlayUnitMap.get(unit);
    }
}
