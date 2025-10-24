package com.zoom.util;

import com.madgag.gif.fmsware.AnimatedGifEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating animated GIFs with zoom effects on images
 */
public class ImageGifExporter {

    /**
     * Create a frame with an image at specified scale
     *
     * @param sourceImage Source image to scale
     * @param scale Scale percentage (100 = normal size)
     * @param width Frame width (4K: 3840)
     * @param height Frame height (4K: 2160)
     * @return BufferedImage with scaled image centered
     */
    public static BufferedImage createImageFrame(BufferedImage sourceImage, double scale, int width, int height) {
        // Create image with transparent background
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        // Start with fully transparent background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Calculate scaled dimensions
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        double scaleFactor = scale / 100.0;
        int scaledWidth = (int) (sourceWidth * scaleFactor);
        int scaledHeight = (int) (sourceHeight * scaleFactor);

        // Center the scaled image
        int x = (width - scaledWidth) / 2;
        int y = (height - scaledHeight) / 2;

        // Draw the scaled image
        g2d.drawImage(sourceImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // Keep the image as-is with proper alpha channel
        // Don't convert to white background - we want to preserve original transparency
        return image;
    }

    /**
     * Export frames to an animated GIF file
     *
     * @param frames List of BufferedImage frames
     * @param outputPath Full path to output GIF file
     * @param delayMs Delay between frames in milliseconds
     * @throws Exception if export fails
     */
    public static void exportToGif(List<BufferedImage> frames, String outputPath, int delayMs) throws Exception {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("No frames to export");
        }

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Calculate bounding box across all frames
        Rectangle bounds = calculateBoundingBox(frames);

        // Crop all frames to the bounding box
        List<BufferedImage> croppedFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            BufferedImage cropped = frame.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
            croppedFrames.add(cropped);
        }

        // Convert frames to indexed color with transparency marker
        List<BufferedImage> transparentFrames = new ArrayList<>();
        Color transparentMarker = new Color(255, 0, 255); // Magenta as transparency marker

        for (BufferedImage frame : croppedFrames) {
            BufferedImage indexed = convertToTransparentIndexed(frame, transparentMarker);
            transparentFrames.add(indexed);
        }

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(outputStream);
            encoder.setDelay(delayMs);
            encoder.setRepeat(0); // Loop indefinitely

            // Set magenta as the transparent color
            encoder.setTransparent(transparentMarker);
            encoder.setDispose(2); // Restore to background color

            for (BufferedImage frame : transparentFrames) {
                encoder.addFrame(frame);
            }

            encoder.finish();
        }
    }

    /**
     * Calculate the bounding box that contains all non-transparent content across all frames
     */
    private static Rectangle calculateBoundingBox(List<BufferedImage> frames) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;

        for (BufferedImage frame : frames) {
            int width = frame.getWidth();
            int height = frame.getHeight();

            // Find bounds for this frame
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = frame.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    // Check if pixel is not fully transparent (alpha > 0)
                    if (alpha > 0) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
        }

        // Add minimal padding (10 pixels)
        int padding = 10;
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(frames.get(0).getWidth() - 1, maxX + padding);
        maxY = Math.min(frames.get(0).getHeight() - 1, maxY + padding);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return new Rectangle(minX, minY, width, height);
    }

    /**
     * Convert ARGB image to RGB, replacing transparent pixels with a marker color
     *
     * @param source Source image with alpha channel
     * @param transparentMarker Color to use for transparent pixels
     * @return BufferedImage in RGB format
     */
    private static BufferedImage convertToTransparentIndexed(BufferedImage source, Color transparentMarker) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int markerRGB = transparentMarker.getRGB();

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;

                // If pixel is fully or mostly transparent, use marker color
                if (alpha < 128) {
                    result.setRGB(x, y, markerRGB);
                } else {
                    // Keep the original color (without alpha)
                    int rgb = argb & 0x00FFFFFF;
                    result.setRGB(x, y, rgb);
                }
            }
        }

        return result;
    }

    /**
     * Load an image from file
     *
     * @param imageFile Image file to load
     * @return BufferedImage
     * @throws Exception if loading fails
     */
    public static BufferedImage loadImage(File imageFile) throws Exception {
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imageFile.getAbsolutePath());
        }
        return img;
    }
}
