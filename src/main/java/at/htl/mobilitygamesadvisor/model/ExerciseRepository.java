package at.htl.mobilitygamesadvisor.model;
import at.htl.mobilitygamesadvisor.util.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseRepository {

    private Integer userId() {
        User user = UserSession.getInstance().getUser();
        return user != null ? user.getId() : null;
    }

    public List<Exercise> getAll() {
        return query("SELECT id, title, description, category, video_url, uploaded_by FROM exercises");
    }

    public void insert(String title, String desc, String category, String videoUrl) {
        String sql = "INSERT INTO exercises (title, description, category, video_url, uploaded_by) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, desc);
            ps.setString(3, category);
            ps.setString(4, videoUrl);
            Integer uid = userId();
            if (uid != null) ps.setInt(5, uid);
            else             ps.setNull(5, Types.INTEGER);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Exercise> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String sql = """
            SELECT id, title, description, category, video_url, uploaded_by FROM exercises
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

    public void updateCategory(String exerciseTitle, String newCategory) {
        String sql = "UPDATE exercises SET category = ? WHERE title = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, newCategory);
            stmt.setString(2, exerciseTitle);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // only delete your own exercise
    public void delete(int id) {
        Integer uid = userId();
        String sql = uid != null
                ? "DELETE FROM exercises WHERE id = ? AND uploaded_by = ?"
                : "DELETE FROM exercises WHERE id = ?";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (uid != null) stmt.setInt(2, uid);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
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
            // uploaded_by can be NULL in DB, getInt returns 0 for NULL which is fine
            // — controller checks e.uploadedBy() == userId so 0 will never match
            list.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("video_url"),
                    rs.getInt("uploaded_by")
            ));
        }
        return list;
    }
    public void update(int id, String title, String desc, String category) {
        String sql = "UPDATE exercises SET title = ?, description = ?, category = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.get().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, desc);
            ps.setString(3, category);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}