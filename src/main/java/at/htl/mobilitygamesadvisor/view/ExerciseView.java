package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.Exercise;

import java.util.List;

/**
 * VIEW contract — everything the Presenter needs from the UI.
 * The concrete JavaFX controller implements this interface.
 */
public interface ExerciseView {

    /** Replace the displayed exercise cards with the given list. */
    void showExercises(List<Exercise> exercises);

    /** Register a callback that fires whenever the search text changes. */
    void setOnSearchChanged(java.util.function.Consumer<String> listener);
}