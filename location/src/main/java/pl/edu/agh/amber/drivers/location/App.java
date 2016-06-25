package pl.edu.agh.amber.drivers.location;

import java.io.Closeable;

public class App implements Closeable {
	LocationController locationController;
	
	public static void main(String[] args) {
		LocationController locationController = new LocationController(System.in, System.out);
		locationController.run();
	}

	@Override
	public void close() {
		locationController.stop();
	}
}
