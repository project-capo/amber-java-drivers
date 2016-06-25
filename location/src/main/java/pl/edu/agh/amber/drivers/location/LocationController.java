package pl.edu.agh.amber.drivers.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;
import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.drivers.common.AbstractMessageHandler;
import pl.edu.agh.amber.location.proto.LocationProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationController extends AbstractMessageHandler {
	private final static Logger logger = LoggerFactory.getLogger(LocationController.class);
	private final static int EXECUTOR_THREADS = 10;
	private Location location;
	private final AtomicInteger atomicInteger;

	public LocationController(InputStream in, OutputStream out) {
		super(in, out, Executors.newFixedThreadPool(EXECUTOR_THREADS));
		this.atomicInteger = new AtomicInteger(0);
		LocationProto.registerAllExtensions(getExtensionRegistry());
		
		this.location =  new Location();
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
					handleCurrentLocationRequest(header, message);
				}
			}
		} catch (Exception e) {
			logger.error("handle DataMsg execption {}", e.getMessage());
		}
	}

	public void handleClientDiedMsg(int clientID) {
		logger.info("Client {} died", clientID);
	}

	private void handleCurrentLocationRequest(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
		logger.info("build current location msg");

		CommonProto.DriverHdr responseHeader = CommonProto.DriverHdr.newBuilder()
				.addAllClientIDs(header.getClientIDsList()).build();

		CommonProto.DriverMsg.Builder responseMessageBuilder = CommonProto.DriverMsg.newBuilder();
		responseMessageBuilder.setType(CommonProto.DriverMsg.MsgType.DATA);
		responseMessageBuilder.setSynNum(message.getSynNum());
		responseMessageBuilder.setAckNum(message.getAckNum());

		LocationProto.Location.Builder currentLocationBuilder = LocationProto.Location.newBuilder();

		// mutex ??
		if (location != null) {
			currentLocationBuilder.setX(location.getX());
			currentLocationBuilder.setY(location.getY());
			currentLocationBuilder.setAlfa(location.getAlfa());
			currentLocationBuilder.setP(location.getProp());
			currentLocationBuilder.setTimeStamp(location.getTimestamp());
		}
		// endmutex ??

		responseMessageBuilder.setExtension(LocationProto.currentLocation, currentLocationBuilder.build());

		getPipes().writeHeaderAndMessageToPipe(responseHeader, responseMessageBuilder.build());
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
