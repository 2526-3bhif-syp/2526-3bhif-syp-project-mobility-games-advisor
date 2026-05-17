package at.htl.mobilitygamesadvisor.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;

public class VideoPlayerView extends VBox {

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    public VideoPlayerView(String videoUrl) {
        this(videoUrl, 640, 480);
    }

    public VideoPlayerView(String videoUrl, double fitWidth, double fitHeight) {
        setSpacing(0);
        setPadding(new Insets(0));

        if (videoUrl == null || videoUrl.isBlank()) {
            getChildren().add(new Label("Kein Video verfügbar."));
            return;
        }

        // avformat demuxer handles fragmented MP4 seeking correctly; VLC's native mp4 demuxer
        // produces "fragment sequence discontinuity" errors. Single-threaded decoding avoids
        // "thread_get_buffer() failed" and "co located POCs unavailable" errors after seeks.
        mediaPlayerFactory = new MediaPlayerFactory("--avcodec-hw=none", "--demux=avformat", "--avcodec-threads=1");
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        ImageView videoImageView = new ImageView();
        videoImageView.setFitWidth(fitWidth);
        videoImageView.setFitHeight(fitHeight);
        videoImageView.setPreserveRatio(true);
        mediaPlayer.videoSurface().set(new ImageViewVideoSurface(videoImageView));

        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(60, 60);

        // ── Hover overlay: -10s (bottom-left) and +10s (bottom-right) ───────
        Button rewindBtn = new Button("⏪  -10s");
        rewindBtn.getStyleClass().add("video-hover-btn");
        rewindBtn.setVisible(false);
        rewindBtn.setOnAction(ev -> seekBy(-10000));

        Button forwardBtn = new Button("+10s  ⏩");
        forwardBtn.getStyleClass().add("video-hover-btn");
        forwardBtn.setVisible(false);
        forwardBtn.setOnAction(ev -> seekBy(10000));

        StackPane.setAlignment(rewindBtn, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(forwardBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(rewindBtn, new Insets(0, 0, 14, 14));
        StackPane.setMargin(forwardBtn, new Insets(0, 14, 14, 0));

        // ── Restart-Button (erscheint wenn Video zu Ende) ────────────────────
        Button restartBtn = new Button("↺");
        restartBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.55); -fx-text-fill: white;" +
                "-fx-font-size: 40px; -fx-cursor: hand;" +
                "-fx-background-radius: 50%; -fx-padding: 12 18 12 18; -fx-border-width: 0;");
        restartBtn.setVisible(false);
        StackPane.setAlignment(restartBtn, Pos.CENTER);

        // ── Video area with rounded corners ──────────────────────────────────
        StackPane videoArea = new StackPane(videoImageView, loadingSpinner, rewindBtn, forwardBtn, restartBtn);

        restartBtn.setOnAction(ev -> {
            restartBtn.setVisible(false);
            mediaPlayer.controls().setTime(0);
            mediaPlayer.controls().play();
            if (videoArea.isHover()) {
                rewindBtn.setVisible(true);
                forwardBtn.setVisible(true);
            }
        });

        // Bind clip directly to the videoArea's actual rendered size → true rounded corners
        Rectangle clip = new Rectangle();
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        clip.widthProperty().bind(videoArea.widthProperty());
        clip.heightProperty().bind(videoArea.heightProperty());
        videoArea.setClip(clip);

        // Klick aufs Video: wenn fertig → Neustart, sonst play/pause
        videoArea.setOnMouseClicked(ev -> {
            if (restartBtn.isVisible()) {
                restartBtn.setVisible(false);
                mediaPlayer.controls().setTime(0);
                mediaPlayer.controls().play();
                rewindBtn.setVisible(true);
                forwardBtn.setVisible(true);
            } else if (mediaPlayer.status().isPlaying()) {
                mediaPlayer.controls().pause();
            } else {
                mediaPlayer.controls().play();
            }
        });

        // Show/hide hover buttons via hoverProperty (stays true while cursor is over any child)
        videoArea.hoverProperty().addListener((obs, wasHovering, isHovering) -> {
            if (!restartBtn.isVisible()) {
                rewindBtn.setVisible(isHovering);
                forwardBtn.setVisible(isHovering);
            }
        });

        // ── Timeline Slider ──────────────────────────────────────────────────
        Slider timeline = new Slider(0, 1, 0);
        timeline.getStyleClass().add("video-slider");
        HBox.setHgrow(timeline, Priority.ALWAYS);

        Label timeLabel = new Label("0:00 / 0:00");
        timeLabel.getStyleClass().add("video-time-label");

        mediaPlayer.events().addMediaPlayerEventListener(new uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter() {
            @Override
            public void timeChanged(MediaPlayer mp, long newTime) {
                Platform.runLater(() -> {
                    long total = mp.status().length();
                    if (total > 0) {
                        timeline.setValue((double) newTime / total);
                        timeLabel.setText(formatDuration(newTime) + " / " + formatDuration(total));
                    }
                });
            }
            @Override
            public void playing(MediaPlayer mp) {
                Platform.runLater(() -> loadingSpinner.setVisible(false));
            }
            @Override
            public void buffering(MediaPlayer mp, float newCache) {
                Platform.runLater(() -> loadingSpinner.setVisible(newCache < 100f));
            }
            @Override
            public void finished(MediaPlayer mp) {
                Platform.runLater(() -> {
                    restartBtn.setVisible(true);
                    rewindBtn.setVisible(false);
                    forwardBtn.setVisible(false);
                    timeline.setValue(1);
                });
            }
            @Override
            public void error(MediaPlayer mediaPlayer) {
                Platform.runLater(() -> loadingSpinner.setVisible(false));
                System.err.println("VLCJ MediaPlayer Error on URL: " + videoUrl);
            }
        });

        // Allow user to scrub
        timeline.setOnMousePressed(e -> mediaPlayer.controls().pause());
        timeline.setOnMouseReleased(e -> {
            long total = mediaPlayer.status().length();
            if (total > 0) {
                mediaPlayer.controls().setTime((long) (timeline.getValue() * total));
                mediaPlayer.controls().play();
            }
        });

        // ── Controls row: slider + time label directly below video ───────────
        HBox controlsRow = new HBox(10, timeline, timeLabel);
        controlsRow.setAlignment(Pos.CENTER_LEFT);
        controlsRow.setPadding(new Insets(8, 4, 4, 4));

        getChildren().addAll(videoArea, controlsRow);

        mediaPlayer.media().play(videoUrl);
    }

    private void seekBy(long deltaMs) {
        boolean wasPaused = !mediaPlayer.status().isPlaying();
        mediaPlayer.controls().skipTime(deltaMs);
        if (wasPaused) {
            mediaPlayer.controls().play();
            new Thread(() -> {
                try { Thread.sleep(80); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                mediaPlayer.controls().pause();
            }).start();
        }
    }

    private String formatDuration(long millis) {
        long totalSec = millis / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    /** Call this when closing the view to free resources. */
    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
    }
}
