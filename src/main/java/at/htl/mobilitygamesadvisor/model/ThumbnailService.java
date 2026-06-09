package at.htl.mobilitygamesadvisor.model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ThumbnailService {

    private static final Path THUMBNAILS_DIR = Path.of("data/thumbnails");
    private static final Set<String> inProgress = ConcurrentHashMap.newKeySet();

    // Single thread: avoids concurrent MediaPlayerFactory/ServiceLoader crashes
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "thumbnail-generator");
        t.setDaemon(true);
        return t;
    });

    public static void loadAsync(String videoUrl, Consumer<Image> onDone) {
        File cached = getCacheFile(videoUrl);
        if (cached.exists()) {
            // Load at 2x display size so downscaling to 220px looks sharp
            Platform.runLater(() -> onDone.accept(
                    new Image(cached.toURI().toString(), 440, 9999, true, true)));
            return;
        }

        if (!inProgress.add(videoUrl)) return;

        executor.submit(() -> {
            try {
                generate(videoUrl, cached);
            } finally {
                inProgress.remove(videoUrl);
            }
            if (cached.exists()) {
                Platform.runLater(() -> onDone.accept(
                        new Image(cached.toURI().toString(), 440, 9999, true, true)));
            }
        });
    }

    // http://localhost:8081/videos/BalloonGame.mp4 → data/videos/BalloonGame.mp4
    public static String toLocalPath(String videoUrl) {
        String prefix = "http://localhost:8081/videos/";
        if (videoUrl.startsWith(prefix)) {
            String encoded = videoUrl.substring(prefix.length());
            try {
                return "data/videos/" + URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
            return "data/videos/" + encoded;
        }
        return videoUrl;
    }

    private static File getCacheFile(String videoUrl) {
        try { Files.createDirectories(THUMBNAILS_DIR); } catch (IOException ignored) {}
        String hash = Integer.toHexString(Math.abs(videoUrl.hashCode()));
        return THUMBNAILS_DIR.resolve(hash + ".jpg").toFile();
    }

    private static void generate(String videoUrl, File outFile) {
        String localPath = toLocalPath(videoUrl);
        if (!new File(localPath).exists()) return;

        // Create ImageView on FX thread (same surface type as hover preview – proven to work)
        CountDownLatch ivLatch = new CountDownLatch(1);
        ImageView[] ivRef = {null};
        Platform.runLater(() -> {
            ivRef[0] = new ImageView();
            ivLatch.countDown();
        });
        try { ivLatch.await(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

        ImageView imageView = ivRef[0];
        MediaPlayerFactory factory = new MediaPlayerFactory(
                "--no-audio", "--avcodec-hw=none", "--demux=avformat", "--avcodec-threads=1");
        EmbeddedMediaPlayer player = factory.mediaPlayers().newEmbeddedMediaPlayer();
        player.videoSurface().set(new ImageViewVideoSurface(imageView));

        player.media().play(localPath);

        // Wait for video to decode and render first frames
        try { Thread.sleep(800); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        player.controls().pause();

        // Wait for pause and any pending Platform.runLater frame-writes to settle
        try { Thread.sleep(150); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Read pixels on FX thread – guaranteed to run after all pending frame-writes
        CountDownLatch captureLatch = new CountDownLatch(1);
        int[][] pixRef = {null};
        int[] wRef = {0}, hRef = {0};
        Platform.runLater(() -> {
            try {
                Image img = imageView.getImage();
                if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
                    int w = (int) img.getWidth();
                    int h = (int) img.getHeight();
                    int[] pixels = new int[w * h];
                    img.getPixelReader().getPixels(
                            0, 0, w, h, PixelFormat.getIntArgbInstance(), pixels, 0, w);
                    pixRef[0] = pixels;
                    wRef[0] = w;
                    hRef[0] = h;
                }
            } finally {
                captureLatch.countDown();
            }
        });
        try { captureLatch.await(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        player.controls().stop();
        player.release();
        factory.release();

        if (pixRef[0] != null) {
            saveJpeg(pixRef[0], wRef[0], hRef[0], outFile);
        }
    }

    private static void saveJpeg(int[] argb, int w, int h, File outFile) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < argb.length; i++) {
            data[i] = argb[i] & 0x00FFFFFF;
        }
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.92f);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outFile)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(bi, null, null), param);
            }
            writer.dispose();
        } catch (IOException e) {
            System.err.println("ThumbnailService: could not save " + outFile + ": " + e.getMessage());
        }
    }
}
