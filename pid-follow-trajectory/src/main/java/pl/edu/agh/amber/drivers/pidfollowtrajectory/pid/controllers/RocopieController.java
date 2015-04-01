package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms.AlgorithmParams;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms.SpeedCalculator;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers.LocalizationDriver;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers.VelocityDriver;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.CurrentPosition;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.RobotStateUpdate;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Trajectory;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.TrajectoryUpdate;

/**
 * Created by kubicz10 on 3/16/15.
 */
public class RocopieController implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(RocopieController.class);

    private Trajectory trajectory;
    private SpeedCalculator speedCalculator;
    private AlgorithmParams algorithmParams;
    private VelocityDriver velocityDriver;
    private LocalizationDriver localizationDriver;
    private TrajectoryUpdate.State trajectoryState;
    private RobotStateUpdate.State robotState;

    public RocopieController(Trajectory trajectory, VelocityDriver velocityDriver, LocalizationDriver localizationDriver, AlgorithmParams algorithmParams){
        this.trajectory = trajectory;
        this.algorithmParams = algorithmParams;
        this.velocityDriver = velocityDriver;
        this.localizationDriver = localizationDriver;
        this.robotState = RobotStateUpdate.State.WAITING;
        this.speedCalculator = new SpeedCalculator(this.trajectory, this.algorithmParams);
    }


    @Override
    public void run() {
        trajectory.setCurrentSegmentId(0);
        CurrentPosition currentPosition = localizationDriver.getCurrentPosition();
        trajectory.setZeroTime(currentPosition.getPosition().getTime());

        robotState = RobotStateUpdate.State.DRIVING;
        logger.info("Starting robot. Current position: {}", currentPosition);
        while (robotState.equals(RobotStateUpdate.State.DRIVING)){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentPosition = localizationDriver.getCurrentPosition();
            while (currentPosition == null){
                currentPosition = localizationDriver.getCurrentPosition();
            }
            robotState = updateRobotState(currentPosition);
        }
        logger.info("Stoping robot. State: {}. Current position: {}", robotState, currentPosition);
        setRobotVelocity(0.0, 0.0);
    }

    private RobotStateUpdate.State updateRobotState(CurrentPosition currentPosition){
        RobotStateUpdate robotStateUpdate = speedCalculator.getSpeed(currentPosition);
        logger.info("Updating robot state: {}", robotStateUpdate);
        setRobotVelocity(robotStateUpdate.getLinearVelocity(), robotStateUpdate.getAngularVelocity());

        return robotStateUpdate.getState();
    }

    private void setRobotVelocity(double linearVelocity, double angularVelocity){
        velocityDriver.setVelocity(linearVelocity, -angularVelocity);
    }
}
