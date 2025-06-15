package model;

public class MesaVotacion {
    private int id;
    private int consecutive;
    private int puestoId;

    public MesaVotacion() {}

    public MesaVotacion(int id, int consecutive, int puestoId) {
        super();
        this.id = id;
        this.consecutive = consecutive;
        this.puestoId = puestoId;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getConsecutive() {
        return consecutive;
    }
    public void setConsecutive(int consecutive) {
        this.consecutive = consecutive;
    }
    public int getPuestoId() {
        return puestoId;
    }
    public void setPuestoId(int puestoId) {
        this.puestoId = puestoId;
    }
    
}