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
    private Spinner<Integer> cyclesSpinner;
    private Spinner<Double> startScaleSpinner;
    private Spinner<Double> middleScaleSpinner;
    private Spinner<Double> endScaleSpinner;
    private TextField fileNameField;
    private TextField outputPathField;
    private Button browseButton;
    private Button goTextButton;
    private Button goImageButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label previewLabel;
    private ListView<String> recentFontsList;
    private ListView<String> recentColorsList;
    private Button selectImageButton;
    private TextField imagePathField;
    private TextField imageExportPathField;
    private Button browseImageExportButton;

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

        // Image selection section
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        Label imageLabel = new Label("Image source:");
        imageLabel.setMinWidth(120);
        selectImageButton = new Button("Choisir une image...");
        selectImageButton.setPrefWidth(200);
        imagePathField = new TextField();
        imagePathField.setPrefWidth(300);
        imagePathField.setEditable(false);
        imagePathField.setPromptText("Aucune image sélectionnée");
        imageBox.getChildren().addAll(imageLabel, selectImageButton, imagePathField);

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

        // Cycles
        Label cyclesLabel = new Label("Cycles:");
        cyclesLabel.setMinWidth(120);
        cyclesSpinner = new Spinner<>(1, 20, 1);
        cyclesSpinner.setEditable(true);
        cyclesSpinner.setPrefWidth(100);
        paramsGrid.add(cyclesLabel, 0, 4);
        paramsGrid.add(cyclesSpinner, 1, 4);

        // Text output path
        HBox pathBox = new HBox(10);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        Label pathLabel = new Label("Dossier export texte:");
        pathLabel.setMinWidth(120);
        outputPathField = new TextField("C:\\temp");
        outputPathField.setPrefWidth(250);
        browseButton = new Button("Parcourir...");
        pathBox.getChildren().addAll(pathLabel, outputPathField, browseButton);

        // Image export path
        HBox imageExportBox = new HBox(10);
        imageExportBox.setAlignment(Pos.CENTER_LEFT);
        Label imageExportLabel = new Label("Dossier export image:");
        imageExportLabel.setMinWidth(120);
        imageExportPathField = new TextField("C:\\temp");
        imageExportPathField.setPrefWidth(250);
        browseImageExportButton = new Button("Parcourir...");
        imageExportBox.getChildren().addAll(imageExportLabel, imageExportPathField, browseImageExportButton);

        // Output filename (will be auto-updated from text field)
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        Label fileLabel = new Label("Nom du fichier:");
        fileLabel.setMinWidth(120);
        fileNameField = new TextField("Texte");
        fileNameField.setPrefWidth(200);
        Label gifLabel = new Label(".gif");
        fileBox.getChildren().addAll(fileLabel, fileNameField, gifLabel);

        // Go buttons (Text and Image)
        HBox goButtonsBox = new HBox(10);
        goButtonsBox.setAlignment(Pos.CENTER_LEFT);
        goTextButton = new Button("GO Texte");
        goTextButton.setPrefWidth(150);
        goTextButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        goImageButton = new Button("GO Image");
        goImageButton.setPrefWidth(150);
        goImageButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #2196F3; -fx-text-fill: white;");
        goButtonsBox.getChildren().addAll(goTextButton, goImageButton);

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
                imageBox,
                previewLabel,
                new Separator(),
                paramsGrid,
                new Separator(),
                pathBox,
                imageExportBox,
                fileBox,
                goButtonsBox,
                progressBar,
                statusLabel
        );

        Scene scene = new Scene(root, 700, 850);
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

    public Button getGoTextButton() {
        return goTextButton;
    }

    public Button getGoImageButton() {
        return goImageButton;
    }

    public Spinner<Integer> getCyclesSpinner() {
        return cyclesSpinner;
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

    public Button getSelectImageButton() {
        return selectImageButton;
    }

    public TextField getImagePathField() {
        return imagePathField;
    }

    public TextField getImageExportPathField() {
        return imageExportPathField;
    }

    public Button getBrowseImageExportButton() {
        return browseImageExportButton;
    }
}
