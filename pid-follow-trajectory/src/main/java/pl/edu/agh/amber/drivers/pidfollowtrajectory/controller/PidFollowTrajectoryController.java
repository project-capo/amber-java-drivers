package pl.edu.agh.amber.drivers.pidfollowtrajectory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.drivers.common.AbstractMessageHandler;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms.AlgorithmParams;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.Point;
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

        logger.warn("Unknown message, cannot handle. Header: {}, Message: {}", header, message);
    }

    private void handleStopExecutionExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Stoping execution now");
        if (currentTask != null){
            currentTask.cancel(true);
            currentTask = null;
            logger.info("Execution stopped");
        }
        else {
            logger.warn("Execution was already stopped");
        }
    }

    private void handleGetVisitedTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Not implemented yet");
    }

    private void handleGetNextTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Not implemented yet");
    }

    private void handleSetTargetsExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        if (message.hasExtension(PidFollowTrajectoryProto.targets) && message.hasExtension(PidFollowTrajectoryProto.configuration)){
            if (currentTask == null){
                PidFollowTrajectoryProto.Targets targets = message.getExtension(PidFollowTrajectoryProto.targets);
                PidFollowTrajectoryProto.Configuration configuration = message.getExtension(PidFollowTrajectoryProto.configuration);
                pidFollowTrajectory.setTrajectory(targetsToTrajectory(targets));
                pidFollowTrajectory.setAlgorithmParams(configurationToAlgorithmParams(configuration));
                logger.info("Starting drive with trajectory: {} and params: {}", pidFollowTrajectory.getTrajectory(), pidFollowTrajectory.getAlgorithmParams());
                pidFollowTrajectory.initializeController();
                currentTask = executorService.submit(pidFollowTrajectory.getControllerRunnable());
            }
            else {
                logger.warn("Drive was already started.");
            }
        }
        else{
            logger.error("Cannot start without targets or configuration");
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
}
