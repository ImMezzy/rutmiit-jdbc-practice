package com.gamerentals.model;

import java.time.OffsetDateTime;

public class GameSession {

    private int id;
    private int gameId;
    private String clientPassNumber;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    boolean gameResult;

    public GameSession() {}

    public GameSession(int id, int gameId, String clientPassNumber, OffsetDateTime startTime, OffsetDateTime endTime, boolean gameResult) {
        this.id = id;
        this.gameId = gameId;
        this.clientPassNumber = clientPassNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.gameResult = false;
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
    public boolean isGameResult() { return gameResult; }
    public void setGameResult(boolean gameResult) { this.gameResult = gameResult; }

    @Override
    public String toString() { return String.format("Игровая сессия{id=%d, игра=%d, клиент=%s, результат=%b}", id, gameId, clientPassNumber, gameResult); }

}
