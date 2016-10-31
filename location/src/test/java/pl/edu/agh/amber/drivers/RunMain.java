package pl.edu.agh.amber.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.amber.drivers.location.Location;
import pl.edu.agh.amber.drivers.location.LocationController;

public class RunMain {
	private static Location loc;
	private static Thread readThr;
	
	private final static Logger logger = LoggerFactory.getLogger(RunMain.class);

	private static Boolean semaphoreEnd;
	
	public static void main(String[] args) {
		try {
			
			semaphoreEnd = new Boolean(true);
			
			synchronized (semaphoreEnd) {
							
			loc = new Location("C://Users//Szymon//git//amber-java-drivers//location//maps//MazeRoboLabFullMap2.roson");
			readThr = new Thread(RunMain::readLocation);
			
			//readThr.start();	
			loc.start();
			
			semaphoreEnd.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.debug(e.getMessage());
		}
		catch(java.lang.IllegalMonitorStateException e)
		{
			logger.debug(e.getMessage());
		}
		
		logger.debug("This is the end");
	}
	
	private static void readLocation()
	{
		Boolean running = true;
		int loop = 50;
		
		while(running)
		{
			
			try {
				
				loc.getCurrentLocation();
				
				System.out.println("Location x: " + loc.getX() + " y: " + loc.getY() + " alfa: " +  loc.getAlfa() + " prop: " + loc.getProp() + " timestamp: " + loc.getTimestamp() );
				
				Thread.sleep(200);
				loop--;
				
				if(loop <= 0)
				{
					running = false;
					loc.Stop();
					
					synchronized (semaphoreEnd) {
					semaphoreEnd.notifyAll();
					}
					
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
