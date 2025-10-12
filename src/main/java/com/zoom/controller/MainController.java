package com.zoom.controller;

import com.zoom.model.AnimationSettings;
import com.zoom.util.GifExporter;
import com.zoom.view.MainView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Main controller for the application
 */
public class MainController {
    private MainView view;
    private AnimationSettings settings;
    private Preferences prefs;
    private static final String PREF_OUTPUT_PATH = "outputPath";
    private static final String PREF_FONT_NAME = "fontName";
    private static final String PREF_FRAME_COUNT = "frameCount";
    private static final String PREF_START_SCALE = "startScale";
    private static final String PREF_MIDDLE_SCALE = "middleScale";
    private static final String PREF_END_SCALE = "endScale";
    private static final String PREF_RECENT_FONTS = "recentFonts";
    private static final String PREF_RECENT_COLORS = "recentColors";
    private static final int MAX_RECENT_ITEMS = 10;

    public MainController(MainView view) {
        this.view = view;
        this.settings = new AnimationSettings();
        this.prefs = Preferences.userNodeForPackage(MainController.class);

        // Load saved preferences
        loadPreferences();

        initializeEventHandlers();
    }

    private void loadPreferences() {
        // Load saved output path, default to C:\temp if not found
        String savedPath = prefs.get(PREF_OUTPUT_PATH, "C:\\temp");
        view.getOutputPathField().setText(savedPath);
        settings.setOutputPath(savedPath);

        // Load saved font name
        String savedFont = prefs.get(PREF_FONT_NAME, null);
        if (savedFont != null && view.getFontComboBox().getItems().contains(savedFont)) {
            view.getFontComboBox().setValue(savedFont);
        }

        // Load saved frame count
        int savedFrameCount = prefs.getInt(PREF_FRAME_COUNT, 18);
        view.getFramesSpinner().getValueFactory().setValue(savedFrameCount);

        // Load saved scales
        double savedStartScale = prefs.getDouble(PREF_START_SCALE, 100.0);
        double savedMiddleScale = prefs.getDouble(PREF_MIDDLE_SCALE, 50.0);
        double savedEndScale = prefs.getDouble(PREF_END_SCALE, 100.0);
        view.getStartScaleSpinner().getValueFactory().setValue(savedStartScale);
        view.getMiddleScaleSpinner().getValueFactory().setValue(savedMiddleScale);
        view.getEndScaleSpinner().getValueFactory().setValue(savedEndScale);

        // Load recent fonts
        String recentFontsStr = prefs.get(PREF_RECENT_FONTS, "");
        if (!recentFontsStr.isEmpty()) {
            String[] recentFonts = recentFontsStr.split(";");
            view.getRecentFontsList().getItems().addAll(recentFonts);
        }

        // Load recent colors
        String recentColorsStr = prefs.get(PREF_RECENT_COLORS, "");
        if (!recentColorsStr.isEmpty()) {
            String[] recentColors = recentColorsStr.split(";");
            view.getRecentColorsList().getItems().addAll(recentColors);
        }
    }

    private void savePreferences() {
        // Save current output path
        prefs.put(PREF_OUTPUT_PATH, view.getOutputPathField().getText());

        // Save current font
        String currentFont = view.getFontComboBox().getValue();
        if (currentFont != null) {
            prefs.put(PREF_FONT_NAME, currentFont);
        }

        // Save frame count
        prefs.putInt(PREF_FRAME_COUNT, view.getFramesSpinner().getValue());

        // Save scales
        prefs.putDouble(PREF_START_SCALE, view.getStartScaleSpinner().getValue());
        prefs.putDouble(PREF_MIDDLE_SCALE, view.getMiddleScaleSpinner().getValue());
        prefs.putDouble(PREF_END_SCALE, view.getEndScaleSpinner().getValue());

        // Save recent fonts list
        StringBuilder recentFontsStr = new StringBuilder();
        for (int i = 0; i < view.getRecentFontsList().getItems().size(); i++) {
            if (i > 0) recentFontsStr.append(";");
            recentFontsStr.append(view.getRecentFontsList().getItems().get(i));
        }
        prefs.put(PREF_RECENT_FONTS, recentFontsStr.toString());

        // Save recent colors list
        StringBuilder recentColorsStr = new StringBuilder();
        for (int i = 0; i < view.getRecentColorsList().getItems().size(); i++) {
            if (i > 0) recentColorsStr.append(";");
            recentColorsStr.append(view.getRecentColorsList().getItems().get(i));
        }
        prefs.put(PREF_RECENT_COLORS, recentColorsStr.toString());
    }

