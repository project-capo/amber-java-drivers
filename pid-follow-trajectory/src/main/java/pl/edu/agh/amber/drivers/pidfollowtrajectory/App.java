package pl.edu.agh.amber.drivers.pidfollowtrajectory;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.controller.PidFollowTrajectory;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.controller.PidFollowTrajectoryController;

public class App
{
    public static void main( String[] args )
    {
        PidFollowTrajectory pidFollowTrajectory = new PidFollowTrajectory();
        PidFollowTrajectoryController pidFollowTrajectoryController = new PidFollowTrajectoryController(System.in, System.out, pidFollowTrajectory);
        pidFollowTrajectoryController.run();
    }
}