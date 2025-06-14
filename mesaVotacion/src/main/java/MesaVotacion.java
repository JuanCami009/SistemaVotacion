import client.MesaPrx;
import client.Voto;
import com.zeroc.Ice.*;

import java.util.Scanner;

public class MesaVotacion {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Por favor, proporciona el ID de la mesa como argumento.");
            System.exit(1);
        }

        int mesaId = Integer.parseInt(args[0]);
        System.out.println("📌 Esta mesa tiene ID: " + mesaId);

        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("client:tcp -h localhost -p 10000");
            MesaPrx cliente = MesaPrx.checkedCast(base);

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
                voto.mesaId = mesaId;

                cliente.enviarVoto(voto);
                System.out.println("✅ Voto enviado correctamente.\n");
            }

        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
