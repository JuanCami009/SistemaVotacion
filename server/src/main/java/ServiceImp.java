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
        System.out.println("Cantidad de votos recibidos: "+ contadorExito++);
        prx.ack(rmessage.getUuid());

        VoteStationImpl validador = new VoteStationImpl();
        int result = validador.vote(msg.documento, msg.idCandidato, current);
        System.out.println("Resultado de validación y almacenamiento: " + result);
    }

}
