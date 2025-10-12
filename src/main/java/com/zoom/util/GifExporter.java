package com.zoom.util;

import com.madgag.gif.fmsware.AnimatedGifEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for exporting frames as an animated GIF
 */
public class GifExporter {

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
        java.awt.Rectangle bounds = calculateBoundingBox(frames);

        // Crop all frames to the bounding box
        List<BufferedImage> croppedFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            BufferedImage cropped = frame.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
            croppedFrames.add(cropped);
        }

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(outputStream);
            encoder.setDelay(delayMs);
            encoder.setRepeat(0); // Loop indefinitely

            // Create a specific transparent color (pure white with full transparency)
            Color transparentColor = new Color(255, 255, 255);
            encoder.setTransparent(transparentColor);
            encoder.setDispose(2); // Restore to background color

            for (BufferedImage frame : croppedFrames) {
                encoder.addFrame(frame);
            }

            encoder.finish();
        }
    }

    /**
     * Calculate the bounding box that contains all non-white content across all frames
     */
    private static java.awt.Rectangle calculateBoundingBox(List<BufferedImage> frames) {
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
                    int rgb = frame.getRGB(x, y);
                    // Check if pixel is not white (0xFFFFFF)
                    if (rgb != 0xFFFFFF && (rgb & 0x00FFFFFF) != 0x00FFFFFF) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
        }

        // Add minimal padding (just 10 pixels)
        int padding = 10;
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(frames.get(0).getWidth() - 1, maxX + padding);
        maxY = Math.min(frames.get(0).getHeight() - 1, maxY + padding);

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        return new java.awt.Rectangle(minX, minY, width, height);
    }

    /**
     * Create a frame with text at specified scale
     *
     * @param text Text to render
     * @param fontName Font name
     * @param scale Scale percentage (100 = normal size)
     * @param width Frame width
     * @param height Frame height
     * @param colorHex Hex color code without # (e.g., "ff00eb")
     * @return BufferedImage with rendered text
     */
    public static BufferedImage createTextFrame(String text, String fontName, double scale, int width, int height, String colorHex) {
        // Create image with indexed color mode for better GIF transparency
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for better text quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Start with fully transparent background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        // Calculate optimal font size to fill the image at 100% scale
        // At 100%, text should fill about 80% of image width
        int baseFontSize = calculateOptimalFontSize(text, fontName, width, height, g2d);
        int fontSize = (int) (baseFontSize * scale / 100.0);
        Font font = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(font);

        // Parse hex color
        Color textColor = parseHexColor(colorHex);
        g2d.setColor(textColor);

        // Create fallback font for unsupported characters (use Arial for special chars)
        Font fallbackFont = new Font("Arial", Font.BOLD, fontSize);

        // Draw text with font fallback support
        drawTextWithFallback(g2d, text, font, fallbackFont, textColor, width, height);
        g2d.dispose();

        // Convert to image with white background for encoder
        return convertToWhiteBackground(image);
    }

    /**
     * Draw text with fallback font support for characters not supported by primary font
     */
    private static void drawTextWithFallback(Graphics2D g2d, String text, Font primaryFont, Font fallbackFont, Color color, int width, int height) {
        g2d.setColor(color);

        // Calculate total width with proper font for each character
        int totalWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Font fontToUse = canFontDisplayChar(primaryFont, c) ? primaryFont : fallbackFont;
            g2d.setFont(fontToUse);
            FontMetrics fm = g2d.getFontMetrics();
            totalWidth += fm.charWidth(c);
        }

        // Calculate starting position to center text
        g2d.setFont(primaryFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textHeight = fm.getHeight();
        int x = (width - totalWidth) / 2;
        int y = (height - textHeight) / 2 + fm.getAscent();

        // Draw each character with appropriate font
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Font fontToUse = canFontDisplayChar(primaryFont, c) ? primaryFont : fallbackFont;
            g2d.setFont(fontToUse);
            String charStr = String.valueOf(c);
            g2d.drawString(charStr, x, y);

            // Move to next character position
            FontMetrics charFm = g2d.getFontMetrics();
            x += charFm.stringWidth(charStr);
        }
    }

    /**
     * Check if a font can properly display a character
     * For special characters and symbols, always use fallback font (Arial)
     */
    private static boolean canFontDisplayChar(Font font, char c) {
        // Allow basic ASCII letters, numbers, space, and common punctuation
        // A-Z, a-z, 0-9, space, and punctuation like . , ! ? : ; ' " - ( ) etc.
        boolean isStandardChar = (c >= 'A' && c <= 'Z') ||
                                 (c >= 'a' && c <= 'z') ||
                                 (c >= '0' && c <= '9') ||
                                 c == ' ' ||
                                 (c >= 33 && c <= 47) ||  // ! " # $ % & ' ( ) * + , - . /
                                 (c >= 58 && c <= 64) ||  // : ; < = > ? @
                                 (c >= 91 && c <= 96) ||  // [ \ ] ^ _ `
                                 (c >= 123 && c <= 126);  // { | } ~

        // For non-standard characters (beyond basic ASCII printable), check more carefully
        if (!isStandardChar) {
            // Check if it's a control character or special Unicode
            if (c < 32 || c == 127) return false;

            // For extended characters (accents, symbols, etc.),
            // only trust the font if it explicitly supports them
            if (c > 127) {
                // Check font.canDisplay AND verify it's not in a problematic Unicode range
                if (!font.canDisplay(c)) return false;

                // Reject known problematic ranges (emoji, dingbats, etc.)
                if ((c >= 0x2600 && c <= 0x26FF) ||  // Miscellaneous Symbols
                    (c >= 0x2700 && c <= 0x27BF) ||  // Dingbats
                    (c >= 0x1F300 && c <= 0x1F9FF)) { // Emoji
                    return false;
                }
            }
        }

        // For standard ASCII, trust the font
        return font.canDisplay(c);
    }

    /**
     * Calculate optimal font size to fill image at 100% scale
     */
    private static int calculateOptimalFontSize(String text, String fontName, int width, int height, Graphics2D g2d) {
        // Target: text should fill 80% of image width at 100% scale
        int targetWidth = (int) (width * 0.8);

        // Start with a reasonable guess
        int fontSize = 100;
        Font testFont = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(testFont);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        // Scale the font size to match target width
        fontSize = (int) ((double) fontSize * targetWidth / textWidth);

        // Make sure it doesn't exceed height
        testFont = new Font(fontName, Font.BOLD, fontSize);
        g2d.setFont(testFont);
        fm = g2d.getFontMetrics();
        int textHeight = fm.getHeight();

        if (textHeight > height * 0.8) {
            // Scale down based on height
            fontSize = (int) ((double) fontSize * (height * 0.8) / textHeight);
        }

        return fontSize;
    }

    /**
     * Convert ARGB image to RGB with white background for GIF encoding
     * Pure white pixels will become transparent in the GIF
     */
    private static BufferedImage convertToWhiteBackground(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // If pixel is mostly transparent (alpha < 10), make it pure white
                if (alpha < 10) {
                    result.setRGB(x, y, 0xFFFFFF); // Pure white
                } else {
                    // Blend with white background based on alpha
                    int newRed = ((red * alpha) + (255 * (255 - alpha))) / 255;
                    int newGreen = ((green * alpha) + (255 * (255 - alpha))) / 255;
                    int newBlue = ((blue * alpha) + (255 * (255 - alpha))) / 255;

                    // If result is very close to white (anti-aliasing artifacts), force to pure white
                    if (newRed > 250 && newGreen > 250 && newBlue > 250) {
                        result.setRGB(x, y, 0xFFFFFF);
                    } else {
                        int rgb = (newRed << 16) | (newGreen << 8) | newBlue;
                        result.setRGB(x, y, rgb);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Parse hex color string to Color object
     *
     * @param hex Hex color code without # (e.g., "ff00eb")
     * @return Color object
     */
    private static Color parseHexColor(String hex) {
        try {
            // Remove # if present
            hex = hex.replace("#", "");

            // Parse RGB values
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            return new Color(r, g, b);
        } catch (Exception e) {
            // Default to black if parsing fails
            return Color.BLACK;
        }
    }
}
