import lugarVotacion.MesaPrx;
import lugarVotacion.Voto;
import lugarVotacion.ValidacionCedula;
import lugarVotacion.Candidato; // Nueva importación
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
        String instanceId = System.getProperty("instance.id", "1");
        int baseId = 1000 * Integer.parseInt(instanceId.replaceAll("\\D+", ""));
        int contadorVotos = 1;

        System.out.println("=== MODO INTERACTIVO UNIFICADO ===");

        while (true) {
            System.out.print("\nIngrese su número de cédula (o escriba 'salir' para terminar): ");
            String cedula = scanner.nextLine().trim();
            if (cedula.equalsIgnoreCase("salir")) break;

            if (cedula.isEmpty()) {
                System.out.println("⚠️ Cédula no puede estar vacía");
                continue;
            }

            try {
                // Validar cédula
                System.out.println("Mesa id prueba: "+ mesaId);
                ValidacionCedula validacion = cliente.consultarCedula(cedula, mesaId);

                if (!validacion.esValida) {
                    System.out.println("No autorizado para votar: " + validacion.mensaje);
                    continue;
                }

                // Mostrar candidatos
                System.out.println("\nValidación exitosa. Mostrando candidatos:");
                Candidato[] candidatos = cliente.obtenerCandidatos();

                for (Candidato c : candidatos) {
                    System.out.printf("%d. %s (%s)\n", c.id, c.nombre, c.partido);
                }

                System.out.print("Seleccione el ID del candidato: ");
                int idCandidato = scanner.nextInt();
                scanner.nextLine(); // limpiar buffer

                // Crear y enviar voto
                Voto voto = new Voto();
                voto.idVoto = baseId + contadorVotos++;
                voto.idCandidato = idCandidato;
                voto.fecha = java.time.LocalDateTime.now().toString();

                cliente.enviarVoto(voto);
                System.out.println("Voto enviado correctamente con ID: " + voto.idVoto);

            } catch (java.lang.Exception e) {
                System.err.println("Error en el proceso de votación: " + e.getMessage());
                scanner.nextLine(); // limpiar en caso de error con nextInt
            }
        }

        System.out.println("Gracias por usar la mesa de votación.");
    }

    
   
}