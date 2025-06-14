package model;

public class Ciudadano {
    private int id;
    private String documento;
    private String nombre;
    private String apellido;
    private int mesaId;

    public Ciudadano() {}
    public Ciudadano(int id, String documento, String nombre, String apellido, int mesaId) {
        super();
        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.mesaId = mesaId;
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
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido() {
        return apellido;
    }
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    public int getMesaId() {
        return mesaId;
    }
    public void setMesaId(int mesaId) {
        this.mesaId = mesaId;
    }
    
}
