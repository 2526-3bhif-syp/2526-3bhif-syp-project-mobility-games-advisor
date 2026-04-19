package at.htl.mobilitygamesadvisor.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayerView extends VBox {

    private MediaPlayer player;

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

        Media media = new Media(videoUrl);
        player = new MediaPlayer(media);

        MediaView view = new MediaView(player);
        view.setFitWidth(width);
        view.setPreserveRatio(true);

        // ── Timeline Slider ─────────────────────────────────────────────────
        Slider timeline = new Slider(0, 1, 0);
        timeline.setMaxWidth(Double.MAX_VALUE);
        timeline.getStyleClass().add("video-slider");

        Label timeLabel = new Label("0:00 / 0:00");
        timeLabel.getStyleClass().add("video-time-label");

        player.currentTimeProperty().addListener((obs, old, now) -> {
            Duration total = player.getTotalDuration();
            if (total != null && !total.isUnknown() && total.toMillis() > 0) {
                timeline.setValue(now.toMillis() / total.toMillis());
                timeLabel.setText(formatDuration(now) + " / " + formatDuration(total));
            }
        });

        // Allow user to scrub
        timeline.setOnMousePressed(e -> player.pause());
        timeline.setOnMouseReleased(e -> {
            Duration total = player.getTotalDuration();
            if (total != null) {
                player.seek(Duration.millis(timeline.getValue() * total.toMillis()));
                player.play();
            }
        });

        // ── Control Buttons ──────────────────────────────────────────────────
        Button rewindBtn = new Button("⏪ -10s");
        rewindBtn.getStyleClass().add("video-ctrl-btn");
        rewindBtn.setOnAction(e -> {
            Duration current = player.getCurrentTime();
            player.seek(current.subtract(Duration.seconds(10)));
        });

        Button playPauseBtn = new Button("▶ Play");
        playPauseBtn.getStyleClass().add("video-ctrl-btn-primary");
        playPauseBtn.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPauseBtn.setText("▶ Play");
            } else {
                player.play();
                playPauseBtn.setText("⏸ Pause");
            }
        });

        Button forwardBtn = new Button("+10s ⏩");
        forwardBtn.getStyleClass().add("video-ctrl-btn");
        forwardBtn.setOnAction(e -> {
            Duration current = player.getCurrentTime();
            player.seek(current.add(Duration.seconds(10)));
        });

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox controls = new HBox(10, rewindBtn, spacer1, playPauseBtn, spacer2, forwardBtn);
        controls.setAlignment(Pos.CENTER);
        controls.getStyleClass().add("video-controls");

        HBox timeRow = new HBox(timeLabel);
        timeRow.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(view, timeline, controls, timeRow);
    }

    private String formatDuration(Duration d) {
        int totalSec = (int) d.toSeconds();
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    /** Call this when closing the view to free resources. */
    public void dispose() {
        if (player != null) player.dispose();
    }
}