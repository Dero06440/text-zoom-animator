package com.zoom.model;

/**
 * Model class representing animation settings
 */
public class AnimationSettings {
    private String text;
    private String fontName;
    private int numberOfFrames;
    private double startScale;
    private double middleScale;
    private double endScale;
    private String outputFileName;
    private String textColor;
    private String outputPath;

    public AnimationSettings() {
        // Default values
        this.numberOfFrames = 18;
        this.startScale = 100.0;
        this.middleScale = 50.0;
        this.endScale = 100.0;
        this.text = "Texte";
        this.fontName = "Arial";
        this.outputFileName = "animation";
        this.textColor = "ff00eb"; // Default pink color
        this.outputPath = "C:\\temp"; // Default export path
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public double getStartScale() {
        return startScale;
    }

    public void setStartScale(double startScale) {
        this.startScale = startScale;
    }

    public double getMiddleScale() {
        return middleScale;
    }

    public void setMiddleScale(double middleScale) {
        this.middleScale = middleScale;
    }

    public double getEndScale() {
        return endScale;
    }

    public void setEndScale(double endScale) {
        this.endScale = endScale;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
