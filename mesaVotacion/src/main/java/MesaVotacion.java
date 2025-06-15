import lugarVotacion.MesaPrx;
import lugarVotacion.Voto;
import lugarVotacion.ValidacionCedula;
import com.zeroc.Ice.*;
import java.util.Scanner;

public class MesaVotacion {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            
            // Permitir configurar el puerto del lugar de votación objetivo
            String lugarHost = System.getProperty("lugar.host", "localhost");
            String lugarPort = System.getProperty("lugar.port", "10000");
            String instanceId = System.getProperty("instance.id", "default");
            
            System.out.println("Mesa de votación (instancia: " + instanceId + ") conectando a " + lugarHost + ":" + lugarPort);
            
            // Conectarse al lugar de votación especificado
            ObjectPrx base = communicator.stringToProxy("LugarVotacion:tcp -h " + lugarHost + " -p " + lugarPort);
            MesaPrx cliente = MesaPrx.checkedCast(base);

            if (cliente == null) {
                System.err.println("No se pudo obtener el proxy del lugar de votación en " + lugarHost + ":" + lugarPort);
                return;
            }

            // Configuración para modo automático o interactivo
            boolean modoAutomatico = Boolean.parseBoolean(System.getProperty("modo.automatico", "true"));
            int mesaId = Integer.parseInt(System.getProperty("mesa.id", "1"));
            
            if (modoAutomatico) {
                // Modo automático para pruebas (como antes)
                ejecutarModoAutomatico(cliente, instanceId, mesaId);
            } else {
                // Modo interactivo para uso real
                ejecutarModoInteractivo(cliente, mesaId);
            }
            
        } catch (java.lang.Exception e) {
            System.err.println("Error en mesa de votacion: " + e.getMessage());
        }
    }
    
    private static void ejecutarModoAutomatico(MesaPrx cliente, String instanceId, int mesaId) {
        // Obtener configuración de votos a enviar
        int cantidadVotos = Integer.parseInt(System.getProperty("votos.cantidad", "25"));
        int delayMs = Integer.parseInt(System.getProperty("votos.delay", "1000"));
        
        System.out.println("Enviando " + cantidadVotos + " votos con delay de " + delayMs + "ms");

        // Enviar múltiples votos con delay para probar el rebalanceo
        for (int i = 1; i <= cantidadVotos; i++) {
            Voto voto = new Voto();
            int baseId = 1000 * Integer.parseInt(instanceId.replaceAll("\\D+", ""));
            voto.idVoto = baseId + i;  // Ejemplo: mesa1 → 1001, 1002... | mesa2 → 2001, 2002...
            
            try {
                cliente.enviarVoto(voto);
                System.out.println("Voto " + i + "/" + cantidadVotos + " enviado (ID: " + voto.idVoto + ")");
                
                // Delay entre votos
                if (i < cantidadVotos) {
                    Thread.sleep(delayMs);
                }
            } catch (java.lang.Exception e) {
                System.err.println("Error enviando voto " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("Mesa de votacion (instancia: " + instanceId + ") completo el envio de votos.");
    }
    
    private static void ejecutarModoInteractivo(MesaPrx cliente, int mesaId) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== MESA DE VOTACIÓN #" + mesaId + " ===");
        System.out.println("Modo interactivo activado");
        
        while (true) {
            System.out.println("\n1. Consultar cédula");
            System.out.println("2. Registrar voto");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            
            try {
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea
                
                switch (opcion) {
                    case 1:
                        consultarCedula(cliente, scanner, mesaId);
                        break;
                    case 2:
                        registrarVoto(cliente, scanner);
                        break;
                    case 3:
                        System.out.println("Cerrando mesa de votación...");
                        return;
                    default:
                        System.out.println("Opción no válida");
                }
            } catch (java.lang.Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }
    
    private static void consultarCedula(MesaPrx cliente, Scanner scanner, int mesaId) {
        System.out.print("Ingrese el número de cédula: ");
        String cedula = scanner.nextLine().trim();
        
        if (cedula.isEmpty()) {
            System.out.println("Cédula no puede estar vacía");
            return;
        }
        
        try {
            System.out.println("Consultando cédula: " + cedula + "...");
            ValidacionCedula resultado = cliente.consultarCedula(cedula, mesaId);
            
            System.out.println("\n=== RESULTADO DE VALIDACIÓN ===");
            System.out.println("Cédula: " + cedula);
            System.out.println("Mesa: " + mesaId);
            System.out.println("Estado: " + (resultado.esValida ? "VÁLIDA" : "INVÁLIDA"));
            System.out.println("Mensaje: " + resultado.mensaje);
            System.out.println("================================");
            
        } catch (java.lang.Exception e) {
            System.err.println("Error consultando cédula: " + e.getMessage());
        }
    }
    
    private static void registrarVoto(MesaPrx cliente, Scanner scanner) {
        System.out.print("Ingrese el ID del voto: ");
        
        try {
            int idVoto = scanner.nextInt();
            scanner.nextLine(); // Consumir salto de línea
            
            Voto voto = new Voto();
            voto.idVoto = idVoto;
            
            cliente.enviarVoto(voto);
            System.out.println("Voto registrado exitosamente (ID: " + idVoto + ")");
            
        } catch (java.lang.Exception e) {
            System.err.println("Error registrando voto: " + e.getMessage());
        }
    }
}