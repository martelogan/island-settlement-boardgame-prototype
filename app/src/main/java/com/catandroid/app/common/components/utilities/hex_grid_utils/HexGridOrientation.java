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
 */package com.catandroid.app.common.components.utilities.hex_grid_utils;

/**
 * Created by logan on 2017-01-31.
 */

class HexGridOrientation
{
    public HexGridOrientation(double f0, double f1, double f2, double f3, double b0, double b1, double b2, double b3, double start_angle)
    {
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.start_angle = start_angle;
    }
    public final double f0;
    public final double f1;
    public final double f2;
    public final double f3;
    public final double b0;
    public final double b1;
    public final double b2;
    public final double b3;
    public final double start_angle;
}
