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

import java.util.ArrayList;

/**
 * Created by logan on 2017-01-31.
 */

public class HexGridUtils
{
    public HexGridUtils()
    {
    }

    static public void equalHex(String name, CubicHexLocation a, CubicHexLocation b)
    {
        if (!(a.q == b.q && a.s == b.s && a.r == b.r))
        {
            complain(name);
        }
    }


    static public void equalOffsetcoord(String name, HexGridOffsetCoord a, HexGridOffsetCoord b)
    {
        if (!(a.col == b.col && a.row == b.row))
        {
            complain(name);
        }
    }


    static public void equalInt(String name, int a, int b)
    {
        if (!(a == b))
        {
            complain(name);
        }
    }


    static public void equalHexArray(String name, ArrayList<CubicHexLocation> a, ArrayList<CubicHexLocation> b)
    {
        equalInt(name, a.size(), b.size());
        for (int i = 0; i < a.size(); i++)
        {
            equalHex(name, a.get(i), b.get(i));
        }
    }


    static public void complain(String name)
    {
        System.out.println("FAIL " + name);
    }

}
