package pl.edu.agh.capo.common;

import pl.edu.agh.capo.robot.CapoRobotConstants;

public class Line {
    private final double rawTheta;
    private final double rawRho;
    private double rho;
    private double theta;

    public Line(double rawTheta, double rawRho) {
        this.rawTheta = rawTheta;
        this.rawRho = rawRho;
    }

    public double getTheta() {
        return theta;
    }

    public double getRawRho() {
        return rawRho;
    }

    public double getRawTheta() {
        return rawTheta;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getRho() {
        return rho;
    }

    public boolean isPerpendicularTo(Line line) {
        double diff = Math.abs(line.getRawTheta() - getRawTheta());
        return Math.abs(diff - 90) < CapoRobotConstants.PERPENDICULARITY_ACCURANCY;
    }

    @Override
    public String toString() {
        return "Line{" +
                "theta=" + theta +
                ", rho=" + rho +
                '}';
    }
}
