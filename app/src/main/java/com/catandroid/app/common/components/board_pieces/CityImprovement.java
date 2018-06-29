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
package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;

/**
 * Created by fred on 2017-03-12.
 */

public class CityImprovement {

    public static final CityImprovementType[] CITY_IMPROVEMENT_TYPES =
            { CityImprovementType.TRADE, CityImprovementType.SCIENCE, CityImprovementType.POLITICS};



    public enum CityImprovementType {
        TRADE, POLITICS, SCIENCE
    }


    public static int toCityImprovementIndex(CityImprovementType cityImprovementType) {
        switch(cityImprovementType) {
            case TRADE:
                return 0;
            case SCIENCE:
                return 1;
            case POLITICS:
                return 2;
            default:
                return -1;
        }
    }

    public static int toRString(CityImprovementType cityImprovementType) {
        switch (cityImprovementType) {
            case TRADE:
                return R.string.tradeImprovement;
            case POLITICS:
                return R.string.politicsImprovement;
            case SCIENCE:
                return R.string.scienceImprovement;
            default:
                return R.string.empty_string;
        }
    }
}
