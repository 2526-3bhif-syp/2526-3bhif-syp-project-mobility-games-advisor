package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import at.htl.mobilitygamesadvisor.model.Exercise;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 * VIEW (JavaFX Controller) — purely responsible for rendering.
 * All logic lives in {@link ExercisePresenter}.
 */
public class Controller implements ExerciseView {

    // ── FXML bindings ────────────────────────────────────────────────────────
    @FXML private FlowPane  exerciseGrid;
    @FXML private TextField searchField;

    @FXML private VBox   paneExercises, paneCategories,
            paneTracking,  paneCollection;
    @FXML private Button  btnExercises,  btnCategories,
            btnTracking,   btnCollection;

    // ── MVP wiring ───────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Construct model and presenter; presenter wires itself to this view
        new ExercisePresenter(new ExerciseRepository(), this);

        showExercises();
    }

    // ── ExerciseView implementation ──────────────────────────────────────────

    @Override
    public void showExercises(List<Exercise> exercises) {
        if (exerciseGrid == null) return;
        exerciseGrid.getChildren().clear();
        exercises.forEach(this::addExerciseCard);
    }

    @Override
    public void setOnSearchChanged(Consumer<String> listener) {
        if (searchField != null) {
            searchField.textProperty().addListener(
                    (obs, oldVal, newVal) -> listener.accept(newVal));
        }
    }

    // ── Navigation (pure view concern) ───────────────────────────────────────

    @FXML private void showExercises()  { switchPage(paneExercises,  btnExercises);  }
    @FXML private void showCategories() { switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking()   { switchPage(paneTracking,   btnTracking);   }
    @FXML private void showCollection() { switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        List.of(paneExercises, paneCategories, paneTracking, paneCollection)
                .forEach(p -> p.setVisible(false));
        activePane.setVisible(true);

        resetButtonStyles();
        activeButton.getStyleClass().add("active-nav");
    }

    private void resetButtonStyles() {
        List.of(btnExercises, btnCategories, btnTracking, btnCollection)
                .forEach(b -> b.getStyleClass().remove("active-nav"));
    }

    // ── Card rendering ────────────────────────────────────────────────────────

    private void addExerciseCard(Exercise e) {
        VBox card = new VBox();
        card.getStyleClass().add("exercise-card");

        // Thumbnail statt "▶ VIDEO" Text
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);

        if (e.videoUrl() != null && !e.videoUrl().isBlank()) {
            try {
                Media media = new Media(e.videoUrl());
                MediaPlayer player = new MediaPlayer(media);

                // Erstes Frame laden und dann sofort pausieren
                player.setAutoPlay(false);
                player.seek(Duration.ZERO);
                player.pause();

                MediaView thumbnail = new MediaView(player);
                thumbnail.setFitWidth(220);
                thumbnail.setFitHeight(120);
                thumbnail.setPreserveRatio(false);

                // Play-Icon Overlay
                Label playIcon = new Label("▶");
                playIcon.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-font-size: 28px;" +
                                "-fx-effect: dropshadow(gaussian, black, 8, 0, 0, 0);"
                );

                imagePlaceholder.getChildren().addAll(thumbnail, playIcon);
            } catch (Exception ex) {
                imagePlaceholder.getChildren().add(new Label("▶ VIDEO"));
            }
        } else {
            imagePlaceholder.getChildren().add(new Label("▶ VIDEO"));
        }

        imagePlaceholder.setOnMouseClicked(event -> openVideo(e.videoUrl()));

        // Klick auf die Karte → Video öffnen
        imagePlaceholder.setOnMouseClicked(event -> openVideo(e.videoUrl()));

        VBox content = new VBox(8);
        content.getStyleClass().add("card-content");

        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("card-title");

        Label descLabel = new Label(e.desc());
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);

        Label tagLabel = new Label(e.category());
        tagLabel.getStyleClass().add("card-tag");

        content.getChildren().addAll(titleLabel, descLabel, tagLabel);
        card.getChildren().addAll(imagePlaceholder, content);
        exerciseGrid.getChildren().add(card);
    }

    private void openVideo(String videoUrl) {
        VideoPlayerView player = new VideoPlayerView(videoUrl);

        Stage videoStage = new Stage();
        videoStage.setTitle("Video");
        videoStage.setScene(new Scene(player, 660, 400));
        videoStage.setOnCloseRequest(e -> player.dispose()); // Ressourcen freigeben
        videoStage.show();
    }
}