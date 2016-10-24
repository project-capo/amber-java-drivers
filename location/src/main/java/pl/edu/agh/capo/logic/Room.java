package pl.edu.agh.capo.logic;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.maze.Gate;
import pl.edu.agh.capo.maze.Wall;
import pl.edu.agh.capo.maze.helper.MazeHelper;
import pl.edu.agh.capo.robot.CapoRobotConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Room {

    private final static Random random = new Random();
    private final List<Wall> walls;
    private final List<Gate> gates;
    private final double minY;
    private final double maxY;
    private final double minX;
    private final double maxX;
    private final List<Gate> northGates;
    private final List<Gate> southGates;
    private final List<Gate> westGates;
    private final List<Gate> eastGates;
    private final String spaceId;
    private Map<Integer, Room> gateRooms;
    private final List<Point2D[]> wallVectors = new ArrayList<>();
    private final List<Point2D[]> gateVectors = new ArrayList<>();
    private List<Room> rooms;

    public Room(List<Wall> walls, List<Gate> gates, String spaceId) {
        this.spaceId = spaceId;
        this.walls = walls;

        groupWalls(walls).forEach(wall -> wallVectors.add(buildWallVector(wall)));

        this.gates = gates;
        gates.forEach(gate -> gateVectors.add(buildGateVector(gate)));

        northGates = new ArrayList<>();
        southGates = new ArrayList<>();
        westGates = new ArrayList<>();
        eastGates = new ArrayList<>();

        minY = MazeHelper.getMinY(walls);
        maxY = MazeHelper.getMaxY(walls);
        minX = MazeHelper.getMinX(walls);
        maxX = MazeHelper.getMaxX(walls);

        splitGates();
    }

    private void printWalls(List<Wall> walls) {
        System.out.println(spaceId);
        for (Wall wall : walls) {
            System.out.println("buildWall(\"" + wall.getId() + "\"" + ", " + wall.getFrom().getX() + ", " + wall.getFrom().getY() + ", " + wall.getTo().getX() + ", " + wall.getTo().getY() + "),");
        }
    }

    private Point2D[] buildGateVector(Gate gate) {
        Point2D wallStart = new Point2D(gate.getFrom().getX(), gate.getFrom().getY());
        Point2D wallEnd = new Point2D(gate.getTo().getX(), gate.getTo().getY());
        return new Point2D[]{wallStart, wallEnd};
    }

    protected Point2D[] buildWallVector(Wall wall) {
        Point2D wallStart = new Point2D(wall.getFrom().getX(), wall.getFrom().getY());
        Point2D wallEnd = new Point2D(wall.getTo().getX(), wall.getTo().getY());
        return new Point2D[]{wallStart, wallEnd};
    }

    private boolean isColinear(Wall wall, Wall wall2) {
        Vector2D wallVector = fromWall(wall);
        Vector2D nextWallVector = fromWall(wall2);
        return wallVector.isColinear(nextWallVector);
    }

    private List<Wall> groupWalls(List<Wall> walls) {
        List<Wall> result = new ArrayList<>();
        int wallSize = walls.size();
        for (int i = 0; i < wallSize; i++) {
            Wall wall = new Wall(walls.get(i));
            int j;
            for (j = i + 1; j < wallSize; j++) {
                Wall nextWall = walls.get(j);
                if (isColinear(nextWall, wall)) {
                    if (wall.getTo().equals(nextWall.getFrom())) {
                        wall.setTo(nextWall.getTo());
                    } else if (wall.getFrom().equals(nextWall.getTo())) {
                        wall.setFrom(nextWall.getFrom());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            i = j - 1;
            result.add(wall);
        }
        return result;
    }

    private Vector2D fromWall(Wall wall) {
        return new Vector2D(new Point2D(wall.getFrom().getX(), wall.getFrom().getY()), new Point2D(wall.getTo().getX(), wall.getTo().getY()));
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public List<Gate> getNorthGates() {
        return northGates;
    }

    public List<Gate> getSouthGates() {
        return southGates;
    }

    public List<Gate> getWestGates() {
        return westGates;
    }

    public List<Gate> getEastGates() {
        return eastGates;
    }

    public String getSpaceId() {
        return spaceId;
    }

    private void splitGates() {
        for (Gate gate : gates) {
            if (gate.getFrom().getX() == gate.getTo().getX()) {
                if (Math.abs(minX - gate.getFrom().getX()) < Math.abs(maxX - gate.getFrom().getX())) {
                    westGates.add(gate);
                } else {
                    eastGates.add(gate);
                }
            } else {
                if (Math.abs(minY - gate.getFrom().getY()) < Math.abs(maxY - gate.getFrom().getY())) {
                    northGates.add(gate);
                } else {
                    southGates.add(gate);
                }
            }
        }
    }

    public boolean coordinatesMatches(double x, double y) {
        return !(x < minX || x > maxX || y < minY || y > maxY);
    }

    public Coordinates getRandomPosition() {
        return getRandom(minX, maxX, minY, maxY);
    }

    public Coordinates getRandomPositionInNeighbourhoodOf(Location location) {
        return getRandomWithGaussianDistribution(location);
    }

    private Coordinates getRandomWithGaussianDistribution(Location location) {
        double x = triangularDistributionOfCoordinate(location.positionX, minX, maxX);
        double y = triangularDistributionOfCoordinate(location.positionY, minY, maxY);
        return createCoordinates(x, y);
    }

    private double triangularDistributionOfCoordinate(double coordinate, double min, double max) {
        double minCoordinate = coordinate - CapoRobotConstants.NEIGHBOURHOOD_SCOPE;
        double maxCoordinate = coordinate + CapoRobotConstants.NEIGHBOURHOOD_SCOPE;

        double result;
        do {
            result = triangularDistribution(minCoordinate, maxCoordinate, coordinate);
        } while (result > max || result < min);
        return result;
    }

    private double triangularDistribution(double left, double right, double middle) {
        double F = (middle - left) / (right - left);
        double rand = Math.random();
        if (rand < F) {
            return left + Math.sqrt(rand * (right - left) * (middle - left));
        } else {
            return right - Math.sqrt((1 - rand) * (right - left) * (right - middle));
        }
    }

    private Coordinates getRandom(double minX, double maxX, double minY, double maxY) {
        double x = randomInRange(minX, maxX);
        double y = randomInRange(minY, maxY);
        return createCoordinates(x, y);
    }

    private double randomInRange(double min, double max) {
        return Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min);
    }

    private Coordinates createCoordinates(double x, double y) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(x);
        coordinates.setY(y);
        return coordinates;
    }

    public void setGateRooms(Map<Integer, Room> gateRooms) {
        this.gateRooms = gateRooms;
        this.rooms = gates.stream().map(this::getRoomBehindGate).collect(Collectors.toList());
    }

    public Room getRoomBehindGate(Gate gate) {
        return gateRooms.get(gates.indexOf(gate));
    }

    public Coordinates getCenter() {
        Coordinates coordinates = new Coordinates();
        coordinates.setX(minX + ((maxX - minX) / 2));
        coordinates.setY(minY + ((maxY - minY) / 2));
        return coordinates;
    }

    @Override
    public String toString() {
        return "Room{" +
                "spaceId='" + spaceId + '\'' +
                '}';
    }

    public List<Point2D[]> getGateVectors() {
        return gateVectors;
    }

    public List<Point2D[]> getWallVectors() {
        return wallVectors;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Room getRoomBehindGate(Point2D[] gate) {
        return gateRooms.get(gateVectors.indexOf(gate));
    }

    public List<Wall> getGroupWalls() {
        return groupWalls(walls);
    }
}
