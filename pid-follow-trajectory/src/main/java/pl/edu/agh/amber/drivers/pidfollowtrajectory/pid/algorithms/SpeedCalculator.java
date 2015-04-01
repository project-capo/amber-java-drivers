package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms;

import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.exception.TimeElapsedException;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.*;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.utils.MathUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class SpeedCalculator {

    private Trajectory trajectory;
    private AlgorithmParams algorithmParams;

    public SpeedCalculator(Trajectory trajectory, AlgorithmParams algorithmParams){
        this.trajectory = trajectory;
        this.algorithmParams = algorithmParams;
    }

    public RobotStateUpdate getSpeed(CurrentPosition currentPosition){
        TrajectoryUpdate trajectoryUpdate = trajectory.passFinishedSegments(currentPosition.getPosition());

        switch (trajectoryUpdate.getState()){
            case END_OF_ROUTE:
                return new RobotStateUpdate(RobotStateUpdate.State.REACHED, 0.0, 0.0);
            case LAST_SEGMENT:
                return calculateSpeedForLastSegment(currentPosition);
            case MIDDLE_SEGMENT:
                return calculateSpeedForMiddleSegment(currentPosition);
            default:
                return null;
        }
    }

    private RobotStateUpdate calculateSpeedForMiddleSegment(CurrentPosition currentPosition) {
        //todo: reimplement!
        List<Double> distances = new LinkedList<>();
        double maxLen = getSegmentsInRangeWithWeights(distances, currentPosition);
        double avgRadiusInv = 0.0;

        if (distances.size() > 0){
            if (distances.size() > 1){
                List<Double> weights = MathUtils.normalize(distances);
                List<Segment> segmentsLeft = new LinkedList<>();
                for (int i = trajectory.getCurrentSegmentId(); i < trajectory.getCurrentSegmentId() + weights.size(); i++){
                    segmentsLeft.add(trajectory.getSegment(i));
                }
                List<Double> radiusInvForSegments = new LinkedList<>();
                for (Segment segment : segmentsLeft){
                    radiusInvForSegments.add(calculateRadiusInvForSegment(segment, currentPosition));
                }
                for (int i = 0; i < weights.size(); i++){
                    avgRadiusInv += weights.get(i) * radiusInvForSegments.get(i);
                }
            }
            else {
                avgRadiusInv = calculateRadiusInvForSegment(trajectory.getCurrentSegment(), currentPosition);
            }
        }

        return calculateRobotStateUpdate(RobotStateUpdate.State.DRIVING, avgRadiusInv, currentPosition.getPosition().getTime());
    }

    /**
     *
     */
    private double getSegmentsInRangeWithWeights(List<Double> distances, CurrentPosition currentPosition){
        Segment currentSegment = trajectory.getCurrentSegment();
        ProjectedPoint projectedPoint = MathUtils.getProjection(currentPosition.getPosition(),
                currentSegment.getStart(), currentSegment.getEnd());
        double d1 = MathUtils.euclideanDistance(projectedPoint.getProjectedPoint(), currentSegment.getEnd());

        distances.add(d1);
        if (d1 >= algorithmParams.getLookahead()){
            trajectory.setDestinationValues(currentSegment.getEnd().getTime(), d1);
            return d1;
        }
        else {
            return getSegmentsInRangeWithWeights2(distances, trajectory.getCurrentSegmentId()+1);
        }
    }

    private double getSegmentsInRangeWithWeights2(List<Double> distances, int segmentId){
        if (segmentId == trajectory.getNumberOfSegments()){ //after last segment
            Segment lastSegment = trajectory.getSegment(trajectory.getNumberOfSegments() - 1);
            double di_1 = distances.get(distances.size() - 1);
            trajectory.setDestinationValues(lastSegment.getEnd().getTime(), di_1);
            return di_1;
        }
        else {
            double lastSum = distances.get(distances.size()-1);
            Segment segment = trajectory.getSegment(segmentId);
            double di = lastSum + segment.getLength();
            if (di >= algorithmParams.getLookahead()){ //whole segment wont fit in lookahead
                double offset = algorithmParams.getLookahead() - lastSum; //find what part of this segment is included
                long timeAtPoint = approxIntermediateTime(offset, segment);
                trajectory.setDestinationValues(timeAtPoint, algorithmParams.getLookahead());
                distances.add(algorithmParams.getLookahead());
                return algorithmParams.getLookahead();
            }
            else {
                distances.add(di);
                return getSegmentsInRangeWithWeights2(distances, segmentId + 1);
            }
        }
    }

    /**
     *
     * @param offset - distance (along the segment) from begining to the point
     * @param segment -  segment
     * @return time - apprximate time in point
     */
    private long approxIntermediateTime(double offset, Segment segment){
        double segProcent = offset / segment.getLength();

        long beginTime = segment.getStart().getTime();
        long endTime = segment.getEnd().getTime();

        return (long)(beginTime + (endTime - beginTime) * segProcent);
    }

    private RobotStateUpdate calculateSpeedForLastSegment(CurrentPosition currentPosition) {
        Segment currentSegment = trajectory.getCurrentSegment();
        ProjectedPoint projectedPoint = MathUtils.getProjection(currentPosition.getPosition(), currentSegment.getStart(), currentSegment.getEnd());
        double distanceToEnd = MathUtils.euclideanDistance(projectedPoint.getProjectedPoint(), currentSegment.getEnd());

        trajectory.setDestinationValues(currentSegment.getEnd().getTime(), distanceToEnd);

        double radInv = calculateRadiusInvForSegment(currentSegment, currentPosition);
        return calculateRobotStateUpdate(RobotStateUpdate.State.DRIVING, radInv, currentPosition.getPosition().getTime());
    }

    private double calculateRadiusInvForSegment(Segment segment, CurrentPosition currentPosition){
        return calculateRadiusInv(segment.getStart(), segment.getEnd(), currentPosition);
    }


    //calculates angular and linear speed desired to achive radius of turn = 1/RadiusInv
    private RobotStateUpdate calculateRobotStateUpdate(RobotStateUpdate.State state, double radiusInv, long timeNow){
        double centreAcc = algorithmParams.getCentreAcceleration();
        double maxV = getMaxV(timeNow);
        double newV = computeNewV(radiusInv, centreAcc, maxV);

        // Omega = V/R <=> Omega = V*RInv
        double omega = newV * radiusInv;
        double newOmega;

        if (Math.abs(omega) < 0.00001){
            newOmega = 0.0;
        }
        else{
            newOmega = omega;
        }

        return new RobotStateUpdate(state, newV, newOmega);
    }

    // minimum of max possible V at this turning radius and max V to follow
    private double computeNewV(double radiusInv, double centreAcc, double maxV){
        if (Math.abs(radiusInv - 0.0) < 0.0000000001){
            return maxV;
        }
        else {
            return Math.min(maxV, Math.sqrt(centreAcc/Math.abs(radiusInv)));
        }
    }

    /**
     *
     * @param start - starting point in given trajectory
     * @param end - ending point in given trajectory
     * @param currentPosition - current position
     * @return InvRadius = 1 / (Radius of Turn) based on PID regulator
     */
    private double calculateRadiusInv(Point start, Point end, CurrentPosition currentPosition){
        double yDist = end.getY() - currentPosition.getPosition().getY();
        double distance = MathUtils.euclideanDistance(end, currentPosition.getPosition());
        List<Double> normalizedParams = MathUtils.normalize(Arrays.asList(algorithmParams.getiAlpha(), algorithmParams.getiTrack()));
        double parTheta = normalizedParams.get(0);
        double parTrack = normalizedParams.get(1);

        // equation of a straight line which should be following now
        Line goalLine = calculateLineThroughPoints(start, end);

        // angle between this line and OX axis
        double lineTheta = calculateLineOrientation(goalLine);

        // angular distance in [-PI,PI]
        double thetaDist = calculateAngularDistance(lineTheta, currentPosition.getAngle());

        // linear distance from actual location to the nearest point of the line
        double trackDist = calculateDistanceBetweenPointAndLine(currentPosition.getPosition(), goalLine);

        // PID output is interpreted as the inverse of the turning radius
        // so to go straight, the radius should by very big

        return (parTheta * thetaDist + parTrack * trackDist) * algorithmParams.getScale();
    }

    /**
     *
     * @param start
     * @param end
     * @return line that goes through start and end points
     */

    private Line calculateLineThroughPoints(Point start, Point end){
        return new Line(end.getY() - start.getY(), start.getX() - end.getX(), end.getX() * start.getY() - end.getY() * start.getX());
    }

    /**
     *
     * @param line
     * @return angle between given line and OX axis in [-pi, pi]
     */
    private double calculateLineOrientation(Line line){
        return Math.atan2(line.getA(), -line.getB());
    }

    private double calculateAngularDistance(double theta1, double theta2){
        return MathUtils.normalizeAngle(theta1 - theta2);
    }

    private double calculateDistanceBetweenPointAndLine(Point point, Line line){
        return (line.getA() * point.getX() + line.getB() * point.getY() + line.getC()) / Math.sqrt(Math.pow(line.getA(), 2) + Math.pow(line.getB(), 2));
    }

    private double getMaxV(long timeNow){
        if (!algorithmParams.isPointsWithTime()){
            return algorithmParams.getMaxLinearVelocity();
        }
        else{
            try {
                return Math.min(trajectory.getCurrentDesiredSpeed(timeNow), algorithmParams.getMaxLinearVelocity());
            } catch (TimeElapsedException e) {
                return algorithmParams.getMaxLinearVelocity();
            }
        }
    }
}
