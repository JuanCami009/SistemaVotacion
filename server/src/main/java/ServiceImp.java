import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Current;

import services.VoteStationImpl;
import model.Message;
import model.ReliableMessage;
import reliableMessage.ACKServicePrx;
import reliableMessage.RMDestination;

public class ServiceImp implements RMDestination {

    private Set<Integer> votosProcesados = ConcurrentHashMap.newKeySet();

    public int contadorExito=0;

    public int contadorDuplicado=0;

    @Override
    public void reciveMessage(ReliableMessage rmessage, ACKServicePrx prx, Current current) {
        Message msg = rmessage.getMessage();
        prx.ack(rmessage.getUuid());

        VoteStationImpl validador = new VoteStationImpl();
        int estado = validador.vote(msg.documento, msg.idCandidato, msg.mesaId, current);

        String result = "";

        if (estado == 0) {
            result = "Voto exitoso";
            contadorExito++;
        } else if (estado == 1) {
            result = "Mesa equivocada";
        } else if (estado == 2) {
            result = "Voto duplicado";
        } else if (estado == 3) {
            result = "Documento no encontrado";
        } else {
            result = "Error al procesar el voto";
        }

        System.out.println("Estado: " + estado + " - " + "Resultado: " + result);
    }

}
