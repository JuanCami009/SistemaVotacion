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
        System.out.println("Intentando conectar con: " + prop.getProperty("ConexionBD"));
        
        Class.forName("org.postgresql.Driver");
        System.out.println("Driver PostgreSQL cargado correctamente");
        
        String cadenaconexionRemota = prop.getProperty("ConexionBD");
        String usuario = prop.getProperty("usuarioBD");
        String password = prop.getProperty("paswordBD");
        
        System.out.println("Conectando a: " + cadenaconexionRemota + " con usuario: " + usuario);
        
        conexion = DriverManager.getConnection(cadenaconexionRemota, usuario, password);
        
        if (conexion == null) {
            return "No se pudo establecer la conexión";
        }
        System.out.println("Conexión establecida correctamente");
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