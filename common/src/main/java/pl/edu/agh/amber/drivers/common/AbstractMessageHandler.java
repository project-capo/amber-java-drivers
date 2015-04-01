package pl.edu.agh.amber.drivers.common;

import com.google.protobuf.ExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.amber.common.proto.CommonProto;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMessageHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);

    private Map<Integer, Future> subscribersMap;
    private Lock subscribersLock;
    private AmberPipes amberPipes;
    private ExecutorService executorService;

    public AbstractMessageHandler(InputStream in, OutputStream out, ExecutorService executorService){
        this.amberPipes = new AmberPipes(this, in, out);
        this.executorService = executorService;
        this.subscribersMap = new HashMap<>();
        this.subscribersLock = new ReentrantLock();
    }

    public ExtensionRegistry getExtensionRegistry(){
        return amberPipes.getExtensionRegistry();
    }

    public void run(){
        amberPipes.run();
    }

    public boolean isAlive(){
        return amberPipes.isAlive();
    }

    public AmberPipes getPipes(){
        return amberPipes;
    }

    public abstract Runnable createSubscriberRunnable(int subscriberId, CommonProto.DriverHdr header, CommonProto.DriverMsg message);

    @Override
    public void handleSubscribeMessage(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Subscribe action for: {}", header.getClientIDsList());

        subscribersLock.lock();
        try {
            for (Integer subscriberId : header.getClientIDsList()){
                if (!subscribersMap.containsKey(subscriberId)){
                    logger.info("Starting subscriber {}", subscriberId);
                    Future subscriberTask = executorService.submit(createSubscriberRunnable(subscriberId, header, message));
                    subscribersMap.put(subscriberId, subscriberTask);
                    logger.info("Subscriber {} started", subscriberId);
                }
                else {
                    logger.warn("Subscriber {} already registered", subscriberId);
                }
            }
        } finally {
            subscribersLock.unlock();
        }
    }

    @Override
    public void handleUnsubscribeMessage(CommonProto.DriverHdr header, CommonProto.DriverMsg message) {
        logger.info("Unsubscribe action for: {}", header.getClientIDsList());
        for (int subscriberId : header.getClientIDsList()){
            removeSubscriber(subscriberId);
        }
    }

    @Override
    public void handleClientDiedMessage(Integer clientId) {
        logger.info("Client {} died action", clientId);
        removeSubscriber(clientId);
    }


    private void removeSubscriber(int subscriberId){
        subscribersLock.lock();
        try{
            if (subscribersMap.containsKey(subscriberId)){
                Future subscriberTask = subscribersMap.get(subscriberId);
                logger.info("Canceling subscriber {}", subscriberId);
                subscriberTask.cancel(true);
                subscribersMap.remove(subscriberId);
                logger.info("Subscriber {} unsubscribed", subscriberId);
            }
            else {
                logger.warn("Client {} is not registered as subscriber", subscriberId);
            }
        } finally {
            subscribersLock.unlock();
        }
    }
}
