import com.zeroc.Ice.*;
import services.BrokerServiceImpl;

public class Broker {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "broker.config")) {
            ObjectAdapter adapter = communicator.createObjectAdapter("BrokerAdapter");
            adapter.add(new BrokerServiceImpl(), Util.stringToIdentity("Broker"));
            adapter.activate();
            System.out.println("Broker iniciado.");
            communicator.waitForShutdown();
        }
    }
}
