package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.CategoryRepository;
import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.model.Sammlung;
import at.htl.mobilitygamesadvisor.model.SammlungRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.stage.Screen;

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

    // —— NEU: Kategorie-Filter ————————————————————————————————————————————————
    private ComboBox<String> categoryFilter;
    private static final String ALL_CATEGORIES = "Alle Kategorien";

    private final CategoryRepository categoryRepo   = new CategoryRepository();
    private final ExerciseRepository  exerciseRepo  = new ExerciseRepository();
    private final SammlungRepository  sammlungRepo  = new SammlungRepository();
    private Integer openSammlungId = null;

    // —— MVP wiring ————————————————————————————————————————————————————————————

    @FXML
    public void initialize() {
        new ExercisePresenter(exerciseRepo, this);
        buildDetailPane();
        buildCategoryFilterBar();
        buildCategoryPane();
        showExercises();
    }

    // —— Kategorie-Filterleiste bauen (neben Suchfeld) ————————————————————————

    private void buildCategoryFilterBar() {
        // Suchfeld-Parent ist eine HBox in der FXML – wir fügen die ComboBox daneben ein
        HBox searchBar = (HBox) searchField.getParent();

        categoryFilter = new ComboBox<>();
        categoryFilter.setVisibleRowCount(8); // scrollbar ab 8 Einträgen
        categoryFilter.getStyleClass().add("search-field");
        categoryFilter.setPrefWidth(200);
        categoryFilter.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;"
        );

        refreshCategoryFilter();

        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                applyFilter()
        );

        Button clearFilterBtn = buildSecondaryBtn("✖ Filter löschen");
        clearFilterBtn.setOnAction(ev -> {
            categoryFilter.setValue(ALL_CATEGORIES);
        });

        searchBar.getChildren().addAll(categoryFilter, clearFilterBtn);
    }

    /** Kategorien in der ComboBox neu laden (z.B. nach Erstellen einer neuen Kategorie). */
    private void refreshCategoryFilter() {
        String current = categoryFilter.getValue();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add(ALL_CATEGORIES);
        categoryFilter.getItems().addAll(categoryRepo.getAll());
        // Selektion beibehalten falls noch gültig, sonst zurücksetzen
        if (current != null && categoryFilter.getItems().contains(current)) {
            categoryFilter.setValue(current);
        } else {
            categoryFilter.setValue(ALL_CATEGORIES);
        }
    }

    /** Wendet Suchtext UND Kategoriefilter gemeinsam an. */
    private void applyFilter() {
        String query    = searchField.getText();
        String category = categoryFilter.getValue();

        List<Exercise> results = exerciseRepo.search(query);

        if (category != null && !category.equals(ALL_CATEGORIES)) {
            results = results.stream()
                    .filter(e -> category.equals(e.category()))
                    .toList();
        }

        showExercises(results);
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
                refreshCategoryFilter(); // NEU: Filter-Dropdown aktualisieren
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

        Button deleteBtn    = buildDeleteBtn("🗑 Löschen");
        Button confirmBtn   = buildDeleteBtn("⚠ Bestätigen");
        Button abortBtn     = buildSecondaryBtn("Abbrechen");
        confirmBtn.setVisible(false); confirmBtn.setManaged(false);
        abortBtn.setVisible(false);   abortBtn.setManaged(false);

        deleteBtn.setOnAction(ev -> {
            renameBtn.setVisible(false);  renameBtn.setManaged(false);
            deleteBtn.setVisible(false);  deleteBtn.setManaged(false);
            confirmBtn.setVisible(true);  confirmBtn.setManaged(true);
            abortBtn.setVisible(true);    abortBtn.setManaged(true);
        });

        confirmBtn.setOnAction(ev -> {
            categoryRepo.delete(categoryName);
            buildCategoryPane();
            refreshCategoryFilter(); // NEU
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
                refreshCategoryFilter(); // NEU
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
        if (openSammlungId == null) {
            buildSammlungListPane();
        } else {
            buildSammlungDetailPane(openSammlungId);
        }
    }

    private void buildSammlungListPane() {
        paneCollection.getChildren().clear();
        paneCollection.setSpacing(20);
        paneCollection.setPadding(new Insets(30, 40, 30, 40));

        Label header = new Label("Sammelmappen");
        header.getStyleClass().add("header-text");
        Label subtitle = new Label("Erstelle und verwalte Übungssammlungen.");
        subtitle.getStyleClass().add("header-subtitle");
        Separator sep = new Separator();

        TextField titleField = new TextField();
        titleField.setPromptText("Titel der neuen Sammelmappe...");
        titleField.getStyleClass().add("search-field");
        titleField.setPrefWidth(310);
        Button createBtn = buildActionBtn("+ Erstellen");
        createBtn.setOnAction(ev -> {
            String t = titleField.getText().trim();
            if (!t.isBlank()) {
                sammlungRepo.create(t);
                titleField.clear();
                buildCollectionPane();
            }
        });
        HBox createRow = new HBox(12, titleField, createBtn);
        createRow.setAlignment(Pos.CENTER_LEFT);

        List<Sammlung> sammlungen = sammlungRepo.getAll();
        VBox list = new VBox(10);
        if (sammlungen.isEmpty()) {
            Label empty = new Label("Noch keine Sammelmappen vorhanden. Erstelle eine oben.");
            empty.getStyleClass().add("card-description");
            list.getChildren().add(empty);
        } else {
            for (Sammlung s : sammlungen) {
                list.getChildren().add(buildSammlungRow(s));
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneCollection.getChildren().addAll(header, subtitle, sep, createRow, scroll);
    }

    private HBox buildSammlungRow(Sammlung s) {
        int count = sammlungRepo.countExercises(s.id());

        Label titleLabel = new Label(s.title());
        titleLabel.getStyleClass().add("card-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        TextField renameField = new TextField(s.title());
        renameField.getStyleClass().add("search-field");
        renameField.setPrefWidth(220);
        renameField.setVisible(false);
        renameField.setManaged(false);
        HBox.setHgrow(renameField, Priority.ALWAYS);

        Label countLabel = new Label(count + " Übung" + (count == 1 ? "" : "en"));
        countLabel.getStyleClass().add("card-tag");

        Button openBtn    = buildActionBtn("▶ Öffnen");
        Button renameBtn  = buildSecondaryBtn("✏ Umbenennen");
        Button saveBtn    = buildActionBtn("✔ Speichern");
        Button cancelBtn  = buildSecondaryBtn("✖ Abbrechen");
        Button deleteBtn  = buildDeleteBtn("🗑 Löschen");
        Button confirmBtn = buildDeleteBtn("⚠ Bestätigen");
        Button abortBtn   = buildSecondaryBtn("Abbrechen");

        saveBtn.setVisible(false);    saveBtn.setManaged(false);
        cancelBtn.setVisible(false);  cancelBtn.setManaged(false);
        confirmBtn.setVisible(false); confirmBtn.setManaged(false);
        abortBtn.setVisible(false);   abortBtn.setManaged(false);

        openBtn.setOnAction(ev -> { openSammlungId = s.id(); buildCollectionPane(); });

        renameBtn.setOnAction(ev -> {
            titleLabel.setVisible(false);  titleLabel.setManaged(false);
            renameField.setVisible(true);  renameField.setManaged(true);
            openBtn.setVisible(false);     openBtn.setManaged(false);
            renameBtn.setVisible(false);   renameBtn.setManaged(false);
            deleteBtn.setVisible(false);   deleteBtn.setManaged(false);
            saveBtn.setVisible(true);      saveBtn.setManaged(true);
            cancelBtn.setVisible(true);    cancelBtn.setManaged(true);
            renameField.requestFocus();
            renameField.selectAll();
        });

        saveBtn.setOnAction(ev -> {
            String newTitle = renameField.getText().trim();
            if (!newTitle.isBlank() && !newTitle.equals(s.title())) {
                sammlungRepo.rename(s.id(), newTitle);
                buildCollectionPane();
            } else {
                cancelBtn.fire();
            }
        });

        cancelBtn.setOnAction(ev -> {
            renameField.setText(s.title());
            renameField.setVisible(false);  renameField.setManaged(false);
            titleLabel.setVisible(true);    titleLabel.setManaged(true);
            saveBtn.setVisible(false);      saveBtn.setManaged(false);
            cancelBtn.setVisible(false);    cancelBtn.setManaged(false);
            openBtn.setVisible(true);       openBtn.setManaged(true);
            renameBtn.setVisible(true);     renameBtn.setManaged(true);
            deleteBtn.setVisible(true);     deleteBtn.setManaged(true);
        });

        deleteBtn.setOnAction(ev -> {
            deleteBtn.setVisible(false);   deleteBtn.setManaged(false);
            openBtn.setVisible(false);     openBtn.setManaged(false);
            renameBtn.setVisible(false);   renameBtn.setManaged(false);
            confirmBtn.setVisible(true);   confirmBtn.setManaged(true);
            abortBtn.setVisible(true);     abortBtn.setManaged(true);
        });
        confirmBtn.setOnAction(ev -> { sammlungRepo.delete(s.id()); buildCollectionPane(); });
        abortBtn.setOnAction(ev -> {
            confirmBtn.setVisible(false);  confirmBtn.setManaged(false);
            abortBtn.setVisible(false);    abortBtn.setManaged(false);
            deleteBtn.setVisible(true);    deleteBtn.setManaged(true);
            openBtn.setVisible(true);      openBtn.setManaged(true);
            renameBtn.setVisible(true);    renameBtn.setManaged(true);
        });

        HBox row = new HBox(12, titleLabel, renameField, countLabel,
                openBtn, renameBtn, saveBtn, cancelBtn, deleteBtn, confirmBtn, abortBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;" +
                "-fx-border-color: #e0ece8; -fx-border-radius: 8px; -fx-border-width: 1;");
        return row;
    }

    private void buildSammlungDetailPane(int sammlungId) {
        List<Sammlung> all = sammlungRepo.getAll();
        Sammlung s = all.stream().filter(x -> x.id() == sammlungId).findFirst().orElse(null);
        if (s == null) { openSammlungId = null; buildCollectionPane(); return; }

        paneCollection.getChildren().clear();
        paneCollection.setSpacing(20);
        paneCollection.setPadding(new Insets(30, 40, 30, 40));

        Button backBtn = buildSecondaryBtn("← Alle Sammelmappen");
        backBtn.setOnAction(ev -> { openSammlungId = null; buildCollectionPane(); });

        Label header = new Label(s.title());
        header.getStyleClass().add("header-text");
        Separator sep = new Separator();

        List<Exercise> exercises = sammlungRepo.getExercises(sammlungId);

        if (exercises.isEmpty()) {
            Label empty = new Label("Diese Sammelmappe ist leer. Füge Übungen über die Übersicht hinzu.");
            empty.getStyleClass().add("card-description");
            empty.setWrapText(true);
            Button goBtn = buildActionBtn("→ Zu den Übungen");
            goBtn.setOnAction(ev -> showExercises());
            paneCollection.getChildren().addAll(backBtn, header, sep, empty, goBtn);
            return;
        }

        Label countLabel = new Label(exercises.size() + " Übung" + (exercises.size() == 1 ? "" : "en"));
        countLabel.getStyleClass().add("card-description");

        Button clearBtn = buildDeleteBtn("🗑 Sammelmappe leeren");
        clearBtn.setOnAction(ev -> {
            for (Exercise e : exercises) sammlungRepo.removeExercise(sammlungId, e.id());
            buildCollectionPane();
            applyFilter(); // NEU: statt showExercises direkt
        });

        HBox topRow = new HBox(12, countLabel, clearBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox list = new VBox(10);
        for (int i = 0; i < exercises.size(); i++) {
            list.getChildren().add(buildCollectionRow(exercises.get(i), sammlungId, exercises, i));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneCollection.getChildren().addAll(backBtn, header, sep, topRow, scroll);
    }

    private HBox buildCollectionRow(Exercise e, int sammlungId, List<Exercise> allExercises, int index) {
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setMinWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label tagLabel = new Label(e.category());
        tagLabel.getStyleClass().add("card-tag");

        Button openBtn = buildSecondaryBtn("▶ Abspielen");
        openBtn.setOnAction(ev -> openDetailInPane(e, paneCollection, btnCollection, allExercises, index));

        Button removeBtn = buildDeleteBtn("✖ Entfernen");
        removeBtn.setOnAction(ev -> {
            sammlungRepo.removeExercise(sammlungId, e.id());
            buildCollectionPane();
            applyFilter(); // NEU: statt showExercises direkt
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

    private Button buildArrowOverlayBtn(String arrow) {
        String base  = "-fx-background-color: rgba(0,0,0,0.45);-fx-text-fill: white;" +
                "-fx-font-size: 18px;-fx-cursor: hand;-fx-background-radius: 6px;" +
                "-fx-padding: 8 12 8 12;-fx-border-width: 0;";
        String hover = "-fx-background-color: rgba(0,0,0,0.68);-fx-text-fill: white;" +
                "-fx-font-size: 18px;-fx-cursor: hand;-fx-background-radius: 6px;" +
                "-fx-padding: 8 12 8 12;-fx-border-width: 0;";
        Button btn = new Button(arrow);
        btn.setStyle(base);
        btn.setOnMouseEntered(ev -> btn.setStyle(hover));
        btn.setOnMouseExited(ev  -> btn.setStyle(base));
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
            // NEU: Suche geht durch applyFilter, nicht direkt an den Presenter
            searchField.textProperty().addListener(
                    (obs, oldVal, newVal) -> applyFilter());
        }
    }

    // —— Navigation ———————————————————————————————————————————————————————————

    @FXML private void showExercises()  { switchPage(paneExercises,  btnExercises);  }
    @FXML private void showCategories() { buildCategoryPane(); switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking()   { switchPage(paneTracking,   btnTracking);   }
    @FXML private void showCollection() { openSammlungId = null; buildCollectionPane(); switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        hideDetailPane();
        if (activePane == paneExercises) {
            applyFilter(); // NEU: statt showExercises direkt
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

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);

        if (e.videoUrl() != null && !e.videoUrl().isBlank()) {
            try {
                Media media = new Media(e.videoUrl());
                MediaPlayer player = new MediaPlayer(media);
                player.setAutoPlay(false);
                player.seek(Duration.ZERO);
                player.pause();

                MediaView thumbnail = new MediaView(player);
                thumbnail.setFitWidth(220);
                thumbnail.setFitHeight(120);
                thumbnail.setPreserveRatio(false);

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
        boolean cardInitAdded = sammlungRepo.isInAnySammlung(e.id());
        Button addBtn = new Button(cardInitAdded ? "✔ Sammelmappe" : "+ Sammelmappe");
        addBtn.setStyle(cardInitAdded ? cardBtnAdded : cardBtnNormal);
        addBtn.setOnAction(ev -> {
            List<Sammlung> sammlungen = sammlungRepo.getAll();
            if (sammlungen.isEmpty()) return;
            ContextMenu menu = new ContextMenu();
            for (Sammlung s : sammlungen) {
                boolean inThis = sammlungRepo.containsExercise(s.id(), e.id());
                MenuItem item = new MenuItem((inThis ? "✔ " : "+ ") + s.title());
                item.setOnAction(mev -> {
                    if (inThis) sammlungRepo.removeExercise(s.id(), e.id());
                    else        sammlungRepo.addExercise(s.id(), e.id());
                    applyFilter(); // NEU: statt showExercises direkt
                });
                menu.getItems().add(item);
            }
            menu.show(addBtn, Side.BOTTOM, 0, 0);
        });

        content.getChildren().addAll(titleLabel, descLabel, tagLabel, addBtn);
        card.getChildren().addAll(imagePlaceholder, content);
        exerciseGrid.getChildren().add(card);
    }

    // —— Detail in gleichem Fenster öffnen ————————————————————————————————————

    private void openDetailInPane(Exercise e, VBox returnPane, Button returnBtn) {
        openDetailInPane(e, returnPane, returnBtn, null, -1);
    }

    private void openDetailInPane(Exercise e, VBox returnPane, Button returnBtn,
                                  List<Exercise> navList, int navIndex) {
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

        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);

        Label categoryBadge = new Label(e.category());
        categoryBadge.getStyleClass().add("card-tag");

        boolean detailInitAdded = sammlungRepo.isInAnySammlung(e.id());
        Button collectionBtn = buildActionBtn(detailInitAdded ? "✔ Sammelmappe" : "+ Zur Sammelmappe");
        collectionBtn.setOnAction(ev -> {
            List<Sammlung> sammlungen = sammlungRepo.getAll();
            if (sammlungen.isEmpty()) return;
            ContextMenu menu = new ContextMenu();
            for (Sammlung s : sammlungen) {
                boolean inThis = sammlungRepo.containsExercise(s.id(), e.id());
                MenuItem item = new MenuItem((inThis ? "✔ " : "+ ") + s.title());
                item.setOnAction(mev -> {
                    if (inThis) sammlungRepo.removeExercise(s.id(), e.id());
                    else        sammlungRepo.addExercise(s.id(), e.id());
                    boolean nowInAny = sammlungRepo.isInAnySammlung(e.id());
                    collectionBtn.setText(nowInAny ? "✔ Sammelmappe" : "+ Zur Sammelmappe");
                });
                menu.getItems().add(item);
            }
            menu.show(collectionBtn, Side.BOTTOM, 0, 0);
        });

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(categoryRepo.getAll());
        categoryCombo.setValue(e.category());
        categoryCombo.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #1a2e2a;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-pref-width: 180px;"
        );

        Label savedLabel = new Label("✔ Gespeichert");
        savedLabel.setStyle("-fx-text-fill: #2d7a5c;-fx-font-size: 13px;");
        savedLabel.setVisible(false);
        savedLabel.managedProperty().bind(savedLabel.visibleProperty());

        Button assignBtn = buildActionBtn("Zuweisen");
        assignBtn.setOnAction(ev -> {
            String selected = categoryCombo.getValue();
            if (selected != null && !selected.isBlank()) {
                exerciseRepo.updateCategory(e.title(), selected);
                categoryBadge.setText(selected);
                savedLabel.setVisible(true);
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> savedLabel.setVisible(false));
                }).start();
            }
        });

        boolean hasNav  = navList != null && navList.size() > 1;
        boolean hasPrev = hasNav && navIndex > 0;
        boolean hasNext = hasNav && navIndex < navList.size() - 1;

        HBox infoRow = new HBox(12,
                categoryBadge, titleLabel, collectionBtn, categoryCombo, assignBtn, savedLabel);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(16, 40, 12, 40));

        String descText = (e.desc() != null && !e.desc().isBlank())
                ? e.desc() : "Keine Beschreibung vorhanden.";
        Label descContent = new Label(descText);
        descContent.getStyleClass().add("detail-description");
        descContent.setWrapText(true);
        HBox.setHgrow(descContent, Priority.ALWAYS);
        HBox descRow = new HBox(descContent);
        descRow.setPadding(new Insets(0, 40, 28, 40));

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double videoFitW = screen.getWidth() - 310;
        double videoFitH = screen.getHeight() - 280;
        currentPlayer = new VideoPlayerView(e.videoUrl(), videoFitW, videoFitH);

        StackPane videoStack = new StackPane(currentPlayer);
        if (hasNav) {
            Button[] overlays = new Button[2];
            if (hasPrev) {
                Button prevArrow = buildArrowOverlayBtn("❮");
                prevArrow.setVisible(false);
                StackPane.setAlignment(prevArrow, Pos.CENTER_LEFT);
                StackPane.setMargin(prevArrow, new Insets(0, 0, 0, 12));
                int prevIdx = navIndex - 1;
                prevArrow.setOnAction(ev -> openDetailInPane(
                        navList.get(prevIdx), returnPane, returnBtn, navList, prevIdx));
                videoStack.getChildren().add(prevArrow);
                overlays[0] = prevArrow;
            }
            if (hasNext) {
                Button nextArrow = buildArrowOverlayBtn("❯");
                nextArrow.setVisible(false);
                StackPane.setAlignment(nextArrow, Pos.CENTER_RIGHT);
                StackPane.setMargin(nextArrow, new Insets(0, 12, 0, 0));
                int nextIdx = navIndex + 1;
                nextArrow.setOnAction(ev -> openDetailInPane(
                        navList.get(nextIdx), returnPane, returnBtn, navList, nextIdx));
                videoStack.getChildren().add(nextArrow);
                overlays[1] = nextArrow;
            }
            videoStack.hoverProperty().addListener((obs, was, now) -> {
                for (Button b : overlays) if (b != null) b.setVisible(now);
            });
        }

        HBox videoWrapper = new HBox(videoStack);
        videoWrapper.setAlignment(Pos.CENTER);
        videoWrapper.setMaxWidth(Double.MAX_VALUE);
        videoWrapper.setPadding(new Insets(16, 0, 0, 0));

        VBox innerLayout = new VBox(0,
                backRow, videoWrapper,
                infoRow, descRow
        );
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