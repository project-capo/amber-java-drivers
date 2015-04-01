package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class ProjectedPoint {
    private Point projectedPoint; // point P projected on line AB
    private double f; // F = BPa/BA (BPa, BA - vectors), in particular when F is negative it means that projection of P is behind point B

    public ProjectedPoint(Point projectedPoint, double f){
        this.projectedPoint = projectedPoint;
        this.f = f;
    }

    public Point getProjectedPoint(){
        return projectedPoint;
    }

    public double getF(){
        return f;
    }
}
