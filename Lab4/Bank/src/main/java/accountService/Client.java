package accountService;

import Bank.AccountPrx;
import Bank.AccountType;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

public class Client {

    public static void main( String[] args )
    {
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);

            ObjectPrx base = communicator.stringToProxy("accountService/accountService:tcp -h localhost -p 10000:udp -h localhost -p 10000");

            AccountPrx obj = AccountPrx.checkedCast(base);
            if (obj == null) throw new Error("Invalid proxy");

            String line = null;
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            do
            {
                try
                {
                    System.out.print("==> ");
                    System.out.flush();
                    line = in.readLine();
                    if (line == null)
                    {
                        break;
                    }
                    if (line.equals("register"))
                    {
                        AccountType r = obj.getAccountType();
                        System.out.println("KEY = " + r);
                    }
                }
                catch (java.io.IOException ex)
                {
                    System.err.println(ex);
                }
            }
            while (!line.equals("x"));


        } catch (LocalException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
