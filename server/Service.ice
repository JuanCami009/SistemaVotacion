module app {
    interface Service {
        void print();
    }

    struct Candidato {
        int id;
        string nombre;
        string partidoPolitico;
    };

    sequence<Candidato> Candidatos;

    interface VoteStation {
        int vote(string document, int candidateId, int mesaId);
        Candidatos listar();
    }

    interface QueryStation {
        string query(string document);
    }
}
