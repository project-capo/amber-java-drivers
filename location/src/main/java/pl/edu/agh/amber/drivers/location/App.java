package pl.edu.agh.amber.drivers.location;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App implements Closeable {
	LocationController locationController;
	
	private final static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		
		if(args.length != 2)
			logger.error("No configuration file");
			
		logger.debug(args[1]);
		LocationController locationController = new LocationController(System.in, System.out,args[1]);
		locationController.run();
	}

	@Override
	public void close() {
		locationController.stop();
	}
}
