package main.java.db;

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
}
