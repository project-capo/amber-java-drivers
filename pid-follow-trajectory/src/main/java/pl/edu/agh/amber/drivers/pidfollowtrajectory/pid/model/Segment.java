package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.utils.MathUtils;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class Segment {
    private int id;
    private Point start;
    private Point end;
    private double length;

    public Segment(int id, Point start, Point end){
        this.id = id;
        this.start = start;
        this.end = end;
        this.length = calculateLength(start, end);
    }

    public int getId() {
        return id;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public double getLength() {
        return length;
    }

    private double calculateLength(Point start, Point end){
        return MathUtils.euclideanDistance(start, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Segment)) return false;

        Segment segment = (Segment) o;

        if (id != segment.id) return false;
        if (Double.compare(segment.length, length) != 0) return false;
        if (!end.equals(segment.end)) return false;
        if (!start.equals(segment.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + start.hashCode();
        result = 31 * result + end.hashCode();
        temp = Double.doubleToLongBits(length);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                ", length=" + length +
                '}';
    }
}
