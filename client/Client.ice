module client {
    struct Voto {
        string documento;
        int idCandidato;
        int mesaId;
    };

    interface Mesa {
        void enviarVoto(Voto voto);
    }
}
