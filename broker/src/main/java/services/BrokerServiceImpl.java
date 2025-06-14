package services;

import broker.BrokerService;
import com.zeroc.Ice.Current;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BrokerServiceImpl implements BrokerService {

    private final Map<String, String> proxies = new ConcurrentHashMap<>();
    private final List<String> proxyIds = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void registrarProxy(String id, String proxyString, Current current) {
        if (!proxies.containsKey(id)) {
            proxies.put(id, proxyString);
            synchronized (proxyIds) {
                proxyIds.add(id);
            }
            System.out.println("Registrado proxy: " + id);
        }
    }

    @Override
    public void registrarCliente(String idLugar, Current current) {
        System.out.println("Registrado cliente: " + idLugar);
    }

    @Override
    public String obtenerProxy(String idLugar, Current current) {
        synchronized (proxyIds) {
            if (proxyIds.isEmpty()) {
                return null;
            }
            int index = counter.getAndUpdate(i -> (i + 1) % proxyIds.size());
            String selectedId = proxyIds.get(index);
            return proxies.get(selectedId);
        }
    }
}
