package pl.edu.agh.capo.logic.fitness.analyzer;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import org.apache.commons.lang.ArrayUtils;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.robot.CapoRobotConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterFitnessAnalyzer {
    protected final Room room;
    private final List<Point2D[]> wallVectors;
    private final List<Point2D[]> gateVectors;
    protected final Point2D coordinates;
    protected final double angle;

    protected Vector2D vectorSS;  //start wall -> start reading
    protected Vector2D vectorEE;  //end wall -> end reading

    protected double vectorSSAngle;
    protected double vectorSEAngle;
    protected double vectorESAngle;
    protected double vectorEEAngle;


    public ClusterFitnessAnalyzer(Room room, List<Point2D[]> wallVectors, List<Point2D[]> gateVectors, Point2D coordinates, double angle) {
        this.room = room;
        this.wallVectors = wallVectors;
        this.gateVectors = gateVectors;
        this.coordinates = coordinates;
        this.angle = angle;
    }
//
//    public static double estimateFitness(Room room, List<Point2D[]> visionSections, List<Point2D[]> wallVectors,
//                                         List<Point2D[]> gateVectors, Point2D coordinates, double horizontalAngle, Map<Point2D[], Double> sectionAward) {
//        ClusterFitnessAnalyzer analyzer = new ClusterFitnessAnalyzer(room, wallVectors, gateVectors, coordinates, horizontalAngle);
//
//        double sum = 0.0, sumLength = 0.0;
//        for (Point2D[] visionSection : visionSections) {
//            double estimate = analyzer.checkWalls(visionSection);
//            if (estimate <= 0) {
//                estimate = analyzer.checkGates(visionSection);
//            }
//            double visionSectionLength = getLength(visionSection);
//            sum += estimate * visionSectionLength;
//            sumLength += visionSectionLength;
//            sectionAward.put(visionSection, estimate);
//        }
//
//        return sum / sumLength;
//    }

    public static double estimateFitness(Room room, List<Point2D[]> visionSections, List<Point2D[]> wallVectors,
                                         List<Point2D[]> gateVectors, Point2D coordinates, double horizontalAngle) {
        ClusterFitnessAnalyzer analyzer = new ClusterFitnessAnalyzer(room, wallVectors, gateVectors, coordinates, horizontalAngle);

        double sum = 0.0, sumLength = 0.0;
        for (Point2D[] visionSection : visionSections) {
            double estimate = analyzer.checkWalls(visionSection);
            if (estimate < 0) {
                estimate = Math.max(estimate, analyzer.checkGates(visionSection));
            }
            double visionSectionLength = getLength(visionSection);
            sum += estimate * visionSectionLength;
            sumLength += visionSectionLength;
        }

        return sum / sumLength;
    }

    public double checkGates(Point2D[] visionSection) {
        for (Point2D[] gate : gateVectors) {
            Point2D[] visionVector = {coordinates, getMiddlePoint(visionSection)};
            if (segmentsIntersect(gate, visionVector)) {
                Room nextRoom = room.getRoomBehindGate(gate);
                List<Point2D[]> filteredGates = nextRoom.getGateVectors().stream().filter(nextGate -> !Arrays.equals(gate, nextGate))
                        .collect(Collectors.toList());
                double award = estimateFitness(nextRoom, Collections.singletonList(visionSection), nextRoom.getWallVectors(),
                        filteredGates, coordinates, angle);
                if (award > -1) {
                    return award;
                }
            }
        }
        return -1.0;
    }

    private Point2D getMiddlePoint(Point2D[] section) {
        double x = (section[0].x() + section[1].x()) / 2;
        double y = (section[0].y() + section[1].y()) / 2;
        return new Point2D(x, y);
    }

    private static boolean segmentsIntersect(Point2D[] seg1, Point2D[] seg2) {
        double a0x = seg1[0].x(), a0y = seg1[0].y(), a1x = seg1[1].x(), a1y = seg1[1].y(),
                b0x = seg2[0].x(), b0y = seg2[0].y(), b1x = seg2[1].x(), b1y = seg2[1].y();

        double d = (b1y - b0y) * (a1x - a0x) - (b1x - b0x) * (a1y - a0y);

        if (d == 0.0)
            return false; // Parallel lines

        double uA = ((b1x - b0x) * (a0y - b0y) - (b1y - b0y) * (a0x - b0x)) / d;
        double uB = ((a1x - a0x) * (a0y - b0y) - (a1y - a0y) * (a0x - b0x)) / d;

        return !(uA < 0 || uA > 1 || uB < 0 || uB > 1);

    }

    public double checkWalls(Point2D[] visionSection) {
        double sectionAngle = getAngle(visionSection);
        for (Point2D[] wall : wallVectors) {
            if (!isPointVisible(coordinates, angle, wall[0]) && !isPointVisible(coordinates, angle, wall[1])) {
                continue;
            }

            double wallAngle = getAngle(wall);
            double angleDiff = angleBetweenTwoVectors(wallAngle, sectionAngle);
            double invertedSectionAngle = sectionAngle + 180;
            invertedSectionAngle = invertedSectionAngle > 360 ? invertedSectionAngle - 360 : invertedSectionAngle;
            double invertedAngleDiff = angleBetweenTwoVectors(wallAngle, invertedSectionAngle);

            if (invertedAngleDiff < angleDiff) {
                sectionAngle = invertedSectionAngle;
                angleDiff = invertedAngleDiff;
                ArrayUtils.reverse(visionSection);
            }

            if (angleDiff < CapoRobotConstants.CLUSTER_ESTIMATOR_ANGLE_ACCURACY || visionSection[0].equals(wall[1]) || visionSection[1].equals(wall[0])) {
                boolean visionStartEqualsWall = wall[0].equals(visionSection[0]);
                boolean visionEndEqualsWall = wall[1].equals(visionSection[1]);
                if (visionEndEqualsWall && visionStartEqualsWall) {
                    return 1.0;
                }
                setupVectors(visionSection, wall);

                if ((visionStartEqualsWall ||
                        (catetoOpuestoVectorNorm(vectorSS, wall) < CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY) ||
                        (catetoOpuestoVectorNorm(vectorSS, visionSection) < CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY))
                        && (visionEndEqualsWall ||
                        (catetoOpuestoVectorNorm(vectorEE, wall) < CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY) ||
                        (catetoOpuestoVectorNorm(vectorEE, visionSection) < CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY))) {


                    if (inDifferentDirection(vectorESAngle, vectorSEAngle)) {
                        if (visionStartEqualsWall) {
                            if (inSameDirection(vectorEEAngle, vectorESAngle)) {
                                // #1
                                return wallIncludesSectionAward(wall);
                            } else {
                                // #2
                                return sectionIncludesWallAward(visionSection, wall);
                            }
                        } else if (visionEndEqualsWall) {
                            if (inSameDirection(vectorSSAngle, vectorSEAngle)) {
                                // #1
                                return wallIncludesSectionAward(wall);
                            } else {
                                // #2
                                return sectionIncludesWallAward(visionSection, wall);
                            }
                        } else {
                            if (inSameDirection(vectorSSAngle, wallAngle)) {
                                if (inSameDirection(vectorEEAngle, wallAngle)) {
                                    //#3 reversed
                                    return sectionShiftInEndDirectionAward(visionSection, wall);
                                } else {
                                    // #1
                                    return wallIncludesSectionAward(wall);
                                }
                            } else {
                                if (inSameDirection(vectorEEAngle, wallAngle)) {
                                    // #2
                                    return sectionIncludesWallAward(visionSection, wall);
                                } else {
                                    //#3
                                    return sectionShiftInStartDirectionAward(visionSection, wall);
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1.0;
    }

    protected double wallIncludesSectionAward(Point2D[] wall) {
        return calculateAward(catetoOpuestoVectorNorm(vectorEE, wall), catetoOpuestoVectorNorm(vectorSS, wall));
    }

    private double sectionIncludesWallAward(Point2D[] visionSection, Point2D[] wall) {
        return divideAndCalculateAward(visionSection, wall);
    }

    protected double sectionShiftInEndDirectionAward(Point2D[] visionSection, Point2D[] wall) {
        double endNorm = catetoContiguoVectorNorm(vectorEE, wall);
        Point2D[] missingEndSection = getMissingEnd(visionSection, endNorm);
        boolean endExceeds = new Vector2D(missingEndSection[0], missingEndSection[1]).norm() > CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY;
        if (!endExceeds) {
            return calculateAward(vectorEE.norm(), catetoOpuestoVectorNorm(vectorSS, wall));
        } else {
            double sum;
            try {
                sum = estimateVectors(missingEndSection);
            } catch (NoMatchException e) {
                return -1.0;
            }
            double middleAward = calculateAward(catetoOpuestoVectorNorm(vectorSS, wall), catetoOpuestoVectorNorm(vectorEE, visionSection));
            Point2D[] middleSection = {visionSection[0], wall[1]};
            double middleNorm = getLength(middleSection);
            return (sum + middleAward * middleNorm) / (middleNorm + getLength(missingEndSection));
        }
    }

    protected double sectionShiftInStartDirectionAward(Point2D[] visionSection, Point2D[] wall) {
        double startNorm = catetoContiguoVectorNorm(vectorSS, wall);
        Point2D[] missingStartSection = getMissingStart(visionSection, startNorm);
        boolean startExceeds = new Vector2D(missingStartSection[0], missingStartSection[1]).norm() > CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY;
        if (!startExceeds) {
            return calculateAward(vectorSS.norm(), catetoOpuestoVectorNorm(vectorEE, wall));
        } else {
            double sum;
            try {
                sum = estimateVectors(missingStartSection);
            } catch (NoMatchException e) {
                return -1.0;
            }
            double middleAward = calculateAward(catetoOpuestoVectorNorm(vectorSS, visionSection), catetoOpuestoVectorNorm(vectorEE, wall));
            Point2D[] middleSection = {wall[0], visionSection[1]};
            double middleNorm = getLength(middleSection);
            return (sum + middleAward * middleNorm) / (middleNorm + getLength(missingStartSection));
        }
    }

    protected static double getLength(Point2D[] vector) {
        return distanceBetween(vector[0], vector[1]);
    }

    private static double distanceBetween(Point2D coordinates1, Point2D coordinates2) {
        double dx = coordinates1.x() - coordinates2.x();
        double dy = coordinates1.y() - coordinates2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double divideAndCalculateAward(Point2D[] visionSection, Point2D[] wall) {
        double startNorm = catetoContiguoVectorNorm(vectorSS, wall);
        double endNorm = catetoContiguoVectorNorm(vectorEE, wall);

        Point2D[] missingStartSection = getMissingStart(visionSection, startNorm);
        Point2D[] missingEndSection = getMissingEnd(visionSection, endNorm);

        boolean startExceeds = startNorm > 0.4;
        boolean endExceeds = endNorm > 0.4;


        double middleNorm = getLength(wall);
        double sum, normSum = middleNorm;

        try {
            if (startExceeds) {
                if (endExceeds) {
                    sum = estimateIfStartAndEndExceeds(visionSection, missingStartSection, missingEndSection, middleNorm);
                    normSum += getLength(missingStartSection) + getLength(missingEndSection);
                } else {
                    sum = estimateIfStartExceeds(visionSection, missingStartSection, middleNorm);
                    normSum += getLength(missingStartSection);
                }
            } else if (endExceeds) {
                sum = estimateIfEndExceeds(visionSection, missingEndSection, middleNorm);
                normSum += getLength(missingEndSection);
            } else {
                return calculateAward(vectorSS.norm(), vectorEE.norm());
            }
        } catch (NoMatchException e) {
            return -1.0;
        }

        return sum / normSum;
    }

    private Point2D[] getMissingStart(Point2D[] visionSection, double norm) {
        Vector2D directionVector = new Vector2D(visionSection[0], visionSection[1]).normalize();
        directionVector = directionVector.times(norm);
        Point2D sectionEnd = new Point2D(visionSection[0].x() + directionVector.x(), visionSection[0].y() + directionVector.y());
        return new Point2D[]{visionSection[0], sectionEnd};
    }

    private Point2D[] getMissingEnd(Point2D[] visionSection, double norm) {
        Vector2D directionVector = new Vector2D(visionSection[1], visionSection[0]).normalize();
        directionVector = directionVector.times(norm);
        Point2D sectionStart = new Point2D(visionSection[1].x() + directionVector.x(), visionSection[1].y() + directionVector.y());
        return new Point2D[]{sectionStart, visionSection[1]};
    }

    private double estimateIfEndExceeds(Point2D[] section, Point2D[] missingEndSection, double middle) throws NoMatchException {
        double sum = estimateVectors(missingEndSection);
        return sum + calculateAward(catetoOpuestoVectorNorm(vectorSS, section), vectorEE.norm()) * middle;
    }

    private double estimateIfStartExceeds(Point2D[] section, Point2D[] missingStartSection, double middle) throws NoMatchException {
        double sum = estimateVectors(missingStartSection);
        return sum + calculateAward(vectorSS.norm(), catetoOpuestoVectorNorm(vectorEE, section)) * middle;
    }

    private double estimateIfStartAndEndExceeds(Point2D[] section, Point2D[] missingStartSection, Point2D[] missingEndSection, double middle) throws NoMatchException {
        double sum = estimateVectors(missingStartSection, missingEndSection);
        return sum + calculateAward(catetoOpuestoVectorNorm(vectorSS, section), catetoOpuestoVectorNorm(vectorEE, section)) * middle;
    }

    protected double estimateVectors(Point2D[]... sections) throws NoMatchException {
        double sum = 0.0;
        for (Point2D[] section : sections) {
            double award = 0.0;
            for (Room nextRoom : room.getRooms()) {
                award = estimateFitness(nextRoom, Collections.singletonList(section),
                        nextRoom.getWallVectors(), nextRoom.getGateVectors(), coordinates, angle);
                if (award > 0) {
                    break;
                }
            }
            if (award <= 0) {
                throw new NoMatchException();
            }
            sum += award * getLength(section);
        }
        return sum;
    }

    protected static double calculateAward(double... vectorNorms) {
        double sum = 0.0;
        for (double norm : vectorNorms) {
            if (norm > CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY) {
                return 0.0;
            }
            sum += norm;
        }
        return 1.0 - (sum / 2 * CapoRobotConstants.CLUSTER_ESTIMATOR_VECTOR_ACCURACY);
    }

    protected static double catetoOpuestoVectorNorm(Vector2D vector, Point2D[] wall) {
        double angleBetween = Math.toRadians(calculateAngleDiff(getAngle(vector), getAngle(wall)));
        return Math.sin(angleBetween) * vector.norm();
    }

    protected static double catetoContiguoVectorNorm(Vector2D vector, Point2D[] wall) {
        double angleBetween = Math.toRadians(calculateAngleDiff(getAngle(vector), getAngle(wall)));
        return Math.cos(angleBetween) * vector.norm();
    }

    private static double calculateAngleDiff(double angle1, double angle2) {
        double invertedAngle = angle1 + 180;
        invertedAngle = invertedAngle > 360 ? invertedAngle - 360 : invertedAngle;
        double closeToZeroDiff = angle1 > angle2 ? angle1 - angle2 - 360 : angle2 - angle1 - 360;
        return Math.min(Math.abs(closeToZeroDiff), Math.min(Math.abs(angle1 - angle2),
                Math.abs(invertedAngle - angle2)));
    }

    private boolean sectionShiftInStartDirection() {
        return inSameDirection(vectorSSAngle, vectorEEAngle, vectorESAngle) && inDifferentDirection(vectorSEAngle, vectorSSAngle, vectorEEAngle);
    }

    private boolean sectionShiftInEndDirection() {
        return inSameDirection(vectorSSAngle, vectorEEAngle, vectorSEAngle) && inDifferentDirection(vectorESAngle, vectorSSAngle, vectorEEAngle);
    }

    private boolean sectionIncludesWall(boolean visionStartEqualsWall, boolean visionEndEqualsWall) {
        return (visionStartEqualsWall || (inSameDirection(vectorSSAngle, vectorESAngle) && inDifferentDirection(vectorSEAngle, vectorSSAngle))) &&
                (visionEndEqualsWall || (inSameDirection(vectorSEAngle, vectorEEAngle) && inDifferentDirection(vectorESAngle, vectorEEAngle))) &&
                ((visionEndEqualsWall || visionStartEqualsWall) || inDifferentDirection(vectorEEAngle, vectorSSAngle));
    }

    private void setupVectors(Point2D[] visionSection, Point2D[] wall) {
        vectorSS = new Vector2D(wall[0], visionSection[0]);   //start wall -> start reading
        Vector2D vectorSE = new Vector2D(wall[0], visionSection[1]);
        Vector2D vectorES = new Vector2D(wall[1], visionSection[0]);
        vectorEE = new Vector2D(wall[1], visionSection[1]);    //end wall -> end reading
        vectorSSAngle = getAngle(vectorSS);
        vectorSEAngle = getAngle(vectorSE);
        vectorESAngle = getAngle(vectorES);
        vectorEEAngle = getAngle(vectorEE);
    }

    private boolean wallIncludesSection(boolean visionStartEqualsWall, boolean visionEndEqualsWall) {
        return (visionStartEqualsWall || (inSameDirection(vectorSSAngle, vectorSEAngle) && inDifferentDirection(vectorSSAngle, vectorESAngle))) &&
                (visionEndEqualsWall || (inSameDirection(vectorEEAngle, vectorESAngle) && inDifferentDirection(vectorEEAngle, vectorSEAngle))) &&
                ((visionEndEqualsWall || visionStartEqualsWall) || inDifferentDirection(vectorSSAngle, vectorEEAngle));
    }

    private static boolean inDifferentDirection(double angle1, double... angles) {
        for (double angle2 : angles) {
            double angleDiff = angleBetweenTwoVectors(angle1, angle2);
            if (angleDiff <= 90) {
                return false;
            }
        }
        return true;
    }

    protected static boolean inSameDirection(double... angles) {
        int size = angles.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                double angleDiff = angleBetweenTwoVectors(angles[i], angles[j]);
                if (angleDiff > 90) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double angleBetweenTwoVectors(double angle1, double angle2) {
        double closeToZeroDiff = angle1 > angle2 ? angle1 - angle2 - 360 : angle2 - angle1 - 360;
        return Math.min(Math.abs(closeToZeroDiff), Math.abs(angle1 - angle2));
    }

    private static boolean isPointVisible(Point2D coordinates, double angle, Point2D point2D) {
        double vectorAngle = getAngle(new Point2D[]{coordinates, point2D});
        return angleBetweenTwoVectors(vectorAngle, angle) < 120;
    }

    private static double getAngle(Point2D[] visionSection) {
        Vector2D lineVector = new Vector2D(visionSection[0], visionSection[1]);
        return getAngle(lineVector);
    }

    private static double getAngle(Vector2D vector2D) {
        return 360 - Math.toDegrees(vector2D.angle());
    }

    private static void print(Point2D wallStart, Point2D wallEnd, Point2D[] visionSection) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("****************************************");
        stringBuilder.append("\n");
        stringBuilder.append(wallStart.x() + "\t" + wallStart.y());
        stringBuilder.append("\n");
        stringBuilder.append(wallEnd.x() + "\t" + wallEnd.y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[0].x() + "\t" + visionSection[0].y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[1].x() + "\t" + visionSection[1].y());
        stringBuilder.append("\n");
        stringBuilder.append(wallStart.x() + "\t" + wallStart.y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[0].x() + "\t" + visionSection[0].y());   //start wall -> start reading
        stringBuilder.append("\n");
        stringBuilder.append(wallStart.x() + "\t" + wallStart.y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[1].x() + "\t" + visionSection[1].y());   //start wall -> start reading
        stringBuilder.append("\n");
        stringBuilder.append(wallEnd.x() + "\t" + wallEnd.y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[0].x() + "\t" + visionSection[0].y());   //start wall -> start reading
        stringBuilder.append("\n");
        stringBuilder.append(wallEnd.x() + "\t" + wallEnd.y());
        stringBuilder.append("\n");
        stringBuilder.append(visionSection[1].x() + "\t" + visionSection[1].y());   //start wall -> start reading
        stringBuilder.append("\n");
        String text = stringBuilder.toString().replaceAll("\\.", ",");
        System.out.println(text);
    }

    private class NoMatchException extends Throwable {
    }
}
