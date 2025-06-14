package model;

import java.io.Serializable;

public class Message implements Serializable {
    public String message;
    public String documento; // opcional, si necesitas identificar el documento del voto
    public int idCandidato; // opcional, si necesitas identificar el candidato del voto
}
