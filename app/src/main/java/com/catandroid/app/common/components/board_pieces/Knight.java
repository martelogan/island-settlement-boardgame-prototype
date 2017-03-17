package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;

/**
 * Created by logan on 2017-03-16.
 */

public class Knight extends OwnableUnit {

    public enum KnightRank {
        BASIC_KNIGHT, STRONG_KNIGHT, MIGHTY_KNIGHT
    }

    public int toRString() {
        return toRString(this.knightRank);
    }

    public static int toRString(KnightRank knightRank) {
        switch (knightRank) {
            case BASIC_KNIGHT:
                return R.string.knight_basic;
            case STRONG_KNIGHT:
                return R.string.knight_strong;
            case MIGHTY_KNIGHT:
                return R.string.knight_mighty;
            default:
                return R.string.empty_string;
        }
    }

    private KnightRank knightRank;
    private int knightId = Integer.MIN_VALUE;
    private boolean isActive = false;
    private boolean hasMovedThisTurn = false;
    private boolean hasBeenPromotedThisTurn = false;

    // wrapper struct only (e.g. state for textures)
    public Knight(KnightRank knightRank, Boolean isActive) {
        this.knightRank = knightRank;
        this.isActive = isActive;
    }

    // default constructor for board
    public Knight(int knightId) {
        this.knightId = knightId;
        this.knightRank = KnightRank.BASIC_KNIGHT;
    }

    // specific constructor for board
    public Knight(int knightId, KnightRank knightRank, Boolean isActive) {
        this.knightId = knightId;
        this.knightRank = knightRank;
        this.isActive = isActive;
    }

    public KnightRank getKnightRank() {
        return this.knightRank;
    }

    public int getId() {
        return this.knightId;
    }

    public void setKnightRank(KnightRank knightRank) {
        this.knightRank = knightRank;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean hasMovedThisTurn() {
        return hasMovedThisTurn;
    }

    public void setHasMovedThisTurn(boolean hasMovedThisTurn) {
        this.hasMovedThisTurn = hasMovedThisTurn;
    }

    public boolean hasBeenPromotedThisTurn() {
        return hasBeenPromotedThisTurn;
    }

    public void setHasBeenPromotedThisTurn(boolean hasBeenPromotedThisTurn) {
        this.hasBeenPromotedThisTurn = hasBeenPromotedThisTurn;
    }
}
