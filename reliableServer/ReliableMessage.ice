module reliableMessage {
    ["java:serializable:model.ReliableMessage"]
    sequence<byte> RMessage;
    ["java:serializable:model.Message"]
    sequence<byte> Message;
    ["java:serializable:model.ConsultaCedula"]
    sequence<byte> ConsultaCedula;

    interface ACKService{
        void ack(string messageId);
    }
    interface RMDestination{
        void reciveMessage(RMessage rmessage, ACKService* prx);
        string consultarValidezCiudadano(string cedula, int mesaId);
        string listarCandidatos();  // Cambiado de ListaCandidatos a string
    }
    interface RMSource{
        void setServerProxy(RMDestination* destination);
        void sendMessage(Message msg);
        string consultarValidezCiudadano(string cedula, int mesaId);
        string listarCandidatos();  // Cambiado de ListaCandidatos a string
    }
}