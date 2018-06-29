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
