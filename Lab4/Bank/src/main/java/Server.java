import accountDataService.AccountDataService;
import accountDataService.AccountFactoryService;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import validationService.ValidationService;


public class Server {

    public static void main(String[] args)
    {
        AccountDataService accountDataService = new AccountDataService();
        ValidationService validationService = new ValidationService();

        Communicator communicator = null;

        try
        {
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            System.out.println("Type name and category:");
            String line = in.readLine();
            String[] parameters = line.split(" ");

            communicator = Util.initialize(args);

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1", "tcp -h localhost -p 10000:udp -h localhost -p 10000");

            AccountFactoryService accountFactoryService = new AccountFactoryService(accountDataService, validationService);

            adapter.add(accountFactoryService, new Identity(parameters[0], parameters[1]));

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