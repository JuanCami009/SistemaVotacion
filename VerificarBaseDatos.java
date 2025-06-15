import java.sql.*;

public class VerificarBaseDatos {
    public static void main(String[] args) {
        // Configuración de conexión
        String host = "localhost";
        String puerto = "5432";
        String usuario = "votaciones_user";  // Usuario correcto
        String password = "votaciones123";  // Cambiar por tu password real
        
        System.out.println("=== VERIFICACIÓN DE BASE DE DATOS ===");
        
        // 1. Verificar conexión a PostgreSQL (base postgres por defecto)
        System.out.println("1. Verificando conexión a PostgreSQL...");
        String urlPostgres = "jdbc:postgresql://" + host + ":" + puerto + "/postgres";
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(urlPostgres, usuario, password);
            System.out.println("✓ Conexión a PostgreSQL exitosa");
            
            // 2. Listar todas las bases de datos
            System.out.println("\n2. Bases de datos disponibles:");
            String sql = "SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            boolean encontrada = false;
            while (rs.next()) {
                String dbName = rs.getString("datname");
                System.out.println("   - " + dbName);
                if ("sistema_votaciones".equals(dbName)) {
                    encontrada = true;
                }
            }
            
            // 3. Resultado de la verificación
            System.out.println("\n3. Verificación de 'sistema_votacion':");
            if (encontrada) {
                System.out.println("✓ La base de datos 'sistema_votaciones' EXISTE");
                verificarTablas(host, puerto, usuario, password);
            } else {
                System.out.println("✗ La base de datos 'sistema_votaciones' NO EXISTE");
                System.out.println("\nPara crearla, ejecuta:");
                System.out.println("CREATE DATABASE sistema_votaciones;");
            }
            
            conn.close();
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver PostgreSQL no encontrado");
            System.err.println("Asegúrate de tener postgresql-XX.jar en el classpath");
        } catch (SQLException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            System.err.println("Verifica:");
            System.err.println("- PostgreSQL está corriendo");
            System.err.println("- Usuario y password son correctos");
            System.err.println("- Puerto " + puerto + " está disponible");
        }
    }
    
    private static void verificarTablas(String host, String puerto, String usuario, String password) {
        System.out.println("\n4. Verificando tablas en 'sistema_votaciones':");
        String urlSistema = "jdbc:postgresql://" + host + ":" + puerto + "/sistema_votaciones";
        
        try {
            Connection conn = DriverManager.getConnection(urlSistema, usuario, password);
            
            String[] tablasEsperadas = {"ciudadano", "ciudadano_voto", "mesa_votacion", 
                                       "puesto_votacion", "municipio", "departamento"};
            
            for (String tabla : tablasEsperadas) {
                if (existeTabla(conn, tabla)) {
                    System.out.println("   ✓ Tabla '" + tabla + "' existe");
                    contarRegistros(conn, tabla);
                } else {
                    System.out.println("   ✗ Tabla '" + tabla + "' NO existe");
                }
            }
            
            conn.close();
            
        } catch (SQLException e) {
            System.err.println("✗ Error conectando a sistema_votaciones: " + e.getMessage());
        }
    }
    
    private static boolean existeTabla(Connection conn, String nombreTabla) {
        try {
            String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreTabla);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
    
    private static void contarRegistros(Connection conn, String nombreTabla) {
        try {
            String sql = "SELECT COUNT(*) FROM " + nombreTabla;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("     → " + count + " registros");
            }
        } catch (SQLException e) {
            System.out.println("     → Error contando registros: " + e.getMessage());
        }
    }
}