package pl.edu.agh.capo.logic.fitness;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.common.Vision;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.maze.Coordinates;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.Measure;

import java.util.List;

public class VisionFitnessEstimator extends AbstractFitnessEstimator {
    protected final Room room;
    protected final List<Vision> visions;
    protected final List<Line> lines;

    public VisionFitnessEstimator(Room room, Measure measure) {
        this.room = room;
        this.visions = measure.getVisionsProbe();
        this.lines = measure.getLines();
    }

    /**
     * estimates fitness of position based on current visions
     */
    protected double estimateFitness(Coordinates coords, Double angle) {
        VisionFitnessAnalyzer analyzer = new VisionFitnessAnalyzer(room, coords.getX(), coords.getY(), angle);
        for (Vision vision : visions) {
            double result = analyzer.estimate(vision.getAngle(), vision.getDistance());
            vision.setFitness(result);
        }

        return countFitness(visions);
    }

    /**
     * To save computation time we first try few visions to check whether calculating fitness of all visions
     * is sensible
     *
     * @param matches nr of visions that need to check out to continue computation
     */
    @Override
    public double estimateFitnessByTries(Coordinates coords, Double angle) {
        if (CapoRobotConstants.ESTIMATION_TRIES > visions.size()) {
            return estimateFitness(coords, angle);
        }

        VisionFitnessAnalyzer analyzer = new VisionFitnessAnalyzer(room, coords.getX(), coords.getY(), angle);
        int step = visions.size() / CapoRobotConstants.ESTIMATION_TRIES;

        if (CapoRobotConstants.ESTIMATION_MATCHED_TRIES > countFitnessMatches(analyzer, step)) {
            return -1.0;
        }

        for (int i = 0; i < visions.size(); i++) {
            if (i % step == 0) {
                continue;
            }

            Vision vision = visions.get(i);
            double result = analyzer.estimate(vision.getAngle(), vision.getDistance());
            vision.setFitness(result);
        }

        return countFitness(visions);
    }

    protected int countFitnessMatches(VisionFitnessAnalyzer analyzer, int step) {
        int currMatches = 0;
        for (int i = 0; i < visions.size(); i += step) {
            Vision vision = visions.get(i);
            double result = analyzer.estimate(vision.getAngle(), vision.getDistance());
            if (result > 0) {
                currMatches++;
            }
            vision.setFitness(result);
        }
        return currMatches;
    }

    @Override
    public double estimateFitness(Location location) {
        return estimateFitness(location.getCoordinates(), location.alpha);
    }

    private double countFitness(List<Vision> visions) {
        double sum = 0.0;
        int count = 0;
        for (Vision vision : visions) {
            if (vision.getFitness() >= 0) {
                sum += vision.getFitness();
                count++;
            }
        }
        return sum / count;
    }
}
