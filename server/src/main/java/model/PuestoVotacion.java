package model;

public class PuestoVotacion {
    private int id;
    private String nombre;
    private int consecutive;
    private String direccion;
    private int municipioId;

    public PuestoVotacion() {}

    public PuestoVotacion(int id, String nombre, int consecutive, String direccion, int municipioId) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.consecutive = consecutive;
        this.direccion = direccion;
        this.municipioId = municipioId;
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
    public int getConsecutive() {
        return consecutive;
    }
    public void setConsecutive(int consecutive) {
        this.consecutive = consecutive;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public int getMunicipioId() {
        return municipioId;
    }
    public void setMunicipioId(int municipioId) {
        this.municipioId = municipioId;
    }

}
