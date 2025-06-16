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
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        String conexionError = connBD.conectarBaseDatos();

        if (conexionError != null) {
            System.err.println("Error de conexión BD: " + conexionError);
            prx.ack(rmessage.getUuid());
            return;
        }

        try {
            ManejadorDatos manejador = new ManejadorDatos(connBD.getConnection());

            // Verificar si el voto ya existe en la BD
            if (manejador.existeVoto(msg.idVoto)) {
                System.out.println("Voto duplicado detectado con ID: " + msg.idVoto);
                contadorDuplicado++;
                prx.ack(rmessage.getUuid());
                return;
            }

            System.out.println("Procesando voto id: " + msg.idVoto);
            contadorExito++;
            System.out.println("Cantidad de votos recibidos: " + contadorExito);

            String[] partes = msg.message.split("\\|");
            if (partes.length != 2) {
                System.err.println("Formato inválido de mensaje: " + msg.message);
                prx.ack(rmessage.getUuid());
                return;
            }

            int idCandidato = Integer.parseInt(partes[0]);
            LocalDateTime fecha = LocalDateTime.parse(partes[1]);

            Voto voto = new Voto(msg.idVoto, idCandidato, fecha);
            manejador.registrarVoto(voto);

            System.out.println("Voto registrado en la base de datos: Candidato " + idCandidato + ", Fecha " + fecha);

        } catch (Exception e) {
            System.err.println("Error al procesar y registrar voto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            connBD.cerrarConexion();
            prx.ack(rmessage.getUuid());
        }
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
            int estado = manejador.validarCiudadano(cedula, mesaId);
            switch (estado) {
                case 3:
                    System.out.println("Ciudadano no encontrado: " + cedula);
                    return "INVALIDA:Ciudadano no registrado en el sistema";

                case 1:
                    System.out.println("Mesa incorrecta para ciudadano: " + cedula);
                    String lugarCorrecto = manejador.obtenerLugarVotacion(cedula);
                    return "INVALIDA:No es su mesa asignada. " + lugarCorrecto;

                case 0:
                    if (!manejador.registrarCiudadanoSiNoExiste(cedula)) {
                        System.out.println("Ciudadano ya votó: " + cedula);
                        return "INVALIDA:Ya has ejercido tu derecho al voto";
                    }
                    System.out.println("Ciudadano válido para votar: " + cedula);
                    return "VALIDA:Ciudadano habilitado para votar";

                default:
                    return "ERROR:Estado de validación desconocido";
            }

        } catch (Exception e) {
            System.err.println("Error procesando validación de ciudadano: " + e.getMessage());
            e.printStackTrace();
            return "ERROR:Error interno del servidor - " + e.getMessage();
        } finally {
            connBD.cerrarConexion();
        }
    }

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
