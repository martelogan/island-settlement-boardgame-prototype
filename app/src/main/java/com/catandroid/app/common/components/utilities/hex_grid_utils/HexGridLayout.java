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

public class HexGridLayout
{
    public HexGridLayout(HexGridOrientation hexGridOrientation, HexPoint size, HexPoint origin)
    {
        this.hexGridOrientation = hexGridOrientation;
        this.size = size;
        this.origin = origin;
    }
    public final HexGridOrientation hexGridOrientation;
    public final HexPoint size;
    public final HexPoint origin;
    static public HexGridOrientation pointy = new HexGridOrientation(Math.sqrt(3.0), Math.sqrt(3.0) / 2.0, 0.0, 3.0 / 2.0, Math.sqrt(3.0) / 3.0, -1.0 / 3.0, 0.0, 2.0 / 3.0, 0.5);
    static public HexGridOrientation flat = new HexGridOrientation(3.0 / 2.0, 0.0, Math.sqrt(3.0) / 2.0, Math.sqrt(3.0), 2.0 / 3.0, 0.0, -1.0 / 3.0, Math.sqrt(3.0) / 3.0, 0.0);
    static public HexPoint size_default = new HexPoint(0.48, 0.84 /(((Math.sqrt(3.0)/2.0)* -2.0) + ((Math.sqrt(3.0))*0.0)));
    static public HexPoint origin_default = new HexPoint(0.0, 0.0);

    static public HexPoint hexToPixel(HexGridLayout hexGridLayout, AxialHexLocation h)
    {
        HexGridOrientation M = hexGridLayout.hexGridOrientation;
        HexPoint size = hexGridLayout.size;
        HexPoint origin = hexGridLayout.origin;
        double x = (M.f0 * h.q + M.f1 * h.r) * size.x;
        double y = (M.f2 * h.q + M.f3 * h.r) * size.y;
        return new HexPoint(x + origin.x, y + origin.y);
    }


    static public FractionalHexLocation pixelToHex(HexGridLayout hexGridLayout, HexPoint p)
    {
        HexGridOrientation M = hexGridLayout.hexGridOrientation;
        HexPoint size = hexGridLayout.size;
        HexPoint origin = hexGridLayout.origin;
        HexPoint pt = new HexPoint((p.x - origin.x) / size.x, (p.y - origin.y) / size.y);
        double q = M.b0 * pt.x + M.b1 * pt.y;
        double r = M.b2 * pt.x + M.b3 * pt.y;
        return new FractionalHexLocation(q, r, -q - r);
    }


    static public HexPoint hexCornerOffset(HexGridLayout hexGridLayout, int corner)
    {
        HexGridOrientation M = hexGridLayout.hexGridOrientation;
        HexPoint size = hexGridLayout.size;
        double angle = 2.0 * Math.PI * (M.start_angle - corner) / 6;
        return new HexPoint(size.x * Math.cos(angle), size.y * Math.sin(angle));
    }


    static public ArrayList<HexPoint> polygonCorners(HexGridLayout hexGridLayout, AxialHexLocation h)
    {
        ArrayList<HexPoint> corners = new ArrayList<HexPoint>(){{}};
        HexPoint center = HexGridLayout.hexToPixel(hexGridLayout, h);
        for (int i = 0; i < 6; i++)
        {
            HexPoint offset = HexGridLayout.hexCornerOffset(hexGridLayout, i);
            corners.add(new HexPoint(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }

}
