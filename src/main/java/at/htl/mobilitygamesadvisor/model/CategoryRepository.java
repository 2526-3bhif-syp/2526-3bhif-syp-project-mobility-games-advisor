package at.htl.mobilitygamesadvisor.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    /** Alle Kategorien aus der DB holen */
    public List<String> getAll() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM exercises WHERE category IS NOT NULL ORDER BY category";
        try (Statement stmt = DatabaseConnection.get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Neue Kategorie anlegen */
    public void create(String categoryName) {
        String sql = "INSERT INTO exercises (title, description, category, video_url) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, "Platzhalter – " + categoryName);
            stmt.setString(2, "Automatisch erstellt für Kategorie: " + categoryName);
            stmt.setString(3, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Bestehende Kategorie umbenennen: updated alle Übungen dieser Kategorie */
    public void rename(String oldName, String newName) {
        String sql = "UPDATE exercises SET category = ? WHERE category = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, oldName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kategorie löschen: setzt bei allen Übungen dieser Kategorie
     * die Kategorie auf 'Unkategorisiert', damit keine Übung verloren geht.
     */
    public void delete(String categoryName) {
        String sql = "UPDATE exercises SET category = 'Unkategorisiert' WHERE category = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Wie viele Übungen hat eine Kategorie */
    public int countExercises(String category) {
        String sql = "SELECT COUNT(*) FROM exercises WHERE category = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}