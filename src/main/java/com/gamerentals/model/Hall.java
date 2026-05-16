package com.gamerentals.model;

/**
 * Кинозал.
 */
public class Hall {
    private short id;
    private String name;
    private short capacity;
    private short type;

    public Hall() {}

    public Hall(short id, String name, short capacity, short type) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.type = type;
    }

    public short getId() { return id; }
    public void setId(short id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public short getCapacity() { return capacity; }
    public void setCapacity(short capacity) { this.capacity = capacity; }
    public short getType() { return type; }
    public void setType(short type) { this.type = type; }

    public String getTypeName() {
        return switch (type) {
            case 1 -> "Стандарт";
            case 2 -> "VIP";
            case 3 -> "IMAX";
            default -> "Неизвестный";
        };
    }

    @Override
    public String toString() {
        return String.format("Зал{№%d, '%s', вместимость=%d, тип=%s}", id, name, capacity, getTypeName());
    }
}
