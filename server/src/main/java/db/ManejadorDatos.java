package db;

import model.Candidato;
import model.Voto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManejadorDatos {

    private Connection conexion;

    public ManejadorDatos(Connection conexion) {
        this.conexion = conexion;
    }


    public void registrarVoto(Voto voto) throws SQLException {
        String sql = "INSERT INTO voto (candidato, fecha_emision) VALUES (?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, voto.getCandidatoId());
            stmt.setTimestamp(2, Timestamp.valueOf(voto.getFechaEmision()));
            stmt.executeUpdate();
        }
    }

    public List<Candidato> listarCandidatos() throws SQLException {
        List<Candidato> candidatos = new ArrayList<>();
        String sql = "SELECT id, nombre, partido_politico FROM candidato";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Candidato c = new Candidato(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("partido_politico")
                );
                candidatos.add(c);
            }
        }
        return candidatos;
    }

    public boolean registrarCiudadanoSiNoExiste(String documento) throws SQLException {
        String checkSql = "SELECT 1 FROM ciudadano_voto WHERE documento = ?";
        try (PreparedStatement checkStmt = conexion.prepareStatement(checkSql)) {
            checkStmt.setString(1, documento);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // El ciudadano ya votó
                return false;
            }
        }

        String insertSql = "INSERT INTO ciudadano_voto (documento, fecha_voto) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conexion.prepareStatement(insertSql)) {
            insertStmt.setString(1, documento);
            insertStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            insertStmt.executeUpdate();
            return true;
        }
    }

    public boolean existeCiudadano(String documento) throws SQLException {
        String sql = "SELECT 1 FROM ciudadano WHERE documento = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, documento);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean esSuMesa(String documento, int mesaId) throws SQLException {
        String sql = "SELECT mesa_id FROM ciudadano WHERE documento = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, documento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int mesaAsignada = rs.getInt("mesa_id");
                return mesaAsignada == mesaId;
            }
            return false;
        }
    }

    public String obtenerLugarVotacion(String documento) throws SQLException {
        String sql = 
            "SELECT " +
            "    p.nombre AS puesto_nombre, " +
            "    p.direccion, " +
            "    m.nombre AS municipio_nombre, " +
            "    d.nombre AS departamento_nombre, " +
            "    mv.consecutive AS mesa " +
            "FROM ciudadano c " +
            "JOIN mesa_votacion mv ON c.mesa_id = mv.id " +
            "JOIN puesto_votacion p ON mv.puesto_id = p.id " +
            "JOIN municipio m ON p.municipio_id = m.id " +
            "JOIN departamento d ON m.departamento_id = d.id " +
            "WHERE c.documento = ?";

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, documento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Usted debe votar en " +
                        rs.getString("puesto_nombre") + " ubicado en " +
                        rs.getString("direccion") + ", " +
                        rs.getString("municipio_nombre") + ", " +
                        rs.getString("departamento_nombre") +
                        " en la mesa " + rs.getInt("mesa") + ".";
            } else {
                return null;
            }
        }
    }

    public int validarCiudadano(String documento, int mesaId) throws SQLException {
    String sql = "SELECT mesa_id FROM ciudadano WHERE documento = ?";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setString(1, documento);
        ResultSet rs = stmt.executeQuery();
        if (!rs.next()) {
            return 3; // No existe
        }
        int mesaAsignada = rs.getInt("mesa_id");
        if (mesaAsignada != mesaId) {
            return 1; // No es su mesa
        }
        return 0; // Es su mesa
    }
}




}