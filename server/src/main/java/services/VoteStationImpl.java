package services;

import app.VoteStation;
import com.zeroc.Ice.Current;
import db.ConexionBD;
import db.ManejadorDatos;
import model.Voto;

import java.sql.Connection;
import java.time.LocalDateTime;

public class VoteStationImpl implements VoteStation {

    @Override
    public int vote(String document, int candidateId, int mesaId, Current current) {
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        connBD.conectarBaseDatos();
        Connection conn = connBD.getConnection();
        ManejadorDatos manejador = new ManejadorDatos(conn);

        try {
            // 3. Verificar si existe el ciudadano en la BD
            if (!manejador.existeCiudadano(document)) {
                return 3; // No existe
            }

            if (!manejador.esSuMesa(document, mesaId)) {
                return 1;
            }

            // 1. Verificar si ya votó
            if (!manejador.registrarCiudadanoSiNoExiste(document)) {
                return 2; // Ya votó
            }

            // Registrar voto
            Voto voto = new Voto(0, candidateId, LocalDateTime.now());
            manejador.registrarVoto(voto);
            return 0; // Puede votar

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            connBD.cerrarConexion();
        }
    }
}
