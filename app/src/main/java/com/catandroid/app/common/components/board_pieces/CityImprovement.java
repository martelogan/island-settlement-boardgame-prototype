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
