import lugarVotacion.Mesa;
import com.zeroc.Ice.*;
import model.Message;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSourcePrx;
import services.LugarVotacionReceiver;
import broker.BrokerServicePrx;
import java.net.ServerSocket;
import java.io.IOException;

public class LugarVotacion {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Obtener proxy del Broker
            ObjectPrx brokerBase = communicator.stringToProxy("Broker:tcp -h localhost -p 10020");
            BrokerServicePrx broker = BrokerServicePrx.checkedCast(brokerBase);

            if (broker == null) {
                System.err.println("No se pudo obtener el proxy del Broker.");
                return;
            }

            // Generar ID único para este lugar de votación
            String instanceId = System.getProperty("instance.id", "default");
            String idLugar = "lugarVotacion-" + instanceId + "-" + System.currentTimeMillis();
            
            // Encontrar puerto disponible
            int puerto = findAvailablePort(10000);
            System.out.println("Usando puerto: " + puerto + " para instancia: " + idLugar);

            // Registrar este lugar de votación
            broker.registrarCliente(idLugar);

            // Crear adaptador local con puerto encontrado
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                "LugarVotacion", "tcp -h localhost -p " + puerto);

            // Crear servicio receptor pasando el broker para rebalanceo
            Mesa servant = new LugarVotacionReceiver(broker, idLugar, communicator);
            adapter.add(servant, Util.stringToIdentity("LugarVotacion"));

            adapter.activate();

            System.out.println("Lugar de votación '" + idLugar + "' activo en puerto " + puerto);
            communicator.waitForShutdown();
        }
    }
    
    /**
     * Encuentra un puerto disponible comenzando desde el puerto base
     */
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