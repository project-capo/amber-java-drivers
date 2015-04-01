package pl.edu.agh.amber.drivers.common;

import com.google.protobuf.ExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;

public class AmberPipes {
    private static final Logger logger = LoggerFactory.getLogger(AmberPipes.class);
    private static final int PACKET_SIZE_LEN = 2;

    private MessageHandler messageHandler;
    private InputStream in;
    private OutputStream out;
    private boolean alive;
    private Lock writeLock;
    private ExtensionRegistry extensionRegistry;

    public AmberPipes(MessageHandler messageHandler, InputStream in, OutputStream out){
        this.messageHandler = messageHandler;
        this.in = in;
        this.out = out;
        this.alive = true;
        this.writeLock = new ReentrantLock();
        this.extensionRegistry = ExtensionRegistry.newInstance();
    }

    public ExtensionRegistry getExtensionRegistry(){
        return extensionRegistry;
    }

    public void run(){
        logger.info("Pipes thread started");
        while (alive) {
            DriverHdr header = null;
            try {
                header = readHeaderFromPipe();
                DriverMsg message = readMessageFromPipe();
                handleHeaderAndMessage(header, message);
            } catch (IOException e) {
                logger.warn("Error occured while reading from mediator pipe: {}", e.getMessage());
                alive = false;
            }

        }
    }

    public void writeHeaderAndMessageToPipe(DriverHdr header, DriverMsg message){
        logger.debug("Writing header and message to pipe: HEADER:{}, MESSAGE:{}", header, message);

        writeLock.lock();
        try {
            writeToPipe(header.toByteArray());
            writeToPipe(message.toByteArray());
            out.flush();
        } catch (IOException e) {
            logger.warn("Error occured while writing to mediator pipe: {}", e.getMessage());
        }
        finally {
            writeLock.unlock();
        }
    }

    public boolean isAlive(){
        return alive;
    }

    private DriverHdr readHeaderFromPipe() throws IOException {
        return DriverHdr.parseFrom(readFromPipe());
    }

    private DriverMsg readMessageFromPipe() throws IOException {
        return DriverMsg.parseFrom(readFromPipe(), extensionRegistry);
    }

    /*
        It is not just protobuff format, erlang appends the packet size to every protobuff packet.
     */
    private byte[] readFromPipe() throws IOException {
        byte[] tmp = new byte[PACKET_SIZE_LEN];
        in.read(tmp, 0, PACKET_SIZE_LEN);
        int len = decodeInt(tmp);

        tmp = new byte[len];
        in.read(tmp, 0, len);

        return tmp;
    }

    private void writeToPipe(byte[] arr) throws IOException {
        if (PACKET_SIZE_LEN != 2){
            logger.error("This need to be reimplmented.");
        }
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(arr.length);

        byte[] lenBytes = b.array();

        out.write(lenBytes, 2, 2);
        out.write(arr);
    }

    private int decodeInt(byte[] arr){
        if (PACKET_SIZE_LEN != 2){
            logger.error("This need to be reimplmented.");
        }
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.BIG_ENDIAN);
        b.put((byte)0x00);
        b.put((byte)0x00);
        b.put(arr[0]);
        b.put(arr[1]);
        b.flip();
        return b.getInt();
    }

    private void handleHeaderAndMessage(DriverHdr header, DriverMsg message){
        switch (message.getType()){
            case DATA:
                handleDataMessage(header, message);
                break;
            case SUBSCRIBE:
                handleSubscribeMessage(header, message);
                break;
            case UNSUBSCRIBE:
                handleUnsubscribeMessage(header, message);
                break;
            case CLIENT_DIED:
                handleClientDiedMessage(header, message);
                break;
            case PING:
                handlePingMessage(header, message);
                break;
            default:
                logger.warn("Received unknown type of message, ignoring. Message: {}, Header: {}", message.toString(), header.toString());
        }
    }

    private void handleDataMessage(DriverHdr header, DriverMsg message){
        logger.debug("Received DATA message");
        messageHandler.handleDataMessage(header, message);
    }

    private void handleSubscribeMessage(DriverHdr header, DriverMsg message){
        logger.debug("Received SUBSCRIBE message");
        messageHandler.handleSubscribeMessage(header, message);
    }

    private void handleUnsubscribeMessage(DriverHdr header, DriverMsg message){
        logger.debug("Received UNSUBSCRIBE message");
        messageHandler.handleUnsubscribeMessage(header, message);
    }

    private void handleClientDiedMessage(DriverHdr header, DriverMsg message){
        logger.debug("Received CLIENT_DIED message");
        if (header.getClientIDsList().isEmpty()){
            logger.warn("CLIENT_DIED: clientID not set, ignoring");
        }
        else{
            messageHandler.handleClientDiedMessage(header.getClientIDsList().get(0));
        }
    }

    private void handlePingMessage(DriverHdr header, DriverMsg message){
        logger.debug("Received PING message");
        if (message.hasSynNum()){
            DriverMsg pongMessage = DriverMsg.newBuilder()
                    .setType(DriverMsg.MsgType.PONG)
                    .setAckNum(message.getSynNum())
                    .build();

            DriverHdr pongHeader = DriverHdr.newBuilder()
                    .addAllClientIDs(header.getClientIDsList())
                    .build();

            logger.debug("Sending PONG message");
            writeHeaderAndMessageToPipe(pongHeader, pongMessage);
        }
        else{
            logger.warn("PING: synNum is not set, ignoring.");
        }
    }
}
