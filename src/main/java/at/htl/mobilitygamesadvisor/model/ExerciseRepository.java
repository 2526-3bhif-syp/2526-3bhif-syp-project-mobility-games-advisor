package at.htl.mobilitygamesadvisor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * MODEL — owns the data and the business/filter logic.
 * No JavaFX imports; completely testable in isolation.
 */
public class ExerciseRepository {

    private final List<Exercise> exercises = new ArrayList<>();

    public ExerciseRepository() {
        loadDemoData();
    }

    private void loadDemoData() {
        exercises.add(new Exercise("Gedächtnis-Training",   "Finde passende Bildpaare.",                         "Kognition"));
        exercises.add(new Exercise("Sitz-Yoga",             "Einfache Dehnübungen im Sitzen.",                   "Mobilität"));
        exercises.add(new Exercise("Wort-Rätsel",           "Ergänze fehlende Buchstaben.",                      "Kognition"));
        exercises.add(new Exercise("Balance-Übung",         "Sicherer Stand auf einem Bein.",                    "Mobilität"));
        exercises.add(new Exercise("Reaktions-Spiel",       "Drücke Knöpfe in der richtigen Reihenfolge.",       "Koordination"));
        exercises.add(new Exercise("Kraft-Training",        "Leichte Übungen mit Wasserflaschen.",               "Kraft"));
    }

    /** Returns all exercises. */
    public List<Exercise> getAll() {
        return List.copyOf(exercises);
    }

    /**
     * Returns exercises whose title or category contains {@code query}
     * (case-insensitive). Returns all exercises when query is blank.
     */
    public List<Exercise> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        String lower = query.toLowerCase();
        return exercises.stream()
                .filter(e -> e.title().toLowerCase().contains(lower)
                        || e.category().toLowerCase().contains(lower))
                .toList();
    }
}