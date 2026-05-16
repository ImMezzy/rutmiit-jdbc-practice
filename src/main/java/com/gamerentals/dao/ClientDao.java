package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao {
    public List<Client> findAll() throws SQLException {
        String sql = "SELECT pass_number,name, last_name, patronymic FROM clients ORDER BY name";
        List<Client> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public Optional<Client> findByName(String name) throws SQLException { // ЗАДАНИЕ 1.2
        String sql = "SELECT pass_number,name, last_name, patronymic FROM clients WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Client> findByPass(String passNumber) throws SQLException {
        String sql = "SELECT pass_number,name, last_name, patronymic FROM clients WHERE pass_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public boolean insert(Client client) throws SQLException {
        String sql = "INSERT INTO clients (pass_number, name, last_name, patronymic) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getPassNumber());
            ps.setString(2, client.getName());
            ps.setString(3, client.getLastName());
            ps.setString(4, client.getPatronymic());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Client client) throws SQLException {
        String sql = "UPDATE clients SET name = ?, last_name = ?, patronymic = ? WHERE pass_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getLastName());
            ps.setString(3, client.getPatronymic());
            ps.setString(4, client.getPassNumber());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String pass_number) throws SQLException {
        String sql = "DELETE FROM clients WHERE pass_number = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pass_number);
            return ps.executeUpdate() > 0;
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getString("pass_number"),
                rs.getString("name"),
                rs.getString("last_name"),
                rs.getString("patronymic")
        );
    }
}
