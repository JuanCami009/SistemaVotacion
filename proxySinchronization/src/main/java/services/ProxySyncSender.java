package services;

import com.zeroc.Ice.Current;
import communication.Notification;
import model.Message;
import model.ReliableMessage;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMDestination;
import reliableMessage.ACKServicePrx;
import threads.RMJob;

/**
 * ProxySyncSender actualizado para enviar ACK inmediato al LugarVotacion
 * cuando recibe un mensaje, y luego manejarlo internamente hacia el servidor
 */
public class ProxySyncSender implements RMDestination {

    private final RMJob job;
    private final Notification notification;

    public ProxySyncSender(RMJob job, Notification notification) {
        this.job = job;
        this.notification = notification;
    }

    /**
     * NUEVO: Implementa RMDestination.reciveMessage para recibir mensajes confiables
     * del LugarVotacion y enviar ACK inmediato
     */
    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        System.out.println("Mensaje confiable recibido en ProxySync: " + rmessage.getUuid());
        
        // ENVIAR ACK INMEDIATO al LugarVotacion
        try {
            prx.ack(rmessage.getUuid());
            System.out.println("ACK enviado inmediatamente al LugarVotacion para mensaje: " + rmessage.getUuid());
        } catch (Exception e) {
            System.err.println("Error enviando ACK al LugarVotacion: " + e.getMessage());
        }
        
        // Ahora procesar el mensaje internamente
        Message msg = rmessage.getMessage();
        System.out.println("Agregando mensaje a cola interna del proxy para reenvío al servidor");
        job.add(msg);
    }

    @Override
    public String consultarValidezCiudadano(String cedula, int mesaId, Current current) {
        System.out.println("Consulta de validez de ciudadano recibida en ProxySync: " + cedula + " mesa: " + mesaId);
        
        try {
            // Obtener el servicio de destino desde notification
            RMDestinationPrx destination = notification.getService();
            if (destination != null) {
                // Reenviar la consulta directamente al servidor
                String resultado = destination.consultarValidezCiudadano(cedula, mesaId);
                System.out.println("Respuesta del servidor: " + resultado);
                return resultado;
            } else {
                System.err.println("No hay servicio de destino configurado");
                return "ERROR:No hay conexión con el servidor central";
            }
        } catch (Exception e) {
            System.err.println("Error consultando validez de ciudadano: " + e.getMessage());
            return "ERROR:Error de comunicación con el servidor - " + e.getMessage();
        }
    }

    @Override
    public String listarCandidatos(Current current) {
        System.out.println("Solicitud de lista de candidatos recibida en ProxySync.");
        try {
            RMDestinationPrx destination = notification.getService();
            if (destination != null) {
                return destination.listarCandidatos();
            } else {
                return "ERROR:Sin conexión al servidor.";
            }
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }
}