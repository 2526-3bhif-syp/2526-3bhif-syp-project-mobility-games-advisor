package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.presenter.ExercisePresenter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import at.htl.mobilitygamesadvisor.model.Exercise;


/**
 * VIEW (JavaFX Controller) — purely responsible for rendering.
 * All logic lives in {@link ExercisePresenter}.
 */
public class Controller implements ExerciseView {

    // ── FXML bindings ────────────────────────────────────────────────────────
    @FXML private FlowPane  exerciseGrid;
    @FXML private TextField searchField;

    @FXML private VBox   paneDashboard, paneExercises, paneCategories,
            paneTracking,  paneCollection;
    @FXML private Button btnDashboard,  btnExercises,  btnCategories,
            btnTracking,   btnCollection;

    // ── MVP wiring ───────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Construct model and presenter; presenter wires itself to this view
        new ExercisePresenter(new ExerciseRepository(), this);

        showDashboard();
    }

    // ── ExerciseView implementation ──────────────────────────────────────────

    @Override
    public void showExercises(List<Exercise> exercises) {
        if (exerciseGrid == null) return;
        exerciseGrid.getChildren().clear();
        exercises.forEach(e -> addExerciseCard(e.title(), e.desc(), e.category()));
    }

    @Override
    public void setOnSearchChanged(Consumer<String> listener) {
        if (searchField != null) {
            searchField.textProperty().addListener(
                    (obs, oldVal, newVal) -> listener.accept(newVal));
        }
    }

    // ── Navigation (pure view concern) ───────────────────────────────────────

    @FXML private void showDashboard()  { switchPage(paneDashboard,  btnDashboard);  }
    @FXML private void showExercises()  { switchPage(paneExercises,  btnExercises);  }
    @FXML private void showCategories() { switchPage(paneCategories, btnCategories); }
    @FXML private void showTracking()   { switchPage(paneTracking,   btnTracking);   }
    @FXML private void showCollection() { switchPage(paneCollection, btnCollection); }

    private void switchPage(VBox activePane, Button activeButton) {
        List.of(paneDashboard, paneExercises, paneCategories, paneTracking, paneCollection)
                .forEach(p -> p.setVisible(false));
        activePane.setVisible(true);

        resetButtonStyles();
        activeButton.getStyleClass().add("active-nav");
    }

    private void resetButtonStyles() {
        List.of(btnDashboard, btnExercises, btnCategories, btnTracking, btnCollection)
                .forEach(b -> b.getStyleClass().remove("active-nav"));
    }

    // ── Card rendering ────────────────────────────────────────────────────────

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