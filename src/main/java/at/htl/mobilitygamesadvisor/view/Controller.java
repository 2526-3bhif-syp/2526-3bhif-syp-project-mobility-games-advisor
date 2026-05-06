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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import at.htl.mobilitygamesadvisor.model.Exercise;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

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
    private final List<Exercise>      collection    = new ArrayList<>();

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
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;" +
                "-fx-border-color: #e0ece8; -fx-border-radius: 8px; -fx-border-width: 1;");
        return row;
    }

    // —— Sammelmappe-Pane bauen ———————————————————————————————————————————————

    private void buildCollectionPane() {
        paneCollection.getChildren().clear();
        paneCollection.setSpacing(20);
        paneCollection.setPadding(new Insets(30, 40, 30, 40));

        Label header = new Label("Sammelmappe / Tagesplan");
        header.getStyleClass().add("header-text");

        Label subtitle = new Label("Übungen für die heutige Sitzung");
        subtitle.getStyleClass().add("header-subtitle");

        Separator sep = new Separator();

        if (collection.isEmpty()) {
            Label empty = new Label("Deine Sammelmappe ist noch leer. Füge Übungen über die Übersicht hinzu.");
            empty.getStyleClass().add("card-description");
            empty.setWrapText(true);

            Button goToExercises = buildActionBtn("→ Zu den Übungen");
            goToExercises.setOnAction(ev -> showExercises());

            paneCollection.getChildren().addAll(header, subtitle, sep, empty, goToExercises);
            return;
        }

        Label countLabel = new Label(collection.size() + " Übung" + (collection.size() == 1 ? "" : "en") + " in der Sammelmappe");
        countLabel.getStyleClass().add("card-description");

        Button clearBtn = buildDeleteBtn("🗑 Sammelmappe leeren");
        clearBtn.setOnAction(ev -> {
            collection.clear();
            buildCollectionPane();
            showExercises(exerciseRepo.search(searchField.getText()));
        });

        HBox topRow = new HBox(12, countLabel, clearBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox list = new VBox(10);
        for (Exercise e : collection) {
            list.getChildren().add(buildCollectionRow(e));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneCollection.getChildren().addAll(header, subtitle, sep, topRow, scroll);
    }

    private HBox buildCollectionRow(Exercise e) {
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setMinWidth(200);

        Label tagLabel = new Label(e.category());
        tagLabel.getStyleClass().add("card-tag");

        Button openBtn = buildSecondaryBtn("▶ Abspielen");
        openBtn.setOnAction(ev -> openDetailInPane(e, paneCollection, btnCollection));

        Button removeBtn = buildDeleteBtn("✖ Entfernen");
        removeBtn.setOnAction(ev -> {
            collection.remove(e);
            buildCollectionPane();
            showExercises(exerciseRepo.search(searchField.getText()));
        });

        HBox row = new HBox(12, titleLabel, tagLabel, openBtn, removeBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;" +
                "-fx-border-color: #e0ece8; -fx-border-radius: 8px; -fx-border-width: 1;");
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
                "-fx-text-fill: #c0392b;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #c0392b;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        String hover  = "-fx-background-color: #c0392b18;" +
                "-fx-text-fill: #c0392b;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #c0392b;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        Button btn = new Button(text);
        btn.setStyle(normal);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev  -> btn.setStyle(normal));
        return btn;
    }
    private Button buildSecondaryBtn(String text) {
        String normal = "-fx-background-color: transparent;" +
                "-fx-text-fill: #2d7a5c;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #2d7a5c;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
        String hover  = "-fx-background-color: #2d7a5c18;" +
                "-fx-text-fill: #2d7a5c;-fx-font-size: 13px;-fx-cursor: hand;" +
                "-fx-border-color: #2d7a5c;-fx-border-radius: 6px;-fx-padding: 6 14 6 14;";
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
    @FXML private void showCollection() { buildCollectionPane(); switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        hideDetailPane();
        if (activePane == paneExercises) {
            showExercises(exerciseRepo.search(searchField.getText()));
        }
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

        imagePlaceholder.setOnMouseClicked(event -> openDetailInPane(e, paneExercises, btnExercises));
        VBox content = new VBox(8);
        content.getStyleClass().add("card-content");

        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("card-title");

        Label descLabel = new Label(e.desc());
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);

        Label tagLabel = new Label(e.category());
        tagLabel.getStyleClass().add("card-tag");

        String cardBtnNormal = "-fx-background-color: transparent;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 11px;-fx-cursor: hand;-fx-padding: 4 8 4 8;" +
                "-fx-border-color: #2d7a5c;-fx-border-radius: 6px;-fx-border-width: 1;";
        String cardBtnAdded = "-fx-background-color: #e8f4ef;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 11px;-fx-cursor: hand;-fx-padding: 4 8 4 8;" +
                "-fx-border-color: #5cad8a;-fx-border-radius: 6px;-fx-border-width: 1;";
        boolean cardInitAdded = collection.stream().anyMatch(ex -> ex.title().equals(e.title()));
        Button addBtn = new Button(cardInitAdded ? "✔ Entfernen" : "+ Sammelmappe");
        addBtn.setStyle(cardInitAdded ? cardBtnAdded : cardBtnNormal);
        addBtn.setOnAction(ev -> {
            boolean inCollection = collection.stream().anyMatch(ex -> ex.title().equals(e.title()));
            if (inCollection) {
                collection.removeIf(ex -> ex.title().equals(e.title()));
                addBtn.setText("+ Sammelmappe");
                addBtn.setStyle(cardBtnNormal);
            } else {
                collection.add(e);
                addBtn.setText("✔ Entfernen");
                addBtn.setStyle(cardBtnAdded);
            }
        });

        content.getChildren().addAll(titleLabel, descLabel, tagLabel, addBtn);
        card.getChildren().addAll(imagePlaceholder, content);
        exerciseGrid.getChildren().add(card);
    }
    // —— Detail in gleichem Fenster öffnen ————————————————————————————————————

    private void openDetailInPane(Exercise e, VBox returnPane, Button returnBtn) {
        disposeCurrentPlayer();
        paneDetail.getChildren().clear();

        String styleNormal = "-fx-background-color: transparent;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #2d7a5c;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";
        String styleHover  = "-fx-background-color: #2d7a5c18;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #2d7a5c;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";

        Button backBtn = new Button("← Zurück zur Übersicht");
        backBtn.getStyleClass().add("video-ctrl-btn");
        backBtn.setStyle(styleNormal);
        backBtn.setOnMouseEntered(ev -> backBtn.setStyle(styleHover));
        backBtn.setOnMouseExited(ev  -> backBtn.setStyle(styleNormal));
        backBtn.setOnAction(ev -> {
            hideDetailPane();
            if (returnPane == paneCollection) buildCollectionPane();
            switchPage(returnPane, returnBtn);
        });

        HBox backRow = new HBox(backBtn);
        backRow.setPadding(new Insets(28, 40, 8, 40));

        // —— Titel + Kategorie-Badge ———————————————————————————————————————————
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label categoryBadge = new Label(e.category());
        categoryBadge.getStyleClass().add("card-tag");

        String detailBtnAdded = "-fx-background-color: #5cad8a;-fx-text-fill: #ffffff;" +
                "-fx-font-size: 13px;-fx-padding: 10 20 10 20;" +
                "-fx-background-radius: 10;-fx-cursor: hand;";
        boolean detailInitAdded = collection.stream().anyMatch(ex -> ex.title().equals(e.title()));
        Button collectionBtn = buildActionBtn(detailInitAdded ? "✔ Entfernen" : "+ Zur Sammelmappe");
        if (detailInitAdded) collectionBtn.setStyle(detailBtnAdded);
        collectionBtn.setOnAction(ev -> {
            boolean inCollection = collection.stream().anyMatch(ex -> ex.title().equals(e.title()));
            if (inCollection) {
                collection.removeIf(ex -> ex.title().equals(e.title()));
                collectionBtn.setText("+ Zur Sammelmappe");
                collectionBtn.setStyle("");
            } else {
                collection.add(e);
                collectionBtn.setText("✔ Entfernen");
                collectionBtn.setStyle(detailBtnAdded);
            }
        });

        HBox headerRow = new HBox(12, titleLabel, categoryBadge, collectionBtn);
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
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #1a2e2a;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-pref-width: 240px;"
        );

        // Feedback-Label (wird nach dem Speichern kurz angezeigt)
        Label savedLabel = new Label("✔ Gespeichert");
        savedLabel.setStyle("-fx-text-fill: #2d7a5c;-fx-font-size: 13px;");
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
        // innerLayout background:
        innerLayout.setStyle("-fx-background-color: #f4f6f5;");

        ScrollPane scroll = new ScrollPane(innerLayout);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: #f4f6f5; -fx-background: #f4f6f5;");
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