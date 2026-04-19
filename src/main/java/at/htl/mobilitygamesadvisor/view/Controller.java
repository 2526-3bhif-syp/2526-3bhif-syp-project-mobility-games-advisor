package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

import at.htl.mobilitygamesadvisor.model.Exercise;
import javafx.stage.Stage;


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
        card.setStyle("-fx-cursor: hand;");

        // ── Video Thumbnail (Placeholder) ────────────────────────────────────
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);

        Label playIcon = new Label("▶ VIDEO");
        playIcon.setStyle(
                "-fx-text-fill: #f5a62360;" +
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;"
        );
        imagePlaceholder.getChildren().add(playIcon);

        // Klick auf Thumbnail → Detail-Dialog öffnen
        imagePlaceholder.setOnMouseClicked(event -> openDetailDialog(e));

        // ── Card Content (unter dem Video) ───────────────────────────────────
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

        // Klick auf Inhaltsbereich → ebenfalls Detail-Dialog
        content.setOnMouseClicked(event -> openDetailDialog(e));

        card.getChildren().addAll(imagePlaceholder, content);
        exerciseGrid.getChildren().add(card);
    }

    // ── Detail-Dialog ─────────────────────────────────────────────────────────

    /**
     * Opens a large detail window (90 % of screen) with the full exercise
     * description and an embedded VideoPlayerView with all controls.
     */
    private void openDetailDialog(Exercise e) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double dlgW = screen.getWidth()  * 0.90;
        double dlgH = screen.getHeight() * 0.90;

        // Header: title + category badge
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label categoryLabel = new Label(e.category());
        categoryLabel.getStyleClass().add("card-tag");

        HBox headerRow = new HBox(12, titleLabel, categoryLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();

        // Description
        Label descSectionTitle = new Label("Beschreibung");
        descSectionTitle.getStyleClass().add("detail-section-title");

        String descText = (e.desc() != null && !e.desc().isBlank())
                ? e.desc()
                : "Keine Beschreibung vorhanden.";
        Label descContent = new Label(descText);
        descContent.getStyleClass().add("detail-description");
        descContent.setWrapText(true);
        descContent.setMaxWidth(Double.MAX_VALUE);

        // Video
        Label videoSectionTitle = new Label("Video");
        videoSectionTitle.getStyleClass().add("detail-section-title");

        // VideoPlayerView noch kleiner machen (maximal 550px Breite)
        double videoW = Math.min(dlgW - 100, 550);
        VideoPlayerView playerView = new VideoPlayerView(e.videoUrl(), videoW);

        // Den gesamten VideoPlayer zentrieren
        HBox videoWrapper = new HBox(playerView);
        videoWrapper.setAlignment(Pos.CENTER);
        videoWrapper.setMaxWidth(Double.MAX_VALUE);

        // Assemble layout
        VBox layout = new VBox(18,
                headerRow, sep,
                descSectionTitle, descContent,
                videoSectionTitle, videoWrapper);
        layout.getStyleClass().add("detail-dialog");
        layout.setPadding(new Insets(28, 32, 28, 32));

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: #0f1117; -fx-background: #0f1117;");

        Stage dialog = new Stage();
        dialog.setTitle(e.title() + " – Details");

        Scene scene = new Scene(scroll, dlgW, dlgH);
        scene.getStylesheets().add(
                getClass().getResource("/at/htl/mobilitygamesadvisor/style.css").toExternalForm());
        dialog.setScene(scene);

        // WICHTIG: Min-Size erzwingen, damit Linux/Wayland das Fenster beim 2. Mal nicht verkleinert
        dialog.setMinWidth(dlgW);
        dialog.setMinHeight(dlgH);

        dialog.setWidth(dlgW);
        dialog.setHeight(dlgH);

        dialog.setOnCloseRequest(ev -> playerView.dispose());

        dialog.show();

        // Hack für manche Window-Manager: Größe nach dem Rendern nochmal fixieren + zentrieren
        dialog.setWidth(dlgW);
        dialog.setHeight(dlgH);
        dialog.centerOnScreen();
    }
}