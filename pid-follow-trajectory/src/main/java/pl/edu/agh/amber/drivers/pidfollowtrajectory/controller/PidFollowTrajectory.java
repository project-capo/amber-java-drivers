package pl.edu.agh.amber.drivers.pidfollowtrajectory.controller;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms.AlgorithmParams;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.controllers.RocopieController;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers.LocalizationDriver;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers.VelocityDriver;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Trajectory;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.utils.AmberClientSingleton;

public class PidFollowTrajectory {
    private RocopieController rocopieController;
    private AlgorithmParams algorithmParams;
    private Trajectory trajectory;

    public void setTrajectory(Trajectory trajectory){
        this.trajectory = trajectory;
    }

    public void setAlgorithmParams(AlgorithmParams algorithmParams){
        this.algorithmParams = algorithmParams;
    }

    public Runnable getControllerRunnable() {
        return rocopieController;
    }

    public void runStep(){
        rocopieController.runStep();
    }

    public void stopExecution(){
        rocopieController.stopExecution();
    }

    public AlgorithmParams getAlgorithmParams() {
        return algorithmParams;
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }

    public void initializeController(){
        VelocityDriver velocityDriver = new VelocityDriver(AmberClientSingleton.getAmberRoboclawProxyInstance());
        LocalizationDriver localizationDriver = new LocalizationDriver(AmberClientSingleton.getAmberLocationProxyInstance());
        rocopieController = new RocopieController(trajectory, velocityDriver, localizationDriver, algorithmParams);
    }
}
