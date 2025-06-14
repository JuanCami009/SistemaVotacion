import app.QueryStationPrx;
import com.zeroc.Ice.*;

import java.util.Scanner;

public class ConsultarLugar {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args, "consulta.config")) {
            ObjectPrx base = communicator.stringToProxy("queryStation:tcp -h localhost -p 10012");
            QueryStationPrx proxy = QueryStationPrx.checkedCast(base);

            if (proxy == null) {
                System.out.println("No se pudo obtener el proxy del servidor.");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Ingrese su número de documento (o 'salir'): ");
                String documento = scanner.nextLine();
                if (documento.equalsIgnoreCase("salir")) break;

                String respuesta = proxy.query(documento);
                if (respuesta == null) {
                    System.out.println("El ciudadano no fue encontrado.");
                } else {
                    System.out.println(respuesta);
                }
                System.out.println();
            }
        }
    }
}
