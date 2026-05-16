package com.gamerentals.model;

import java.time.LocalTime;

/**
 * Фильм.
 */
public class Film {
    private int id;
    private String title;
    private LocalTime duration;

    public Film() {}

    public Film(int id, String title, LocalTime duration) {
        this.id = id;
        this.title = title;
        this.duration = duration;
    }

    public Film(String title, LocalTime duration) {
        this(0, title, duration);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalTime getDuration() { return duration; }
    public void setDuration(LocalTime duration) { this.duration = duration; }

    @Override
    public String toString() {
        return String.format("Фильм{id=%d, '%s', %s}", id, title, duration);
    }
}
