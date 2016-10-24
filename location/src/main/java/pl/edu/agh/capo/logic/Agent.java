package pl.edu.agh.capo.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.logic.estimator.PerpendicularLinesLocationEstimator;
import pl.edu.agh.capo.logic.fitness.AbstractFitnessEstimator;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.maze.Gate;
import pl.edu.agh.capo.robot.CapoRobotMotionModel;
import pl.edu.agh.capo.robot.Measure;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;

public class Agent {
    private static final Logger logger = LoggerFactory.getLogger(Agent.class);
    private static final int FITNESS_QUEUE_MAX_SIZE = 15;
    private final Random random = new Random();
    private final CapoRobotMotionModel motionModel;
    private final Class<? extends AbstractFitnessEstimator> fitnessEstimatorClass;
    private PerpendicularLinesLocationEstimator locationEstimator;
    private AbstractFitnessEstimator fitnessEstimator;
    private Measure measure;
    private double fitness;
    private Room room;
    private boolean isTheBest;
    private final Queue<Double> fitnesses = new LinkedList<>();
    private double energy;
    private Location bestLocationInRoom;
    private double bestLocationFitness = -2;

    public Agent(Class<? extends AbstractFitnessEstimator> fitnessEstimatorClass, Room room) {
        this(fitnessEstimatorClass, room, new Location(room.getCenter(), 0), 0.0);
    }

    public Agent(Class<? extends AbstractFitnessEstimator> fitnessEstimatorClass, Room room, Location location, double startEnergy) {
        this.fitnessEstimatorClass = fitnessEstimatorClass;
        this.room = room;
        this.motionModel = new CapoRobotMotionModel(location);
        this.energy = startEnergy;
        this.fitness = energy;
        for (int i = 0; i < FITNESS_QUEUE_MAX_SIZE; i++) {
            fitnesses.add(fitness);
        }
    }

    private AbstractFitnessEstimator buildEstimator() {
        try {
            Constructor constructor = fitnessEstimatorClass.getDeclaredConstructor(Room.class, Measure.class);
            return (AbstractFitnessEstimator) constructor.newInstance(room, measure);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            logger.error("Could not initialize FitnessEstimator, counstuctor with Room.class param is required");
            System.exit(-1);
        }
        return null;
    }

    public void setIsTheBest(boolean isTheBest) {
        this.isTheBest = isTheBest;
    }

    public void setMeasure(Measure measure, double deltaTimeInMillis) {
        this.measure = measure;
        fitnessEstimator = buildEstimator();

        if (deltaTimeInMillis > 0) {
            applyMotion(measure, deltaTimeInMillis);
        }
        fitness = estimateLocation();
        updateAlphaWithVisionAngles(getLocation().getCoordinates());
        resetBestLocation();
    }

    private synchronized void applyMotion(Measure measure, double deltaTimeInMillis) {
        Location location = motionModel.getLocationAfterTime(measure, deltaTimeInMillis);
        updateLocationAndRoomIfNeeded(measure, location, deltaTimeInMillis);
    }

    private void updateLocationAndRoomIfNeeded(Measure measure, Location location, double deltaTimeInMillis) {
        Gate gate;
        if (location.positionX <= room.getMinX()) {
            gate = checkWestGates(location);
        } else if (location.positionX >= room.getMaxX()) {
            gate = checkEastGates(location);
        } else if (location.positionY <= room.getMinY()) {
            gate = checkNorthGates(location);
        } else if (location.positionY >= room.getMaxY()) {
            gate = checkSouthGates(location);
        } else {
            motionModel.applyLocation(location, measure, deltaTimeInMillis);
            //printIfBest(measure.toString());
            return;
        }
        if (gate != null) {
            motionModel.applyLocation(location, measure, deltaTimeInMillis);
            this.room = room.getRoomBehindGate(gate);
            //printIfBEst("Changed to " + room.getSpaceId());
        }
    }

    private Gate checkSouthGates(Location location) {
        return checkGates(room.getSouthGates(), location.positionX, this::horizontalGateStart, this::horizontalGateEnd);
    }

    private Gate checkEastGates(Location location) {
        return checkGates(room.getEastGates(), location.positionY, this::verticalGateStart, this::verticalGateEnd);
    }

    private Gate checkNorthGates(Location location) {
        return checkGates(room.getNorthGates(), location.positionX, this::horizontalGateStart, this::horizontalGateEnd);
    }

    private Gate checkWestGates(Location location) {
        return checkGates(room.getWestGates(), location.positionY, this::verticalGateStart, this::verticalGateEnd);
    }

    private double horizontalGateStart(Gate gate) {
        return Math.min(gate.getFrom().getX(), gate.getTo().getX());
    }

    private double horizontalGateEnd(Gate gate) {
        return Math.max(gate.getFrom().getX(), gate.getTo().getX());
    }

    private double verticalGateStart(Gate gate) {
        return Math.min(gate.getFrom().getY(), gate.getTo().getY());
    }

