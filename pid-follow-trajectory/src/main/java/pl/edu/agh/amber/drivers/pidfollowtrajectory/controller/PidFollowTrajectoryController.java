package pl.edu.agh.amber.drivers.pidfollowtrajectory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.drivers.common.AbstractMessageHandler;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms.AlgorithmParams;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Point;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Segment;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Trajectory;
import pl.edu.agh.amber.pidfollowtrajectory.proto.PidFollowTrajectoryProto;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PidFollowTrajectoryController extends AbstractMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(PidFollowTrajectoryController.class);
    private PidFollowTrajectory pidFollowTrajectory;
    private ExecutorService executorService;
    private Future currentTask;

    public PidFollowTrajectoryController(InputStream in, OutputStream out, PidFollowTrajectory pidFollowTrajectory){
        super(in, out, Executors.newSingleThreadExecutor());
        this.pidFollowTrajectory = pidFollowTrajectory;
        this.executorService = Executors.newSingleThreadExecutor();
        this.currentTask = null;
        PidFollowTrajectoryProto.registerAllExtensions(getExtensionRegistry());
    }

    @Override
    public Runnable createSubscriberRunnable(int subscriberId, CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        //not implemented - there will be no option to subscribe just yet
        throw new NotImplementedException();
    }

    @Override
    public void handleDataMessage(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Received data message.");
        if (message.hasExtension(PidFollowTrajectoryProto.setTargets)){
            handleSetTargetsExtension(header, message);
            return;
        }
        if (message.hasExtension(PidFollowTrajectoryProto.getNextTargets)){
            handleGetNextTargetsExtension(header, message);
            return;
        }
        if (message.hasExtension(PidFollowTrajectoryProto.getVisitedTargets)){
            handleGetVisitedTargetsExtension(header, message);
            return;
        }
        if (message.hasExtension(PidFollowTrajectoryProto.stopExecution)){
            handleStopExecutionExtension(header, message);
            return;
        }
        if (message.hasExtension(PidFollowTrajectoryProto.startExecution)){
            handleStartExecutionExtension(header, message);
            return;
        }
        if (message.hasExtension(PidFollowTrajectoryProto.stepExecution)){
            handleStepExecutionExtension(header, message);
            return;
        }

        logger.warn("Unknown message, cannot handle. Header: {}, Message: {}", header, message);
    }

    private void handleStopExecutionExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Stoping execution now");
        if (currentTask != null){
            pidFollowTrajectory.stopExecution();
            currentTask.cancel(true);
            currentTask = null;
            logger.info("Execution stopped");
        }
        else {
            logger.warn("Execution was already stopped");
        }
    }

    private void handleGetVisitedTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        List<Segment> segments = new LinkedList<>();
        List<Point> points = new LinkedList<>();
        for (int i = 0; i < pidFollowTrajectory.getTrajectory().getCurrentSegmentId(); i++){
            segments.add(pidFollowTrajectory.getTrajectory().getSegment(i));
        }
        for (Segment segment : segments){
            points.add(segment.getStart());
            points.add(segment.getEnd());
        }
        if (points.size() > 0){
            points.remove(points.get(points.size() - 1));
        }

        logger.info("Sending visited targets to client: {}", points);

        CommonProto.DriverHdr responseHeader = CommonProto.DriverHdr.newBuilder()
                .addAllClientIDs(header.getClientIDsList())
                .build();
        CommonProto.DriverMsg responseMessage = null;
        CommonProto.DriverMsg.Builder responseMessageBuilder = CommonProto.DriverMsg.newBuilder()
                .setType(CommonProto.DriverMsg.MsgType.DATA)
                .setAckNum(message.getSynNum());

        responseMessageBuilder.setExtension(PidFollowTrajectoryProto.targets, pointsToTargets(points));
        responseMessage = responseMessageBuilder.build();

        getPipes().writeHeaderAndMessageToPipe(header, message);
    }

    private void handleGetNextTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        List<Segment> segments = new LinkedList<>();
        List<Point> points = new LinkedList<>();
        for (int i = pidFollowTrajectory.getTrajectory().getCurrentSegmentId(); i < pidFollowTrajectory.getTrajectory().getNumberOfSegments(); i++){
            segments.add(pidFollowTrajectory.getTrajectory().getSegment(i));
        }
        for (Segment segment : segments){
            points.add(segment.getStart());
            points.add(segment.getEnd());
        }
        if (points.size() > 0){
            points.remove(points.get(0));
        }

        logger.info("Sending next targets to client: {}", points);

        CommonProto.DriverHdr responseHeader = CommonProto.DriverHdr.newBuilder()
                .addAllClientIDs(header.getClientIDsList())
                .build();
        CommonProto.DriverMsg responseMessage = null;
        CommonProto.DriverMsg.Builder responseMessageBuilder = CommonProto.DriverMsg.newBuilder()
                .setType(CommonProto.DriverMsg.MsgType.DATA)
                .setAckNum(message.getSynNum());

        responseMessageBuilder.setExtension(PidFollowTrajectoryProto.targets, pointsToTargets(points));
        responseMessage = responseMessageBuilder.build();

        getPipes().writeHeaderAndMessageToPipe(header, message);
    }

    private void handleSetTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        if (message.hasExtension(PidFollowTrajectoryProto.targets) && message.hasExtension(PidFollowTrajectoryProto.configuration)){
            if (currentTask == null){
                PidFollowTrajectoryProto.Targets targets = message.getExtension(PidFollowTrajectoryProto.targets);
                PidFollowTrajectoryProto.Configuration configuration = message.getExtension(PidFollowTrajectoryProto.configuration);
                pidFollowTrajectory.setTrajectory(targetsToTrajectory(targets));
                pidFollowTrajectory.setAlgorithmParams(configurationToAlgorithmParams(configuration));
                logger.info("Setting drive with trajectory: {} and params: {}", pidFollowTrajectory.getTrajectory(), pidFollowTrajectory.getAlgorithmParams());
                pidFollowTrajectory.initializeController();
            }
            else {
                logger.warn("Drive was already started.");
            }
        }
        else{
            logger.error("Cannot start without targets or configuration");
        }
    }

    private void handleStartExecutionExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message){
        if (currentTask == null){
            currentTask = executorService.submit(pidFollowTrajectory.getControllerRunnable());
        }
        else {
            logger.warn("Drive was already started.");
        }
    }

    private void handleStepExecutionExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        if (currentTask == null){
            pidFollowTrajectory.runStep();
        }
        else {
            logger.warn("Drive was already started.");
        }
    }

    private AlgorithmParams configurationToAlgorithmParams(PidFollowTrajectoryProto.Configuration configuration){
        return new AlgorithmParams(configuration.getMaxLookahead(), configuration.getWeightAngular(), configuration.getWeighLinear(), configuration.getMaxCentricAcceleration(), configuration.getMaxLinearVelocity(), 0, false, configuration.getScale(), configuration.getLoopSleepTime());
    }

    private Trajectory targetsToTrajectory(PidFollowTrajectoryProto.Targets targets){
        List<Point> points = new LinkedList<>();
        for (int i = 0; i < targets.getLatitudesList().size(); i++){
            points.add(new Point(targets.getLatitudes(i), targets.getLongitudes(i), targets.getTimes(i)));
        }
        return new Trajectory(points);
    }

    private PidFollowTrajectoryProto.Targets pointsToTargets(List<Point> points){
        PidFollowTrajectoryProto.Targets.Builder targetsBuilder = PidFollowTrajectoryProto.Targets.newBuilder();

        List<Double> longitudes = new LinkedList<Double>();
        for (Point target : points) {
            longitudes.add(target.getY());
        }

        List<Double> latitudes = new LinkedList<Double>();
        for (Point target : points) {
            latitudes.add(target.getX());
        }

        List<Long> times = new LinkedList<Long>();
        for (Point target : points) {
            times.add(target.getTime());
        }

        targetsBuilder.addAllLongitudes(longitudes);
        targetsBuilder.addAllLatitudes(latitudes);
        targetsBuilder.addAllTimes(times);

        return targetsBuilder.build();
    }
}
