package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

/**
 * Created by kubicz10 on 3/11/15.
 */

//class representing line equation: Ax + By + C = 0
public class Line {
    private double A;
    private double B;
    private double C;

    public Line(double a, double b, double c) {
        A = a;
        B = b;
        C = c;
    }

    public double getA() {
        return A;
    }

    public double getB() {
        return B;
    }

    public double getC() {
        return C;
    }
}
