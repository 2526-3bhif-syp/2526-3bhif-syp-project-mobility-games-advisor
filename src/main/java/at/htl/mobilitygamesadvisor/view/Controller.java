package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.util.UserSession;
import at.htl.mobilitygamesadvisor.model.*;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.stage.Screen;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
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
        setupAddVideoButton();
        showExercises();

        setupUserHeader();
    }

    private void setupUserHeader() {
        User user = UserSession.getInstance().getUser();
        if (user == null) return;

        Label userLabel = new Label("👤 " + user.getUsername());
        userLabel.setStyle("-fx-text-fill: #2d7a5c; -fx-font-size: 13px;");

        Button logoutBtn = buildSecondaryBtn("Abmelden");
        logoutBtn.setOnAction(ev -> {
            UserSession.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Stage stage = (Stage) btnExercises.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
            } catch (IOException e) { e.printStackTrace(); }
        });

        // add to your existing sidebar or top bar — wherever btnExercises lives
        // e.g. if there's a VBox sidebar:
        // sidebar.getChildren().addAll(userLabel, logoutBtn);
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
        paneCollection.setSpacing(16);
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

        javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane();
        grid.setHgap(16);
        grid.setVgap(16);

        if (sammlungen.isEmpty()) {
            Label empty = new Label("Noch keine Sammelmappen vorhanden. Erstelle eine oben.");
            empty.getStyleClass().add("card-description");
            grid.getChildren().add(empty);
        } else {
            for (Sammlung s : sammlungen) {
                grid.getChildren().add(buildSammlungCard(s));
            }
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneCollection.getChildren().addAll(header, subtitle, sep, createRow, scroll);
    }

    private VBox buildSammlungCard(Sammlung s) {
        int count = sammlungRepo.countExercises(s.id());

        VBox card = new VBox(0);
        card.getStyleClass().add("exercise-card");
        card.setPrefWidth(460);
        card.setMaxWidth(460);
        card.setStyle("-fx-pref-width: 460; -fx-max-width: 460;");

        // — Header with gradient and count badge
        StackPane cardHeader = new StackPane();
        cardHeader.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #b8dece, #e8f4ef);" +
                "-fx-background-radius: 14 14 0 0;" +
                "-fx-min-height: 120px; -fx-pref-height: 120px;" +
                "-fx-border-color: #dceae5; -fx-border-width: 0 0 1 0;");
        cardHeader.setPrefHeight(120);

        Label folderIcon = new Label("📁");
        folderIcon.setStyle("-fx-font-size: 52px;");

        Label countBadge = new Label(count + (count == 1 ? " Übung" : " Übungen"));
        countBadge.setStyle(
                "-fx-background-color: #2d7a5c; -fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 4 10 4 10; -fx-background-radius: 20;");
        StackPane.setAlignment(countBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(countBadge, new Insets(12, 12, 0, 0));

        cardHeader.getChildren().addAll(folderIcon, countBadge);

        // — Content
        VBox content = new VBox(10);
        content.getStyleClass().add("card-content");
        content.setPadding(new Insets(14, 16, 16, 16));

        Label titleLabel = new Label(s.title());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 15px;");

        TextField renameField = new TextField(s.title());
        renameField.getStyleClass().add("search-field");
        renameField.setVisible(false);
        renameField.setManaged(false);

        Button openBtn = buildActionBtn("▶  Sammelmappe öffnen");
        openBtn.setMaxWidth(Double.MAX_VALUE);

        // Normal buttons row
        Button renameBtn  = buildSecondaryBtn("✏  Umbenennen");
        Button deleteBtn  = buildDeleteBtn("🗑  Löschen");
        HBox normalRow = new HBox(8, renameBtn, deleteBtn);
        normalRow.setAlignment(Pos.CENTER_LEFT);

        // Rename buttons row
        Button saveBtn    = buildActionBtn("✔  Speichern");
        Button cancelBtn  = buildSecondaryBtn("✖  Abbrechen");
        HBox renameRow = new HBox(8, saveBtn, cancelBtn);
        renameRow.setAlignment(Pos.CENTER_LEFT);
        renameRow.setVisible(false); renameRow.setManaged(false);

        // Delete confirm row
        Button confirmBtn = buildDeleteBtn("⚠  Ja, löschen");
        Button abortBtn   = buildSecondaryBtn("Abbrechen");
        HBox deleteRow = new HBox(8, confirmBtn, abortBtn);
        deleteRow.setAlignment(Pos.CENTER_LEFT);
        deleteRow.setVisible(false); deleteRow.setManaged(false);

        openBtn.setOnAction(ev -> { openSammlungId = s.id(); buildCollectionPane(); });

        renameBtn.setOnAction(ev -> {
            titleLabel.setVisible(false);  titleLabel.setManaged(false);
            renameField.setVisible(true);  renameField.setManaged(true);
            openBtn.setVisible(false);     openBtn.setManaged(false);
            normalRow.setVisible(false);   normalRow.setManaged(false);
            renameRow.setVisible(true);    renameRow.setManaged(true);
            renameField.requestFocus();    renameField.selectAll();
        });

        saveBtn.setOnAction(ev -> {
            String t = renameField.getText().trim();
            if (!t.isBlank()) { sammlungRepo.rename(s.id(), t); buildCollectionPane(); }
            else cancelBtn.fire();
        });

        cancelBtn.setOnAction(ev -> {
            renameField.setText(s.title());
            renameField.setVisible(false);  renameField.setManaged(false);
            titleLabel.setVisible(true);    titleLabel.setManaged(true);
            openBtn.setVisible(true);       openBtn.setManaged(true);
            normalRow.setVisible(true);     normalRow.setManaged(true);
            renameRow.setVisible(false);    renameRow.setManaged(false);
        });

        deleteBtn.setOnAction(ev -> {
            openBtn.setVisible(false);     openBtn.setManaged(false);
            normalRow.setVisible(false);   normalRow.setManaged(false);
            deleteRow.setVisible(true);    deleteRow.setManaged(true);
        });

        confirmBtn.setOnAction(ev -> { sammlungRepo.delete(s.id()); buildCollectionPane(); });

        abortBtn.setOnAction(ev -> {
            deleteRow.setVisible(false);   deleteRow.setManaged(false);
            openBtn.setVisible(true);      openBtn.setManaged(true);
            normalRow.setVisible(true);    normalRow.setManaged(true);
        });

        content.getChildren().addAll(titleLabel, renameField, openBtn, normalRow, renameRow, deleteRow);
        card.getChildren().addAll(cardHeader, content);
        return card;
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

        javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane();
        grid.setHgap(16);
        grid.setVgap(16);
        for (int i = 0; i < exercises.size(); i++) {
            grid.getChildren().add(buildCollectionCard(exercises.get(i), sammlungId, exercises, i));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneCollection.getChildren().addAll(backBtn, header, sep, topRow, scroll);
    }

    private VBox buildCollectionCard(Exercise e, int sammlungId, List<Exercise> allExercises, int index) {
        VBox card = new VBox(0);
        card.getStyleClass().add("exercise-card");

        // — Placeholder area (same as exercise cards)
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        imagePlaceholder.setPrefHeight(120);
        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-text-fill: #5cad8a; -fx-font-size: 28px;");
        imagePlaceholder.getChildren().add(playIcon);
        imagePlaceholder.setOnMouseClicked(ev ->
                openDetailInPane(e, paneCollection, btnCollection, allExercises, index));

        // — Content
        VBox content = new VBox(8);
        content.getStyleClass().add("card-content");
        content.setOnMouseClicked(ev ->
                openDetailInPane(e, paneCollection, btnCollection, allExercises, index));

        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        Label tagLabel = new Label(e.category() != null ? e.category() : "Unkategorisiert");
        tagLabel.getStyleClass().add("card-tag");

        String btnNormal = "-fx-background-color: transparent;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 11px;-fx-cursor: hand;-fx-padding: 4 8 4 8;" +
                "-fx-border-color: #c0392b;-fx-border-radius: 6px;-fx-border-width: 1;-fx-text-fill: #c0392b;";
        Button removeBtn = new Button("✖ Entfernen");
        removeBtn.setStyle(btnNormal);
        removeBtn.setOnAction(ev -> {
            sammlungRepo.removeExercise(sammlungId, e.id());
            buildCollectionPane();
            applyFilter();
        });

        content.getChildren().addAll(titleLabel, tagLabel, removeBtn);
        card.getChildren().addAll(imagePlaceholder, content);
        return card;
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
    // add videos
    private boolean addButtonAdded = false;  // flag to prevent duplicate

    private void setupAddVideoButton() {
        if (addButtonAdded) return;

        if (searchField.getParent() instanceof HBox parentBox) {
            Button addVideoBtn = buildActionBtn("+ Neue Übung");

            addVideoBtn.setOnAction(e -> {
                StackPane root = (StackPane) paneExercises.getParent();
                AddVideoDialog.showAsOverlay(root, this::applyFilter);
            });

            parentBox.getChildren().add(addVideoBtn);
            addButtonAdded = true;
        }
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

                MediaView thumbnail = new MediaView(player);
                thumbnail.setFitWidth(220);
                thumbnail.setFitHeight(120);
                thumbnail.setPreserveRatio(false);

                Label playIcon = new Label("▶");
                playIcon.setStyle(
                        "-fx-text-fill: white; -fx-font-size: 28px;" +
                                "-fx-effect: dropshadow(gaussian, black, 8, 0, 0, 0);"
                );

                // Wait until media is ready, THEN seek to first frame
                player.setOnReady(() -> {
                    player.seek(Duration.ZERO);
                    player.pause();
                });

                imagePlaceholder.getChildren().addAll(thumbnail, playIcon);
            } catch (Exception ex) {
                imagePlaceholder.getChildren().add(new Label("▶ VIDEO"));
            }
        }

        imagePlaceholder.setOnMouseClicked(event -> openDetailInPane(e, paneExercises, btnExercises));
        VBox content = new VBox(8);
        content.setOnMouseClicked(event -> openDetailInPane(e, paneExercises, btnExercises));
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
                    boolean currentlyIn = sammlungRepo.containsExercise(s.id(), e.id());
                    if (currentlyIn) sammlungRepo.removeExercise(s.id(), e.id());
                    else             sammlungRepo.addExercise(s.id(), e.id());
                    applyFilter();
                });
                menu.getItems().add(item);
            }
            menu.show(addBtn, Side.BOTTOM, 0, 0);
        });
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null && e.uploadedBy() == currentUser.getId()) {
            Button deleteBtn = buildDeleteBtn("🗑");
            deleteBtn.setOnAction(ev -> {
                deleteBtn.setText("⚠ Sicher?");
                deleteBtn.setOnAction(confirm -> {
                    exerciseRepo.delete(e.id());
                    applyFilter();
                });
            });
            content.getChildren().add(deleteBtn);
        }

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
        disposeCurrentPlayer(); // Standard cleanup
        paneDetail.getChildren().clear();

        // --- BUTTON STYLES ---
        String styleNormal = "-fx-background-color: transparent;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #2d7a5c;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";
        String styleHover  = "-fx-background-color: #2d7a5c18;-fx-text-fill: #2d7a5c;" +
                "-fx-font-size: 14px;-fx-cursor: hand;-fx-border-color: #2d7a5c;" +
                "-fx-border-radius: 6px;-fx-padding: 8 16 8 16;";

        // --- BACK BUTTON ---
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

        // --- TITLE & DESCRIPTION DISPLAY ---
        Label titleLabel = new Label(e.title());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);

        String descText = (e.desc() != null && !e.desc().isBlank())
                ? e.desc() : "Keine Beschreibung vorhanden.";
        Label descContent = new Label(descText);
        descContent.getStyleClass().add("detail-description");
        descContent.setWrapText(true);

        Label categoryBadge = new Label(e.category());
        categoryBadge.getStyleClass().add("card-tag");

        // --- EDIT FIELDS ---
        TextField titleField = new TextField(e.title());
        titleField.getStyleClass().add("search-field");
        titleField.setVisible(false); titleField.setManaged(false);

        TextArea descField = new TextArea(e.desc() != null ? e.desc() : "");
        descField.setWrapText(true); descField.setPrefRowCount(3);
        descField.getStyleClass().add("search-field");
        descField.setVisible(false); descField.setManaged(false);

        ComboBox<String> editCategoryCombo = new ComboBox<>();
        editCategoryCombo.getItems().addAll(categoryRepo.getAll());
        editCategoryCombo.setValue(e.category());
        editCategoryCombo.setVisible(false); editCategoryCombo.setManaged(false);

        // --- EDIT CONTROLS ---
        Label editSavedLabel = new Label("✔ Gespeichert");
        editSavedLabel.setStyle("-fx-text-fill: #2d7a5c; -fx-font-size: 13px;");
        editSavedLabel.setVisible(false); editSavedLabel.setManaged(false);

        Button editBtn      = buildSecondaryBtn("✏ Bearbeiten");
        Button saveEditBtn  = buildActionBtn("✔ Speichern");
        Button cancelEditBtn = buildSecondaryBtn("✖ Abbrechen");
        saveEditBtn.setVisible(false);    saveEditBtn.setManaged(false);
        cancelEditBtn.setVisible(false);  cancelEditBtn.setManaged(false);

        // --- BUTTON ACTIONS ---
        editBtn.setOnAction(ev -> {
            // Hide Labels
            titleLabel.setVisible(false);        titleLabel.setManaged(false);
            descContent.setVisible(false);       descContent.setManaged(false);
            categoryBadge.setVisible(false);     categoryBadge.setManaged(false);
            // Show Fields
            titleField.setVisible(true);         titleField.setManaged(true);
            descField.setVisible(true);          descField.setManaged(true);
            editCategoryCombo.setVisible(true);  editCategoryCombo.setManaged(true);
            // Toggle Buttons
            editBtn.setVisible(false);           editBtn.setManaged(false);
            saveEditBtn.setVisible(true);        saveEditBtn.setManaged(true);
            cancelEditBtn.setVisible(true);      cancelEditBtn.setManaged(true);
        });

        cancelEditBtn.setOnAction(ev -> {
            titleField.setText(e.title());
            descField.setText(e.desc() != null ? e.desc() : "");
            editCategoryCombo.setValue(e.category());
            // Restore Visibility
            titleField.setVisible(false);        titleField.setManaged(false);
            descField.setVisible(false);         descField.setManaged(false);
            editCategoryCombo.setVisible(false); editCategoryCombo.setManaged(false);
            titleLabel.setVisible(true);         titleLabel.setManaged(true);
            descContent.setVisible(true);        descContent.setManaged(true);
            categoryBadge.setVisible(true);      categoryBadge.setManaged(true);
            saveEditBtn.setVisible(false);       saveEditBtn.setManaged(false);
            cancelEditBtn.setVisible(false);     cancelEditBtn.setManaged(false);
            editBtn.setVisible(true);            editBtn.setManaged(true);
        });

        saveEditBtn.setOnAction(ev -> {
            String newTitle    = titleField.getText().trim();
            String newDesc     = descField.getText().trim();
            String newCategory = editCategoryCombo.getValue();
            if (newTitle.isBlank()) return;

            exerciseRepo.update(e.id(), newTitle, newDesc, newCategory);

            // Update UI with new values
            titleLabel.setText(newTitle);
            descContent.setText(newDesc.isBlank() ? "Keine Beschreibung vorhanden." : newDesc);
            categoryBadge.setText(newCategory != null ? newCategory : "");

            cancelEditBtn.fire(); // Re-use the visibility toggle logic

            editSavedLabel.setVisible(true); editSavedLabel.setManaged(true);
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    editSavedLabel.setVisible(false); editSavedLabel.setManaged(false);
                });
            }).start();
        });

        // --- NAVIGATION & VIDEO ---
        boolean hasNav  = navList != null && navList.size() > 1;
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double videoFitW = screen.getWidth() - 310;
        double videoFitH = screen.getHeight() - 280;
        currentPlayer = new VideoPlayerView(e.videoUrl(), videoFitW, videoFitH);

        StackPane videoStack = new StackPane(currentPlayer);
        if (hasNav) {
            if (navIndex > 0) {
                Button prevArrow = buildArrowOverlayBtn("❮");
                StackPane.setAlignment(prevArrow, Pos.CENTER_LEFT);
                prevArrow.setOnAction(ev -> openDetailInPane(navList.get(navIndex - 1), returnPane, returnBtn, navList, navIndex - 1));
                videoStack.getChildren().add(prevArrow);
            }
            if (navIndex < navList.size() - 1) {
                Button nextArrow = buildArrowOverlayBtn("❯");
                StackPane.setAlignment(nextArrow, Pos.CENTER_RIGHT);
                nextArrow.setOnAction(ev -> openDetailInPane(navList.get(navIndex + 1), returnPane, returnBtn, navList, navIndex + 1));
                videoStack.getChildren().add(nextArrow);
            }
        }

        // --- LAYOUT ASSEMBLY ---
        HBox videoWrapper = new HBox(videoStack);
        videoWrapper.setAlignment(Pos.CENTER);

        HBox infoRow = new HBox(12, categoryBadge, editCategoryCombo, titleLabel, titleField);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.setPadding(new Insets(16, 40, 12, 40));

        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null && e.uploadedBy() == currentUser.getId()) {
            infoRow.getChildren().addAll(editBtn, saveEditBtn, cancelEditBtn, editSavedLabel);
        }

        VBox descBox = new VBox(5, descContent, descField);
        descBox.setPadding(new Insets(0, 40, 28, 40));

        VBox innerLayout = new VBox(0, backRow, videoWrapper, infoRow, descBox);
        innerLayout.setStyle("-fx-background-color: #f4f6f5;");

        ScrollPane scroll = new ScrollPane(innerLayout);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        paneDetail.getChildren().add(scroll);
        hideMainPanes(); // Helper to set visible(false) on all other panes
        paneDetail.setVisible(true);
    }

    private void hideMainPanes() {
        List.of(paneExercises, paneCategories, paneTracking, paneCollection)
                .forEach(p -> {
                    if (p != null) p.setVisible(false);
                });
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