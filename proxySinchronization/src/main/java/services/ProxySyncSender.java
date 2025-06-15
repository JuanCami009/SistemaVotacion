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
    public void setServerProxy(RMDestinationPrx destination, Current current) {
        // Este método ya no se usa porque el destino se configura directamente en ProxySynchronization
        System.out.println("setServerProxy llamado, pero el destino ya está configurado.");
    }


    // Asegurarse que el método listarCandidatos está implementado
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