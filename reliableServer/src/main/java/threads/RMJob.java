package threads;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import communication.Notification;
import model.Message;
import model.ReliableMessage;

public class RMJob extends Thread {

    public static final String PENDING = "Pending";
    public static final String SENDED = "Sended";

    private final Map<String, ReliableMessage> messagesPendig = new ConcurrentHashMap<>();
    private final Map<String, ReliableMessage> forConfirm = new ConcurrentHashMap<>();

    private long sequenceNumber = 0;
    private final Object lock = new Object();
    private boolean enable = true;
    private final Notification notification;

    private long tiempoInicio = -1;
    private int votosEsperados = 0;
    private int votosRecibidos = 0;

    public RMJob(Notification notification) {
        this.notification = notification;
    }

    public void setVotosEsperados(int n) {
        this.votosEsperados = n;
    }

    public void add(Message message) {
        synchronized (lock) {
            ReliableMessage mes = new ReliableMessage(UUID.randomUUID().toString(), sequenceNumber++, PENDING, message);
            messagesPendig.put(mes.getUuid(), mes);
        }
    }

    public void confirmMessage(String uid) {
        forConfirm.remove(uid);
        votosRecibidos++;

        if (votosEsperados > 0 && votosRecibidos == votosEsperados) {
            long tiempoFin = System.currentTimeMillis();
            long duracion = tiempoFin - tiempoInicio;
            System.out.println("TODOS los votos procesados.");
            System.out.println("Tiempo total (ms): " + duracion);
            System.out.println("Tiempo total (segundos): " + (duracion / 1000.0));
            setEnable(false); // Detener el hilo
        }
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void run() {
        while (enable) {
            for (Map.Entry<String, ReliableMessage> rm : messagesPendig.entrySet()) {
                try {
                    if (tiempoInicio == -1) {
                        tiempoInicio = System.currentTimeMillis(); // ⏱ iniciar cronómetro
                    }

                    System.out.println("Enviando mensaje...");
                    notification.sendMessage(rm.getValue());
                    messagesPendig.remove(rm.getKey());
                    forConfirm.put(rm.getKey(), rm.getValue());
                } catch (Exception e) {
                    System.err.println("Error enviando mensaje: " + e.getMessage());
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Hilo interrumpido.");
            }
        }
    }
}
