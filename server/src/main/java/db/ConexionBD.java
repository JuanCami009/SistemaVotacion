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

			conexion = DriverManager.getConnection(cadenaconexionRemota, prop.getProperty("usuarioBD"),
					prop.getProperty("paswordBD"));
			if (conexion == null) {
				return "imposible de conectar";
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
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