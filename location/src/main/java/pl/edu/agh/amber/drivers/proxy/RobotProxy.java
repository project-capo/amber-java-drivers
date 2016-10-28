package pl.edu.agh.amber.drivers.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.amber.common.AmberClient;

import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.MapPoint;
import pl.edu.agh.amber.hokuyo.Scan;
import pl.edu.agh.amber.roboclaw.MotorsCurrentSpeed;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

import pl.edu.agh.capo.common.Vision;
import pl.edu.agh.capo.robot.IMeasureReader;
import pl.edu.agh.capo.robot.Measure;

public class RobotProxy implements IMeasureReader {
	private final static Logger logger = LoggerFactory.getLogger(RobotProxy.class);

	private AmberClient clientRoboClaw;
	private AmberClient clientHokuyo;

	private RoboclawProxy roboclawProxy;
	private HokuyoProxy hokuyoProxy;

	public RobotProxy(String hostname) {
		logger.info("RobotProxy");

		try {
			logger.info("before AmberClient");
			clientRoboClaw = new AmberClient(hostname, 26233);
			clientHokuyo = new AmberClient(hostname, 26233);
			
			logger.info("after AmberClient");

			roboclawProxy = new RoboclawProxy(clientRoboClaw, 0);
			hokuyoProxy = new HokuyoProxy(clientHokuyo, 0);

		} catch (IOException e) {
			logger.error("RobotProxy");
			logger.error("Unable to connect to robot: " + e);
		}
	}

	@Override
	public Measure read() {
		logger.debug("RobotProxy read() enter");
		
		MotorsCurrentSpeed mcs;
		Scan singleScan;
		List<MapPoint> points = null;
		List<Vision> visions = null;
		double rightVelocity = 0.0;
		double leftVelocity = 0.0;
		Boolean isError = true;
		
		logger.debug("RobotProxy read() -> Before read speed and scan");
		
		while (isError) {
			try {
				points = new ArrayList<MapPoint>();
				visions = new ArrayList<Vision>();

				mcs = roboclawProxy.getCurrentMotorsSpeed();
				mcs.waitAvailable(200);

				rightVelocity = (mcs.getFrontLeftSpeed() + mcs.getRearLeftSpeed()) / 2;
				leftVelocity = (mcs.getFrontRightSpeed() + mcs.getRearRightSpeed()) / 2;

				singleScan = hokuyoProxy.getSingleScan();

				if (singleScan != null) {
					points = singleScan.getPoints(200);

					if (points != null) {
						for (int i = 0; i < points.size(); i++)
							visions.add(new Vision(points.get(i).getAngle(), points.get(i).getDistance() / 1000));

						if (visions.size() != 0)
							isError = false;
					}
				}
			} catch (IOException e) {
				logger.error("RobotProxy " + "Error in sending a command: " + e);
			} catch (Exception e) {
				logger.error("RobotProxy " + e.getMessage());
			}
		}

		logger.debug("RobotProxy read() -> After read speed and scan");

		Date date = new Date();

		return new Measure(date, rightVelocity, leftVelocity, visions);
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public boolean isIdle() {
		return false;
	}
}
