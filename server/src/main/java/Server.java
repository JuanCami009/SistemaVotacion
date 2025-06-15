import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class Server {
    public static void main(String[] args) {
    System.out.println("Iniciando servidor...");
    System.out.println("Cargando configuración desde: server.config");
    
    Communicator com = Util.initialize(args, "server.config");
    System.out.println("Communicator inicializado");
    
    ServiceImp imp = new ServiceImp();
    ObjectAdapter adapter = com.createObjectAdapterWithEndpoints("Server", "tcp -h localhost -p 10012");
    
    System.out.println("Registrando servant como RMDestination...");
    adapter.add(imp, Util.stringToIdentity("RMDestination"));
    
    adapter.activate();
    System.out.println("Servidor Central iniciado y esperando mensajes confiables en tcp://localhost:10012");
    com.waitForShutdown();
}
}