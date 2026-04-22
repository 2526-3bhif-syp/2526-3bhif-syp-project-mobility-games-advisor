package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.CategoryRepository;
import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

    private VBox paneDetail;
    private VideoPlayerView currentPlayer;

    private final CategoryRepository categoryRepo   = new CategoryRepository();
    private final ExerciseRepository  exerciseRepo  = new ExerciseRepository();

    // —— MVP wiring ————————————————————————————————————————————————————————————

    @FXML
    public void initialize() {
        new ExercisePresenter(exerciseRepo, this);
        buildDetailPane();
        buildCategoryPane();
        showExercises();
    }

    // —— Detail-Pane bauen ————————————————————————————————————————————————————

    private void buildDetailPane() {
        StackPane contentArea = (StackPane) paneExercises.getParent();
        paneDetail = new VBox(0);
        paneDetail.setVisible(false);
        paneDetail.getStyleClass().add("detail-dialog");
        paneDetail.setStyle("-fx-background-color: #0f1117;");
        contentArea.getChildren().add(paneDetail);
    }

    // —— Kategorie-Pane bauen & befüllen ——————————————————————————————————————

    private void buildCategoryPane() {
        paneCategories.getChildren().clear();
        paneCategories.setSpacing(20);
        paneCategories.setPadding(new Insets(30, 40, 30, 40));

        Label header = new Label("Kategorien verwalten");
        header.getStyleClass().add("header-text");

        Label subtitle = new Label("Erstelle neue Kategorien oder benenne bestehende um.");
        subtitle.getStyleClass().add("header-subtitle");

        Separator sep = new Separator();

        Label newCatLabel = new Label("Neue Kategorie");
        newCatLabel.getStyleClass().add("detail-section-title");

        TextField newCatField = new TextField();
        newCatField.setPromptText("Kategoriename...");
        newCatField.getStyleClass().add("search-field");
        newCatField.setMaxWidth(320);

        Button createBtn = buildActionBtn("+ Erstellen");
        createBtn.setOnAction(ev -> {
            String name = newCatField.getText().trim();
            if (!name.isBlank()) {
                categoryRepo.create(name);
                newCatField.clear();
                buildCategoryPane();
            }
        });

        HBox createRow = new HBox(12, newCatField, createBtn);
        createRow.setAlignment(Pos.CENTER_LEFT);

        Label existingLabel = new Label("Bestehende Kategorien");
        existingLabel.getStyleClass().add("detail-section-title");

        VBox categoryList = new VBox(10);
        List<String> categories = categoryRepo.getAll();

        if (categories.isEmpty()) {
            Label empty = new Label("Noch keine Kategorien vorhanden.");
            empty.getStyleClass().add("card-description");
            categoryList.getChildren().add(empty);
        } else {
            for (String cat : categories) {
                categoryList.getChildren().add(buildCategoryRow(cat));
            }
        }

        ScrollPane listScroll = new ScrollPane(categoryList);
        listScroll.setFitToWidth(true);
        listScroll.getStyleClass().add("transparent-scroll");
        listScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        paneCategories.getChildren().addAll(
                header, subtitle, sep,
                newCatLabel, createRow,
                existingLabel, listScroll
        );
    }

    private HBox buildCategoryRow(String categoryName) {
        int count = categoryRepo.countExercises(categoryName);

        Label nameLabel = new Label(categoryName);
        nameLabel.getStyleClass().add("card-title");
        nameLabel.setMinWidth(180);

        Label countLabel = new Label(count + " Übung" + (count == 1 ? "" : "en"));
        countLabel.getStyleClass().add("card-tag");

        TextField renameField = new TextField(categoryName);
        renameField.getStyleClass().add("search-field");
        renameField.setPrefWidth(200);
        renameField.setVisible(false);
        renameField.setManaged(false);

        Button renameBtn = buildSecondaryBtn("✏ Umbenennen");
        Button saveBtn   = buildActionBtn("✔ Speichern");
        Button cancelBtn = buildSecondaryBtn("✖ Abbrechen");
        saveBtn.setVisible(false);   saveBtn.setManaged(false);
        cancelBtn.setVisible(false); cancelBtn.setManaged(false);

        // Löschen-Button mit Bestätigungsschritt
        Button deleteBtn    = buildDeleteBtn("🗑 Löschen");
        Button confirmBtn   = buildDeleteBtn("⚠ Bestätigen");
        Button abortBtn     = buildSecondaryBtn("Abbrechen");
        confirmBtn.setVisible(false); confirmBtn.setManaged(false);
        abortBtn.setVisible(false);   abortBtn.setManaged(false);

        deleteBtn.setOnAction(ev -> {
            // Umbenennen-Buttons ausblenden, Bestätigung einblenden
            renameBtn.setVisible(false);  renameBtn.setManaged(false);
            deleteBtn.setVisible(false);  deleteBtn.setManaged(false);
            confirmBtn.setVisible(true);  confirmBtn.setManaged(true);
            abortBtn.setVisible(true);    abortBtn.setManaged(true);
        });

        confirmBtn.setOnAction(ev -> {
            categoryRepo.delete(categoryName);
            buildCategoryPane();
        });

        abortBtn.setOnAction(ev -> {
            confirmBtn.setVisible(false); confirmBtn.setManaged(false);
            abortBtn.setVisible(false);   abortBtn.setManaged(false);
            renameBtn.setVisible(true);   renameBtn.setManaged(true);
            deleteBtn.setVisible(true);   deleteBtn.setManaged(true);
        });

        renameBtn.setOnAction(ev -> {
            nameLabel.setVisible(false);  nameLabel.setManaged(false);
            renameField.setVisible(true); renameField.setManaged(true);
            renameBtn.setVisible(false);  renameBtn.setManaged(false);
            deleteBtn.setVisible(false);  deleteBtn.setManaged(false);
            saveBtn.setVisible(true);     saveBtn.setManaged(true);
            cancelBtn.setVisible(true);   cancelBtn.setManaged(true);
            renameField.requestFocus();
            renameField.selectAll();
        });

        saveBtn.setOnAction(ev -> {
            String newName = renameField.getText().trim();
            if (!newName.isBlank() && !newName.equals(categoryName)) {
                categoryRepo.rename(categoryName, newName);
                buildCategoryPane();
            } else {
                cancelBtn.fire();
            }
        });

        cancelBtn.setOnAction(ev -> {
            renameField.setText(categoryName);
            renameField.setVisible(false);  renameField.setManaged(false);
            nameLabel.setVisible(true);     nameLabel.setManaged(true);
            saveBtn.setVisible(false);      saveBtn.setManaged(false);
            cancelBtn.setVisible(false);    cancelBtn.setManaged(false);
            renameBtn.setVisible(true);     renameBtn.setManaged(true);
            deleteBtn.setVisible(true);     deleteBtn.setManaged(true);
        });

        HBox row = new HBox(12, nameLabel, renameField, countLabel, renameBtn, deleteBtn, saveBtn, cancelBtn, confirmBtn, abortBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #1a1d27; -fx-background-radius: 8px;");
        return row;
    }

    // —— Button-Hilfsmethoden —————————————————————————————————————————————————

    private Button buildActionBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("action-button");
        return btn;
    }

    private Button buildDeleteBtn(String text) {
        String normal = "-fx-background-color: transparent;" +
                "-fx-text-fill: #e05252;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #e05252;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        String hover  = "-fx-background-color: #e0525222;" +
                "-fx-text-fill: #e05252;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #e05252;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        Button btn = new Button(text);
        btn.setStyle(normal);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev  -> btn.setStyle(normal));
        return btn;
    }

    private Button buildSecondaryBtn(String text) {
        String normal = "-fx-background-color: transparent;" +
                "-fx-text-fill: #f5a623;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #f5a623;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        String hover  = "-fx-background-color: #f5a62322;" +
                "-fx-text-fill: #f5a623;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #f5a623;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        Button btn = new Button(text);
        btn.setStyle(normal);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev  -> btn.setStyle(normal));
        return btn;
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

    // —— Navigation ———————————————————————————————————————————————————————————

    @FXML private void showExercises()  { switchPage(paneExercises,  btnExercises);  }
    @FXML private void showCategories() { buildCategoryPane(); switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking()   { switchPage(paneTracking,   btnTracking);   }
    @FXML private void showCollection() { switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
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

        javafx.scene.layout.StackPane imagePlaceholder = new javafx.scene.layout.StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);

        Label playIcon = new Label("▶ VIDEO");
        playIcon.setStyle("-fx-text-fill: #f5a62360;-fx-font-size: 22px;-fx-font-weight: bold;");
        imagePlaceholder.getChildren().add(playIcon);
        imagePlaceholder.setOnMouseClicked(event -> openDetailInPane(e));

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
        disposeCurrentPlayer();
        paneDetail.getChildren().clear();

        String styleNormal = "-fx-background-color: transparent;-fx-text-fill: #f5a623;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #f5a623;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";
        String styleHover  = "-fx-background-color: #f5a62322;-fx-text-fill: #f5a623;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #f5a623;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";

        Button backBtn = new Button("← Zurück zur Übersicht");
        backBtn.getStyleClass().add("video-ctrl-btn");
        backBtn.setStyle(styleNormal);
        backBtn.setOnMouseEntered(ev -> backBtn.setStyle(styleHover));
        backBtn.setOnMouseExited(ev  -> backBtn.setStyle(styleNormal));
        backBtn.setOnAction(ev -> { hideDetailPane(); switchPage(paneExercises, btnExercises); });

        HBox backRow = new HBox(backBtn);
        backRow.setPadding(new Insets(28, 40, 8, 40));

        // —— Titel + Kategorie-Badge ———————————————————————————————————————————
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label categoryBadge = new Label(e.category());
        categoryBadge.getStyleClass().add("card-tag");

        HBox headerRow = new HBox(12, titleLabel, categoryBadge);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(12, 40, 8, 40));

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(0, 40, 0, 40));

        // —— Kategorie zuweisen ———————————————————————————————————————————————
        Label catSectionTitle = new Label("Kategorie zuweisen");
        catSectionTitle.getStyleClass().add("detail-section-title");
        HBox catTitleRow = new HBox(catSectionTitle);
        catTitleRow.setPadding(new Insets(20, 40, 6, 40));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categoryRepo.getAll());
        categoryCombo.setValue(e.category());
        categoryCombo.setStyle(
                "-fx-background-color: #1a1d27;" +
                        "-fx-text-fill: #e0e0e0;" +
                        "-fx-border-color: #2e3347;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-pref-width: 240px;"
        );

        // Feedback-Label (wird nach dem Speichern kurz angezeigt)
        Label savedLabel = new Label("✔ Gespeichert");
        savedLabel.setStyle("-fx-text-fill: #4caf50;-fx-font-size: 13px;");
        savedLabel.setVisible(false);

        Button assignBtn = buildActionBtn("Zuweisen");
        assignBtn.setOnAction(ev -> {
            String selected = categoryCombo.getValue();
            if (selected != null && !selected.isBlank()) {
                exerciseRepo.updateCategory(e.title(), selected);
                // Badge im Header aktualisieren
                categoryBadge.setText(selected);
                savedLabel.setVisible(true);
                // Feedback nach 2 Sekunden ausblenden
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> savedLabel.setVisible(false));
                }).start();
            }
        });

        HBox assignRow = new HBox(12, categoryCombo, assignBtn, savedLabel);
        assignRow.setAlignment(Pos.CENTER_LEFT);
        assignRow.setPadding(new Insets(0, 40, 0, 40));

        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(16, 40, 0, 40));

        // —— Beschreibung —————————————————————————————————————————————————————
        Label descSectionTitle = new Label("Beschreibung");
        descSectionTitle.getStyleClass().add("detail-section-title");
        HBox descTitleRow = new HBox(descSectionTitle);
        descTitleRow.setPadding(new Insets(16, 40, 6, 40));

        String descText = (e.desc() != null && !e.desc().isBlank())
                ? e.desc() : "Keine Beschreibung vorhanden.";
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
                backRow, headerRow, sep1,
                catTitleRow, assignRow, sep2,
                descTitleRow, descContentRow,
                videoTitleRow, videoWrapper
        );
        innerLayout.setStyle("-fx-background-color: #0f1117;");

        ScrollPane scroll = new ScrollPane(innerLayout);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: #0f1117; -fx-background: #0f1117;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneDetail.getChildren().add(scroll);

        List.of(paneExercises, paneCategories, paneTracking, paneCollection)
                .forEach(p -> p.setVisible(false));
        paneDetail.setVisible(true);
    }

    private void hideDetailPane() {
        if (paneDetail != null) paneDetail.setVisible(false);
        disposeCurrentPlayer();
    }

    private void disposeCurrentPlayer() {
        if (currentPlayer != null) {
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }
}