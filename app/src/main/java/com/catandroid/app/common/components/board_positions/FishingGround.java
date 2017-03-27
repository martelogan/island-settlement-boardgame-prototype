package com.catandroid.app.common.components.board_positions;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.NumberToken;

/**
 * Created by logan on 2017-03-26.
 */

public class FishingGround {
    public enum Position {
        NORTH, SOUTH, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST
    }

    // default positions
    private static final Position[] POSITIONS_BY_VDIRECT = {
            Position.NORTHEAST, Position.SOUTHEAST, Position.SOUTH,
            Position.SOUTHWEST, Position.NORTHWEST, Position.NORTH
    };

    private NumberToken numberToken;
    private Position position;
    private int id;
    private int edgeId = -1;

    private transient Board board;

    public FishingGround(Board board, NumberToken numberToken, int id) {
        this.numberToken = numberToken;
        this.id = id;
        this.board = board;
    }


    public void setBoard(Board board) {
        this.board = board;
    }

    public void setNumberToken(NumberToken numberToken) {
        this.numberToken = numberToken;
    }

    public NumberToken getNumberToken() {
        return numberToken;
    }

    public void setPosition(Position p) {
        this.position = p;
    }

    public Position getPosition() {
        return position;
    }

    public void setEdge(Edge edge) {
        this.edgeId = edge.getId();
    }

    public Edge getEdge() {
        return board.getEdgeById(edgeId);
    }

    public static Position vdirectToPosition(int vdirect) {
        return POSITIONS_BY_VDIRECT[vdirect];
    }

    public int getId() {
        return id;
    }

}
