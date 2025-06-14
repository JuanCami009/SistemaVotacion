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
        System.out.println("Voto recibido de documento " + voto.documento + " para candidato " + voto.idCandidato);

        Message msg = new Message();
        msg.message = "Documento: " + voto.documento + ", Candidato: " + voto.idCandidato+ ", Mesa: " + voto.mesaId+ ", Mesa ID: " + voto.mesaId;
        msg.documento = voto.documento;
        msg.idCandidato = voto.idCandidato;
        msg.mesaId = voto.mesaId;

        try {
            rm.sendMessage(msg);
            System.out.println("Voto reenviado a ReliableMessaging.");
        } catch (Exception e) {
            System.err.println("Error reenviando voto: " + e.getMessage());
        }
    }

}
