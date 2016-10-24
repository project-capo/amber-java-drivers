package pl.edu.agh.capo.logic.fitness;

import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.maze.Coordinates;

public abstract class AbstractFitnessEstimator {
    public abstract double estimateFitnessByTries(Coordinates coords, Double angle);

    public abstract double estimateFitness(Location location);

    public void printDebug() {
    }
}
