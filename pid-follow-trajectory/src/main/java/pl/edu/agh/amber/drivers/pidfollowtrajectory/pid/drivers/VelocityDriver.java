package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

import java.io.IOException;

/**
 * Created by kubicz10 on 3/6/15.
 */
public class VelocityDriver {
    private static Logger logger = LoggerFactory.getLogger(VelocityDriver.class);
    private static final double ROBOT_WHEEL_TRACK = 0.25; // - l - distance between the right and the left wheel in meters

    private RoboclawProxy roboclawProxy;

    public VelocityDriver(RoboclawProxy roboclawProxy){
        this.roboclawProxy = roboclawProxy;
    }

    /**
     * Sets robot velocity
     *
     * @param linearVelocity desired linear velocity of the robot in m/s
     * @param angularVelocity desired angular velocity of the robot in rad/s
     */
    public void setVelocity(double linearVelocity, double angularVelocity) {
        RobotWheelsVelocities wheelsVelocities = getRobotWheelsDesired(linearVelocity, angularVelocity);

        logger.info("Setting robot velocity: linear: {}, angular: {}", linearVelocity, angularVelocity);
        logger.info("Setting wheels velocities: {}", wheelsVelocities);

        try {
            roboclawProxy.sendMotorsCommand(wheelsVelocities.getLeftFront(), wheelsVelocities.getRightFront(), wheelsVelocities.getLeftRear(), wheelsVelocities.getRightRear());
        } catch (IOException e) {
            logger.error("Error while setting wheels velocities: {}", e);
        }
    }

    private RobotWheelsVelocities getRobotWheelsDesired(double linearVelocity, double angularVelocity){
        double angularFactor = angularVelocity * ROBOT_WHEEL_TRACK;

        double rightWheelVelocity = linearVelocity + 0.5 * angularFactor;
        double leftWheelVelocity = linearVelocity - 0.5 * angularFactor;

        rightWheelVelocity = rightWheelVelocity * 1000; //m/s -> mm/s
        leftWheelVelocity = leftWheelVelocity * 1000; //m/s -> mm/s

        return new RobotWheelsVelocities((int)Math.round(leftWheelVelocity), (int)Math.round(rightWheelVelocity), (int)Math.round(leftWheelVelocity), (int)Math.round(rightWheelVelocity));
    }


    private class RobotWheelsVelocities {
        private int leftFront;
        private int rightFront;
        private int leftRear;
        private int rightRear;

        public RobotWheelsVelocities(int leftFront, int rightFront, int leftRear, int rightRear){
            this.leftFront = leftFront;
            this.rightFront = rightFront;
            this.leftRear = leftRear;
            this.rightRear = rightRear;
        }

        public int getLeftRear() {
            return leftRear;
        }

        public int getLeftFront() {
            return leftFront;
        }

        public int getRightFront() {
            return rightFront;
        }

        public int getRightRear() {
            return rightRear;
        }

        @Override
        public String toString() {
            return "RobotWheelsVelocities{" +
                    "leftFront=" + leftFront +
                    ", rightFront=" + rightFront +
                    ", leftRear=" + leftRear +
                    ", rightRear=" + rightRear +
                    '}';
        }
    }
}
