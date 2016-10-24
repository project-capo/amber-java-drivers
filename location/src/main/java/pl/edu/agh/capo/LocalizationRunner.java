package pl.edu.agh.capo;

import com.google.gson.Gson;

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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class LocalizationRunner {

    private AbstractTimeDivider timeDivider;

    public void start(IMeasureReader measureReader, String mapPath) throws FileNotFoundException {
        Gson gson = new Gson();
        MazeMap map = gson.fromJson(new FileReader(mapPath), MazeMap.class);
        List<Room> rooms = MazeHelper.buildRooms(map);
        timeDivider = new EnergyTimeDivider(rooms, CapoRobotConstants.FITNESS_ESTIMATOR_CLASS,
                CapoRobotConstants.INTERVAL_TIME);
        Scheduler scheduler = new Scheduler(timeDivider, measureReader);
        new Thread(scheduler::start).start();
    }

    public Agent getBestAgent() {
        return timeDivider.getBest().getAgent();
    }   
}
