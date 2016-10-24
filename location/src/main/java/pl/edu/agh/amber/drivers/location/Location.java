package pl.edu.agh.amber.drivers.location;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import pl.edu.agh.amber.drivers.proxy.RobotProxy;
import pl.edu.agh.capo.logic.Agent;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.logic.scheduler.Scheduler;
import pl.edu.agh.capo.logic.scheduler.divider.AbstractTimeDivider;
import pl.edu.agh.capo.logic.scheduler.divider.EnergyTimeDivider;
import pl.edu.agh.capo.maze.MazeMap;
import pl.edu.agh.capo.maze.helper.MazeHelper;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.IMeasureReader;

public class Location extends Thread {

	private final static Logger logger = LoggerFactory.getLogger(Location.class);

	private AbstractTimeDivider timeDivider;
	private Thread thrLocation;
	private Gson gson;
	private Scheduler scheduler;
	private IMeasureReader measureReader;

	private double x = 0;
	private double y = 0;
	private double prop = 0;
	private double alfa = 0;
	private long timestamp = 0;

	public synchronized double getX() {
		return x;
	}

	public synchronized void setX(double x) {
		this.x = x;
	}

	public synchronized double getY() {
		return y;
	}

	public synchronized void setY(double y) {
		this.y = y;
	}

	public synchronized double getAlfa() {
		return alfa;
	}

	public synchronized void setAlfa(double alfa) {
		this.alfa = alfa;
	}

	public synchronized double getProp() {
		return prop;
	}

	public synchronized void setProp(double prop) {
		this.prop = prop;
	}

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public synchronized void getCurrentLocation() {
		logger.debug("getCurrentLocation");
		
		try {

			Date today = new Date();
			Timestamp ts1 = new Timestamp(today.getTime());
			Agent bestAgent = timeDivider.getBest().getAgent();

			setX(bestAgent.getLocation().positionX);
			setY(bestAgent.getLocation().positionY);
			setAlfa(Math.toRadians(bestAgent.getLocation().alpha));
			setProp(0.0);
			setTimestamp(ts1.getTime());
		} catch (Exception e) {
			logger.error("Location error method getCurrentLocation()");
			logger.error(e.getMessage());
		}
	}

	public Location(String mapPath, String sIPAdress) {
		logger.debug("Location constructor");

		//mapPath = "C:\\Users\\Szymon\\git\\szia_capo\\MazeRoboLabEmptyMap2.roson";
		mapPath = ".//MazeRoboLabEmptyMap2.roson";
		sIPAdress = "127.0.0.1";

		try {
			measureReader = new RobotProxy(sIPAdress);
			gson = new Gson();
			MazeMap map;
			map = gson.fromJson(new FileReader(mapPath), MazeMap.class);
			List<Room> rooms = MazeHelper.buildRooms(map);
			timeDivider = new EnergyTimeDivider(rooms, CapoRobotConstants.FITNESS_ESTIMATOR_CLASS,
					CapoRobotConstants.INTERVAL_TIME);
			scheduler = new Scheduler(timeDivider, measureReader);

		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			logger.error("Location error");
			logger.error(e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			thrLocation = new Thread(scheduler::start);
			thrLocation.start();
			thrLocation.wait();
		} catch (InterruptedException e) {
			logger.error("Location error method run()");
			logger.error(e.getMessage());
		}
	}

	public void Stop() {
		thrLocation.destroy();
		this.interrupt();
	}
}
