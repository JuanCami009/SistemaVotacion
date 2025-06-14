import lugarVotacion.MesaPrx;
import lugarVotacion.Voto;
import com.zeroc.Ice.*;

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
        } catch (java.lang.Exception e) {

            System.err.println("Error en mesa de votacion: " + e.getMessage());
        }
    }
}