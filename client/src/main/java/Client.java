import client.Mesa;
import com.zeroc.Ice.*;
import services.ClientReceiver;

public class Client {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Creamos el ObjectAdapter y definimos el endpoint (host y puerto) directamente
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Client", "tcp -h localhost -p 10000");

            // Creamos el objeto que implementa la interfaz Mesa
            Mesa servant = new ClientReceiver();

            // Lo añadimos con la identidad "client"
            adapter.add(servant, Util.stringToIdentity("client"));

            // Activamos el adaptador para que empiece a escuchar
            adapter.activate();

            System.out.println("Cliente activo, esperando mensajes de mesa...");
            communicator.waitForShutdown();
        }
    }
}
