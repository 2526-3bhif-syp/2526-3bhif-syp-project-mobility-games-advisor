package at.htl.mobilitygamesadvisor.view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class VideoPlayerView extends VBox {

    private MediaPlayer player;

    public VideoPlayerView(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            getChildren().add(new javafx.scene.control.Label("Kein Video verfügbar."));
            return;
        }

        Media media       = new Media(videoUrl);
        player            = new MediaPlayer(media);
        MediaView view    = new MediaView(player);
        view.setFitWidth(640);
        view.setPreserveRatio(true);

        Button playPause = new Button("▶ Play");
        playPause.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPause.setText("▶ Play");
            } else {
                player.play();
                playPause.setText("⏸ Pause");
            }
        });

        getChildren().addAll(view, playPause);
    }

    /** Call this when closing the view to free resources. */
    public void dispose() {
        if (player != null) player.dispose();
    }
}