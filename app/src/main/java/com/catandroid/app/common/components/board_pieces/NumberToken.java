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
