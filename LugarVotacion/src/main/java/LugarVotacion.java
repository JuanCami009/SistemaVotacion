import client.Mesa;
import com.zeroc.Ice.*;
import model.Message;
import reliableMessage.RMDestinationPrx;
import reliableMessage.RMSourcePrx;
import services.LugarVotacionReceiver;

public class LugarVotacion {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Proxy hacia reliable message
            RMSourcePrx rm = RMSourcePrx.checkedCast(communicator.stringToProxy("Sender:tcp -h localhost -p 10010"));
            RMDestinationPrx dest = RMDestinationPrx.uncheckedCast(communicator.stringToProxy("Service:tcp -h localhost -p 10012"));
            rm.setServerProxy(dest);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("LugarVotacion", "tcp -h localhost -p 10000");
            // Pasamos rm al servicio que implementa Mesa para poder reenviar
            Mesa servant = new LugarVotacionReceiver(rm);

            adapter.add(servant, Util.stringToIdentity("lugarVotacion"));
            adapter.activate();

            System.out.println("Lugar de votacion activo, esperando mensajes de mesa...");
            communicator.waitForShutdown();
        }
    }
}
