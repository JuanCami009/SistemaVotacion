package services;

import client.Mesa;
import client.Voto;
import com.zeroc.Ice.Current;
import model.Message;
import reliableMessage.RMSourcePrx;

public class ClientReceiver implements Mesa {

    private final RMSourcePrx rm;

    public ClientReceiver(RMSourcePrx rm) {
        this.rm = rm;
    }

    @Override
    public void enviarVoto(Voto voto, Current current) {
        System.out.println("Voto recibido: id " + voto.idVoto);

        // Convertir Voto a Message (ajusta según tu modelo)
        Message msg = new Message();
        msg.message = "Voto id: " + voto.idVoto; // o serializa más campos si quieres

        try {
            rm.sendMessage(msg);
            System.out.println("Voto reenviado a reliable message.");
        } catch (Exception e) {
            System.err.println("Error reenviando voto: " + e.getMessage());
        }
    }
}
