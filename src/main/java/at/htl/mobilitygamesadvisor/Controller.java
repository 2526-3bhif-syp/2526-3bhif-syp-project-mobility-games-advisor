package at.htl.mobilitygamesadvisor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML private FlowPane exerciseGrid;
    @FXML private TextField searchField; // Neues Feld für die Suchleiste

    @FXML private VBox paneDashboard, paneExercises, paneCategories, paneTracking, paneCollection;
    @FXML private Button btnDashboard, btnExercises, btnCategories, btnTracking, btnCollection;

    // Speicher für alle Übungen, um filtern zu können
    private final List<Exercise> allExercises = new ArrayList<>();

    // Hilfsklasse für die Daten
    private record Exercise(String title, String desc, String category) {}

    @FXML
    public void initialize() {
        setupDemoData();
        renderExercises(allExercises); // Initial alle anzeigen

        // Listener für die Suchleiste hinzufügen
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterExercises(newValue);
            });
        }

        showDashboard();
    }

    private void setupDemoData() {
        allExercises.add(new Exercise("Gedächtnis-Training", "Finde passende Bildpaare.", "Kognition"));
        allExercises.add(new Exercise("Sitz-Yoga", "Einfache Dehnübungen im Sitzen.", "Mobilität"));
        allExercises.add(new Exercise("Wort-Rätsel", "Ergänze fehlende Buchstaben.", "Kognition"));
        allExercises.add(new Exercise("Balance-Übung", "Sicherer Stand auf einem Bein.", "Mobilität"));
        allExercises.add(new Exercise("Reaktions-Spiel", "Drücke Knöpfe in der richtigen Reihenfolge.", "Koordination"));
        allExercises.add(new Exercise("Kraft-Training", "Leichte Übungen mit Wasserflaschen.", "Kraft"));
    }

    private void filterExercises(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            renderExercises(allExercises);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        List<Exercise> filteredList = allExercises.stream()
                .filter(e -> e.title().toLowerCase().contains(lowerCaseFilter) ||
                        e.category().toLowerCase().contains(lowerCaseFilter))
                .toList();

        renderExercises(filteredList);
    }

    private void renderExercises(List<Exercise> exercises) {
        if (exerciseGrid == null) return;
        exerciseGrid.getChildren().clear(); // Grid leeren
        for (Exercise e : exercises) {
            addExerciseCard(e.title(), e.desc(), e.category());
        }
    }

    // Deine bestehende Navigation Logic (switchPage etc.) bleibt gleich...
    @FXML private void showDashboard() { switchPage(paneDashboard, btnDashboard); }
    @FXML private void showExercises() { switchPage(paneExercises, btnExercises); }
    @FXML private void showCategories() { switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking() { switchPage(paneTracking, btnTracking); }
    @FXML private void showCollection() { switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        paneDashboard.setVisible(false);
        paneExercises.setVisible(false);
        paneCategories.setVisible(false);
        paneTracking.setVisible(false);
        paneCollection.setVisible(false);
        activePane.setVisible(true);
        resetButtonStyles();
        activeButton.getStyleClass().add("active-nav");
    }

    private void resetButtonStyles() {
        btnDashboard.getStyleClass().remove("active-nav");
        btnExercises.getStyleClass().remove("active-nav");
        btnCategories.getStyleClass().remove("active-nav");
        btnTracking.getStyleClass().remove("active-nav");
        btnCollection.getStyleClass().remove("active-nav");
    }

    private void addExerciseCard(String title, String desc, String category) {
        VBox card = new VBox();
        card.getStyleClass().add("exercise-card");

        StackPane imagePlaceholder = new StackPane(new Label("VIDEO"));
        imagePlaceholder.getStyleClass().add("card-image-placeholder");

        VBox content = new VBox(8);
        content.getStyleClass().add("card-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);

        Label tagLabel = new Label(category);
        tagLabel.getStyleClass().add("card-tag");

        content.getChildren().addAll(titleLabel, descLabel, tagLabel);
        card.getChildren().addAll(imagePlaceholder, content);

        exerciseGrid.getChildren().add(card);
    }
}