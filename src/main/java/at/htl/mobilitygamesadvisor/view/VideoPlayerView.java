package at.htl.mobilitygamesadvisor.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;

public class VideoPlayerView extends VBox {

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    public VideoPlayerView(String videoUrl) {
        this(videoUrl, 640);
    }

    public VideoPlayerView(String videoUrl, double width) {
        setSpacing(12);
        setPadding(new Insets(0));

        if (videoUrl == null || videoUrl.isBlank()) {
            getChildren().add(new Label("Kein Video verfügbar."));
            return;
        }

        // Initialize VLCJ
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        // Use ImageView to display VLC frames in JavaFX
        ImageView videoImageView = new ImageView();
        videoImageView.setFitWidth(width);
        videoImageView.setPreserveRatio(true);
        mediaPlayer.videoSurface().set(new ImageViewVideoSurface(videoImageView));

        // ── Timeline Slider ─────────────────────────────────────────────────
        Slider timeline = new Slider(0, 1, 0);
        timeline.setMaxWidth(Double.MAX_VALUE);
        timeline.getStyleClass().add("video-slider");

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
            public void error(MediaPlayer mediaPlayer) {
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

        // ── Control Buttons ──────────────────────────────────────────────────
        Button rewindBtn = new Button("⏪ -10s");
        rewindBtn.getStyleClass().add("video-ctrl-btn");
        rewindBtn.setOnAction(e -> mediaPlayer.controls().skipTime(-10000));

        Button playPauseBtn = new Button("⏸ Pause");
        playPauseBtn.getStyleClass().add("video-ctrl-btn-primary");
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer.status().isPlaying()) {
                mediaPlayer.controls().pause();
                playPauseBtn.setText("▶ Play");
            } else {
                mediaPlayer.controls().play();
                playPauseBtn.setText("⏸ Pause");
            }
        });

        Button forwardBtn = new Button("+10s ⏩");
        forwardBtn.getStyleClass().add("video-ctrl-btn");
        forwardBtn.setOnAction(e -> mediaPlayer.controls().skipTime(10000));

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox controls = new HBox(10, rewindBtn, spacer1, playPauseBtn, spacer2, forwardBtn);
        controls.setAlignment(Pos.CENTER);
        controls.getStyleClass().add("video-controls");

        HBox timeRow = new HBox(timeLabel);
        timeRow.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(videoImageView, timeline, controls, timeRow);

        // Start playback directly across NGINX docker network (No bypasses!)
        mediaPlayer.media().play(videoUrl);
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