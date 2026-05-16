package com.gamerentals.model;

import java.time.OffsetDateTime;

/**
 * Билет на сеанс.
 */
public class Ticket {
    private int id;
    private int visitorId;
    private int screeningId;
    private short seatNumber;
    private short hallNumber;
    private OffsetDateTime purchaseDateTime;

    // Дополнительные поля для JOIN-запросов
    private String visitorName;
    private String filmTitle;
    private String hallName;
    private OffsetDateTime screeningDateTime;

    public Ticket() {}

    public Ticket(int id, int visitorId, int screeningId, short seatNumber, short hallNumber, OffsetDateTime purchaseDateTime) {
        this.id = id;
        this.visitorId = visitorId;
        this.screeningId = screeningId;
        this.seatNumber = seatNumber;
        this.hallNumber = hallNumber;
        this.purchaseDateTime = purchaseDateTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVisitorId() { return visitorId; }
    public void setVisitorId(int visitorId) { this.visitorId = visitorId; }
    public int getScreeningId() { return screeningId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public short getSeatNumber() { return seatNumber; }
    public void setSeatNumber(short seatNumber) { this.seatNumber = seatNumber; }
    public short getHallNumber() { return hallNumber; }
    public void setHallNumber(short hallNumber) { this.hallNumber = hallNumber; }
    public OffsetDateTime getPurchaseDateTime() { return purchaseDateTime; }
    public void setPurchaseDateTime(OffsetDateTime purchaseDateTime) { this.purchaseDateTime = purchaseDateTime; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public OffsetDateTime getScreeningDateTime() { return screeningDateTime; }
    public void setScreeningDateTime(OffsetDateTime screeningDateTime) { this.screeningDateTime = screeningDateTime; }

    @Override
    public String toString() {
        return String.format("Билет{id=%d, посетитель=%d, сеанс=%d, место=%d, зал=%d}",
                id, visitorId, screeningId, seatNumber, hallNumber);
    }
}
