import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import services.VoteStationImpl;
import app.VoteStation;
import services.QueryStationImpl;
import app.QueryStation;

public class Server {

    public static void main(String[] args) {
        Communicator com = Util.initialize(args, "server.config"); // << aquí está el cambio
        ServiceImp imp = new ServiceImp();
        VoteStation voteService = new VoteStationImpl();
        QueryStation queryService = new QueryStationImpl();


        ObjectAdapter adapter = com.createObjectAdapter("Server");

        adapter.add(voteService, Util.stringToIdentity("voteStation"));
        adapter.add(imp, Util.stringToIdentity("Service"));
        adapter.add(queryService, Util.stringToIdentity("queryStation"));
        adapter.activate();
        com.waitForShutdown();
    }
}
