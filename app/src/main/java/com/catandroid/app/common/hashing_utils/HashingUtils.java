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
