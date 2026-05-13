package at.htl.mobilitygamesadvisor.model;

import at.htl.mobilitygamesadvisor.util.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SammlungRepository {

    private int userId() {
        return UserSession.getInstance().getUser().getId();
    }

    // —— only return sammlung owned by the logged-in user ————————————————————
    public List<Sammlung> getAll() {
        List<Sammlung> list = new ArrayList<>();
        // FIX: was querying sammlung_excercise (wrong table + typo), should be sammlung
        String sql = "SELECT id, title FROM sammlung WHERE owner_id = ? ORDER BY id";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, userId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Sammlung(rs.getInt("id"), rs.getString("title")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // —— stamp owner_id on insert ——————————————————————————————————————————————
    public void create(String title) {
        // FIX: column was uploaded_by, now owner_id
        String sql = "INSERT INTO sammlung (title, owner_id) VALUES (?, ?)";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setInt(2, userId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // —— guard: only delete your own sammlung ————————————————————————————————
    public void delete(int id) {
        // FIX: column was uploaded_by, now owner_id
        String sql = "DELETE FROM sammlung WHERE id = ? AND owner_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // —— guard: only rename your own sammlung ————————————————————————————————
    public void rename(int id, String newTitle) {
        // FIX: column was uploaded_by, now owner_id
        String sql = "UPDATE sammlung SET title = ? WHERE id = ? AND owner_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setInt(2, id);
            stmt.setInt(3, userId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // —— only fetch exercises from your own sammlung ——————————————————————————
    public List<Exercise> getExercises(int sammlungId) {
        List<Exercise> list = new ArrayList<>();
        String sql = """
            SELECT e.id, e.title, e.description, e.category, e.video_url, e.uploaded_by
            FROM exercises e
            JOIN sammlung_exercise se ON se.exercise_id = e.id
            JOIN sammlung s ON s.id = se.sammlung_id
            WHERE se.sammlung_id = ?
              AND s.owner_id = ?
            ORDER BY e.title
            """;
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, userId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Exercise(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("video_url"),
                        rs.getInt("uploaded_by")   // needed for delete-own-video check
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // —— guard: only add to your own sammlung ————————————————————————————————
    public void addExercise(int sammlungId, int exerciseId) {
        if (!isOwnedByCurrentUser(sammlungId)) return;
        System.out.println("addExercise called: sammlungId=" + sammlungId + " exerciseId=" + exerciseId + " userId=" + userId());
        if (!isOwnedByCurrentUser(sammlungId)) {
            System.out.println("BLOCKED: sammlung " + sammlungId + " not owned by user " + userId());
            return;
        }
        // FIX: sammlung_exercise has no uploaded_by column anymore
        String sql = "INSERT INTO sammlung_exercise (sammlung_id, exercise_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, exerciseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // —— guard: only remove from your own sammlung ————————————————————————————
    public void removeExercise(int sammlungId, int exerciseId) {
        if (!isOwnedByCurrentUser(sammlungId)) return;
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
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInAnySammlung(int exerciseId) {
        String sql = "SELECT 1 FROM sammlung_exercise WHERE exercise_id = ? LIMIT 1";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            return stmt.executeQuery().next();
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

    // —— private ownership check ——————————————————————————————————————————————
    private boolean isOwnedByCurrentUser(int sammlungId) {
        // FIX: column was uploaded_by, now owner_id
        String sql = "SELECT 1 FROM sammlung WHERE id = ? AND owner_id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, sammlungId);
            stmt.setInt(2, userId());
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}