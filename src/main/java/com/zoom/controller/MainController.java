package com.zoom.controller;

import com.zoom.model.AnimationSettings;
import com.zoom.model.ImageAnimationSettings;
import com.zoom.util.GifExporter;
import com.zoom.util.ImageGifExporter;
import com.zoom.view.MainView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

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
    private ImageAnimationSettings imageSettings;
    private File selectedImageFile;
    private Preferences prefs;
    private static final String PREF_OUTPUT_PATH = "outputPath";
    private static final String PREF_IMAGE_EXPORT_PATH = "imageExportPath";
    private static final String PREF_LAST_IMAGE_SOURCE_DIR = "lastImageSourceDir";
    private static final String PREF_FONT_NAME = "fontName";
    private static final String PREF_FRAME_COUNT = "frameCount";
    private static final String PREF_CYCLES = "cycles";
    private static final String PREF_START_SCALE = "startScale";
    private static final String PREF_MIDDLE_SCALE = "middleScale";
    private static final String PREF_END_SCALE = "endScale";
    private static final String PREF_RECENT_FONTS = "recentFonts";
    private static final String PREF_RECENT_COLORS = "recentColors";
    private static final int MAX_RECENT_ITEMS = 10;

    public MainController(MainView view) {
        this.view = view;
        this.settings = new AnimationSettings();
        this.imageSettings = new ImageAnimationSettings();
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

        // Load saved image export path
        String savedImageExportPath = prefs.get(PREF_IMAGE_EXPORT_PATH, "C:\\temp");
        view.getImageExportPathField().setText(savedImageExportPath);

        // Load saved font name
        String savedFont = prefs.get(PREF_FONT_NAME, null);
        if (savedFont != null && view.getFontComboBox().getItems().contains(savedFont)) {
            view.getFontComboBox().setValue(savedFont);
        }

        // Load saved frame count
        int savedFrameCount = prefs.getInt(PREF_FRAME_COUNT, 18);
        view.getFramesSpinner().getValueFactory().setValue(savedFrameCount);

        // Load saved cycles
        int savedCycles = prefs.getInt(PREF_CYCLES, 1);
        view.getCyclesSpinner().getValueFactory().setValue(savedCycles);

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

        // Save image export path
        prefs.put(PREF_IMAGE_EXPORT_PATH, view.getImageExportPathField().getText());

        // Save current font
        String currentFont = view.getFontComboBox().getValue();
        if (currentFont != null) {
            prefs.put(PREF_FONT_NAME, currentFont);
        }

        // Save frame count
        prefs.putInt(PREF_FRAME_COUNT, view.getFramesSpinner().getValue());

        // Save cycles
        prefs.putInt(PREF_CYCLES, view.getCyclesSpinner().getValue());

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
        view.getGoTextButton().setOnAction(event -> startTextAnimation());
        view.getGoImageButton().setOnAction(event -> startImageAnimation());

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

        // Save preferences when cycles change
        view.getCyclesSpinner().valueProperty().addListener((observable, oldValue, newValue) -> {
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

        // Handle image selection button
        view.getSelectImageButton().setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner une image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );

            // Set initial directory from last used location
            String lastImageDir = prefs.get(PREF_LAST_IMAGE_SOURCE_DIR, null);
            if (lastImageDir != null) {
                File lastDir = new File(lastImageDir);
                if (lastDir.exists() && lastDir.isDirectory()) {
                    fileChooser.setInitialDirectory(lastDir);
                }
            }

            File selected = fileChooser.showOpenDialog(view.getStage());
            if (selected != null) {
                selectedImageFile = selected;
                view.getImagePathField().setText(selected.getAbsolutePath());

                // Update filename field with image name (without extension)
                String fileName = selected.getName();
                String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                view.getFileNameField().setText(fileNameWithoutExt);

                // Save the directory for next time
                prefs.put(PREF_LAST_IMAGE_SOURCE_DIR, selected.getParent());
            }
        });

        // Handle image export directory browser
        view.getBrowseImageExportButton().setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier d'export image");

            File currentPath = new File(view.getImageExportPathField().getText());
            if (currentPath.exists() && currentPath.isDirectory()) {
                directoryChooser.setInitialDirectory(currentPath);
            }

            File selectedDirectory = directoryChooser.showDialog(view.getStage());
            if (selectedDirectory != null) {
                view.getImageExportPathField().setText(selectedDirectory.getAbsolutePath());
                savePreferences();
            }
        });

        // Save preferences when image export path changes
        view.getImageExportPathField().textProperty().addListener((observable, oldValue, newValue) -> {
            savePreferences();
        });
    }

    private void startTextAnimation() {
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

        // Check if output file already exists
        String outputPath = settings.getOutputPath() + File.separator + settings.getOutputFileName() + ".gif";
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            boolean confirmed = showConfirmOverwrite(outputFile.getName());
            if (!confirmed) {
                return; // User cancelled
            }
        }

        // Add to recent lists
        addToRecentFonts(settings.getFontName());
        addToRecentColors(settings.getTextColor());

        // Disable buttons during processing
        view.getGoTextButton().setDisable(true);
        view.getGoImageButton().setDisable(true);
        view.getProgressBar().setVisible(true);
        view.getProgressBar().setProgress(0);

        // Run animation generation in background thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                generateTextAnimation();
                return null;
            }

            @Override
            protected void succeeded() {
                view.getGoTextButton().setDisable(false);
                view.getGoImageButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                String outputPath = settings.getOutputPath() + File.separator + settings.getOutputFileName() + ".gif";
                showStatus("Animation créée avec succès: " + outputPath, false);
            }

            @Override
            protected void failed() {
                view.getGoTextButton().setDisable(false);
                view.getGoImageButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                Throwable error = getException();
                showStatus("Erreur: " + (error != null ? error.getMessage() : "Unknown error"), true);
                error.printStackTrace();
            }
        };

        new Thread(task).start();
    }

    private void startImageAnimation() {
        // Read settings from view
        imageSettings.setSourceImageFile(selectedImageFile);
        imageSettings.setNumberOfFrames(view.getFramesSpinner().getValue());
        imageSettings.setStartScale(view.getStartScaleSpinner().getValue());
        imageSettings.setMiddleScale(view.getMiddleScaleSpinner().getValue());
        imageSettings.setEndScale(view.getEndScaleSpinner().getValue());
        imageSettings.setOutputDirectory(view.getImageExportPathField().getText());

        // Use filename from the field (same field as text mode)
        String fileName = view.getFileNameField().getText();
        if (fileName == null || fileName.trim().isEmpty()) {
            // Fallback to source image name if field is empty
            String sourceFileName = selectedImageFile.getName();
            fileName = sourceFileName.substring(0, sourceFileName.lastIndexOf('.'));
        }
        imageSettings.setOutputFileName(fileName + ".gif");

        // Validate inputs
        if (!selectedImageFile.exists()) {
            showStatus("Erreur: Le fichier image n'existe pas", true);
            return;
        }

        // Check if output file already exists
        String outputPath = imageSettings.getOutputDirectory() + File.separator + imageSettings.getOutputFileName();
        File outputFile = new File(outputPath);
        if (outputFile.exists()) {
            boolean confirmed = showConfirmOverwrite(outputFile.getName());
            if (!confirmed) {
                return; // User cancelled
            }
        }

        // Disable buttons during processing
        view.getGoTextButton().setDisable(true);
        view.getGoImageButton().setDisable(true);
        view.getProgressBar().setVisible(true);
        view.getProgressBar().setProgress(0);

        // Run animation generation in background thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                generateImageAnimation();
                return null;
            }

            @Override
            protected void succeeded() {
                view.getGoTextButton().setDisable(false);
                view.getGoImageButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                String outputPath = imageSettings.getOutputDirectory() + File.separator + imageSettings.getOutputFileName();
                showStatus("Animation image créée avec succès: " + outputPath, false);
            }

            @Override
            protected void failed() {
                view.getGoTextButton().setDisable(false);
                view.getGoImageButton().setDisable(false);
                view.getProgressBar().setVisible(false);
                Throwable error = getException();
                showStatus("Erreur: " + (error != null ? error.getMessage() : "Unknown error"), true);
                error.printStackTrace();
            }
        };

        new Thread(task).start();
    }

    private void generateTextAnimation() throws Exception {
        int framesPerCycle = settings.getNumberOfFrames();
        int cycles = view.getCyclesSpinner().getValue();
        int totalFrames = framesPerCycle * cycles;
        List<BufferedImage> frames = new ArrayList<>();

        // Much larger size for highest quality
        int frameWidth = 3840;
        int frameHeight = 2160;

        showStatus("Génération des frames (" + totalFrames + " images, " + cycles + " cycle(s))...", StatusColor.PROCESSING);

        // Calculate scale for each frame
        int halfFrames = framesPerCycle / 2;

        for (int cycle = 0; cycle < cycles; cycle++) {
            for (int i = 0; i < framesPerCycle; i++) {
                double scale;

                if (i < halfFrames) {
                    // Zoom in phase (start to middle)
                    double progress = (double) i / halfFrames;
                    scale = settings.getStartScale() +
                            (settings.getMiddleScale() - settings.getStartScale()) * progress;
                } else {
                    // Zoom out phase (middle to end)
                    double progress = (double) (i - halfFrames) / (framesPerCycle - halfFrames);
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
                final int frameNum = (cycle * framesPerCycle) + i + 1;
                final double progress = (double) frameNum / totalFrames;
                Platform.runLater(() -> {
                    view.getProgressBar().setProgress(progress);
                    showStatus("Génération des frames: " + frameNum + "/" + totalFrames, StatusColor.PROCESSING);
                });
            }
        }

        showStatus("Calcul du crop automatique...", StatusColor.PROCESSING);

        // Export to GIF
        String outputPath = settings.getOutputPath() + File.separator + settings.getOutputFileName() + ".gif";

        // Create output directory if it doesn't exist
        File outputDir = new File(settings.getOutputPath());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        showStatus("Encodage du GIF...", StatusColor.PROCESSING);

        // Calculate delay: slower animation (3 seconds total duration)
        int delayMs = Math.max(50, 3000 / totalFrames);

        GifExporter.exportToGif(frames, outputPath, delayMs);
    }

    private void generateImageAnimation() throws Exception {
        int framesPerCycle = imageSettings.getNumberOfFrames();
        int cycles = view.getCyclesSpinner().getValue();
        int totalFrames = framesPerCycle * cycles;
        List<BufferedImage> frames = new ArrayList<>();

        // 4K resolution for highest quality
        int frameWidth = 3840;
        int frameHeight = 2160;

        showStatus("Chargement de l'image source...", StatusColor.PROCESSING);

        // Load source image
        BufferedImage sourceImage = ImageGifExporter.loadImage(imageSettings.getSourceImageFile());

        showStatus("Génération des frames (" + totalFrames + " images, " + cycles + " cycle(s))...", StatusColor.PROCESSING);

        // Calculate scale for each frame (same logic as text animation)
        int halfFrames = framesPerCycle / 2;

        for (int cycle = 0; cycle < cycles; cycle++) {
            for (int i = 0; i < framesPerCycle; i++) {
                double scale;

                if (i < halfFrames) {
                    // First phase (start to middle)
                    double progress = (double) i / halfFrames;
                    scale = imageSettings.getStartScale() +
                            (imageSettings.getMiddleScale() - imageSettings.getStartScale()) * progress;
                } else {
                    // Second phase (middle to end)
                    double progress = (double) (i - halfFrames) / (framesPerCycle - halfFrames);
                    scale = imageSettings.getMiddleScale() +
                            (imageSettings.getEndScale() - imageSettings.getMiddleScale()) * progress;
                }

                BufferedImage frame = ImageGifExporter.createImageFrame(
                        sourceImage,
                        scale,
                        frameWidth,
                        frameHeight
                );
                frames.add(frame);

                // Update progress
                final int frameNum = (cycle * framesPerCycle) + i + 1;
                final double progress = (double) frameNum / totalFrames;
                Platform.runLater(() -> {
                    view.getProgressBar().setProgress(progress);
                    showStatus("Génération des frames: " + frameNum + "/" + totalFrames, StatusColor.PROCESSING);
                });
            }
        }

        showStatus("Calcul du crop automatique...", StatusColor.PROCESSING);

        // Export to GIF
        String outputPath = imageSettings.getOutputDirectory() + File.separator + imageSettings.getOutputFileName();

        // Create output directory if it doesn't exist
        File outputDir = new File(imageSettings.getOutputDirectory());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        showStatus("Encodage du GIF...", StatusColor.PROCESSING);

        // Calculate delay: 3 seconds total duration
        int delayMs = Math.max(50, 3000 / totalFrames);

        ImageGifExporter.exportToGif(frames, outputPath, delayMs);
    }

    private void showStatus(String message, boolean isError) {
        showStatus(message, isError ? StatusColor.ERROR : StatusColor.SUCCESS);
    }

    private void showStatus(String message, StatusColor color) {
        Platform.runLater(() -> {
            view.getStatusLabel().setText(message);
            switch (color) {
                case PROCESSING:
                    view.getStatusLabel().setStyle("-fx-text-fill: blue;");
                    break;
                case SUCCESS:
                    view.getStatusLabel().setStyle("-fx-text-fill: green;");
                    break;
                case ERROR:
                    view.getStatusLabel().setStyle("-fx-text-fill: red;");
                    break;
            }
        });
    }

    private enum StatusColor {
        PROCESSING, SUCCESS, ERROR
    }

    private boolean showConfirmOverwrite(String fileName) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Le fichier existe déjà");
        alert.setContentText("Le fichier '" + fileName + "' existe déjà.\nVoulez-vous l'écraser ?");

        javafx.scene.control.ButtonType buttonTypeYes = new javafx.scene.control.ButtonType("Oui");
        javafx.scene.control.ButtonType buttonTypeNo = new javafx.scene.control.ButtonType("Non", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeYes;
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
