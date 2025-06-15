import com.zeroc.Ice.*;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestinationPrx;
import model.ReliableMessage;
import communication.Notification;
import services.ProxySyncReceiver;
import services.ProxySyncSender;
import threads.RMJob;
import broker.BrokerServicePrx;
import java.net.ServerSocket;
import java.io.IOException;

public class ProxySinchronization {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "proxy.config")) {

            // Generar ID único para este proxy
            String instanceId = System.getProperty("instance.id", "default");
            String proxyId = "proxySync-" + instanceId + "-" + System.currentTimeMillis();
            
            // Encontrar puerto disponible
            int puerto = findAvailablePort(10030);
            System.out.println("ProxySync '" + proxyId + "' usando puerto: " + puerto);

            // Notificación entre receptor y emisor (para reenviar y confirmar mensajes)
            Notification notification = new Notification();

            // Hilo de reintentos confiables (para mensajes hacia el servidor)
            RMJob job = new RMJob(notification);
            job.start();

            // Servicio receptor (ACK desde servidor)
            ProxySyncReceiver receiver = new ProxySyncReceiver(job);

            // ACTUALIZADO: Servicio emisor ahora implementa RMDestination
            // para recibir mensajes confiables del LugarVotacion
            ProxySyncSender sender = new ProxySyncSender(job, notification);

            // Adaptador local para recibir conexiones con puerto dinámico
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "ProxySyncAdapter", "tcp -h localhost -p " + puerto);

            // ACTUALIZADO: Registrar objetos en el adaptador
            // sender ahora se registra como RMDestination para recibir del LugarVotacion
            ObjectPrx senderPrx = adapter.add(sender, Util.stringToIdentity("RMDestination"));
            ObjectPrx ackPrx = adapter.add(receiver, Util.stringToIdentity("AckReceiver")); // ACKService

            // Configurar ACK para ReliableMessaging interno (hacia servidor)
            notification.setAckService(ACKServicePrx.checkedCast(ackPrx));

            // Activar el adaptador
            adapter.activate();

            // -----------------------------------------------
            // CONFIGURAR DESTINO (ServidorCentral)
            // -----------------------------------------------
            String serverHost = System.getProperty("server.host", "localhost");
            String serverPort = System.getProperty("server.port", "10012");
            
            ObjectPrx serverBase = communicator.stringToProxy("RMDestination:tcp -h " + serverHost + " -p " + serverPort);
            RMDestinationPrx server = RMDestinationPrx.checkedCast(serverBase);

            if (server == null) {
                System.err.println("No se pudo obtener el proxy del ServidorCentral en " + serverHost + ":" + serverPort);
                return;
            }

            notification.setService(server);
            System.out.println("ServidorCentral configurado como destino en ProxySynchronization.");

            // -----------------------------------------------
            // REGISTRO EN EL BROKER
            // -----------------------------------------------
            String brokerHost = System.getProperty("broker.host", "localhost");
            String brokerPort = System.getProperty("broker.port", "10020");
            
            ObjectPrx brokerBase = communicator.stringToProxy("Broker:tcp -h " + brokerHost + " -p " + brokerPort);
            BrokerServicePrx broker = BrokerServicePrx.checkedCast(brokerBase);

            if (broker == null) {
                System.err.println("No se pudo obtener el proxy del Broker en " + brokerHost + ":" + brokerPort);
                return;
            }

            // ACTUALIZADO: Registrar como RMDestination (no RMSource)
            String proxyString = communicator.proxyToString(senderPrx);
            broker.registrarProxy(proxyId, proxyString);
            System.out.println("ProxySync '" + proxyId + "' registrado en el broker como RMDestination.");

            // Esperar señal de apagado
            System.out.println("ProxySynchronization '" + proxyId + "' activo en puerto " + puerto + " y listo.");
            communicator.waitForShutdown();
        }
    }
    
    private static int findAvailablePort(int basePort) {
        for (int port = basePort; port <= basePort + 1000; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException e) {
                // Puerto ocupado, intentar el siguiente
            }
        }
        throw new RuntimeException("No se pudo encontrar un puerto disponible");
    }
}