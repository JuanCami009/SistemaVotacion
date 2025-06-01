import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroc.Ice.Current;

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

        if (!votosProcesados.add(msg.idVoto)) {
            System.out.println("Duplicado detectado para voto id: " + msg.idVoto);
            System.out.println("Contador Duplicados: "+contadorDuplicado++);
            
            prx.ack(rmessage.getUuid());
            return;
        }

        System.out.println("Procesando voto id: " + msg.idVoto);
        System.out.println("Cantidad de votos recibidos: "+ contadorExito++);
        prx.ack(rmessage.getUuid());
    }
}
