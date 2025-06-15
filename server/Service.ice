module app {

    struct Candidato {
        int id;
        string nombre;
        string partidoPolitico;
    };

    sequence<Candidato> ListaCandidatos; // 👈 definición de secuencia

    interface VoteStation {
        int vote(string document, int candidateId, int mesaId);
        ListaCandidatos listar(); // 👈 usar la secuencia en el retorno
    }

    interface QueryStation {
        string query(string document);
    }
}
