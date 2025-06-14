package main.java.model;

public class Departamento {
    private int id;
    private String nombre;

    public Departamento(){}

    public Departamento(int id, String nombre) {
        super();
        this.id = id;
        this.nombre = nombre;
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

    
}
