import client.MesaPrx;
import client.Voto;
import com.zeroc.Ice.*;

import java.util.Scanner;

public class MesaVotacion {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("client:tcp -h localhost -p 10000");
            MesaPrx cliente = MesaPrx.checkedCast(base);

            if (cliente == null) {
                System.err.println("No se pudo obtener el proxy del cliente.");
                return;
            }

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Documento del votante (o 'salir'): ");
                String documento = scanner.nextLine();
                if (documento.equalsIgnoreCase("salir")) break;

                System.out.print("ID del candidato: ");
                int idCandidato = Integer.parseInt(scanner.nextLine());

                Voto voto = new Voto();
                voto.documento = documento;
                voto.idCandidato = idCandidato;

                cliente.enviarVoto(voto);
                System.out.println("Voto enviado correctamente al cliente.\n");
            }

            scanner.close();
        }
    }
}
