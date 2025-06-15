package threads;

import model.Message;
import reliableMessage.RMSourcePrx;
import broker.BrokerServicePrx;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRetryJob extends Thread {

    private final Map<String, Message> mensajesPendientes = new ConcurrentHashMap<>();
    private RMSourcePrx proxyActual;
    private final BrokerServicePrx broker;
    private final String idLugar;
    private boolean activo = true;

    private static final int REINTENTOS_MAXIMOS = 3;
    private static final int TIEMPO_ENTRE_REINTENTOS_MS = 10000; // 10 segundos
    private int intentosFallidos = 0;

    public LocalRetryJob(BrokerServicePrx broker, String idLugar) {
        this.broker = broker;
        this.idLugar = idLugar;
    }

    public void agregarMensaje(Message msg) {
        String uuid = UUID.randomUUID().toString();
        mensajesPendientes.put(uuid, msg);
    }

    public void actualizarProxy(RMSourcePrx nuevoProxy) {
        this.proxyActual = nuevoProxy;
        this.intentosFallidos = 0; // reiniciar contador al cambiar de proxy
    }

    public void detener() {
        activo = false;
    }

    @Override
    public void run() {
        while (activo) {
            if (proxyActual == null) {
                System.out.println("[RetryJob] Proxy nulo, solicitando uno al Broker...");
                proxyActual = obtenerNuevoProxy();
                intentosFallidos = 0;
            }

            for (Map.Entry<String, Message> entry : mensajesPendientes.entrySet()) {
                try {
                    if (proxyActual != null) {
                        proxyActual.sendMessage(entry.getValue());
                        mensajesPendientes.remove(entry.getKey());
                        intentosFallidos = 0;
                        System.out.println("[RetryJob] Mensaje reenviado exitosamente: " + entry.getKey());
                    } else {
                        System.err.println("[RetryJob] No hay proxy para reenviar.");
                    }
                } catch (Exception e) {
                    intentosFallidos++;
                    System.err.println("[RetryJob] Error reenviando mensaje (intento " + intentosFallidos + "): " + e.getMessage());

                    if (intentosFallidos >= REINTENTOS_MAXIMOS) {
                        System.err.println("[RetryJob] Proxy falló " + intentosFallidos + " veces. Buscando uno nuevo...");
                        proxyActual = obtenerNuevoProxy();
                        intentosFallidos = 0;
                    } else {
                        System.out.println("[RetryJob] Se volverá a intentar con el mismo proxy en 10s.");
                    }
                }
            }

            try {
                Thread.sleep(TIEMPO_ENTRE_REINTENTOS_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private RMSourcePrx obtenerNuevoProxy() {
        try {
            String proxyString = broker.obtenerProxy(idLugar);
            if (proxyString == null) {
                System.err.println("[RetryJob] Broker no tiene proxies disponibles.");
                return null;
            }

            RMSourcePrx nuevoPrx = RMSourcePrx.checkedCast(
                    broker.ice_getCommunicator().stringToProxy(proxyString));
            System.out.println("[RetryJob] Nuevo proxy obtenido del broker.");
            return nuevoPrx;
        } catch (Exception e) {
            System.err.println("[RetryJob] Error al obtener nuevo proxy: " + e.getMessage());
            return null;
        }
    }
}
