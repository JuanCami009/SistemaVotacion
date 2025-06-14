package main.java.model;

public class Candidato {
    private int id;
    private String nombre;
    private String partidoPolitico;

    public Candidato() {}

    public Candidato(int id, String nombre, String partidoPolitico) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.partidoPolitico = partidoPolitico;
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

    public String getPartidoPolitico() {
        return partidoPolitico;
    }

    public void setPartidoPolitico(String partidoPolitico) {
        this.partidoPolitico = partidoPolitico;
    }


}
