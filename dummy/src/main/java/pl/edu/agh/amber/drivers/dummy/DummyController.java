package pl.edu.agh.amber.drivers.dummy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;
import pl.edu.agh.amber.drivers.common.AbstractMessageHandler;
import pl.edu.agh.amber.dummy.proto.DummyProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DummyController extends AbstractMessageHandler {
    private final static Logger logger = LoggerFactory.getLogger(DummyController.class);
    private final static int EXECUTOR_THREADS = 10;
    private Dummy dummy;
    private final AtomicInteger atomicInteger;

    public DummyController(InputStream in, OutputStream out, Dummy dummy){
        super(in, out, Executors.newFixedThreadPool(EXECUTOR_THREADS));
        this.dummy = dummy;
        this.atomicInteger = new AtomicInteger(0);
        DummyProto.registerAllExtensions(getExtensionRegistry());
    }

    @Override
    public void handleDataMessage(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        if (message.hasExtension(DummyProto.enable)){
            handleEnableExtension(header, message);
        }
        else {
            if (message.hasExtension(DummyProto.message)){
                handleMessageExtension(header, message);
            }
            else {
                if (message.hasExtension(DummyProto.getStatus)){
                    handleStatusExtension(header, message);
                }
                else {
                    logger.warn("Unknown message, cannot handle. Header: {}, Message: {}", header, message);
                }
            }
        }
    }

    private void handleEnableExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message){
        boolean enable = message.getExtension(DummyProto.enable);
        logger.info("Setting enabled: {}", enable);
        dummy.setEnabled(enable);
    }

    private void handleMessageExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message){
        String m = message.getExtension(DummyProto.message);
        logger.info("Setting message: {}", m);
        dummy.setMessage(m);
    }

    private void handleStatusExtension(CommonProto.DriverHdr header, CommonProto.DriverMsg message){
        CommonProto.DriverHdr responseHeader = CommonProto.DriverHdr.newBuilder()
                                                    .addAllClientIDs(header.getClientIDsList())
                                                    .build();
        CommonProto.DriverMsg responseMessage = null;
        CommonProto.DriverMsg.Builder responseMessageBuilder = CommonProto.DriverMsg.newBuilder()
                                                    .setType(CommonProto.DriverMsg.MsgType.DATA)
                                                    .setAckNum(message.getSynNum());

        logger.info("Getting status");

        responseMessageBuilder.setExtension(DummyProto.enable, dummy.isEnabled());
        responseMessageBuilder.setExtension(DummyProto.message, dummy.getMessage());
        responseMessage = responseMessageBuilder.build();

        getPipes().writeHeaderAndMessageToPipe(responseHeader, responseMessage);
    }

    @Override
    public Runnable createSubscriberRunnable(final int subscriberId, final CommonProto.DriverHdr header, final CommonProto.DriverMsg message) {
        return new Runnable() {
            @Override
            public void run() {
                logger.info("Starting subscriber for: {}", subscriberId);
                boolean running = true;
                while (running){
                    if (Thread.interrupted()){
                        running = false;
                    }
                    else {
                        CommonProto.DriverHdr responseHeader = CommonProto.DriverHdr.newBuilder()
                                .addClientIDs(subscriberId)
                                .build();
                        CommonProto.DriverMsg responseMessage = null;
                        CommonProto.DriverMsg.Builder responseMessageBuilder = CommonProto.DriverMsg.newBuilder()
                                .setType(CommonProto.DriverMsg.MsgType.DATA)
                                .setAckNum(0);

                        responseMessageBuilder.setExtension(DummyProto.message, String.format("Response %d", atomicInteger.getAndIncrement()));
                        responseMessage = responseMessageBuilder.build();

                        logger.info("Sending response num {} to {}", atomicInteger.get(), subscriberId);
                        getPipes().writeHeaderAndMessageToPipe(responseHeader, responseMessage);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            running = false;
                            logger.info("Interrupted.");
                        }
                    }
                }
            }
        };
    }
}
