package com.gamerentals.model;

import java.time.LocalTime;

public class Game {
    private int id;
    private String name;
    private String authors;
    private String difficulty;
    private LocalTime avgGameTime;

    public Game () {}

    public Game (int id, String name, String authors, String difficulty, LocalTime avgGameTime) {
        this.id = id;
        this.name = name;
        this.authors = authors;
        this.difficulty = difficulty;
        this.avgGameTime = avgGameTime;
    }

    public Game(String name, String authors, String difficulty, LocalTime avgGameTime) {
        this(0, name, authors, difficulty, avgGameTime);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getAuthors() { return authors; }
    public String getDifficulty() { return difficulty; }
    public void setAuthors(String authors) { this.authors = authors; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setName(String name) { this.name = name; }
    public LocalTime getAvgGameTime() { return avgGameTime; };
    public void setAvgGameTime(LocalTime avgGameTime) { this.avgGameTime = avgGameTime; }


    @Override
    public String toString() {
        return String.format("Игра{id=%d, '%s', %s, %s}", id, name, difficulty, avgGameTime);
    }

}
