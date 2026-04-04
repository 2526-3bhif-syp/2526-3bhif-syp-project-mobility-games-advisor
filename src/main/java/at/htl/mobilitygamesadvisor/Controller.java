package at.htl.mobilitygamesadvisor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class Controller {

    @FXML private FlowPane exerciseGrid;
    
    // Panes for Navigation
    @FXML private VBox paneDashboard;
    @FXML private VBox paneExercises;
    @FXML private VBox paneCategories;
    @FXML private VBox paneTracking;
    @FXML private VBox paneCollection;

    // Buttons to update active style
    @FXML private Button btnDashboard;
    @FXML private Button btnExercises;
    @FXML private Button btnCategories;
    @FXML private Button btnTracking;
    @FXML private Button btnCollection;

    @FXML
    public void initialize() {
        if (exerciseGrid != null) {
            setupDemoData();
        }
        showDashboard(); // Startseite setzen
    }

    private void setupDemoData() {
        addExerciseCard("Gedächtnis-Training", "Finde passende Bildpaare.", "Kognition");
        addExerciseCard("Sitz-Yoga", "Einfache Dehnübungen im Sitzen.", "Mobilität");
        addExerciseCard("Wort-Rätsel", "Ergänze fehlende Buchstaben.", "Kognition");
        addExerciseCard("Balance-Übung", "Sicherer Stand auf einem Bein.", "Mobilität");
        addExerciseCard("Reaktions-Spiel", "Drücke Knöpfe in der richtigen Reihenfolge.", "Koordination");
        addExerciseCard("Kraft-Training", "Leichte Übungen mit Wasserflaschen.", "Kraft");
    }

    // Navigation Logic
    @FXML
    private void showDashboard() {
        switchPage(paneDashboard, btnDashboard);
    }

    @FXML
    private void showExercises() {
        switchPage(paneExercises, btnExercises);
    }

    @FXML
    private void showCategories() {
        switchPage(paneCategories, btnCategories);
    }

    @FXML
    private void showTracking() {
        switchPage(paneTracking, btnTracking);
    }

    @FXML
    private void showCollection() {
        switchPage(paneCollection, btnCollection);
    }

    private void switchPage(VBox activePane, Button activeButton) {
        // Alle unsichtbar machen
        paneDashboard.setVisible(false);
        paneExercises.setVisible(false);
        paneCategories.setVisible(false);
        paneTracking.setVisible(false);
        paneCollection.setVisible(false);

        // Aktive sichtbar machen
        activePane.setVisible(true);

        // CSS Styles für Buttons zurücksetzen
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

        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        Label imgText = new Label("VIDEO");
        imgText.getStyleClass().add("card-image-text");
        imagePlaceholder.getChildren().add(imgText);

        VBox content = new VBox(8);
        content.getStyleClass().add("card-content");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("card-description");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(40);

        Label tagLabel = new Label(category);
        tagLabel.getStyleClass().add("card-tag");

        content.getChildren().addAll(titleLabel, descLabel, tagLabel);
        card.getChildren().addAll(imagePlaceholder, content);

        exerciseGrid.getChildren().add(card);
    }
}
