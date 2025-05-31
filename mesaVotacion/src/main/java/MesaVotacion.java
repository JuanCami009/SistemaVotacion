import client.MesaPrx;
import client.Voto;
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

            // Crear un voto y asignar valores
            for (int i = 0; i < 100; i++) {
                Voto voto = new Voto();
                voto.idVoto = i;  // Ejemplo de id, puedes asignar lo que necesites

                // Enviar el voto al cliente
                cliente.enviarVoto(voto);
                System.out.println("Voto enviado correctamente al cliente.");
            }
            
        }
    }
}
