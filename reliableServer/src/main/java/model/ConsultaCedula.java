package model;

import java.io.Serializable;

public class ConsultaCedula implements Serializable {
    public String cedula;
    public int mesaId;
    public String tipoConsulta; // "VALIDACION_CEDULA"
    
    public ConsultaCedula() {}
    
    public ConsultaCedula(String cedula, int mesaId) {
        this.cedula = cedula;
        this.mesaId = mesaId;
        this.tipoConsulta = "VALIDACION_CEDULA";
    }
}