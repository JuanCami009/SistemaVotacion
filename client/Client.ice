module client {
    struct Voto {
        string documento;
        int idCandidato;
    };

    interface Mesa {
        void enviarVoto(Voto voto);
    }
}
