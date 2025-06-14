package services;

import app.QueryStation;
import com.zeroc.Ice.Current;
import db.ConexionBD;
import db.ManejadorDatos;

import java.sql.Connection;

public class QueryStationImpl implements QueryStation {
    @Override
    public String query(String document, Current current) {
        ConexionBD con = new ConexionBD(current.adapter.getCommunicator());
        con.conectarBaseDatos();
        Connection conn = con.getConnection();

        try {
            ManejadorDatos manejador = new ManejadorDatos(conn);
            return manejador.obtenerLugarVotacion(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            con.cerrarConexion();
        }
    }
}
