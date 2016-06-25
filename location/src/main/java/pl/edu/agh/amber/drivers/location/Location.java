package pl.edu.agh.amber.drivers.location;

import java.util.Random;

public class Location extends Thread {

	private Boolean work = true;

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

	private synchronized void setWork(Boolean work) {
		this.work = work;
	}

	public Location() {
	}

	@Override
	public void run() {
		while (work) {
			Random generator = new Random();
			setX(generator.nextDouble());
			setTimestamp(generator.nextLong());

			try {
				sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void Stop() {
		setWork(false);
		this.interrupt();
	}
}
