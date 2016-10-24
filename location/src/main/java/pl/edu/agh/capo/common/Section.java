package pl.edu.agh.capo.common;

import math.geom2d.Point2D;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.robot.CapoRobotConstants;


public class Section {
    protected final Point2D[] vector;

    @SuppressWarnings("unused") //kht compability
    public Section(double rawRho, double rawTheta, Coordinates[] pixels) {
        this.vector = new Point2D[]{adjust(pixels[0]), adjust(pixels[pixels.length - 1])};
    }

    public Section(Point2D[] vector) {
        this.vector = vector;
    }

    protected static Point2D adjust(Coordinates pixel) {
        return new Point2D(pixel.getX() * CapoRobotConstants.KHT_VISION_PER_PIXEL, -pixel.getY() * CapoRobotConstants.KHT_VISION_PER_PIXEL);
    }

    public static Point2D rotate(Point2D point2D, double theta) {
        return point2D.rotate(Math.toRadians(-theta));
    }

    public Point2D[] getTranslatedVector(Point2D coordinates, double theta) {
        return new Point2D[]{translated(vector[0], coordinates, theta), translated(vector[1], coordinates, theta)};
    }

    public static Point2D translated(Point2D vector, Point2D location, double theta) {
        Point2D rotated = rotate(vector, theta);
        return new Point2D(rotated.x(), -rotated.y()).plus(location);
    }

    public Point2D[] getVector() {
        return vector;
    }

//    private static double distanceBetween(Coordinates coordinates1, Coordinates coordinates2) {
//        double dx = coordinates1.getX() - coordinates2.getX();
//        double dy = coordinates1.getY() - coordinates2.getX();
//        return Math.sqrt(dx * dx + dy * dy);
//    }

//
//    public double calculateLength() {
//        Coordinates minPosition = caclculateMinXPosition();
//        Coordinates maxPosition = caclculateMaxXosition();
//        if (maxPosition.getX() == maxPosition.getX()) {
//            minPosition = caclculateMinYPosition();
//            maxPosition = caclculateMaxYosition();
//        }
//        return distanceBetween(minPosition, maxPosition);
//    }
//
//    private Coordinates caclculateMinXPosition() {
//        double minX = Double.MAX_VALUE;
//        Coordinates result = null;
//        for (Coordinates pixel : pixels) {
//            if (pixel.getX() < minX) {
//                minX = pixel.getX();
//                result = pixel;
//            }
//        }
//        return result;
//    }

//    private Coordinates caclculateMaxXosition() {
//        double maxX = Double.MIN_VALUE;
//        Coordinates result = null;
//        for (Coordinates pixel : pixels) {
//            if (pixel.getX() > maxX) {
//                maxX = pixel.getX();
//                result = pixel;
//            }
//        }
//        return result;
//    }
//
//    private Coordinates caclculateMinYPosition() {
//        double minY = Double.MAX_VALUE;
//        Coordinates result = null;
//        for (Coordinates pixel : pixels) {
//            if (pixel.getY() < minY) {
//                minY = pixel.getY();
//                result = pixel;
//            }
//        }
//        return result;
//    }
//
//    private Coordinates caclculateMaxYosition() {
//        double maxY = Double.MIN_VALUE;
//        Coordinates result = null;
//        for (Coordinates pixel : pixels) {
//            if (pixel.getY() > maxY) {
//                maxY = pixel.getY();
//                result = pixel;
//            }
//        }
//        return result;
//    }
}
