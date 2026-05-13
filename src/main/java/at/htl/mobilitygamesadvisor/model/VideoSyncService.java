// at.htl.mobilitygamesadvisor.model.VideoSyncService
package at.htl.mobilitygamesadvisor.model;

import at.htl.mobilitygamesadvisor.util.UserSession;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
public class VideoSyncService {
    private static final String VIDEO_DIR = "data/videos/";
    private static final String VIDEO_BASE_URL = "http://localhost:8081/videos/";

    private static String toHttpUrl(File file) {
        try {
            return "http://localhost:8081/videos/" +
                    URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
                            .replace("+", "%20");
        } catch (Exception e) {
            return "http://localhost:8081/videos/" + file.getName();
        }
    }

    public void sync() {
        File folder = new File(VIDEO_DIR);
        if (!folder.exists()) return;

        Set<String> existingUrls = getExistingUrls();
        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".mp4")) continue;
            String videoUrl = toHttpUrl(file); // http://localhost:8081/videos/BalloonGame.mp4
            if (existingUrls.contains(videoUrl)) continue;
            String title = file.getName().replace(".mp4", "").replace("-", " ");
            insertExercise(title, videoUrl);
        }
    }

    private Set<String> getExistingUrls() {
        Set<String> urls = new HashSet<>();
        String sql = "SELECT video_url FROM exercises WHERE video_url IS NOT NULL";
        try (Statement stmt = DatabaseConnection.get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) urls.add(rs.getString("video_url"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return urls;
    }

    private void insertExercise(String title, String videoUrl) {
        // resolve userId here, not at construction time
        User user = UserSession.getInstance().getUser();
        Integer userId = (user != null) ? user.getId() : null;

        String sql = "INSERT INTO exercises (uploaded_by, title, description, category, video_url) VALUES (?, ?, 'Automatisch importiert', 'Unkategorisiert', ?)";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER); // uploaded_by allows NULL per your schema
            }
            stmt.setString(2, title);
            stmt.setString(3, videoUrl);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}