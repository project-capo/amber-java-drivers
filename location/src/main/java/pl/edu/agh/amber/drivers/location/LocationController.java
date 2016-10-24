package pl.edu.agh.amber.drivers.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg.MsgType;
import pl.edu.agh.amber.drivers.common.AbstractMessageHandler;

import pl.edu.agh.amber.location.proto.LocationProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class LocationController extends AbstractMessageHandler {
	private final static Logger logger = LoggerFactory.getLogger(LocationController.class);
	private final static int EXECUTOR_THREADS = 1;
	private Location location;

	public LocationController(InputStream in, OutputStream out) {
		super(in, out, Executors.newFixedThreadPool(EXECUTOR_THREADS));
		logger.info("LocationController");
		LocationProto.registerAllExtensions(getExtensionRegistry());

		this.location = new Location("","");
		location.start();
	}

	@Override
	public void handleDataMessage(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {

		try {
			int clientId = header.getClientIDsCount() > 0 ? header.getClientIDs(0) : 0;

			// DataRequest
			if (message.hasExtension(LocationProto.getLocation)) {
				if (!message.hasSynNum()) {
					logger.warn("Got CurrentLocationRequest, but syn num not set. Ignoring.");
					return;
				}

				if (message.getExtension(LocationProto.getLocation)) {
					handleCurrentLocationRequest(clientId, message.getSynNum());
				}
			}
		} catch (Exception e) {
			logger.error("handle DataMsg execption {}", e.getMessage());
		}
	}

	public void handleClientDiedMsg(int clientID) {
		logger.info("Client {} died", clientID);
	}

	private void handleCurrentLocationRequest(int clientId, int synNum) {
		logger.info("build current location msg");

		DriverHdr.Builder headerBuilder = DriverHdr.newBuilder();
		headerBuilder.addClientIDs(clientId);

		DriverMsg.Builder messageBuilder = DriverMsg.newBuilder();
		messageBuilder.setType(MsgType.DATA);

		LocationProto.Location.Builder currentLocationBuilder = LocationProto.Location.newBuilder();

		if (location != null) {
			location.getCurrentLocation();
			currentLocationBuilder.setX(location.getX());
			currentLocationBuilder.setY(location.getY());
			currentLocationBuilder.setAlfa(location.getAlfa());
			currentLocationBuilder.setP(location.getProp());
			currentLocationBuilder.setTimeStamp(location.getTimestamp());
		}

		messageBuilder.setExtension(LocationProto.currentLocation, currentLocationBuilder.build());
		messageBuilder.setAckNum(synNum);

		DriverHdr header = headerBuilder.build();
		DriverMsg message =  messageBuilder.build();
		
		getPipes().writeHeaderAndMessageToPipe(header,message);
	}

	@Override
	public void run() {
		super.run();

		if (!isAlive()) {
			stop();
			System.exit(1);
		}
	}

	@Override
	public Runnable createSubscriberRunnable(int subscriberId, DriverHdr header, DriverMsg message) {
		// TODO Auto-generated method stub
		return null;
	}

	public void stop() {
		location.Stop();
	}
}