    private void addToRecentFonts(String fontName) {
        if (fontName == null || fontName.isEmpty()) return;

        // Remove if already exists
        view.getRecentFontsList().getItems().remove(fontName);

        // Add to top
        view.getRecentFontsList().getItems().add(0, fontName);

        // Limit to MAX_RECENT_ITEMS
        while (view.getRecentFontsList().getItems().size() > MAX_RECENT_ITEMS) {
            view.getRecentFontsList().getItems().remove(MAX_RECENT_ITEMS);
        }

        savePreferences();
    }

    private void addToRecentColors(String color) {
        if (color == null || color.isEmpty()) return;

        // Normalize color (remove # if present)
        color = color.replace("#", "");

        // Remove if already exists
        view.getRecentColorsList().getItems().remove(color);

        // Add to top
        view.getRecentColorsList().getItems().add(0, color);

        // Limit to MAX_RECENT_ITEMS
        while (view.getRecentColorsList().getItems().size() > MAX_RECENT_ITEMS) {
            view.getRecentColorsList().getItems().remove(MAX_RECENT_ITEMS);
        }

        savePreferences();
    }

    private void initializeEventHandlers() {
        view.getGoButton().setOnAction(event -> startAnimation());

        // Update filename field in real-time as user types
        view.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
            String sanitizedFileName = sanitizeFileName(newValue);
            view.getFileNameField().setText(sanitizedFileName);
            updatePreview();
        });

        // Update preview when font changes
        view.getFontComboBox().valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview();
        });

        // Update preview when color changes
        view.getColorField().textProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview();
        });

        // Initial preview update
        updatePreview();

        // Browse button to select output directory
        view.getBrowseButton().setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier d'export");

            // Set initial directory if it exists
            File currentPath = new File(view.getOutputPathField().getText());
            if (currentPath.exists() && currentPath.isDirectory()) {
                directoryChooser.setInitialDirectory(currentPath);
            }

            File selectedDirectory = directoryChooser.showDialog(view.getStage());
            if (selectedDirectory != null) {
                view.getOutputPathField().setText(selectedDirectory.getAbsolutePath());
                savePreferences(); // Save the new path
            }
        });

        // Save preferences when output path field changes
        view.getOutputPathField().textProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        // Save preferences when font changes
        view.getFontComboBox().valueProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        // Save preferences when frame count changes
        view.getFramesSpinner().valueProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        // Save preferences when scales change
        view.getStartScaleSpinner().valueProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        view.getMiddleScaleSpinner().valueProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        view.getEndScaleSpinner().valueProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });

        // Handle recent fonts list selection
        view.getRecentFontsList().setOnMouseClicked(event -> {
            String selectedFont = view.getRecentFontsList().getSelectionModel().getSelectedItem();
            if (selectedFont != null) {
                view.getFontComboBox().setValue(selectedFont);
            }
        });

        // Handle recent colors list selection
        view.getRecentColorsList().setOnMouseClicked(event -> {
            String selectedColor = view.getRecentColorsList().getSelectionModel().getSelectedItem();
            if (selectedColor != null) {
                view.getColorField().setText(selectedColor);
            }
        });
    }

    private void startAnimation() {
        // Read settings from view
        settings.setText(view.getTextField().getText());
        settings.setFontName(view.getFontComboBox().getValue());
        settings.setNumberOfFrames(view.getFramesSpinner().getValue());
        settings.setStartScale(view.getStartScaleSpinner().getValue());
        settings.setMiddleScale(view.getMiddleScaleSpinner().getValue());
        settings.setEndScale(view.getEndScaleSpinner().getValue());
        settings.setTextColor(view.getColorField().getText());
        settings.setOutputPath(view.getOutputPathField().getText());

        // Use text field content as filename (sanitized)
        String fileName = view.getFileNameField().getText();
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = sanitizeFileName(settings.getText());
        }
        settings.setOutputFileName(fileName);

        // Validate inputs
        if (settings.getText() == null || settings.getText().trim().isEmpty()) {
            showStatus("Erreur: Veuillez entrer du texte", true);
            return;
        }

        if (settings.getFontName() == null || settings.getFontName().isEmpty()) {
            showStatus("Erreur: Veuillez sélectionner une police", true);
            return;
        }

        if (settings.getOutputFileName() == null || settings.getOutputFileName().trim().isEmpty()) {
            showStatus("Erreur: Veuillez entrer un nom de fichier", true);
            return;
        }

        // Add to recent lists
        addToRecentFonts(settings.getFontName());
        addToRecentColors(settings.getTextColor());

        // Disable button during processing
        view.getGoButton().setDisable(true);
        view.getProgressBar().setVisible(true);
        view.getProgressBar().setProgress(0);

        // Run animation generation in background thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                generateAnimation();
                return null;
            }

            @Override
            protected void succeeded() {
                view.getGoButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                String outputPath = settings.getOutputPath() + File.separator + settings.getOutputFileName() + ".gif";
                showStatus("Animation créée avec succès: " + outputPath, false);
            }

            @Override
            protected void failed() {
                view.getGoButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                Throwable error = getException();
                showStatus("Erreur: " + (error != null ? error.getMessage() : "Unknown error"), true);
                error.printStackTrace();
            }
        };

        new Thread(task).start();
    }

    private void generateAnimation() throws Exception {
        int totalFrames = settings.getNumberOfFrames();
        List<BufferedImage> frames = new ArrayList<>();

        // Much larger size for highest quality
        int frameWidth = 3840;
        int frameHeight = 2160;

        showStatus("Génération des frames (" + totalFrames + " images)...", false);

        // Calculate scale for each frame
        int halfFrames = totalFrames / 2;

        for (int i = 0; i < totalFrames; i++) {
            double scale;

            if (i < halfFrames) {
                // Zoom in phase (start to middle)
                double progress = (double) i / halfFrames;
                scale = settings.getStartScale() +
                        (settings.getMiddleScale() - settings.getStartScale()) * progress;
            } else {
                // Zoom out phase (middle to end)
                double progress = (double) (i - halfFrames) / (totalFrames - halfFrames);
                scale = settings.getMiddleScale() +
                        (settings.getEndScale() - settings.getMiddleScale()) * progress;
            }

            BufferedImage frame = GifExporter.createTextFrame(
                    settings.getText(),
                    settings.getFontName(),
                    scale,
                    frameWidth,
                    frameHeight,
                    settings.getTextColor()
            );
            frames.add(frame);

            // Update progress and status
            final int frameNum = i + 1;
            final double progress = (double) frameNum / totalFrames;
            Platform.runLater(() -> {
                view.getProgressBar().setProgress(progress);
                showStatus("Génération des frames: " + frameNum + "/" + totalFrames, false);
            });
        }

        showStatus("Calcul du crop automatique...", false);

        // Export to GIF
        String outputPath = settings.getOutputPath() + File.separator + settings.getOutputFileName() + ".gif";

        // Create output directory if it doesn't exist
        File outputDir = new File(settings.getOutputPath());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        showStatus("Encodage du GIF...", false);

        // Calculate delay: slower animation (3 seconds total duration)
        int delayMs = Math.max(50, 3000 / totalFrames);

        GifExporter.exportToGif(frames, outputPath, delayMs);
    }

    private void showStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            view.getStatusLabel().setText(message);
            if (isError) {
                view.getStatusLabel().setStyle("-fx-text-fill: red;");
            } else {
                view.getStatusLabel().setStyle("-fx-text-fill: green;");
            }
        });
    }

    private String sanitizeFileName(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "animation";
        }
        // Remove invalid filename characters and limit length
        return text.replaceAll("[\\\\/:*?\"<>|]", "_")
                   .replaceAll("\\s+", "_")
                   .substring(0, Math.min(text.length(), 50));
    }

    private void updatePreview() {
        String text = view.getTextField().getText();
        String fontName = view.getFontComboBox().getValue();
        String colorHex = view.getColorField().getText();

        // Update preview label
        if (text != null && !text.trim().isEmpty()) {
            view.getPreviewLabel().setText(text);
        } else {
            view.getPreviewLabel().setText("Texte");
        }

        // Build style string with font fallback
        StringBuilder style = new StringBuilder();
        style.append("-fx-font-size: 48px; -fx-font-weight: bold; ");

        // Set font family with Arial as fallback for special characters
        if (fontName != null && !fontName.isEmpty()) {
            style.append("-fx-font-family: \"").append(fontName).append("\", Arial; ");
        } else {
            style.append("-fx-font-family: Arial; ");
        }

        // Set color
        if (colorHex != null && !colorHex.isEmpty()) {
            String color = colorHex.startsWith("#") ? colorHex : "#" + colorHex;
            style.append("-fx-text-fill: ").append(color).append("; ");
        } else {
            style.append("-fx-text-fill: #ff00eb; ");
        }

        style.append("-fx-padding: 20px; -fx-background-color: #f0f0f0; -fx-background-radius: 5px;");

        view.getPreviewLabel().setStyle(style.toString());
    }
}
