package com.gamerentals.dao;

import com.gamerentals.db.ConnectionManager;
import com.gamerentals.model.Film;
import com.gamerentals.model.Genre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы Фильм + связь M:N с Жанром.
 */
public class FilmDao {

    public List<Film> findAll() throws SQLException {
        String sql = "SELECT id_фильма, название, продолжительность FROM Фильм ORDER BY id_фильма";
        List<Film> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public Optional<Film> findById(int id) throws SQLException {
        String sql = "SELECT id_фильма, название, продолжительность FROM Фильм WHERE id_фильма = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Film film) throws SQLException {
        String sql = "INSERT INTO Фильм (название, продолжительность) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, film.getTitle());
            ps.setTime(2, Time.valueOf(film.getDuration()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    film.setId(id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ключ");
        }
    }

    public boolean update(Film film) throws SQLException {
        String sql = "UPDATE Фильм SET название = ?, продолжительность = ? WHERE id_фильма = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, film.getTitle());
            ps.setTime(2, Time.valueOf(film.getDuration()));
            ps.setInt(3, film.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM Фильм WHERE id_фильма = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ========== M:N связь с жанрами ==========

    public void addGenre(int filmId, int genreId) throws SQLException {
        String sql = "INSERT INTO Жанр_фильма (id_жанра, id_фильма) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, genreId);
            ps.setInt(2, filmId);
            ps.executeUpdate();
        }
    }

    public void removeGenre(int filmId, int genreId) throws SQLException {
        String sql = "DELETE FROM Жанр_фильма WHERE id_жанра = ? AND id_фильма = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, genreId);
            ps.setInt(2, filmId);
            ps.executeUpdate();
        }
    }

    public List<Genre> findGenresByFilm(int filmId) throws SQLException {
        String sql = """
                SELECT ж.id_жанра, ж.название
                FROM Жанр ж
                JOIN Жанр_фильма жф ON ж.id_жанра = жф.id_жанра
                WHERE жф.id_фильма = ?
                ORDER BY ж.название
                """;
        List<Genre> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, filmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Genre(rs.getInt("id_жанра"), rs.getString("название")));
                }
            }
        }
        return result;
    }

    private Film mapRow(ResultSet rs) throws SQLException {
        return new Film(
                rs.getInt("id_фильма"),
                rs.getString("название"),
                rs.getTime("продолжительность").toLocalTime()
        );
    }
}
