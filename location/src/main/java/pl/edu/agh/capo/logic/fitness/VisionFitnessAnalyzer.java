package pl.edu.agh.capo.logic.fitness;


import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.maze.Gate;
import pl.edu.agh.capo.robot.CapoRobotConstants;

import java.util.List;

public class VisionFitnessAnalyzer {

    private final Room room;
    private final double x;
    private final double y;
    private final double angle;

    private double angleNW;
    private double angleNE;
    private double angleSE;
    private double angleSW;

    public VisionFitnessAnalyzer(Room room, double x, double y, double angle) {
        this.room = room;
        this.x = x;
        this.y = y;
        this.angle = angle;

        findLimitAngle();
    }

    private void findLimitAngle() {
        angleNW = -Math.toDegrees(Math.atan((x - room.getMinX()) / (y - room.getMinY())));
        angleNE = Math.toDegrees(Math.atan((room.getMaxX() - x) / (y - room.getMinY())));
        angleSE = 90.0 + Math.toDegrees(Math.atan((room.getMaxY() - y) / (room.getMaxX() - x)));
        angleSW = -(90.0 + Math.toDegrees(Math.atan((room.getMaxY() - y) / (x - room.getMinX()))));
    }


    private double normalizeAngle(double angle) {
        double result = angle;
        if (angle < -180.0) {
            result = angle + 360.0;
        }
        if (angle > 180) {
            result = angle - 360.0;
        }
        return result;
    }

    public double estimate(double angle, double distance) {
        boolean overMaxRange = distance > CapoRobotConstants.MAX_VISION_DISTANCE;
        return estimate(angle, distance, overMaxRange);
    }

    private double estimate(double angle, double distance, boolean overMaxRange) {
        double alpha = normalizeAngle(angle + this.angle);
        if (alpha > 0) {
            if (alpha == 180.0) {
                return checkMeasureHorizontally(x, y, x, room.getMaxY(), distance, angle, room.getSouthGates(), overMaxRange);
            } else if (alpha > angleSE) {
                double x1 = x - (Math.tan(Math.toRadians(alpha)) * (room.getMaxY() - y));
                return checkMeasureHorizontally(x, y, x1, room.getMaxY(), distance, angle, room.getSouthGates(), overMaxRange);
            } else if (alpha == angleSE) {
                return countMeasureEstimation(distance, getDistance(x, y, room.getMaxX(), room.getMaxY()), overMaxRange);
            } else if (alpha > 90.0) {
                double y1 = y + (Math.tan(Math.toRadians(alpha - 90.0)) * (room.getMaxX() - x));
                return checkMeasureVertically(x, y, room.getMaxX(), y1, distance, angle, room.getEastGates(), overMaxRange);
            } else if (alpha == 90.0) {
                return checkMeasureHorizontally(x, y, room.getMaxX(), y, distance, angle, room.getEastGates(), overMaxRange);
            } else if (alpha > angleNE) {
                double y1 = y - (Math.tan(Math.toRadians(90.0 - alpha)) * (room.getMaxX() - x));
                return checkMeasureVertically(x, y, room.getMaxX(), y1, distance, angle, room.getEastGates(), overMaxRange);
            } else if (alpha == angleNE) {
                return countMeasureEstimation(distance, getDistance(x, y, room.getMaxX(), room.getMinY()), overMaxRange);
            } else {
                double x1 = (Math.tan(Math.toRadians(alpha)) * (y - room.getMinY())) + x;
                return checkMeasureHorizontally(x, y, x1, room.getMinY(), distance, angle, room.getNorthGates(), overMaxRange);
            }
        } else if (alpha < 0) {
            if (alpha == -180.0) {
                return checkMeasureHorizontally(x, y, x, room.getMaxY(), distance, angle, room.getSouthGates(), overMaxRange);
            } else if (alpha < angleSW) {
                double x1 = x - (Math.tan(Math.toRadians(alpha)) * (room.getMaxY() - y));
                return checkMeasureHorizontally(x, y, x1, room.getMaxY(), distance, angle, room.getSouthGates(), overMaxRange);
            } else if (alpha == angleSW) {
                return countMeasureEstimation(distance, getDistance(x, y, room.getMinX(), room.getMaxY()), overMaxRange);
            } else if (alpha < -90.0) {
                double y1 = y - (Math.tan(Math.toRadians(90 + alpha)) * (x - room.getMinX()));
                return checkMeasureVertically(x, y, room.getMinX(), y1, distance, angle, room.getWestGates(), overMaxRange);
            } else if (alpha == -90.0) {
                return checkMeasureHorizontally(x, y, room.getMinX(), y, distance, angle, room.getWestGates(), overMaxRange);
            } else if (alpha < angleNW) {
                double y1 = y - (Math.tan(Math.toRadians(90 + alpha)) * (x - room.getMinX()));
                return checkMeasureVertically(x, y, room.getMinX(), y1, distance, angle, room.getWestGates(), overMaxRange);
            } else if (alpha == angleNW) {
                return countMeasureEstimation(distance, getDistance(x, y, room.getMinX(), room.getMinY()), overMaxRange);
            } else {
                double x1 = (Math.tan(Math.toRadians(alpha)) * (y - room.getMinY())) + x;
                return checkMeasureHorizontally(x, y, x1, room.getMinY(), distance, angle, room.getNorthGates(), overMaxRange);
            }
        } else {
            return checkMeasureHorizontally(x, y, x, room.getMinY(), distance, angle, room.getNorthGates(), overMaxRange);
        }
    }

