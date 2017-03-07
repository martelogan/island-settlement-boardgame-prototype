/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.catandroid.app.common.logistics.multiplayer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.catandroid.app.common.components.Board;
import com.google.gson.Gson;

/**
 * Basic turn data. It's just a blank data string and a turn number counter.
 * 
 * @author wolff
 * 
 */
public class CatandroidTurn {

    public static final String TAG = "CatandroidTurn";
    public static Board currentBoard;

    public CatandroidTurn() {
    }

    // This is the byte array we will write out to the TBMP API.
    public static byte[] persist() {
        Gson gson = new Gson();

        String st = gson.toJson(currentBoard);

        Log.d(TAG, "==== PERSISTING\n" + st);

        return st.getBytes(Charset.forName("UTF-8"));
    }

    // Creates a new instance of Board
    static public Board unpersist(byte[] byteArray) {

        if (byteArray == null) {
            Log.d(TAG, "Empty array---possible bug.");
            return null;
        }

        String st = null;
        try {
            st = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }

        Log.d(TAG, "====UNPERSIST \n" + st);

        Gson gson = new Gson();
        Board myBoard;

        myBoard = gson.fromJson(st,Board.class);

        currentBoard = myBoard;

        return myBoard;
    }
}
