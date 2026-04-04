package at.htl.mobilitygamesadvisor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class Controller {

    @FXML
    private FlowPane exerciseGrid;

    @FXML
    public void initialize() {
        if (exerciseGrid != null) {
            addExerciseCard("Gedächtnis-Training", "Finde passende Bildpaare.", "Kognition");
            addExerciseCard("Sitz-Yoga", "Einfache Dehnübungen im Sitzen.", "Mobilität");
            addExerciseCard("Wort-Rätsel", "Ergänze fehlende Buchstaben.", "Kognition");
            addExerciseCard("Balance-Übung", "Sicherer Stand auf einem Bein.", "Mobilität");
            addExerciseCard("Reaktions-Spiel", "Drücke Knöpfe in der richtigen Reihenfolge.", "Koordination");
            addExerciseCard("Kraft-Training", "Leichte Übungen mit Wasserflaschen.", "Kraft");
        }
    }

    private void addExerciseCard(String title, String desc, String category) {
        VBox card = new VBox();
        card.getStyleClass().add("exercise-card");

        // Bild-Platzhalter
        StackPane imagePlaceholder = new StackPane();
        imagePlaceholder.getStyleClass().add("card-image-placeholder");
        Label imgText = new Label("VIDEO");
        imgText.getStyleClass().add("card-image-text");
        imagePlaceholder.getChildren().add(imgText);

        // Inhalt
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
