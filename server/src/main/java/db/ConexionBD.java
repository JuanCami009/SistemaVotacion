package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Properties;

public class ConexionBD {

	private Communicator com;
	private Connection conexion;

	public ConexionBD(Communicator com) {
		this.com = com;
	}

	public String conectarBaseDatos() {
    try {
        Properties prop = com.getProperties();
        Class.forName("org.postgresql.Driver");
        String cadenaconexionRemota = prop.getProperty("ConexionBD");
        String usuario = prop.getProperty("usuarioBD");
        String password = prop.getProperty("paswordBD");
    
        conexion = DriverManager.getConnection(cadenaconexionRemota, usuario, password);
        
        if (conexion == null) {
            return "No se pudo establecer la conexión";
        }
        return null;
    } catch (ClassNotFoundException e) {
        return "Error: Driver PostgreSQL no encontrado: " + e.getMessage();
    } catch (SQLException e) {
        return "Error SQL al conectar: " + e.getMessage();
    }
}

	public Connection getConnection() {
		return conexion;
	}

	public void cerrarConexion() {
		try {
			conexion.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}