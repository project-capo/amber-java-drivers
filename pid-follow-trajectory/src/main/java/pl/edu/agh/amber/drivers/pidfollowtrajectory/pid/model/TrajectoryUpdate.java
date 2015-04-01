package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class TrajectoryUpdate {
    public enum State { END_OF_ROUTE, LAST_SEGMENT, MIDDLE_SEGMENT, UNKNOWN }

    private State state;
    private double weight;

    public TrajectoryUpdate(State state, double weight){
        this.state = state;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public State getState() {
        return state;
    }

}
