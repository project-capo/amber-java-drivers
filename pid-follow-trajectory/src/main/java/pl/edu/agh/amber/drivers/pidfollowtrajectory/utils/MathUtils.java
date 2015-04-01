package pl.edu.agh.amber.drivers.pidfollowtrajectory.utils;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Point;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.ProjectedPoint;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class MathUtils {

    /**
     * Calculates euclidean distance between two points
     * @param a - starting point of segment
     * @param b - ending point of segment
     * @return euclidean distance between a(start) and b(end)
     */
    public static double euclideanDistance(Point a, Point b){
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    /**
     * Calculates APB angle in [0,pi]
     * @param a - point A
     * @param p - point P (point of the angle "root" - between a and b)
     * @param b - point B
     * @return angle APB in radians [0, pi]
     */
    public static double getAngle(Point a, Point p, Point b){
        return Math.acos(cutToAcos(getCosinusOfAngle(a, p, b)));
    }

    /**
     * Calculates projection of point p to the segment defined by [a,b]
     * @param p - point p (point that we want to project)
     * @param a - point a (starting point of segment)
     * @param b - point b (ending point of segment)
     * @return projected point p on segment a,b
     */
    public static ProjectedPoint getProjection(Point p, Point a, Point b){
        double uX = a.getX() - b.getX();
        double uY = a.getY() - b.getY();
        double vX = p.getX() - b.getX();
        double vY = p.getY() - b.getY();

        double uDotV = uX * vX + uY * vY;
        double uLenSegment = Math.pow((a.getX() - b.getX()), 2.0) + Math.pow((a.getY() - b.getY()), 2.0);
        double f = uDotV / uLenSegment;

        return new ProjectedPoint(new Point(b.getX() + f * uX, b.getY() + f * uY, 0), f);
    }

    public static List<Double> normalize(List<Double> numbers){
        double sum = 0.0;
        for (Double number : numbers){
            sum += number;
        }
        List<Double> normalizedNumbers = new LinkedList<Double>();
        for (Double number : numbers){
            normalizedNumbers.add(number/sum);
        }

        return normalizedNumbers;
    }

    /**
     *
     * @param angleInRad - angle in radians to normalize
     * @return angle in radians normalized to [-PI, PI]
     */
    public static double normalizeAngle(double angleInRad){
        return angleInRad - (2 * Math.PI) * Math.floor((angleInRad + Math.PI) / (2 * Math.PI));
    }

    private static double cutToAcos(double x){
        if (x > 1.0){
            return 1.0;
        }
        if (x < -1.0){
            return -1.0;
        }

        return x;
    }

    /**
     * Calculates cosinus of APB angle
     * @param a - point A
     * @param p - point P (point of the angle "root" - between a and b)
     * @param b - point B
     * @return cosinus of angle APB
     */
    private static double getCosinusOfAngle(Point a, Point p, Point b){
        double aX = a.getX() - p.getX();
        double aY = a.getY() - p.getY();
        double bX = b.getX() - p.getX();
        double bY = b.getY() - p.getY();
        double aDotB = aX * bX + aY * bY;

        double denom = euclideanDistance(a, p) * euclideanDistance(p, b);
        if (denom == 0.00){
            return 1.0;
        }
        else{
            return aDotB/denom;
        }
    }
}
