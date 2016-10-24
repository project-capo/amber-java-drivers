package pl.edu.agh.amber.drivers;

import pl.edu.agh.amber.drivers.location.Location;
import pl.edu.agh.amber.drivers.location.LocationController;

public class RunMain {
	private static Location loc;
	private static Thread readThr;

	public static void main(String[] args) {
		try {
			loc = new Location("","");
			readThr = new Thread(RunMain::readLocation);
			
			//readThr.start();
			
			loc.start();
			loc.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static void readLocation()
	{
		while(true)
		{
			
			try {
				
				loc.getCurrentLocation();
				
				System.out.println("Location x: " + loc.getX() + " y: " + loc.getY() + " alfa: " +  loc.getAlfa() + " prop: " + loc.getProp() + " timestamp: " + loc.getTimestamp() );
				
				
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
