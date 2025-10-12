package com.zoom.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Main view class for the application
 */
public class MainView {
    private Stage stage;
    private ComboBox<String> fontComboBox;
    private TextField textField;
    private TextField colorField;
    private Spinner<Integer> framesSpinner;
    private Spinner<Double> startScaleSpinner;
    private Spinner<Double> middleScaleSpinner;
    private Spinner<Double> endScaleSpinner;
    private TextField fileNameField;
    private TextField outputPathField;
    private Button browseButton;
    private Button goButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label previewLabel;
    private ListView<String> recentFontsList;
    private ListView<String> recentColorsList;

    public MainView(Stage stage) {
        this.stage = stage;
        initialize();
    }

    private void initialize() {
        stage.setTitle("Animateur Zoom/Dezoom de Texte");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        // Text input
        HBox textBox = new HBox(10);
        textBox.setAlignment(Pos.CENTER_LEFT);
        Label textLabel = new Label("Texte:");
        textLabel.setMinWidth(120);
        textField = new TextField("Texte");
        textField.setPrefWidth(300);
        textBox.getChildren().addAll(textLabel, textField);

        // Font selection with recent fonts
        HBox fontBox = new HBox(10);
        fontBox.setAlignment(Pos.CENTER_LEFT);
        Label fontLabel = new Label("Police:");
        fontLabel.setMinWidth(120);
        fontComboBox = new ComboBox<>();
        fontComboBox.setPrefWidth(200);
        loadSystemFonts();

        // Recent fonts ListView
        recentFontsList = new ListView<>();
        recentFontsList.setPrefWidth(150);
        recentFontsList.setPrefHeight(80);
        recentFontsList.setPlaceholder(new Label("Récentes"));

        fontBox.getChildren().addAll(fontLabel, fontComboBox, recentFontsList);

        // Color input with recent colors
        HBox colorBox = new HBox(10);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        Label colorLabel = new Label("Couleur (hex):");
        colorLabel.setMinWidth(120);
        colorField = new TextField("ff00eb");
        colorField.setPrefWidth(100);
        Label hashLabel = new Label("#");

        // Recent colors ListView
        recentColorsList = new ListView<>();
        recentColorsList.setPrefWidth(150);
        recentColorsList.setPrefHeight(80);
        recentColorsList.setPlaceholder(new Label("Récentes"));

        colorBox.getChildren().addAll(colorLabel, hashLabel, colorField, recentColorsList);

        // Preview label
        previewLabel = new Label("Texte");
        previewLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #ff00eb; -fx-padding: 20px; -fx-background-color: #f0f0f0; -fx-background-radius: 5px;");
        previewLabel.setMinHeight(100);
        previewLabel.setAlignment(Pos.CENTER);
        previewLabel.setMaxWidth(Double.MAX_VALUE);

        // Parameters grid
        GridPane paramsGrid = new GridPane();
        paramsGrid.setHgap(10);
        paramsGrid.setVgap(10);
        paramsGrid.setPadding(new Insets(10, 0, 10, 0));

        // Number of frames
        Label framesLabel = new Label("Nombre d'images:");
        framesLabel.setMinWidth(120);
        framesSpinner = new Spinner<>(1, 100, 18);
        framesSpinner.setEditable(true);
        framesSpinner.setPrefWidth(100);
        paramsGrid.add(framesLabel, 0, 0);
        paramsGrid.add(framesSpinner, 1, 0);

        // Start scale
        Label startLabel = new Label("Échelle début (%):");
        startLabel.setMinWidth(120);
        startScaleSpinner = new Spinner<>(1.0, 500.0, 100.0, 5.0);
        startScaleSpinner.setEditable(true);
        startScaleSpinner.setPrefWidth(100);
        paramsGrid.add(startLabel, 0, 1);
        paramsGrid.add(startScaleSpinner, 1, 1);

        // Middle scale
        Label middleLabel = new Label("Échelle milieu (%):");
        middleLabel.setMinWidth(120);
        middleScaleSpinner = new Spinner<>(1.0, 500.0, 50.0, 5.0);
        middleScaleSpinner.setEditable(true);
        middleScaleSpinner.setPrefWidth(100);
        paramsGrid.add(middleLabel, 0, 2);
        paramsGrid.add(middleScaleSpinner, 1, 2);

        // End scale
        Label endLabel = new Label("Échelle fin (%):");
        endLabel.setMinWidth(120);
        endScaleSpinner = new Spinner<>(1.0, 500.0, 100.0, 5.0);
        endScaleSpinner.setEditable(true);
        endScaleSpinner.setPrefWidth(100);
        paramsGrid.add(endLabel, 0, 3);
        paramsGrid.add(endScaleSpinner, 1, 3);

        // Output path
        HBox pathBox = new HBox(10);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        Label pathLabel = new Label("Dossier export:");
        pathLabel.setMinWidth(120);
        outputPathField = new TextField("C:\\temp");
        outputPathField.setPrefWidth(250);
        browseButton = new Button("Parcourir...");
        pathBox.getChildren().addAll(pathLabel, outputPathField, browseButton);

        // Output filename (will be auto-updated from text field)
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        Label fileLabel = new Label("Nom du fichier:");
        fileLabel.setMinWidth(120);
        fileNameField = new TextField("Texte");
        fileNameField.setPrefWidth(200);
        Label gifLabel = new Label(".gif");
        fileBox.getChildren().addAll(fileLabel, fileNameField, gifLabel);

        // Go button
        goButton = new Button("GO");
        goButton.setPrefWidth(150);
        goButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Progress bar and status
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #666666;");

        // Add all to root
        root.getChildren().addAll(
                textBox,
                fontBox,
                colorBox,
                previewLabel,
                new Separator(),
                paramsGrid,
                new Separator(),
                pathBox,
                fileBox,
                goButton,
                progressBar,
                statusLabel
        );

        Scene scene = new Scene(root, 700, 750);
        stage.setScene(scene);
    }

    private void loadSystemFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox.getItems().addAll(Arrays.asList(fontNames));

        // Set default font
        if (fontComboBox.getItems().contains("Arial")) {
            fontComboBox.setValue("Arial");
        } else if (!fontComboBox.getItems().isEmpty()) {
            fontComboBox.setValue(fontComboBox.getItems().get(0));
        }
    }

    public void show() {
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    // Getters for UI components
    public ComboBox<String> getFontComboBox() {
        return fontComboBox;
    }

    public TextField getTextField() {
        return textField;
    }

    public TextField getColorField() {
        return colorField;
    }

    public Spinner<Integer> getFramesSpinner() {
        return framesSpinner;
    }

    public Spinner<Double> getStartScaleSpinner() {
        return startScaleSpinner;
    }

    public Spinner<Double> getMiddleScaleSpinner() {
        return middleScaleSpinner;
    }

    public Spinner<Double> getEndScaleSpinner() {
        return endScaleSpinner;
    }

    public TextField getFileNameField() {
        return fileNameField;
    }

    public TextField getOutputPathField() {
        return outputPathField;
    }

    public Button getBrowseButton() {
        return browseButton;
    }

    public Button getGoButton() {
        return goButton;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getPreviewLabel() {
        return previewLabel;
    }

    public ListView<String> getRecentFontsList() {
        return recentFontsList;
    }

    public ListView<String> getRecentColorsList() {
        return recentColorsList;
    }
}
