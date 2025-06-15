import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Current;

import db.ConexionBD;
import db.ManejadorDatos;
import model.Message;
import model.ReliableMessage;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestination;

public class ServiceImp implements RMDestination {

    private Set<Integer> votosProcesados = ConcurrentHashMap.newKeySet();
    public int contadorExito = 0;
    public int contadorDuplicado = 0;

    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        Message msg = rmessage.getMessage();

        if (!votosProcesados.add(msg.idVoto)) {
            System.out.println("Duplicado detectado para voto id: " + msg.idVoto);
            System.out.println("Contador Duplicados: " + contadorDuplicado++);
            
            prx.ack(rmessage.getUuid());
            return;
        }

        System.out.println("Procesando voto id: " + msg.idVoto);
        contadorExito++;
        System.out.println("Cantidad de votos recibidos: " + contadorExito);
        prx.ack(rmessage.getUuid());
    }

    @Override
    public String consultarValidezCiudadano(String cedula, int mesaId, Current current) {
        System.out.println("Consultando validez de ciudadano: " + cedula + " para mesa: " + mesaId);
        
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        String resultado = connBD.conectarBaseDatos();
        
        if (resultado != null) {
            System.err.println("Error conectando a base de datos: " + resultado);
            return "ERROR:Error de conexión a base de datos";
        }
        
        ManejadorDatos manejador = new ManejadorDatos(connBD.getConnection());
        
        try {
            // Validación 1: ¿Existe el ciudadano?
            if (!manejador.existeCiudadano(cedula)) {
                System.out.println("Ciudadano no encontrado: " + cedula);
                return "INVALIDA:Ciudadano no registrado en el sistema";
            }
            
            // Validación 2: ¿Es su mesa asignada?
            if (!manejador.esSuMesa(cedula, mesaId)) {
                System.out.println("Mesa incorrecta para ciudadano: " + cedula);
                String lugarCorrect = manejador.obtenerLugarVotacion(cedula);
                return "INVALIDA:No es su mesa asignada. " + lugarCorrect;
            }
            
            // Validación 3: ¿Ya ha votado?
            if (!manejador.registrarCiudadanoSiNoExiste(cedula)) {
                System.out.println("Ciudadano ya votó: " + cedula);
                return "INVALIDA:Ya has ejercido tu derecho al voto";
            }
            
            System.out.println("Ciudadano válido para votar: " + cedula);
            return "VALIDA:Ciudadano habilitado para votar";
            
        } catch (Exception e) {
            System.err.println("Error procesando validación de ciudadano: " + e.getMessage());
            e.printStackTrace();
            return "ERROR:Error interno del servidor - " + e.getMessage();
        } finally {
            connBD.cerrarConexion();
        }
    }
}