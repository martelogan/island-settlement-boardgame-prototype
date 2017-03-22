package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;
import com.catandroid.app.common.components.board_pieces.InventoryItem;

/**
 * Created by logan on 2017-03-05.
 */

public class ProgressCard extends InventoryItem {
    /**
     * Get the string resource for a card type
     *
     * @param card
     *            the card type
     * @return the string resource
     */
    public static int getCardStringResource(ProgressCardType card) {
        switch (card) {
            case MERCHANT:
                return R.string.merchant;
            case PRINTER:
                return R.string.printer;
            case CONSTITUTION:
                return R.string.constitution;
            case MEDICINE:
                return R.string.medicine;
            case RESOURCE_MONOPOLY:
                return R.string.resource_monopoly;
            case TRADE_MONOPOLY:
                return R.string.trade_monopoly;
            case ROAD_BUILDING:
                return R.string.road_building;
            case INVENTOR:
                return R.string.inventor;
            case DIPLOMAT:
                return R.string.diplomat;
            default:
                return R.string.empty_string;
        }
    }
    /**
     * Get the string description resource for a card type
     *
     * @param card
     *            the card type
     * @return the string resource description
     */
    public static int getCardDescriptionStringResource(ProgressCardType card) {
        switch (card) {
            case MERCHANT:
                return R.string.merchant_description;
            case PRINTER:
                return R.string.printer_constitution_description;
            case CONSTITUTION:
                return R.string.printer_constitution_description;
            case MEDICINE:
                return R.string.medicine_description;
            case RESOURCE_MONOPOLY:
                return R.string.resource_monopoly_description;
            case TRADE_MONOPOLY:
                return R.string.trade_monopoly_description;
            case ROAD_BUILDING:
                return R.string.road_building_description;
            case INVENTOR:
                return R.string.inventor_description;
            case DIPLOMAT:
                return R.string.diplomat_description;

            default:
                return R.string.empty_string;
        }
    }

    public static CityImprovement.CityImprovementType getDisciplineFromCard(ProgressCardType card){
        switch(card){
            case COMMERCIAL_HARBOR:
            case MASTER_MERCHANT:
            case MERCHANT:
            case MERCHANT_FLEET:
            case RESOURCE_MONOPOLY:
            case TRADE_MONOPOLY:
                return CityImprovement.CityImprovementType.TRADE;
            case ALCHEMIST:
            case CRANE:
            case ENGINEER:
            case INVENTOR:
            case IRIGIATION:
            case MEDICINE:
            case MINING:
            case PRINTER:
            case ROAD_BUILDING:
            case SMITH:
                return CityImprovement.CityImprovementType.SCIENCE;
            case BISHOP:
            case CONSTITUTION:
            case DESERTER:
            case DIPLOMAT:
            case INTRIGUE:
            case SABOTEUR:
            case SPY:
            case WARLORD:
            case WEDDING:
                return CityImprovement.CityImprovementType.POLITICS;
            default:
                return null;
        }
    }

    public enum ProgressCardType {
        COMMERCIAL_HARBOR, MASTER_MERCHANT, MERCHANT, MERCHANT_FLEET, RESOURCE_MONOPOLY, TRADE_MONOPOLY,

        ALCHEMIST, CRANE, ENGINEER, INVENTOR, IRIGIATION, MEDICINE, MINING, PRINTER, ROAD_BUILDING, SMITH,

        BISHOP, CONSTITUTION, DESERTER, DIPLOMAT, INTRIGUE, SABOTEUR, SPY, WARLORD, WEDDING
    }
}
