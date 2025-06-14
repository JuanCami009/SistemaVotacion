module broker {

    interface BrokerService {
        void registrarProxy(string id, string proxyString);
        void registrarCliente(string idLugar);
        string obtenerProxy(string idLugar);
    }

}
