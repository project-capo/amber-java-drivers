package pl.edu.agh.capo.logic.fitness;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.common.Section;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.logic.fitness.analyzer.ClusterFitnessAnalyzer;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.maze.Wall;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.Measure;

import java.util.LinkedList;
import java.util.List;

public class ClusterFitnessEstimator extends VisionFitnessEstimator {
    private final Room room;
    private final List<Section> sections;
    // private Map<Point2D[], Double> sectionAward;

    public ClusterFitnessEstimator(Room room, Measure measure) {
        super(room, measure);
        this.room = room;
        this.sections = measure.getSections();
    }

    private static double distanceBetween(Coordinates coordinates1, Coordinates coordinates2) {
        double dx = coordinates1.getX() - coordinates2.getX();
        double dy = coordinates1.getY() - coordinates2.getX();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public double estimateFitnessByTries(Coordinates coords, Double angle) {
        if (CapoRobotConstants.ESTIMATION_TRIES < visions.size()) {
            VisionFitnessAnalyzer analyzer = new VisionFitnessAnalyzer(room, coords.getX(), coords.getY(), angle);
            int step = visions.size() / CapoRobotConstants.ESTIMATION_TRIES;

            if (CapoRobotConstants.ESTIMATION_MATCHED_TRIES > countFitnessMatches(analyzer, step)) {
                return -1.0;
            }
        }
        return estimateFitness(coords.toPoint2D(), angle);
    }

    @Override
    public double estimateFitness(Location location) {
        return estimateFitness(location.getCoordinates().toPoint2D(), location.alpha);
    }

    @Override
    public void printDebug() {
        super.printDebug();
        printSections(sections);
        //printWalls(room.getGroupWalls());
        //printGates(room.getGateVectors());
    }

    private void printGates(List<Point2D[]> gateVectors) {
        for (Point2D[] gate : gateVectors) {
            System.out.println("buildVector(" + gate[0].x() + ", " + gate[0].y() + ", " + gate[1].x() + ", " + gate[1].y() + "),");
        }
        for (Point2D[] gate : gateVectors) {
            System.out.println(room.getRoomBehindGate(gate).getSpaceId());
        }
    }

    private static void printSections(List<Section> sections) {
        for (Section section : sections) {
            StringBuilder builder = new StringBuilder("buildSection(");
            for (Point2D point2D : section.getVector()) {
                builder.append(point2D.x())
                        .append(", ")
                        .append(point2D.y());
            }
            builder.append("),");
            System.out.println(builder.toString());
        }
    }

    private void printWalls(List<Wall> walls) {
        for (Wall wall : walls) {
            System.out.println("buildWall(\"" + wall.getId() + "\"" + ", " + wall.getFrom().getX() + ", " + wall.getFrom().getY() + ", " + wall.getTo().getX() + ", " + wall.getTo().getY() + "),");
        }
    }

    protected Point2D[] getVisionSection(Section section, Point2D coordinates, double alpha) {
        return section.getTranslatedVector(coordinates, alpha);
    }

    protected double estimateFitness(Point2D coordinates, double alpha) {
        List<Point2D[]> visionSections = new LinkedList<>();
        sections.forEach(section -> visionSections.add(getVisionSection(section, coordinates, alpha)));
        double normalizedAngle = (-alpha + 360 + 90) % 360;
        // double award = ClusterFitnessAnalyzer.estimateFitness(room, visionSections, room.getWallVectors(), room.getGateVectors(), coordinates, normalizedAngle);
        //  return award < 0? 0.0: award;
        // sectionAward = new HashMap<>();
        return ClusterFitnessAnalyzer.estimateFitness(room, visionSections, room.getWallVectors(), room.getGateVectors(),
                coordinates, normalizedAngle);
    }

    protected static Point2D computeIntersectionOfSegments(Point2D[] seg1, Point2D[] seg2) {
        double a0x = seg1[0].x(), a0y = seg1[0].y(), a1x = seg1[1].x(), a1y = seg1[1].y(),
                b0x = seg2[0].x(), b0y = seg2[0].y(), b1x = seg2[1].x(), b1y = seg2[1].y();

        double d = (b1y - b0y) * (a1x - a0x) - (b1x - b0x) * (a1y - a0y);

        if (d == 0.0)
            return null; // Parallel lines

        double uA = ((b1x - b0x) * (a0y - b0y) - (b1y - b0y) * (a0x - b0x)) / d;
        double uB = ((a1x - a0x) * (a0y - b0y) - (a1y - a0y) * (a0x - b0x)) / d;

        if (uA < 0 || uA > 1 || uB < 0 || uB > 1)
            return null;  // lines can't intersect

        //  /* Compute cross product */
//        double crossAB = (a0x - b0x) * (b1y - b0y) - (b1x - b0x) * (a0y - b0y);

        return new Point2D(a0x + uA * (a1x - a0x), a0y + uA * (a1y - a0y));
    }

    private void printVectors(Vector2D... vectors) {
        for (Vector2D vector : vectors) {
            System.out.println(vector.x() + " " + vector.y());
        }
    }

//    public Map<Point2D[], Double> getSectionAward() {
//        return null; //sectionAward;
//    }

}
