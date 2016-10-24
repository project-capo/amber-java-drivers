package pl.edu.agh.capo.logic.fitness;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.maze.Gate;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.Measure;

import java.util.List;

public class HoughFitnessEstimator extends VisionFitnessEstimator {

    public HoughFitnessEstimator(Room room, Measure measure) {
        super(room, measure);
    }

    @Override
    protected double estimateFitness(Coordinates coords, Double angle) {
        double srb = 0;
        for (Line line : lines) {
            double wallAlpha = Location.normalizeAlpha(angle + line.getTheta());
            Wall wallType;
            if (wallAlpha >= 45 && wallAlpha <= 135) {
                wallType = Wall.EAST;
            } else if (wallAlpha >= -45 && wallAlpha < 45) {
                wallType = Wall.NORTH;
            } else if (wallAlpha >= -135 || wallAlpha < -45) {
                wallType = Wall.WEST;
            } else {
                wallType = Wall.SOUTH;
            }
            double lineFitness = estimateLineFitness(wallType, line, coords);
            double alphaFitness = estimateAngleFitness(wallType, wallAlpha);
            srb += (alphaFitness + lineFitness) / 2;
        }
        srb /= lines.size();
        return srb;
    }

    private double estimateAngleFitness(Wall wall, double wallAlpha) {
        double alphaDifference = wall.calculateAlphaDifference(wallAlpha);
        if (alphaDifference > CapoRobotConstants.LINE_ESTIMATOR_ANGLE_ACCURACY) {
            return 0.0;
        }
        return 1.0 - (alphaDifference / CapoRobotConstants.LINE_ESTIMATOR_ANGLE_ACCURACY);
    }

    private double estimateLineFitness(Wall wall, Line line, Coordinates coordinates) {
        double visionLineDistance = line.getRho();
        double minWallDistanceDifference = estimateMinDistanceDifference(wall, room, coordinates, visionLineDistance, Double.MAX_VALUE);
        if (minWallDistanceDifference > CapoRobotConstants.LINE_ESTIMATOR_RHO_ACCURACY) {
            return 0.0;
        }
        return 1.0 - (minWallDistanceDifference / CapoRobotConstants.LINE_ESTIMATOR_RHO_ACCURACY);
    }


    private double estimateMinDistanceDifference(Wall wall, Room room, Coordinates coordinates, double visionLineDistance, double minDistanceDifference) {
        double distanceDifference = calculateDistanceDifference(wall, visionLineDistance, room, coordinates);
        if (distanceDifference < minDistanceDifference) {
            minDistanceDifference = distanceDifference;
            for (Wall currentWall : Wall.values()) {
                if (!currentWall.equals(wall.oppositeWall())) {
                    for (Gate gate : currentWall.getGates(room)) {
                        Room nextRoom = room.getRoomBehindGate(gate);
                        minDistanceDifference = Math.min(minDistanceDifference, estimateMinDistanceDifference(wall, nextRoom, coordinates, visionLineDistance, minDistanceDifference));
                    }
                }
            }
        }
        return minDistanceDifference;
    }

    private double calculateDistanceDifference(Wall wall, double visionLineDistance, Room room, Coordinates coordinates) {
        double rightWallDistance = wall.calculateDistanceToWall(room, coordinates);
        return Math.abs(rightWallDistance - visionLineDistance);
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
        return estimateFitness(coords, angle);
    }

    private enum Wall {
        EAST {
            @Override
            Wall oppositeWall() {
                return WEST;
            }

            @Override
            List<Gate> getGates(Room room) {
                return room.getEastGates();
            }

            @Override
            double calculateDistanceToWall(Room room, Coordinates coordinates) {
                return room.getMaxX() - coordinates.getX();
            }

            @Override
            public double calculateAlphaDifference(double wallAlpha) {
                return Math.abs(90 - wallAlpha);
            }

            @Override
            public double getLength(Room room) {
                return room.getMaxY() - room.getMinY();
            }
        },
        WEST {
            @Override
            Wall oppositeWall() {
                return EAST;
            }

            @Override
            List<Gate> getGates(Room room) {
                return room.getWestGates();
            }

            @Override
            double calculateDistanceToWall(Room room, Coordinates coordinates) {
                return coordinates.getX() - room.getMinX();
            }

            @Override
            public double calculateAlphaDifference(double wallAlpha) {
                return Math.abs(-90 - wallAlpha);
            }

            @Override
            public double getLength(Room room) {
                return room.getMaxY() - room.getMinY();
            }
        },
        NORTH {
            @Override
            Wall oppositeWall() {
                return SOUTH;
            }

            @Override
            List<Gate> getGates(Room room) {
                return room.getNorthGates();
            }

            @Override
            double calculateDistanceToWall(Room room, Coordinates coordinates) {
                return coordinates.getY() - room.getMinY();
            }

            @Override
            public double calculateAlphaDifference(double wallAlpha) {
                return Math.abs(wallAlpha);
            }

            @Override
            public double getLength(Room room) {
                return room.getMaxX() - room.getMinX();
            }
        },
        SOUTH {
            @Override
            Wall oppositeWall() {
                return NORTH;
            }

            @Override
            List<Gate> getGates(Room room) {
                return room.getSouthGates();
            }

            @Override
            double calculateDistanceToWall(Room room, Coordinates coordinates) {
                return room.getMaxY() - coordinates.getY();
            }

            @Override
            public double calculateAlphaDifference(double wallAlpha) {
                return 180 - Math.abs(wallAlpha);
            }

            @Override
            public double getLength(Room room) {
                return room.getMaxX() - room.getMinX();
            }
        };

        abstract Wall oppositeWall();

        abstract List<Gate> getGates(Room room);

        abstract double calculateDistanceToWall(Room room, Coordinates coordinates);

        public abstract double calculateAlphaDifference(double wallAlpha);

        public abstract double getLength(Room room);
    }
}
