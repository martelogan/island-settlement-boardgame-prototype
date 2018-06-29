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
