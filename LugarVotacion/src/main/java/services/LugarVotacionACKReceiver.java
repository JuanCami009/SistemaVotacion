package services;

import com.zeroc.Ice.Current;
import reliableMessage.ACKService;
import threads.RMJob;

/**
 * Servicio que recibe ACKs del proxy para confirmar que recibió los mensajes
 */
public class LugarVotacionACKReceiver implements ACKService {

    private final RMJob job;

    public LugarVotacionACKReceiver(RMJob job) {
        this.job = job;
    }

    @Override
    public void ack(String messageId, Current current) {
        System.out.println("ACK recibido del proxy para mensaje: " + messageId);
        job.confirmMessage(messageId);
    }
}