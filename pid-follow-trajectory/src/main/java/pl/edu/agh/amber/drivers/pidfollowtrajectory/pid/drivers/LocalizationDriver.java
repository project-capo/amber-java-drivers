package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.model.CurrentPosition;
import pl.edu.agh.amber.drivers.pidfollowtrajectory.utils.MathUtils;
import pl.edu.agh.amber.location.LocationCurrent;
import pl.edu.agh.amber.location.LocationProxy;

import java.io.IOException;

/**
 * Created by kubicz10 on 3/6/15.
 */
public class LocalizationDriver {
    private static Logger logger = LoggerFactory.getLogger(LocalizationDriver.class);
    private static final int LOCATION_PROXY_WAIT_TIMEOUT = 100;

    private LocationProxy locationProxy;

    public LocalizationDriver(LocationProxy locationProxy){
        this.locationProxy = locationProxy;
    }

    public CurrentPosition getCurrentPosition(){
        try {
            logger.info("Getting location in {}", LOCATION_PROXY_WAIT_TIMEOUT);
            LocationCurrent locationCurrent = null;

            while(readingFailed(locationCurrent)){
                locationCurrent = locationProxy.getCurrentLocation();
                locationCurrent.waitAvailable(LOCATION_PROXY_WAIT_TIMEOUT);
            }

            return new CurrentPosition(locationCurrent.getX(), locationCurrent.getY(), MathUtils.normalizeAngle(locationCurrent.getAngle()), locationCurrent.getTimeStamp());
        } catch (IOException e) {
            logger.error("LocationProxy.getCurrentLocation() failed with IOException: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("LocationCurrent.waitAvailable({}) failed with Exception: {}", LOCATION_PROXY_WAIT_TIMEOUT, e.getMessage());
        }
        return null;
    }

    private boolean readingFailed(LocationCurrent locationCurrent) throws Exception {
        if (locationCurrent == null){
            return true;
        }
        else {
            if (locationCurrent.getTimeStamp() == 0
                    && isAlmostEqualTo(locationCurrent.getY(), 0.00)
                    && isAlmostEqualTo(locationCurrent.getY(), 0.00)
                    && isAlmostEqualTo(locationCurrent.getAngle(), 0.00)
                    && isAlmostEqualTo(locationCurrent.getP(), 0.00)){
                return true;
            }
        }
        return false;
    }

    private boolean isAlmostEqualTo(double a, double b){
        return a == b ? true : Math.abs(a - b) < 0.0000001;
    }
}
