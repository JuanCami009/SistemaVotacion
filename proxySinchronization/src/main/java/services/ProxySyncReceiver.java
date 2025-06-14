package services;

import com.zeroc.Ice.Current;
import model.ReliableMessage;
import reliableMessage.ACKService;
import threads.RMJob;

public class ProxySyncReceiver implements ACKService {

    private final RMJob job;

    public ProxySyncReceiver(RMJob job) {
        this.job = job;
    }

    @Override
    public void ack(String messageId, Current current) {
        System.out.println("ACK recibido en ProxySync para mensaje: " + messageId);
        job.confirmMessage(messageId);
    }
}
