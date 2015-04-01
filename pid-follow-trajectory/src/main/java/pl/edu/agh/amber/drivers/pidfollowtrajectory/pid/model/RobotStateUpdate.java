package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class RobotStateUpdate {
    public enum State { WAITING, DRIVING, REACHED, TIME_ELAPSED, STOPPED }

    private State state;
    private double linearVelocity;
    private double angularVelocity;

    public RobotStateUpdate(State state, double linearVelocity, double angularVelocity){
        this.state = state;
        this.linearVelocity = linearVelocity;
        this.angularVelocity = angularVelocity;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public State getState() {
        return state;
    }

    public double getLinearVelocity() {
        return linearVelocity;
    }

    @Override
    public String toString() {
        return "RobotStateUpdate{" +
                "state=" + state +
                ", linearVelocity=" + linearVelocity +
                ", angularVelocity=" + angularVelocity +
                '}';
    }
}
