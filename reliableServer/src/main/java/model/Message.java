package model;

import java.io.Serializable;

public class Message implements Serializable {
    public String message;
    public int idVoto; // nuevo campo explícito para identificar el voto
}
