package com.catandroid.app.common.components.utilities.hex_grid_utils;

/**
 * Created by logan on 2017-01-31.
 */

class HexGridOffsetCoord
{
    public HexGridOffsetCoord(int col, int row)
    {
        this.col = col;
        this.row = row;
    }
    public final int col;
    public final int row;
    static public int EVEN = 1;
    static public int ODD = -1;

    static public HexGridOffsetCoord qoffsetFromCube(int offset, CubicHexLocation h)
    {
        int col = h.q;
        int row = h.r + (h.q + offset * (h.q & 1)) / 2;
        return new HexGridOffsetCoord(col, row);
    }


    static public CubicHexLocation qoffsetToCube(int offset, HexGridOffsetCoord h)
    {
        int q = h.col;
        int r = h.row - (h.col + offset * (h.col & 1)) / 2;
        int s = -q - r;
        return new CubicHexLocation(q, r, s);
    }


    static public HexGridOffsetCoord roffsetFromCube(int offset, CubicHexLocation h)
    {
        int col = h.q + (h.r + offset * (h.r & 1)) / 2;
        int row = h.r;
        return new HexGridOffsetCoord(col, row);
    }


    static public CubicHexLocation roffsetToCube(int offset, HexGridOffsetCoord h)
    {
        int q = h.col - (h.row + offset * (h.row & 1)) / 2;
        int r = h.row;
        int s = -q - r;
        return new CubicHexLocation(q, r, s);
    }

}
