package com.gamerentals.model;

public class Box {
    int id;
    int gameId;
    float status;

    public Box() {}

    public Box(int id, int gameId, float status) {
        this.id = id;
        this.gameId = gameId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public float getStatus() { return status; }
    public void setStatus(float status) { this.status = status; }

    @Override
    public String toString() { return String.format("Коробка{id=%d, id игры=%d,статус=%.2f}", id, gameId, status); }

}
