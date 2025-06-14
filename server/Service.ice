module app{
    interface Service{
        void print();
    }

    interface VoteStation {
        int vote(string document, int candidateId);
    }
    
}