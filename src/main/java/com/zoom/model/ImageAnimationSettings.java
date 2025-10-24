package com.zoom.model;

import java.io.File;

/**
 * Model class holding all parameters for image animation
 */
public class ImageAnimationSettings {
    private File sourceImageFile;
    private int numberOfFrames;
    private double startScale;
    private double middleScale;
    private double endScale;
    private String outputDirectory;
    private String outputFileName;

    public ImageAnimationSettings() {
    }

    public File getSourceImageFile() {
        return sourceImageFile;
    }

    public void setSourceImageFile(File sourceImageFile) {
        this.sourceImageFile = sourceImageFile;
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

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public File getOutputFile() {
        return new File(outputDirectory + File.separator + outputFileName);
    }
}
