package com.gamerentals.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Сеанс (показ фильма в зале).
 */
public class Screening {
    private int id;
    private short hallNumber;
    private int filmId;
    private OffsetDateTime dateTime;
    private BigDecimal basePrice;

    // Дополнительные поля для JOIN-запросов
    private String filmTitle;
    private String hallName;

    public Screening() {}

    public Screening(int id, short hallNumber, int filmId, OffsetDateTime dateTime, BigDecimal basePrice) {
        this.id = id;
        this.hallNumber = hallNumber;
        this.filmId = filmId;
        this.dateTime = dateTime;
        this.basePrice = basePrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public short getHallNumber() { return hallNumber; }
    public void setHallNumber(short hallNumber) { this.hallNumber = hallNumber; }
    public int getFilmId() { return filmId; }
    public void setFilmId(int filmId) { this.filmId = filmId; }
    public OffsetDateTime getDateTime() { return dateTime; }
    public void setDateTime(OffsetDateTime dateTime) { this.dateTime = dateTime; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    @Override
    public String toString() {
        return String.format("Сеанс{id=%d, зал=%d, фильм=%d, %s, цена=%s}",
                id, hallNumber, filmId, dateTime, basePrice);
    }
}
