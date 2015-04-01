package pl.edu.agh.amber.drivers.common;

import pl.edu.agh.amber.common.proto.CommonProto.DriverMsg;
import pl.edu.agh.amber.common.proto.CommonProto.DriverHdr;

public interface MessageHandler {
    public void handleDataMessage(DriverHdr header, DriverMsg message);
    public void handleSubscribeMessage(DriverHdr header, DriverMsg message);
    public void handleUnsubscribeMessage(DriverHdr header, DriverMsg message);
    public void handleClientDiedMessage(Integer clientId);
}