    private double checkMeasureHorizontally(double agentX, double agentY, double roomX, double roomY, double distance, double angle, List<Gate> gates, boolean overMaxRange) {
        double distanceToWall = getDistance(agentX, agentY, roomX, roomY);
        double estimation = getEstimationIfMeasureInGateHorizontally(roomX, roomY, angle, distance, gates, distanceToWall, overMaxRange);
        if (estimation < 0) {
            return countMeasureEstimation(distance, distanceToWall, overMaxRange);
        }
        return estimation;
    }

    private double checkMeasureVertically(double agentX, double agentY, double roomX, double roomY, double distance, double angle, List<Gate> gates, boolean overMaxRange) {
        double distanceToWall = getDistance(agentX, agentY, roomX, roomY);
        double estimation = getEstimationIfMeasureInGateVertically(roomX, roomY, angle, distance, gates, distanceToWall, overMaxRange);
        if (estimation < 0) {
            return countMeasureEstimation(distance, distanceToWall, overMaxRange);
        }
        return estimation;
    }

    private double estimateInRoomBehindGate(Room room, double x, double y, double angle, double distance, boolean overMaxRange) {
        if (distance < -CapoRobotConstants.VISION_ESTIMATOR_DISTANCE_ACCURACY) {
            return 0;
        }
        VisionFitnessAnalyzer fitnessAnalyzer = new VisionFitnessAnalyzer(room, x, y, this.angle);
        return fitnessAnalyzer.estimate(angle, distance, overMaxRange);
    }

    private double getEstimationIfMeasureInGateHorizontally(double x, double y, double angle, double distance, List<Gate> gates, double distanceToWall, boolean overMaxRange) {
        for (Gate gate : gates) {
            double start = Math.min(gate.getFrom().getX(), gate.getTo().getX());
            double end = Math.max(gate.getFrom().getX(), gate.getTo().getX());
            if (x > start && x < end) {
                return estimateInRoomBehindGate(room.getRoomBehindGate(gate), x, y, angle, (distance - distanceToWall), overMaxRange);
            }
        }
        return -1;
    }

    private double getEstimationIfMeasureInGateVertically(double x, double y, double angle, double distance, List<Gate> gates, double distanceToWall, boolean overMaxRange) {
        for (Gate gate : gates) {
            double start = Math.min(gate.getFrom().getY(), gate.getTo().getY());
            double end = Math.max(gate.getFrom().getY(), gate.getTo().getY());
            if (y > start && y < end) {
                return estimateInRoomBehindGate(room.getRoomBehindGate(gate), x, y, angle, (distance - distanceToWall), overMaxRange);
            }
        }
        return -1;
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    private double countMeasureEstimation(double distance, double distanceToWall, boolean overMaxRange) {
        if (overMaxRange) {
            return (distanceToWall < distance) ? 0.0 : 1.0;
        }

        double diff = Math.abs(distanceToWall - distance);
        if (diff > CapoRobotConstants.VISION_ESTIMATOR_DISTANCE_ACCURACY) {
            return 0.0;
        }
        return 1.0 - (diff / CapoRobotConstants.VISION_ESTIMATOR_DISTANCE_ACCURACY);
    }
}
