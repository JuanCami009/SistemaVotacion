package model;

public class Municipio {
    private int id;
    private String nombre;
    private int departamentoId;

    public Municipio() {}
    public Municipio(int id, String nombre, int departamentoId) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.departamentoId = departamentoId;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public int getDepartamentoId() {
        return departamentoId;
    }
    public void setDepartamentoId(int departamentoId) {
        this.departamentoId = departamentoId;
    }
    
}
