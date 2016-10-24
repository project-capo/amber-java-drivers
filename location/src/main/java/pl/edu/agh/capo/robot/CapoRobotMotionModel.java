package pl.edu.agh.capo.robot;


import pl.edu.agh.capo.common.Location;

public class CapoRobotMotionModel {

    private Location location;
    private double velocityLeft;
    private double velocityRight;
    private double accelerationLeft;
    private double accelerationRight;
    private boolean isRandom;

    public CapoRobotMotionModel(Location location) {
        this.location = location;
    }

    /**
     * First calculate the Center of the circle:
     * arcCenter = [x − R sin(θ) , y + R cos(θ)]
     * Then calculate new location:
     * rotating a distance R about its ICC with an angular velocity of ω.
     * <p>
     * http://chess.eecs.berkeley.edu/eecs149/documentation/differentialDrive.pdf
     */
    public Location getLocationAfterTime(Measure measure, double deltaTimeInMillis) {
        double velocityRight = measure.getRightVelocity();
        double velocityLeft = measure.getLeftVelocity();
        double deltaTime = deltaTimeInMillis / 1000.0;

        if (velocitiesNeedCorrection(velocityLeft, velocityRight, deltaTime)) {
            velocityRight = this.velocityRight + accelerationRight * deltaTime;
            velocityLeft = this.velocityLeft + accelerationLeft * deltaTime;
        }

        if (velocityLeft == velocityRight) {
            return calculateLocation(velocityRight, deltaTime);
        }
        return calculateLocationWithDirectionChange(velocityRight, velocityLeft, deltaTime);
    }

    public void applyLocation(Location location, Measure measure, double deltaTimeInMillis) {
        double deltaTime = deltaTimeInMillis / 1000.0;
        this.location = location;
        this.accelerationLeft = calculateAcceleration(velocityLeft, measure.getLeftVelocity(), deltaTime);
        this.accelerationRight = calculateAcceleration(velocityRight, measure.getRightVelocity(), deltaTime);
        this.velocityRight = measure.getRightVelocity();
        this.velocityLeft = measure.getLeftVelocity();
        this.isRandom = false;
    }

    public void applyLocation(Location location) {
        this.location = location;
        this.isRandom = true;
    }

    private Location calculateLocationWithDirectionChange(double velocityRight, double velocityLeft, double deltaTime) {
        double alpha = getAdjustedDirectionToRadians();
        double alphaDiff = ((velocityRight - velocityLeft) * deltaTime) / CapoRobotConstants.WHEELS_DISTANCE;

        double newX = location.positionX + ((CapoRobotConstants.WHEELS_DISTANCE * (velocityRight + velocityLeft))
                / (2 * (velocityRight - velocityLeft)) * (Math.sin(alphaDiff + alpha) - Math.sin(alpha)));
        double newY = location.positionY - ((CapoRobotConstants.WHEELS_DISTANCE * (velocityRight + velocityLeft)) /
                (2 * (velocityRight - velocityLeft)) * (Math.cos(alphaDiff + alpha) - Math.cos(alpha)));


        return new Location(newX, newY, adjustDirectionToDegrees(alpha + alphaDiff));
    }

    private double adjustDirectionToDegrees(double alpha) {
        return Location.normalizeAlpha(Math.toDegrees(alpha) + 90);
    }

    private double getAdjustedDirectionToRadians() {
        double normalizedAngle = Location.normalizeAlpha(location.alpha - 90);
        return Math.toRadians(normalizedAngle);
    }

    private Location calculateLocation(double velocityLeft, double deltaTime) {
        double angle = getAdjustedDirectionToRadians();
        double x = location.positionX + velocityLeft * Math.cos(angle) * deltaTime;
        double y = location.positionY + velocityLeft * Math.sin(angle) * deltaTime;
        return new Location(x, y, location.alpha);
    }

    private double getLinearVelocity(double velocityRight, double velocityLeft) {
        return (velocityLeft + velocityRight) / 2;
    }

    private double getLinearVelocity() {
        return getLinearVelocity(velocityRight, velocityLeft);
    }

    private boolean velocityExceedMax(double linearVelocity) {
        return Math.abs(linearVelocity) > CapoRobotConstants.MAX_LINEAR_VELOCITY;
    }

    private boolean accelerationExceedMax(double linearVelocity, double deltaTime) {
        double acceleration = calculateAcceleration(getLinearVelocity(), linearVelocity, deltaTime);
        return !isRandom && Math.abs(acceleration) > CapoRobotConstants.MAX_ACCELERATION;
    }

    private boolean velocitiesNeedCorrection(double velocityRight, double velocityLeft, double deltaTime) {
        double linearVelocity = getLinearVelocity(velocityLeft, velocityRight);
        return velocityExceedMax(linearVelocity) || accelerationExceedMax(linearVelocity, deltaTime);
    }

    private double calculateAcceleration(double V1, double V2, double deltaTime) {
        return (V2 - V1) / deltaTime;
    }

    public Location getLocation() {
        return location;
    }
}


