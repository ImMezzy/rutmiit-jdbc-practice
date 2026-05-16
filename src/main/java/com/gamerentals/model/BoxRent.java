package com.gamerentals.model;

import java.time.OffsetDateTime;

public class BoxRent {

    private int id;
    private int boxId;
    private int gameId;
    private String clientPassNumber;
    private OffsetDateTime dateOfRent;
    private OffsetDateTime dateOfReturn;
    private int fine;

    public BoxRent () {}

    public BoxRent(int id, int boxId, int gameId, String clientPassNumber, OffsetDateTime dateOfRent, OffsetDateTime dateOfReturn, int fine) {
        this.id = id;
        this.boxId = boxId;
        this.gameId = gameId;
        this.clientPassNumber = clientPassNumber;
        this.dateOfRent = dateOfRent;
        this.dateOfReturn = dateOfReturn;
        this.fine = fine;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBoxId() { return boxId; }
    public void setBoxId(int boxId) { this.boxId = boxId; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getClientPassNumber() { return clientPassNumber; }
    public void setClientPassNumber(String clientPassNumber) { this.clientPassNumber = clientPassNumber; }
    public OffsetDateTime getDateOfRent() { return dateOfRent; }
    public void setDateOfRent(OffsetDateTime dateOfRent) { this.dateOfRent = dateOfRent; }
    public OffsetDateTime getDateOfReturn() { return dateOfReturn; }
    public void setDateOfReturn(OffsetDateTime dateOfReturn) { this.dateOfReturn = dateOfReturn; }
    public int getFine() { return fine; }
    public void setFine(int fine) { this.fine = fine; }

    @Override
    public String toString() {
        return String.format("Аренда{id=%d, коробка=%d, игра=%d, клиент=%s}", id, boxId, gameId, clientPassNumber);
    }

}