    private double verticalGateEnd(Gate gate) {
        return Math.max(gate.getFrom().getY(), gate.getTo().getY());
    }

    private Gate checkGates(List<Gate> gatesToCheck, double coordinate, Function<Gate, Double> getStart, Function<Gate, Double> getEnd) {
        for (Gate gate : gatesToCheck) {
            double start = getStart.apply(gate);
            double end = getEnd.apply(gate);
            if (numberInRange(coordinate, start, end)) {
                return gate;
            }
        }
        return null;
    }

    private boolean numberInRange(double number, double start, double end) {
        return number > start && number < end;
    }

    public Location getLocation() {
        return motionModel.getLocation();
    }

    public double getFitness() {
        return fitness;
    }

    public Room getRoom() {
        return room;
    }

    public void prepareCalculations() {
        locationEstimator = new PerpendicularLinesLocationEstimator(room, measure);
    }

    public boolean calculate() {
        if (locationEstimator.size() > 0) {
            Location location = locationEstimator.pop();
            //double fitness = getFitness();
            tryAndChangeBestPositionIfBetterEstimation(location.getCoordinates(), location.alpha);
/*            if (fitness != getFitness() && getFitness() > 0.7) {
                System.out.println("Changed**************************************************************************");
            }*/
            return true;
        } else {
            return false;
        }
    }

    public void estimateRandomWithAngles() {
        Coordinates coordinates = room.getRandomPosition();
        for (Double angle : measure.getAngles()) {
            tryAndChangeBestPositionIfBetterEstimation(coordinates, angle);
        }
    }

    public void estimateRandom() {
        Coordinates coordinates = room.getRandomPosition();
        double angle = random.nextDouble() * 360 - 180;
        tryAndChangeBestPositionIfBetterEstimation(coordinates, angle);
    }

    public void estimateInNeighbourhood() {
        Coordinates coordinates = room.getRandomPositionInNeighbourhoodOf(getLocation());
        double angle = random.nextDouble() * 360 - 180;
        tryAndChangePositionIfBetterEstimation(coordinates, angle);
    }

    public void estimateWithAnglesInNeighbourhood() {
        Coordinates coordinates = room.getRandomPositionInNeighbourhoodOf(getLocation());
        updateAlphaWithVisionAngles(coordinates);
    }

    private void tryAndChangeBestPositionIfBetterEstimation(Coordinates coords, Double angle) {
        Location location = new Location(coords, angle);
        if (location.inNeighbourhoodOf(getLocation())) {
            tryAndChangePositionIfBetterEstimation(coords, angle);
        } else {
            double estimated = fitnessEstimator.estimateFitnessByTries(coords, angle);
            if (estimated > bestLocationFitness) {
                this.bestLocationFitness = estimated;
                bestLocationInRoom = location;
            }
        }
    }

    private void tryAndChangePositionIfBetterEstimation(Coordinates coords, Double angle) {
        double estimated = fitnessEstimator.estimateFitnessByTries(coords, angle
        );
        if (estimated > fitness) {
            this.fitness = estimated;
            Location location = new Location(coords, angle);
            motionModel.applyLocation(location);
//
//            if (fitnessEstimator instanceof ClusterFitnessEstimator) {
//                sectionAward = ((ClusterFitnessEstimator) fitnessEstimator).getSectionAward();
//            }
        }
    }

    /**
     * Match coordinates with vision angles
     */
    private void updateAlphaWithVisionAngles(Coordinates coords) {
        for (Double angle : measure.getAngles()) {
            tryAndChangePositionIfBetterEstimation(coords, angle);
        }
    }

    public void setLocation(Location location, double fitness) {
        this.fitness = fitness;
        motionModel.applyLocation(location);
    }

    public void recalculateEnergy() {
        fitnesses.add(fitness);
        energy += fitness / FITNESS_QUEUE_MAX_SIZE;
        double fitness = fitnesses.poll();
        energy -= fitness / FITNESS_QUEUE_MAX_SIZE;
    }

    public double estimateLocation() {
        return fitnessEstimator.estimateFitness(getLocation());
    }

    public double getEnergy() {
        return fitnesses.size() > 0 ? energy : 0.0;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "room=" + room +
                '}';
    }

    public void resetEnergy() {
        fitnesses.clear();
        energy = 0.0;
    }

    public Measure getMeasure() {
        return measure;
    }

    public boolean isBetterLocationInRoom(double fitness) {
        return bestLocationFitness > fitness;
    }

    public double getBestLocationFitness() {
        return bestLocationFitness;
    }

    public Location getBestLocationInRoom() {
        return bestLocationInRoom;
    }

    private void resetBestLocation() {
        bestLocationInRoom = getLocation();
        bestLocationFitness = fitness;
    }

    public AbstractFitnessEstimator getFitnessEstimator() {
        return fitnessEstimator;
    }
}
