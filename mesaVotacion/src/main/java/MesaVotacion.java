import client.MesaPrx;
import com.zeroc.Ice.*;

public class MesaVotacion {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Conectarse al cliente que está escuchando en el puerto 10000
            ObjectPrx base = communicator.stringToProxy("client:tcp -h localhost -p 10000");
            MesaPrx cliente = MesaPrx.checkedCast(base);

            if (cliente == null) {
                System.err.println("No se pudo obtener el proxy del cliente.");
                return;
            }

            cliente.enviarACliente("Hola desde mesa de votación");
            System.out.println("Mensaje enviado correctamente al cliente.");
        }
    }
}
