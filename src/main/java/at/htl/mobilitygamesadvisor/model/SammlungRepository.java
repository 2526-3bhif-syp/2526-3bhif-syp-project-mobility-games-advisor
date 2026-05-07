package at.htl.mobilitygamesadvisor.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SammlungRepository {

    public List<Sammlung> getAll() {
        List<Sammlung> list = new ArrayList<>();
        String sql = "SELECT id, title FROM sammlung ORDER BY id";
        try (Statement stmt = DatabaseConnection.get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Sammlung(rs.getInt("id"), rs.getString("title")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void create(String title) {
        String sql = "INSERT INTO sammlung (title) VALUES (?)";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM sammlung WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void rename(int id, String newTitle) {
        String sql = "UPDATE sammlung SET title = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Exercise> getExercises(int sammlungId) {
        List<Exercise> list = new ArrayList<>();
        String sql = """
            SELECT e.id, e.title, e.description, e.category, e.video_url
            FROM exercises e
            JOIN sammlung_exercise se ON se.exercise_id = e.id
            WHERE se.sammlung_id = ?
            ORDER BY e.title
            """;
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Exercise(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("video_url")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addExercise(int sammlungId, int exerciseId) {
        String sql = "INSERT INTO sammlung_exercise (sammlung_id, exercise_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, exerciseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeExercise(int sammlungId, int exerciseId) {
        String sql = "DELETE FROM sammlung_exercise WHERE sammlung_id = ? AND exercise_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, exerciseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean containsExercise(int sammlungId, int exerciseId) {
        String sql = "SELECT 1 FROM sammlung_exercise WHERE sammlung_id = ? AND exercise_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, exerciseId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInAnySammlung(int exerciseId) {
        String sql = "SELECT 1 FROM sammlung_exercise WHERE exercise_id = ? LIMIT 1";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countExercises(int sammlungId) {
        String sql = "SELECT COUNT(*) FROM sammlung_exercise WHERE sammlung_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
