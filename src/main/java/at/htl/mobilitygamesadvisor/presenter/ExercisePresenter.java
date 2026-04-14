package at.htl.mobilitygamesadvisor.presenter;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.view.ExerciseView;

/**
 * PRESENTER — reacts to view events, queries the model, updates the view.
 * No JavaFX imports; the only JavaFX knowledge lives in the Controller.
 */
public class ExercisePresenter {

    private final ExerciseRepository repository;
    private final ExerciseView view;

    public ExercisePresenter(ExerciseRepository repository, ExerciseView view) {
        this.repository = repository;
        this.view = view;

        // Wire up the search callback once
        view.setOnSearchChanged(this::onSearchChanged);

        // Show all exercises on startup
        view.showExercises(repository.getAll());
    }

    private void onSearchChanged(String query) {
        view.showExercises(repository.search(query));
    }
}