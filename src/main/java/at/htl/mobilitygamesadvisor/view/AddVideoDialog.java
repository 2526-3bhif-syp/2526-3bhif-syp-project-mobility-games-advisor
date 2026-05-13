// at.htl.mobilitygamesadvisor.view.AddVideoDialog
package at.htl.mobilitygamesadvisor.view;

import at.htl.mobilitygamesadvisor.model.ExerciseRepository;
import at.htl.mobilitygamesadvisor.model.VideoUploadService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AddVideoDialog {

    private static final ExerciseRepository repo = new ExerciseRepository();

    /**
     * Shows the dialog as a centered overlay over the root StackPane.
     * @param rootPane    the app's root StackPane (to dim + overlay the dialog)
     * @param onVideoAdded callback after successful insertion (for UI refresh)
     */
    public static void showAsOverlay(StackPane rootPane, Runnable onVideoAdded) {

        // ── 1. Dim overlay ────────────────────────────────────────────────────
        VBox overlayWrapper = new VBox();
        overlayWrapper.setAlignment(Pos.CENTER);
        overlayWrapper.setStyle("-fx-background-color: rgba(26, 46, 42, 0.55);");

        // ── 2. Dialog card — matches .exercise-card / .detail-dialog tone ────
        VBox dialogCard = new VBox(18);
        dialogCard.setMaxWidth(500);
        dialogCard.setMinWidth(460);
        dialogCard.setStyle(
                "-fx-background-color: #f4f6f5;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(26,46,42,0.22), 32, 0, 0, 8);" +
                        "-fx-padding: 32 36 28 36;"
        );

        // ── Header ────────────────────────────────────────────────────────────
        Label titleLabel = new Label("Neue Übung hinzufügen");
        titleLabel.setStyle(
                "-fx-text-fill: #1a2e2a;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label("Video hochladen & Übung erfassen");
        subtitleLabel.setStyle(
                "-fx-text-fill: #8aada8;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 0 0 4 0;"
        );

        VBox headerBox = new VBox(4, titleLabel, subtitleLabel);

        // ── Thin divider ──────────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #d8e4e0;");

        // ── Section label helper (replicates .detail-section-title) ──────────
        // used inline below

        // ── Input: Titel ──────────────────────────────────────────────────────
        Label nameLabel = makeSectionLabel("TITEL DER ÜBUNG");
        TextField nameInput = makeTextField("z. B. Kniebeuge mit Unterstützung...");

        // ── Input: Kategorie ──────────────────────────────────────────────────
        Label catLabel = makeSectionLabel("KATEGORIE");
        TextField catInput = makeTextField("z. B. Gleichgewicht, Mobilität...");

        // ── Input: Beschreibung ───────────────────────────────────────────────
        Label descLabel = makeSectionLabel("BESCHREIBUNG");
        TextArea descInput = new TextArea();
        descInput.setPromptText("Kurze Beschreibung der Übung und ihrer Ziele...");
        descInput.setPrefRowCount(4);
        descInput.setWrapText(true);
        descInput.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #1a2e2a;" +
                        "-fx-prompt-text-fill: #adc0bc;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 10 14 10 14;"
        );

        // ── File picker ───────────────────────────────────────────────────────
        Label fileLabel = makeSectionLabel("VIDEO-DATEI (MP4)");

        final File[] selectedFile = {null};

        Button fileBtn = new Button("📁  Datei auswählen");
        fileBtn.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #2d7a5c;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 9 18 9 18;" +
                        "-fx-background-radius: 9;" +
                        "-fx-border-color: #5cad8a;" +
                        "-fx-border-radius: 9;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-cursor: hand;"
        );
        fileBtn.setOnMouseEntered(e -> fileBtn.setStyle(
                "-fx-background-color: #e8f4ef;" +
                        "-fx-text-fill: #2d7a5c;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 9 18 9 18;" +
                        "-fx-background-radius: 9;" +
                        "-fx-border-color: #2d7a5c;" +
                        "-fx-border-radius: 9;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-cursor: hand;"
        ));
        fileBtn.setOnMouseExited(e -> fileBtn.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #2d7a5c;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 9 18 9 18;" +
                        "-fx-background-radius: 9;" +
                        "-fx-border-color: #5cad8a;" +
                        "-fx-border-radius: 9;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-cursor: hand;"
        ));

        // Tag-style label showing selected filename
        Label fileNameTag = new Label("Keine Datei gewählt");
        fileNameTag.setStyle(
                "-fx-background-color: #edf4f1;" +
                        "-fx-text-fill: #8aada8;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 12 6 12;" +
                        "-fx-background-radius: 20;"
        );

        fileBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("MP4-Video auswählen");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("MP4-Videos", "*.mp4")
            );
            File f = chooser.showOpenDialog(rootPane.getScene().getWindow());
            if (f != null) {
                selectedFile[0] = f;
                fileNameTag.setText("✓  " + f.getName());
                fileNameTag.setStyle(
                        "-fx-background-color: #e8f4ef;" +
                                "-fx-text-fill: #2d7a5c;" +
                                "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 12 6 12;" +
                                "-fx-background-radius: 20;"
                );
            }
        });

        HBox fileRow = new HBox(12, fileBtn, fileNameTag);
        fileRow.setAlignment(Pos.CENTER_LEFT);

        // ── Action buttons ────────────────────────────────────────────────────
        Button cancelBtn = new Button("Abbrechen");
        cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #8aada8;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 18 10 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                "-fx-background-color: #edf4f1;" +
                        "-fx-text-fill: #3d5c57;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 18 10 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #b0cdc6;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        ));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #8aada8;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 18 10 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        ));

        Button saveBtn = new Button("✚  Übung hinzufügen");
        saveBtn.setStyle(
                "-fx-background-color: #2d7a5c;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(45,122,92,0.45), 12, 0, 0, 3);"
        );
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
                "-fx-background-color: #38926e;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(45,122,92,0.65), 16, 0, 0, 4);"
        ));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
                "-fx-background-color: #2d7a5c;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 22 10 22;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(45,122,92,0.45), 12, 0, 0, 3);"
        ));

        cancelBtn.setOnAction(e -> rootPane.getChildren().remove(overlayWrapper));

        saveBtn.setOnAction(e -> {
            String title    = nameInput.getText().trim();
            String desc     = descInput.getText().trim();
            String category = catInput.getText().trim();

            if (!title.isBlank() && selectedFile[0] != null) {
                // 1. copy file into the nginx-mounted folder
                File dest = new File("data/videos/" + selectedFile[0].getName());
                try {
                    Files.copy(selectedFile[0].toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }

                // 2. store the HTTP URL, not a file path
                String videoUrl = "http://localhost:8081/videos/" +
                        URLEncoder.encode(selectedFile[0].getName(), StandardCharsets.UTF_8)
                                .replace("+", "%20"); // encode spaces as %20 not +

                repo.insert(title, desc, category, videoUrl);
                onVideoAdded.run();
                rootPane.getChildren().remove(overlayWrapper);
            }
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, cancelBtn, spacer, saveBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(6, 0, 0, 0));

        // ── Assemble card ─────────────────────────────────────────────────────
        dialogCard.getChildren().addAll(
                headerBox,
                sep,
                nameLabel, nameInput,
                catLabel, catInput,
                descLabel, descInput,
                fileLabel, fileRow,
                actions
        );

        overlayWrapper.getChildren().add(dialogCard);

        // Close on click outside the card
        overlayWrapper.setOnMouseClicked(e -> {
            if (e.getTarget() == overlayWrapper) {
                rootPane.getChildren().remove(overlayWrapper);
            }
        });

        rootPane.getChildren().add(overlayWrapper);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Label makeSectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-text-fill: #2d7a5c;" +
                        "-fx-font-family: 'Segoe UI Semibold', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 4 0 2 0;"
        );
        return l;
    }

    private static TextField makeTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-text-fill: #1a2e2a;" +
                        "-fx-prompt-text-fill: #adc0bc;" +
                        "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 16 10 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(26,46,42,0.08), 6, 0, 0, 2);"
        );
        // Green glow on focus — mirrors .search-field:focused
        tf.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            String base =
                    "-fx-background-color: #ffffff;" +
                            "-fx-text-fill: #1a2e2a;" +
                            "-fx-prompt-text-fill: #adc0bc;" +
                            "-fx-font-family: 'Segoe UI', 'Helvetica Neue', sans-serif;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 10 16 10 16;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1;";
            if (isFocused) {
                tf.setStyle(base +
                        "-fx-border-color: #5cad8a;" +
                        "-fx-effect: dropshadow(gaussian, rgba(92,173,138,0.3), 10, 0, 0, 0);");
            } else {
                tf.setStyle(base +
                        "-fx-border-color: #d8e4e0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(26,46,42,0.08), 6, 0, 0, 2);");
            }
        });
        return tf;
    }
}