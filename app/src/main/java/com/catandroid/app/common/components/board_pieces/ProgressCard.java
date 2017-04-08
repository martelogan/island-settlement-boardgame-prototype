package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;

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
            case SABOTEUR:
                return R.string.saboteur;
            case COMMERCIAL_HARBOR:
                return R.string.commercial_harbor;
            case SPY:
                return R.string.spy;
            case INTRIGUE:
                return R.string.intrigue;
            case ALCHEMIST:
                return R.string.alchemist;
            case MERCHANT_FLEET:
                return R.string.merchant_fleet;
            case CRANE:
                return R.string.crane;
            case ENGINEER:
                return R.string.engineer;
            case IRRIGIATION:
                return R.string.irrigation;
            case MINING:
                return R.string.mining;
            case DESERTER:
                return R.string.deserter;
            case WARLORD:
                return R.string.warlord;
            case MASTER_MERCHANT:
                return R.string.master_merchant;
            case PRINTER:
                return R.string.printer;
            case CONSTITUTION:
                return R.string.constitution;
            case RESOURCE_MONOPOLY:
                return R.string.resource_monopoly;
            case TRADE_MONOPOLY:
                return R.string.trade_monopoly;
            case MEDICINE:
                return R.string.medicine;
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
            case BISHOP:
                return R.string.bishop_description;
            case COMMERCIAL_HARBOR:
                return R.string.commercial_harbor_description;
            case INTRIGUE:
                return R.string.intrigue_description;
            case CRANE:
                return R.string.crane_description;
            case ENGINEER:
                return R.string.engineer_description;
            case IRRIGIATION:
                return R.string.irrigation_description;
            case MINING:
                return R.string.mining_description;
            case DESERTER:
                return R.string.deserter_description;
            case WARLORD:
                return R.string.warlord_description;
            case MASTER_MERCHANT:
                return R.string.master_merchant_description;
            case RESOURCE_MONOPOLY:
                return R.string.resource_monopoly_description;
            case TRADE_MONOPOLY:
                return R.string.trade_monopoly;
            case PRINTER:
                return R.string.printer;
            case MEDICINE:
                return R.string.medicine_description;
            case ALCHEMIST:
                return R.string.alchemist_description;
            case SPY:
                return R.string.spy_description;
            case MERCHANT_FLEET:
                return R.string.merchant_fleet_description;
            case SABOTEUR:
                return R.string.saboteur;
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
            case IRRIGIATION:
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

        ALCHEMIST, CRANE, ENGINEER, INVENTOR, IRRIGIATION, MEDICINE, MINING, PRINTER, ROAD_BUILDING, SMITH,

        BISHOP, CONSTITUTION, DESERTER, DIPLOMAT, INTRIGUE, SABOTEUR, SPY, WARLORD, WEDDING
    }
}
