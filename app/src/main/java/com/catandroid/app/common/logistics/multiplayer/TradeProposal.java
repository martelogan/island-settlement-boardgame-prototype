package com.catandroid.app.common.logistics.multiplayer;

import com.catandroid.app.common.components.Board;
import com.catandroid.app.common.components.board_pieces.Resource;
import com.catandroid.app.common.players.Player;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by Fred on 2017-03-09.
 */

public class TradeProposal {

    private transient Board board;

    private Resource.ResourceType tradeResource;
    private int[] originalOffer, counterOffer = null;
    private int currentPlayerToProposeId;
    private int tradeCreatorPlayerId;
    private ArrayList<Integer> pendingPlayers;
    private boolean tradeReplied = false;

    public TradeProposal(Resource.ResourceType tradeResource, int tradeCreatorPlayerId,
                         int currentPlayerToProposeId, int[] originalOffer,
                         ArrayList<Integer> pendingPlayers) {

        this.tradeResource = tradeResource;
        this.originalOffer = originalOffer;
        this.pendingPlayers = pendingPlayers;
        this.currentPlayerToProposeId = currentPlayerToProposeId;
        this.tradeCreatorPlayerId = tradeCreatorPlayerId;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setTradeResource(Resource.ResourceType tradeResource) {
        this.tradeResource = tradeResource;
    }

    public void setOriginalOffer(int[] originalOffer) {
        this.originalOffer = originalOffer;
    }

    public void setCounterOffer(int[] counterOffer) {
        this.counterOffer = counterOffer;
    }

    public void setCurrentPlayerToProposeId(int currentPlayerToProposeId) {
        this.currentPlayerToProposeId = currentPlayerToProposeId;
    }

    public void setTradeReplied(boolean tradeReplied) {
        this.tradeReplied = tradeReplied;
    }

    public boolean hasNextPlayerToPropose(){
        return !pendingPlayers.isEmpty();
    }

    public Player getNextPlayerToPropose(){
        return board.getPlayerById(pendingPlayers.remove(0));
    }

    public int getTradeCreatorPlayerId() {
        return tradeCreatorPlayerId;
    }

    public Resource.ResourceType getTradeResource() {
        return tradeResource;
    }

    public int[] getOriginalOffer() {
        return originalOffer;
    }

    public int[] getCounterOffer() {
        return counterOffer;
    }

    public int getCurrentPlayerToProposeId() {
        return currentPlayerToProposeId;
    }

    public boolean isTradeReplied() {
        return tradeReplied;
    }
}
