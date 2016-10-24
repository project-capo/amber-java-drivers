package pl.edu.agh.capo.hough.jni;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Vision;
import pl.edu.agh.capo.robot.CapoRobotConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VisionImage {


    private static final byte PIXEL_VALUE = 1;
    private final byte[] bytes;
    private final int size;
    private final int halfSize;

    public VisionImage(List<Vision> visions) {
        Vision vision = visions.stream().max((v1, v2) -> Double.compare(v1.getDistance(), v2.getDistance())).get();
        double maxDistance = vision.getDistance();
        this.halfSize = (int) Math.ceil(maxDistance / CapoRobotConstants.KHT_VISION_PER_PIXEL) + 1;
        this.size = halfSize * 2;
        this.bytes = new byte[size * size];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
        addVisions(visions);
    }

    public int getSize() {
        return size;
    }

    private static int grayScale(int rgb) {
        int r = rgb >> 16 & 0xff;
        int g = rgb >> 8 & 0xff;
        int b = rgb & 0xff;
        int cmax = Math.max(Math.max(r, g), b);
        return (rgb & 0xFF000000) | (cmax << 16) | (cmax << 8) | cmax;
    }

    private void addVisions(List<Vision> visions) {
        visions.forEach(this::addPoint);
    }

    private void addPoint(Vision vision) {
        double distance = vision.getDistance() / CapoRobotConstants.KHT_VISION_PER_PIXEL;
        double angleInRadians = Math.toRadians(vision.getAngle());
        addPoint(distance, angleInRadians);
    }

    private void addPoint(double distance, double angleInRadians) {
        int x = (int) Math.round(halfSize + (Math.sin(angleInRadians) * distance));
        int y = (int) Math.round(halfSize - (Math.cos(angleInRadians) * distance));
        bytes[y * size + x] = PIXEL_VALUE;
    }

    public void translateLines(List<Line> lines) {
        lines.forEach(this::translateLine);
    }

    private void translateLine(Line line) {
        double rho = line.getRawRho() * PIXEL_VALUE;
        double theta = line.getRawTheta();
        if (rho > 0) {
            if (theta <= 90) {
                theta = theta + 90;
            } else {
                theta = theta - 270;
            }
        } else {
            theta -= 90;
            rho = -rho;
        }
        line.setTheta(normalizeAlpha((theta)));
        line.setRho(rho);
    }

    private double normalizeAlpha(double alpha) {
        while (alpha < -180.0) {
            alpha += 360.0;
        }
        while (alpha > 180.0) {
            alpha -= 360.0;
        }
        return alpha;
    }

    public byte[] toByteArray() {
        return bytes;
    }

    @SuppressWarnings("unused")
    public void writeToFile(String filename) throws IOException {
        BufferedImage pixelImage = createImage();
        drawBytes(pixelImage);
        drawRobot(pixelImage);
        saveImage(pixelImage, filename);
    }

    private void saveImage(BufferedImage image, String filename) throws IOException {
        File file = new File(filename);
        ImageIO.write(image, "BMP", file);
    }

    private BufferedImage createImage() {
        return new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
    }

    private void drawBytes(BufferedImage pixelImage) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int b = bytes[y * size + x];
                if (b > 0) {
                    pixelImage.setRGB(x, y, grayScale(Color.WHITE.getRGB()));
                }
            }
        }
    }

    private void drawLines(BufferedImage bufferedImage, List<Line> lines) {
        lines.forEach(line -> drawLine(bufferedImage, line, grayScale(Color.GRAY.getRGB())));
    }

    private void drawRobot(BufferedImage bufferedImage) {
        bufferedImage.setRGB(halfSize, halfSize, grayScale(Color.WHITE.getRGB()));
    }

    @SuppressWarnings("unused")
    public void writeToFileWithLines(String filename, List<Line> lines) throws IOException {
        BufferedImage pixelImage = createImage();
        drawLines(pixelImage, lines);
        drawBytes(pixelImage);
        drawRobot(pixelImage);
        saveImage(pixelImage, filename);
    }

    public void drawLine(BufferedImage image, Line line, int color) {
        double theta = Math.toRadians(line.getRawTheta());
        double rho = line.getRawRho();

        // Find edge points and vote in array
        float centerX = halfSize;
        float centerY = halfSize;

        // Draw edges in output array
        double tsin = Math.sin(theta);
        double tcos = Math.cos(theta);

        if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
            // Draw vertical-ish lines
            for (int y = 0; y < size; y++) {
                int x = (int) (((rho - ((y - centerY) * tsin)) / tcos) + centerX);
                if (x < size && x >= 0) {
                    image.setRGB(x, y, color);
                }
            }
        } else {
            // Draw horizontal-sh lines
            for (int x = 0; x < size; x++) {
                int y = (int) (((rho - ((x - centerX) * tcos)) / tsin) + centerY);
                if (y < size && y >= 0) {
                    image.setRGB(x, y, color);
                }
            }
        }
    }
}
