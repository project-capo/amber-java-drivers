package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.exception.TimeElapsedException;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.utils.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class Trajectory {
    private Map<Integer, Segment> segments;
    private long zeroTime;
    private int currentSegmentId;
    private long destinationTime;
    private double lengthToDestination;

    public Trajectory(List<Point> points){
        this.segments = createSegments(points);
    }

    public long getZeroTime() {
        return zeroTime;
    }

    public void setZeroTime(long zeroTime) {
        this.zeroTime = zeroTime;
    }

    public int getNumberOfSegments(){
        return segments.size();
    }

    public int getCurrentSegmentId() {
        return currentSegmentId;
    }

    public Segment getCurrentSegment(){
        return getSegment(getCurrentSegmentId());
    }

    public Segment getSegment(int segmentId){
        return segments.get(segmentId);
    }

    public void setCurrentSegmentId(int currentSegmentId) {
        this.currentSegmentId = currentSegmentId;
    }

    public void setDestinationValues(long destinationTime, double lengthToDestination){
        this.destinationTime = destinationTime;
        this.lengthToDestination = lengthToDestination;
    }

    public TrajectoryUpdate passFinishedSegments(Point position){
        Segment currentSegment = getCurrentSegment();
        if (currentSegmentId == (getNumberOfSegments() - 1)){
            ProjectedPoint projectedPoint = MathUtils.getProjection(position, currentSegment.getStart(), currentSegment.getEnd());

            if (projectedPoint.getF() < 0.0){
                return new TrajectoryUpdate(TrajectoryUpdate.State.END_OF_ROUTE, 0.0);
            }
            else {
                return new TrajectoryUpdate(TrajectoryUpdate.State.LAST_SEGMENT, 0.0);
            }
        }
        else{
            Segment nextSegment = getSegment(currentSegmentId + 1);
            double angleAB = MathUtils.getAngle(currentSegment.getStart(), currentSegment.getEnd(), nextSegment.getEnd());
            double angleAPos = MathUtils.getAngle(currentSegment.getStart(), currentSegment.getEnd(), position);
            double angleBisect = angleAB / 2.0;

            if (angleAPos >= angleBisect){ //segment passed
                setCurrentSegmentId(getCurrentSegmentId() + 1);
                return passFinishedSegments(position);
            }
            else { //position within segment
                return new TrajectoryUpdate(TrajectoryUpdate.State.MIDDLE_SEGMENT, 1 -(angleAPos / angleBisect));
            }
        }
    }

    public double getCurrentDesiredSpeed(long nowTimestamp) throws TimeElapsedException {
        long timeLeft = zeroTime + destinationTime - nowTimestamp;
        if (timeLeft > 0){
            return lengthToDestination/timeLeft;
        }
        else {
            throw new TimeElapsedException();
        }
    }

    private Map<Integer, Segment> createSegments(List<Point> points){
        Map<Integer, Segment> map = new HashMap<>();

        for (int i = 0; i < points.size() - 1; i++){
            map.put(i, new Segment(i, points.get(i), points.get(i + 1)));
        }

        return map;
    }

    @Override
    public String toString() {
        return "Trajectory{" +
                "segments=" + segments +
                ", zeroTime=" + zeroTime +
                ", currentSegmentId=" + currentSegmentId +
                ", destinationTime=" + destinationTime +
                ", lengthToDestination=" + lengthToDestination +
                '}';
    }
}
