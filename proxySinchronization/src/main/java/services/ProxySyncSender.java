package services;

import com.zeroc.Ice.Current;
import communication.Notification;
import model.Message;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSource;
import threads.RMJob;

public class ProxySyncSender implements RMSource {

    private final RMJob job;
    private final Notification notification;

    public ProxySyncSender(RMJob job, Notification notification) {
        this.job = job;
        this.notification = notification;
    }

    @Override
    public void sendMessage(Message msg, Current current) {
        System.out.println("Mensaje recibido en ProxySync para reenviar al servidor central.");
        job.add(msg);
    }

    @Override
    public void setServerProxy(RMDestinationPrx destination, Current current) {
        // Este método ya no se usa porque el destino se configura directamente en ProxySynchronization
        System.out.println("setServerProxy llamado, pero el destino ya está configurado.");
    }
}