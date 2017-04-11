package com.catandroid.app.common.components.board_pieces;

import com.catandroid.app.R;
import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_positions.Vertex;
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
    private int curVertexLocationId = -1;
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

    public Vertex getCurrentVertexLocation() {
        return board.getVertexById(curVertexLocationId);
    }

    public void setCurrentVertexLocation(Vertex currentVertexLocation) {
        this.curVertexLocationId = currentVertexLocation.getId();
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

    public boolean canMakeMove() {
        return isActive && !hasBeenActivatedThisTurn();
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

    public boolean deactivate() {
        if (!isActive) {
            return false;
        }
        this.isActive = false;
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

    public boolean canChaseRobber() {
        if (!canMakeMove()) {
            return false;
        }
        return getCurrentVertexLocation().canChaseRobberFromHere(getOwnerPlayer());
    }

    public boolean canChasePirate() {
        if (!canMakeMove()) {
            return false;
        }
        return getCurrentVertexLocation().canChasePirateFromHere(getOwnerPlayer());
    }

    public boolean canStartMoving() {
        if (!canMakeMove() || hasMovedThisTurn()) {
            return false;
        }
        return true;
    }


    public boolean canMoveTo(Vertex target, boolean isPeaceful) {
        if (!canMakeMove() || hasMovedThisTurn()) {
            return false;
        }

        if(target.getCurUnitType() != Vertex.NONE) {
            if(target.getCurUnitType() != Vertex.KNIGHT) {
                return false;
            }
            else if (!isPeaceful && canDisplace(target.getPlacedKnight())) {
                Vertex myStartLocation = board.getStartLocationOfMovingKnight();
                return target.canDisplaceKnightFromHere(getOwnerPlayer(), this)
                        && myStartLocation.hasTradeRouteTo(target, getOwnerPlayer(), false);
            }
        }

        // placing a knight at an unoccupied vertex
        Vertex myStartLocation = board.getStartLocationOfMovingKnight();
        return target.canPlaceNewKnightHere(getOwnerPlayer())
                && myStartLocation.hasTradeRouteTo(target, getOwnerPlayer(), true);
    }

    public boolean canDisplaceKnightTo(Vertex target) {
        // NOTE: BE CAREFUL: displaced movement is free

        if(target.getCurUnitType() != Vertex.NONE) {
            return false;
        }

        // placing a knight at an unoccupied vertex
        Vertex myStartLocation;
        if (!board.isKnightDisplacementPhase()) {
            /* WARNING: occurs before passing turn,
               needs curVertexLocationId to be  set at this time */
            myStartLocation = board.getVertexById(curVertexLocationId);
        } else {
            myStartLocation = board.getStartLocationOfMovingKnight();
        }
        return target.canPlaceNewKnightHere(getOwnerPlayer())
                && myStartLocation.hasTradeRouteTo(target, getOwnerPlayer(), true);
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

    public boolean demote() {
        switch(knightRank) {
            case BASIC_KNIGHT:
                // it can't really get any worse...
                return false;
            case STRONG_KNIGHT:
                knightRank = KnightRank.BASIC_KNIGHT;
                break;
            case MIGHTY_KNIGHT:
                knightRank = KnightRank.STRONG_KNIGHT;
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean chaseRobber() {
        if(!canChaseRobber()) {
            return false;
        }
        board.setReturnPhase(board.getPhase());
        board.startRobberPhase();
        deactivate();
        return true;
    }

    public boolean chasePirate() {
        if(!canChasePirate()) {
            return false;
        }
        board.setReturnPhase(board.getPhase());
        board.startPiratePhase();
        deactivate();
        return true;
    }

    public boolean startMoving() {
        if(!canStartMoving()) {
            return false;
        }
        board.startMovingKnightPhase(this);
        // WARNING: only remove vertex location after caching to board
        curVertexLocationId = -1;
        //NOTE: deactivation and other state change will follow successful movement
        return true;
    }

    public boolean displaceFromPost() {
        // WARNING: only remove vertex location after caching to board
        curVertexLocationId = -1;
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

    public boolean canDisplace(Knight defender) {
        return canDisplace(this, defender);
    }

    public static boolean canDisplace(Knight attacker, Knight defender) {

        if(attacker.getOwnerPlayer() == defender.getOwnerPlayer()) {
            // can't displace one of your own knights...
            return false;
        }

        KnightRank attackerRank = attacker.getKnightRank(),
                defenderRank = defender.getKnightRank();

        switch (attackerRank) {
            case BASIC_KNIGHT:
                return false;
            case STRONG_KNIGHT:
                if(defenderRank == KnightRank.BASIC_KNIGHT) {
                    return true;
                }
                else {
                    return false;
                }
            case MIGHTY_KNIGHT:
                if(defenderRank == KnightRank.MIGHTY_KNIGHT) {
                    return false;
                }
                else {
                    return true;
                }
            default:
                return false;
        }
    }

}
