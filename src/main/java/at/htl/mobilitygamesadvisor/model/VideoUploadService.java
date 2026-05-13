// at.htl.mobilitygamesadvisor.model.VideoUploadService
package at.htl.mobilitygamesadvisor.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class VideoUploadService {

    private static final String VIDEO_DIR = "data/videos/";

    /**
     * Copies the selected video file to the local video directory.
     * @param sourceFile the user's MP4 file
     * @return a file:// URL to the stored video
     * @throws IOException if copying fails
     */
    public static String storeVideo(File sourceFile) throws IOException {
        if (!sourceFile.getName().toLowerCase().endsWith(".mp4")) {
            throw new IllegalArgumentException("Only MP4 files are allowed.");
        }

        Path targetDir = Paths.get(VIDEO_DIR);
        Files.createDirectories(targetDir);

        String uniqueName = UUID.randomUUID() + ".mp4";
        Path targetPath = targetDir.resolve(uniqueName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // return "file:///..." URL
        return targetPath.toUri().toString();
    }
}