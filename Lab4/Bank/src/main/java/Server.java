
import accountService.AccountService;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;


public class Server {

    public static void main(String[] args)
    {
        Communicator communicator = null;

        try
        {
            communicator = Util.initialize(args);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1", "tcp -h localhost -p 10000:udp -h localhost -p 10000");

            AccountService clientConfigServant = new AccountService();

            adapter.add(clientConfigServant, new Identity("accountService", "accountService"));

            adapter.activate();

            System.out.println("Server start");

            communicator.waitForShutdown();

        }
        catch (Exception e)
        {
            System.err.println(e);
        }
        if (communicator != null)
        {
            try
            {
                communicator.destroy();
            }
            catch (Exception e)
            {
                System.err.println(e);
            }
        }
    }
}