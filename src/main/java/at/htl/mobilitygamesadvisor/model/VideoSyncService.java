package at.htl.mobilitygamesadvisor.model;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
/*to add videos automaticalley from the folder*/

public class VideoSyncService {

    private static final String VIDEO_DIR = "data/videos/";
    private static final String BASE_URL  = "http://localhost:8081/videos/";

    public void sync() {
        File folder = new File(VIDEO_DIR);
        if (!folder.exists()) return;

        Set<String> existingUrls = getExistingUrls();

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".mp4")) continue;

            String url = BASE_URL + file.getName();
            if (existingUrls.contains(url)) continue; // bereits in DB

            // Dateiname ohne Extension als Titel verwenden
            String title = file.getName().replace(".mp4", "").replace("-", " ");
            insertExercise(title, url);
            System.out.println("Neu importiert: " + title);
        }
    }

    private Set<String> getExistingUrls() {
        Set<String> urls = new HashSet<>();
        try (Statement stmt = DatabaseConnection.get().createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT video_url FROM exercises WHERE video_url IS NOT NULL")) {
            while (rs.next()) urls.add(rs.getString("video_url"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return urls;
    }

    private void insertExercise(String title, String videoUrl) {
        String sql = "INSERT INTO exercises (title, description, category, video_url) VALUES (?, 'Automatisch importiert', 'Unkategorisiert', ?)";
        try (PreparedStatement stmt = DatabaseConnection.get().prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, videoUrl);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}