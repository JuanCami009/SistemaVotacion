module app{
    interface Service{
        void print();
    }

    interface VoteStation {
        int vote(string document, int candidateId, int mesaId);
    }

    interface QueryStation {
        string query(string document);
    }
    
}