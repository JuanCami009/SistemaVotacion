module lugarVotacion {

    struct Voto {
        int idVoto;
    };

    interface Mesa {
        void enviarVoto(Voto voto);
    }
}
