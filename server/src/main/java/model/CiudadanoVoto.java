package model;

import java.time.LocalDateTime;

public class CiudadanoVoto {
    private int id;
    private String documento;
    private LocalDateTime fechaVoto;

    public CiudadanoVoto() {}

    public CiudadanoVoto(int id, String documento, LocalDateTime fechaVoto) {
        this.id = id;
        this.documento = documento;
        this.fechaVoto = fechaVoto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public LocalDateTime getFechaVoto() {
        return fechaVoto;
    }

    public void setFechaVoto(LocalDateTime fechaVoto) {
        this.fechaVoto = fechaVoto;
    }
}
