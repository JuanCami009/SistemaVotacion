import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Server {
    public static void main(String[] args) {
        Communicator com = Util.initialize();
        ServiceImp imp = new ServiceImp();
        ObjectAdapter adapter = com.createObjectAdapterWithEndpoints("Server", "tcp -h localhost -p 10012");
        
        // CAMBIO CRÍTICO: Registrar como "RMDestination" en lugar de "Service"
        // porque ServiceImp implementa RMDestination, no app.Service
        adapter.add(imp, Util.stringToIdentity("RMDestination"));
        
        adapter.activate();
        System.out.println("Servidor Central iniciado y esperando mensajes confiables...");
        com.waitForShutdown();
    }
}