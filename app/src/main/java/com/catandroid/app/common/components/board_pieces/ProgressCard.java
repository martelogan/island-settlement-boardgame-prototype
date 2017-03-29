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
            case BISHOP:
                return R.string.bishop;
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
            case BISHOP:
                return R.string.bishop_description;
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
            case ROAD_BUILLDING:
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

        ALCHEMIST, CRANE, ENGINEER, INVENTOR, IRIGIATION, MEDICINE, MINING, PRINTER, ROAD_BUILLDING, SMITH,

        BISHOP, CONSTITUTION, DESERTER, DIPLOMAT, INTRIGUE, SABOTEUR, SPY, WARLORD, WEDDING
    }
}
