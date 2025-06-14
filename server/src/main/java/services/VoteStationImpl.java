package services;

import app.Candidato;
import app.VoteStation;
import com.zeroc.Ice.Current;
import db.ConexionBD;
import db.ManejadorDatos;
import model.Voto;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class VoteStationImpl implements VoteStation {

    @Override
    public int vote(String document, int candidateId, int mesaId, Current current) {
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        connBD.conectarBaseDatos();
        Connection conn = connBD.getConnection();
        ManejadorDatos manejador = new ManejadorDatos(conn);

        try {
            if (!manejador.existeCiudadano(document)) {
                return 3;
            }
            if (!manejador.esSuMesa(document, mesaId)) {
                return 1;
            }
            if (!manejador.registrarCiudadanoSiNoExiste(document)) {
                return 2;
            }

            Voto voto = new Voto(0, candidateId, LocalDateTime.now());
            manejador.registrarVoto(voto);
            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            connBD.cerrarConexion();
        }
    }

    @Override
    public Candidato[] listar(Current current) {
        ConexionBD connBD = new ConexionBD(current.adapter.getCommunicator());
        connBD.conectarBaseDatos();
        Connection conn = connBD.getConnection();
        ManejadorDatos manejador = new ManejadorDatos(conn);

        try {
            List<model.Candidato> lista = manejador.listarCandidatos();
            Candidato[] resultado = new Candidato[lista.size()];
            for (int i = 0; i < lista.size(); i++) {
                model.Candidato c = lista.get(i);
                resultado[i] = new Candidato(c.getId(), c.getNombre(), c.getPartidoPolitico());
            }
            return resultado;

        } catch (Exception e) {
            e.printStackTrace();
            return new Candidato[0];

        } finally {
            connBD.cerrarConexion();
        }
    }
}
