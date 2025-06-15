import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.zeroc.Ice.Current;

import db.ConexionBD;
import db.ManejadorDatos;
import model.Message;
import model.ReliableMessage;
import model.Voto;
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

        try {
            // Parsear el contenido: idCandidato|fechaISO8601
            String[] partes = msg.message.split("\\|");
            if (partes.length != 2) {
                System.err.println("Formato inválido de mensaje: " + msg.message);
                prx.ack(rmessage.getUuid());
                return;
            }

            int idCandidato = Integer.parseInt(partes[0]);
            LocalDateTime fecha = LocalDateTime.parse(partes[1]);  // ✅ esta es la corrección clave

            Voto voto = new Voto(0, idCandidato, fecha);

            // Guardar en BD
            ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
            String conexionError = connBD.conectarBaseDatos();
            if (conexionError != null) {
                System.err.println("Error de conexión BD: " + conexionError);
                prx.ack(rmessage.getUuid());
                return;
            }

            ManejadorDatos manejador = new ManejadorDatos(connBD.getConnection());
            manejador.registrarVoto(voto);

            System.out.println("Voto registrado en la base de datos: Candidato " + idCandidato + ", Fecha " + fecha);
            connBD.cerrarConexion();

        } catch (Exception e) {
            System.err.println("Error al procesar y registrar voto: " + e.getMessage());
            e.printStackTrace();
        }

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
            
            //Validación 2: ¿Es su mesa asignada?
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

   // Modificar el método listarCandidatos existente
    @Override
    public String listarCandidatos(Current current) {
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        connBD.conectarBaseDatos();
        ManejadorDatos manejador = new ManejadorDatos(connBD.getConnection());
        try {
            return manejador.listarCandidatos()
                    .stream()
                    .map(c -> c.getId() + "|" + c.getNombre() + "|" + c.getPartidoPolitico())
                    .collect(Collectors.joining(";"));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR:" + e.getMessage();
        } finally {
            connBD.cerrarConexion();
        }
    }

}