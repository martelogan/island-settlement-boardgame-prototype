package com.catandroid.app.common.components.utilities.hex_grid_utils;

import java.util.ArrayList;

/**
 * Created by logan on 2017-01-31.
 */

class FractionalHexLocation
{
    public FractionalHexLocation(double q, double r, double s)
    {
        this.q = q;
        this.r = r;
        this.s = s;
    }
    public final double q;
    public final double r;
    public final double s;

    static public CubicHexLocation hexRound(FractionalHexLocation h)
    {
        int q = (int)(Math.round(h.q));
        int r = (int)(Math.round(h.r));
        int s = (int)(Math.round(h.s));
        double q_diff = Math.abs(q - h.q);
        double r_diff = Math.abs(r - h.r);
        double s_diff = Math.abs(s - h.s);
        if (q_diff > r_diff && q_diff > s_diff)
        {
            q = -r - s;
        }
        else
        if (r_diff > s_diff)
        {
            r = -q - s;
        }
        else
        {
            s = -q - r;
        }
        return new CubicHexLocation(q, r, s);
    }


    static public FractionalHexLocation hexLerp(FractionalHexLocation a, FractionalHexLocation b, double t)
    {
        return new FractionalHexLocation(a.q * (1 - t) + b.q * t, a.r * (1 - t) + b.r * t, a.s * (1 - t) + b.s * t);
    }


    static public ArrayList<CubicHexLocation> hexLinedraw(CubicHexLocation a, CubicHexLocation b)
    {
        int N = CubicHexLocation.distance(a, b);
        FractionalHexLocation a_nudge = new FractionalHexLocation(a.q + 0.000001, a.r + 0.000001, a.s - 0.000002);
        FractionalHexLocation b_nudge = new FractionalHexLocation(b.q + 0.000001, b.r + 0.000001, b.s - 0.000002);
        ArrayList<CubicHexLocation> results = new ArrayList<CubicHexLocation>(){{}};
        double step = 1.0 / Math.max(N, 1);
        for (int i = 0; i <= N; i++)
        {
            results.add(FractionalHexLocation.hexRound(FractionalHexLocation.hexLerp(a_nudge, b_nudge, step * i)));
        }
        return results;
    }

}
