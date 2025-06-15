module lugarVotacion {

    struct Voto {
        int idVoto;
    };

    // Estructura para la respuesta de validación de cédula
    struct ValidacionCedula {
        bool esValida;
        string mensaje;
        int mesaId; // ID de la mesa asignada al ciudadano
    };

    interface Mesa {
        void enviarVoto(Voto voto);
        ValidacionCedula consultarCedula(string cedula, int mesaId);
    }
}