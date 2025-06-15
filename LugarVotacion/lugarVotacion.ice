module lugarVotacion {

    struct Voto {
        int idVoto;
        int idCandidato;
        string fecha;
    };

    struct ValidacionCedula {
        bool esValida;
        string mensaje;
        int mesaId;
    };

    // Nueva estructura local para candidatos
    struct Candidato {
        int id;
        string nombre;
        string partido;
    };

    sequence<Candidato> ListaCandidatos;

    interface Mesa {
        void enviarVoto(Voto voto);
        ValidacionCedula consultarCedula(string cedula, int mesaId);
        ListaCandidatos obtenerCandidatos();  // Usa la definición local
    }
}