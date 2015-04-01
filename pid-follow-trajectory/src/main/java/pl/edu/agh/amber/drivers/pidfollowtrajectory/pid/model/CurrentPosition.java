package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class CurrentPosition {
    private Point position;
    private double angle;

    public CurrentPosition(double x, double y, double angle, long time){
        this.position = new Point(x, y, time);
        this.angle = angle;
    }

    public Point getPosition(){
        return position;
    }

    public double getAngle(){
        return angle;
    }

    @Override
    public String toString() {
        return "CurrentPosition{" +
                "position=" + position +
                ", angle=" + angle +
                '}';
    }
}
