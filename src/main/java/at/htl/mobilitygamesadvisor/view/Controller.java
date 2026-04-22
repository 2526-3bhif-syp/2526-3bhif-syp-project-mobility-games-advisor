package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.util.List;
import java.util.function.Consumer;

import at.htl.mobilitygamesadvisor.model.Exercise;

/**
 * VIEW (JavaFX Controller) – purely responsible for rendering.
 * All logic lives in {@link ExercisePresenter}.
 */
public class Controller implements ExerciseView {

    // —— FXML bindings ————————————————————————————————————————————————————————
    @FXML private FlowPane  exerciseGrid;
    @FXML private TextField searchField;

    @FXML private VBox   paneExercises, paneCategories,
            paneTracking,  paneCollection;
    @FXML private Button  btnExercises,  btnCategories,
            btnTracking,   btnCollection;

    // Detail-Pane (programmatisch erzeugt und in den StackPane eingefügt)
    private VBox paneDetail;
    private VideoPlayerView currentPlayer;

    // —— MVP wiring ————————————————————————————————————————————————————————————

    @FXML
    public void initialize() {
        new ExercisePresenter(new ExerciseRepository(), this);
        buildDetailPane();
        showExercises();
    }

    // —— Detail-Pane bauen ————————————————————————————————————————————————————

    private void buildDetailPane() {
        // Den StackPane (Parent von paneExercises) ermitteln
        StackPane contentArea = (StackPane) paneExercises.getParent();

        paneDetail = new VBox(0);
        paneDetail.setVisible(false);
        paneDetail.getStyleClass().add("detail-dialog");
        paneDetail.setStyle("-fx-background-color: #0f1117;");

        contentArea.getChildren().add(paneDetail);
    }

    // —— ExerciseView implementation ——————————————————————————————————————————

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

    // —— Navigation (pure view concern) ———————————————————————————————————————

    @FXML private void showExercises()  { switchPage(paneExercises,  btnExercises);  }
    @FXML private void showCategories() { switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking()   { switchPage(paneTracking,   btnTracking);   }
    @FXML private void showCollection() { switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        // Detail-Pane verstecken und Player stoppen wenn vorhanden
        hideDetailPane();

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

    // —— Card rendering ————————————————————————————————————————————————————————

    private void addExerciseCard(Exercise e) {
        VBox card = new VBox();
        card.getStyleClass().add("exercise-card");
        card.setStyle("-fx-cursor: hand;");

        // —— Video Thumbnail (Placeholder) ————————————————————————————————————
        javafx.scene.layout.StackPane imagePlaceholder = new javafx.scene.layout.StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);

        Label playIcon = new Label("▶ VIDEO");
        playIcon.setStyle(
                "-fx-text-fill: #f5a62360;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;"
        );
        imagePlaceholder.getChildren().add(playIcon);

        imagePlaceholder.setOnMouseClicked(event -> openDetailInPane(e));

        // —— Card Content (unter dem Video) ———————————————————————————————————
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
        content.setOnMouseClicked(event -> openDetailInPane(e));

        card.getChildren().addAll(imagePlaceholder, content);
        exerciseGrid.getChildren().add(card);
    }

    // —— Detail in gleichem Fenster öffnen ————————————————————————————————————

    private void openDetailInPane(Exercise e) {
        // Alten Player stoppen
        disposeCurrentPlayer();

        paneDetail.getChildren().clear();

        // —— Back-Button ——————————————————————————————————————————————————————
        String styleNormal = "-fx-background-color: transparent;" +
                "-fx-text-fill: #f5a623;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #f5a623;" +
                "-fx-border-radius: 6px;" +
                "-fx-padding: 8 16 8 16;";
        String styleHover  = "-fx-background-color: #f5a62322;" +
                "-fx-text-fill: #f5a623;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #f5a623;" +
                "-fx-border-radius: 6px;" +
                "-fx-padding: 8 16 8 16;";

        Button backBtn = new Button("← Zurück zur Übersicht");
        backBtn.getStyleClass().add("video-ctrl-btn");
        backBtn.setStyle(styleNormal);
        backBtn.setOnMouseEntered(ev -> backBtn.setStyle(styleHover));
        backBtn.setOnMouseExited(ev  -> backBtn.setStyle(styleNormal));
        backBtn.setOnAction(ev -> {
            hideDetailPane();
            switchPage(paneExercises, btnExercises);
        });

        HBox backRow = new HBox(backBtn);
        backRow.setPadding(new Insets(28, 40, 8, 40));

        // —— Header ———————————————————————————————————————————————————————————
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label categoryLabel = new Label(e.category());
        categoryLabel.getStyleClass().add("card-tag");

        HBox headerRow = new HBox(12, titleLabel, categoryLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(12, 40, 8, 40));

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 40, 0, 40));

        // —— Beschreibung —————————————————————————————————————————————————————
        Label descSectionTitle = new Label("Beschreibung");
        descSectionTitle.getStyleClass().add("detail-section-title");
        HBox descTitleRow = new HBox(descSectionTitle);
        descTitleRow.setPadding(new Insets(20, 40, 6, 40));

        String descText = (e.desc() != null && !e.desc().isBlank())
                ? e.desc()
                : "Keine Beschreibung vorhanden.";
        Label descContent = new Label(descText);
        descContent.getStyleClass().add("detail-description");
        descContent.setWrapText(true);
        HBox.setHgrow(descContent, Priority.ALWAYS);
        HBox descContentRow = new HBox(descContent);
        descContentRow.setPadding(new Insets(10, 40, 0, 40));

        // —— Video ————————————————————————————————————————————————————————————
        Label videoSectionTitle = new Label("Video");
        videoSectionTitle.getStyleClass().add("detail-section-title");
        HBox videoTitleRow = new HBox(videoSectionTitle);
        videoTitleRow.setPadding(new Insets(10, 40, 8, 40));

        currentPlayer = new VideoPlayerView(e.videoUrl(), 550);

        HBox videoWrapper = new HBox(currentPlayer);
        videoWrapper.setAlignment(Pos.CENTER);
        videoWrapper.setMaxWidth(Double.MAX_VALUE);
        videoWrapper.setPadding(new Insets(0, 40, 32, 40));

        // —— Zusammenbauen ————————————————————————————————————————————————————
        VBox innerLayout = new VBox(0,
                backRow,
                headerRow,
                sep,
                descTitleRow,
                descContentRow,
                videoTitleRow,
                videoWrapper
        );
        innerLayout.setStyle("-fx-background-color: #0f1117;");

        ScrollPane scroll = new ScrollPane(innerLayout);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: #0f1117; -fx-background: #0f1117;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneDetail.getChildren().add(scroll);

        // Alle anderen Panes ausblenden, Detail einblenden
        List.of(paneExercises, paneCategories, paneTracking, paneCollection)
                .forEach(p -> p.setVisible(false));
        paneDetail.setVisible(true);
    }

    private void hideDetailPane() {
        if (paneDetail != null) {
            paneDetail.setVisible(false);
        }
        disposeCurrentPlayer();
    }

    private void disposeCurrentPlayer() {
        if (currentPlayer != null) {
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }
}