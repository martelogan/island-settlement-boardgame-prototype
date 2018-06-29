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
package com.catandroid.app.common.components.board_pieces;

/**
 * Created by logan on 2017-02-27.
 */

public class NumberToken {

    private final static int[] TOTAL_WAYS_TO_SUM = { 0, 0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1 };

    private int tokenNum;
    private int totalWaysToSum;

    public NumberToken(int tokenNum) {
        if (tokenNum < 0 || tokenNum > 12) {
            throw new IllegalArgumentException("Token number must be in range [0,12]");
        }
        this.tokenNum = tokenNum;
        this.totalWaysToSum = TOTAL_WAYS_TO_SUM[tokenNum];
    }

    public int getTokenNum() {
        return this.tokenNum;
    }

    public int getTotalWaysToSum() {
        return this.totalWaysToSum;
    }

    public static int getTotalWaysToSum(int tokenNum) {
        return TOTAL_WAYS_TO_SUM[tokenNum];
    }
}
