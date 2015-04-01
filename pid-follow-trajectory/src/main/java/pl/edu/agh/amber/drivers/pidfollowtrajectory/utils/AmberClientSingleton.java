package pl.edu.agh.amber.drivers.pidfollowtrajectory.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.location.LocationProxy;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;

import java.io.IOException;

public class AmberClientSingleton {
    private static Logger logger = LoggerFactory.getLogger(AmberClientSingleton.class);

    private static AmberClient client;
    private static RoboclawProxy roboclawProxy;
    private static LocationProxy locationProxy;

    private AmberClientSingleton(){}

    public static synchronized AmberClient getAmberClientInstance() {
        if(client == null){
            try {
                client = new AmberClient("127.0.0.1", 26233);
            } catch (IOException e) {
                logger.error("Exception while creating AmberClient: {}", e);
            }
        }
        return client;
    }

    public static synchronized RoboclawProxy getAmberRoboclawProxyInstance() {
        if(roboclawProxy == null){
            roboclawProxy = new RoboclawProxy(getAmberClientInstance(), 0);
        }
        return roboclawProxy;
    }

    public static synchronized LocationProxy getAmberLocationProxyInstance() {
        if(locationProxy == null){
            locationProxy = new LocationProxy(getAmberClientInstance(), 0);
        }
        return locationProxy;
    }
}