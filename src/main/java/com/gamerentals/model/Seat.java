package com.gamerentals.model;

import java.math.BigDecimal;

/**
 * Место в зале (составной ключ: номер_места + номер_зала).
 */
public class Seat {
    private short seatNumber;
    private short hallNumber;
    private short row;
    private short type;
    private BigDecimal priceCoefficient;

    public Seat() {}

    public Seat(short seatNumber, short hallNumber, short row, short type, BigDecimal priceCoefficient) {
        this.seatNumber = seatNumber;
        this.hallNumber = hallNumber;
        this.row = row;
        this.type = type;
        this.priceCoefficient = priceCoefficient;
    }

    public short getSeatNumber() { return seatNumber; }
    public void setSeatNumber(short seatNumber) { this.seatNumber = seatNumber; }
    public short getHallNumber() { return hallNumber; }
    public void setHallNumber(short hallNumber) { this.hallNumber = hallNumber; }
    public short getRow() { return row; }
    public void setRow(short row) { this.row = row; }
    public short getType() { return type; }
    public void setType(short type) { this.type = type; }
    public BigDecimal getPriceCoefficient() { return priceCoefficient; }
    public void setPriceCoefficient(BigDecimal priceCoefficient) { this.priceCoefficient = priceCoefficient; }

    public String getTypeName() {
        return switch (type) {
            case 1 -> "Стандарт";
            case 2 -> "VIP";
            default -> "Неизвестный";
        };
    }

    @Override
    public String toString() {
        return String.format("Место{№%d, зал=%d, ряд=%d, тип=%s, коэфф=%.2f}",
                seatNumber, hallNumber, row, getTypeName(), priceCoefficient);
    }
}
