package at.htl.mobilitygamesadvisor.model;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ExerciseRepository {
    public List<Exercise> getAll() {
        return query("SELECT id, title, description, category, video_url FROM exercises");
    }
    public List<Exercise> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String sql = """
        SELECT id, title, description, category, video_url FROM exercises
        WHERE LOWER(title) LIKE ? OR LOWER(category) LIKE ?
        """;
        String pattern = "%" + query.toLowerCase() + "%";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            return mapResults(stmt.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Kategorie einer Übung anhand des Titels aktualisieren */
    public void updateCategory(String exerciseTitle, String newCategory) {
        String sql = "UPDATE exercises SET category = ? WHERE title = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, newCategory);
            stmt.setString(2, exerciseTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Exercise> query(String sql) {
        try (Statement stmt = DatabaseConnection.get().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            return mapResults(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    private List<Exercise> mapResults(ResultSet rs) throws SQLException {
        List<Exercise> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("video_url")
            ));
        }
        return list;
    }
}