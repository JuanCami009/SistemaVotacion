package services;

import client.Mesa;
import client.Voto;
import com.zeroc.Ice.Current;
import model.Message;
import reliableMessage.RMSourcePrx;

public class LugarVotacionReceiver implements Mesa {

    private final RMSourcePrx rm;

    public LugarVotacionReceiver(RMSourcePrx rm) {
        this.rm = rm;
    }

    @Override
    public void enviarVoto(Voto voto, Current current) {
        System.out.println("Voto recibido: id " + voto.idVoto);

        // Convertir Voto a Message incluyendo el idVoto explícitamente
        Message msg = new Message();
        msg.idVoto = voto.idVoto;               // campo nuevo para identificación única
        msg.message = "Voto id: " + voto.idVoto; // campo descriptivo, opcional

        try {
            rm.sendMessage(msg);
            System.out.println("Voto reenviado a reliable message.");
        } catch (Exception e) {
            System.err.println("Error reenviando voto: " + e.getMessage());
        }
    }

}
