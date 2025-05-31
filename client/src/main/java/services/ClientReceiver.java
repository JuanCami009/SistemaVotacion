package services;

import client.Mesa;
import com.zeroc.Ice.Current;

public class ClientReceiver implements Mesa {

    @Override
    public void enviarACliente(String mensaje, Current current) {
        System.out.println("Mensaje recibido desde mesa: " + mensaje);
    }
}
