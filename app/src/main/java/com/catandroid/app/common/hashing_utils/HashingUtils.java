/*
 * Original work Copyright (c) 2011-2016, Data Geekery GmbH (http://www.datageekery.com)
 * Modified work Copyright (C) 2017, Logan Martel, Frederick Parsons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package com.catandroid.app.common.hashing_utils;

import com.catandroid.app.common.components.utilities.hex_grid_utils.AxialHexLocation;
import com.catandroid.app.common.hashing_utils.unsigned_data.ULong;

/**
 * Created by logan on 2017-03-16.
 */

public class HashingUtils {
    public static Long perfectHash(int a, int b)
    {
        ULong A = ULong.valueOf(a >= 0 ? 2 * (long)a : -2 * (long)a - 1);
        ULong B = ULong.valueOf (b >= 0 ? 2 * (long)b : -2 * (long)b - 1);
        ULong C = ULong.valueOf(0);
        ULong i = ULong.valueOf(0);
        if(A.compareTo(B) >= 0) {
            while(i.compareTo(A) == -1) {
                C = C.add(A);
                i = i.add(1);
            }
            C = C.add(A);
            C = C.add(B);
        }
        else {
            while(i.compareTo(B) == -1) {
                C = C.add(B);
                i = i.add(1);
            }
            C = C.add(A);
        }
        return a < 0 && b < 0 || a >= 0 && b >= 0 ? C.longValue() : -C.longValue() - 1;
    }

    static public Long perfectHash(AxialHexLocation location)
    {
        return perfectHash(location.q, location.r);
    }

    static public Double cantorHash(double q, double r)
    {
        return 0.5 * ((q + r) * (q + r + 1)) + r;
    }

    static public Double cantorHash(AxialHexLocation location)
    {
        return cantorHash(location.q, location.r);
    }
}
