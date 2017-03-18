package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.players.Player;

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
    private int ownerPlayerNumber = -1;
    private boolean isActive = false;
    private int turnLastActivated = -1, turnLastMoved = -1, turnLastPromoted = -1;

    private transient Board board;

    // wrapper struct only (e.g. state for textures)
    public Knight(KnightRank knightRank, Boolean isActive) {
        this.knightRank = knightRank;
        this.isActive = isActive;
    }

    // default constructor for usage by board
    public Knight(int knightId) {
        this.knightId = knightId;
        this.knightRank = KnightRank.BASIC_KNIGHT;
    }

    // specific constructor for usage by board
    public Knight(int knightId, KnightRank knightRank, Boolean isActive) {
        this.knightId = knightId;
        this.knightRank = knightRank;
        this.isActive = isActive;
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

    public Player getOwnerPlayer() {
        return board.getPlayerById(ownerPlayerNumber);
    }

    public void setOwnerPlayerNumber(int ownerPlayerNumber) {
        this.ownerPlayerNumber = ownerPlayerNumber;
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

    public boolean activate() {
        if (isActive) {
            return false;
        }
        this.isActive = true;
        this.turnLastActivated = board.getGameTurnNumber();
        return true;
    }

    public boolean canPromote() {
        if (hasBeenPromotedThisTurn()) {
            return false;
        }
        switch(knightRank) {
            case BASIC_KNIGHT:
                return true;
            case STRONG_KNIGHT:
                return getOwnerPlayer().getPoliticsLevel() >= 3;
            default:
                return false;
        }
    }

    public boolean promote() {
        if (!canPromote()) {
            return false;
        }
        switch(knightRank) {
            case BASIC_KNIGHT:
                knightRank = KnightRank.STRONG_KNIGHT;
                break;
            case STRONG_KNIGHT:
                knightRank = KnightRank.MIGHTY_KNIGHT;
                break;
            default:
                return false;
        }
        turnLastPromoted = board.getGameTurnNumber();
        return true;
    }

    public boolean hasBeenActivatedThisTurn() {
        return turnLastActivated == board.getGameTurnNumber();
    }

    public boolean hasMovedThisTurn() {
        return turnLastMoved == board.getGameTurnNumber();
    }

    public boolean hasBeenPromotedThisTurn() {
        return turnLastPromoted == board.getGameTurnNumber();
    }

}
