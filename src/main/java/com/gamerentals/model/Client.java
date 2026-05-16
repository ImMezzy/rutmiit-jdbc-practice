package com.gamerentals.model;

public class Client {
    String passNumber;
    String name;
    String lastName;
    String patronymic;

    public Client () {}

    public Client (String passNumber, String name, String lastName, String patronymic) {
        this.passNumber = passNumber;
        this.name = name;
        this.lastName = lastName;
        this.patronymic = patronymic;
    }

    public String getPassNumber() { return passNumber; }
    public void setPassNumber(String passNumber) { this.passNumber = passNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    @Override
    public String toString() {
        return String.format("Клиент{passNumber=%s, имя='%s', фамилия='%s', отчество='%s'}", passNumber, name, lastName, patronymic);
    }

}
