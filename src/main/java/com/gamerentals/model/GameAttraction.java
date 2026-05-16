package com.gamerentals.model;

import java.time.OffsetDateTime;

public class GameAttraction {

    private int id;
    private int gameId;
    private String clientPassNumber;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    public GameAttraction() {}

    public GameAttraction(int id, int gameId, String clientPassNumber, OffsetDateTime startTime, OffsetDateTime endTime) {
        this.id = id;
        this.gameId = gameId;
        this.clientPassNumber = clientPassNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getClientPassNumber() { return clientPassNumber; }
    public void setClientPassNumber(String clientPassNumber) { this.clientPassNumber = clientPassNumber; }
    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime dateOfRent) { this.startTime = dateOfRent; }
    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime dateOfReturn) { this.endTime = dateOfReturn; }

    @Override
    public String toString() { return String.format("Игротека{id=%d, игра=%d, клиент=%s}", id, gameId, clientPassNumber); }

}
