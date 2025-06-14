package main.java.model;

import java.time.LocalDateTime;

public class Voto {
    private int id;
    private int candidatoId;
    private LocalDateTime fechaEmision;

    public Voto() {}

    public Voto(int id, int candidatoId, LocalDateTime fechaEmision) {
        super();
        this.id = id;
        this.candidatoId = candidatoId;
        this.fechaEmision = fechaEmision;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCandidatoId() {
        return candidatoId;
    }

    public void setCandidatoId(int candidatoId) {
        this.candidatoId = candidatoId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}