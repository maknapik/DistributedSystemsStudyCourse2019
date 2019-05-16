package accountService;

import Bank.*;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.LocalException;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

public class Client {

    public static void main( String[] args )
    {
        AccountPrx accountPrx;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);

            ObjectPrx base = communicator.stringToProxy("accountFactory/accountFactory:tcp -h localhost -p 10000:udp -h localhost -p 10000");

            AccountFactoryPrx accountFactoryPrx  = AccountFactoryPrx.checkedCast(base);
            //if (obj == null) throw new Error("Invalid proxy");

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
//                        AccountType r = obj.getAccountType();
//                        System.out.println("KEY = " + r);
                    }
                    if (line.equals("obtain"))
                    {
//                        AccountType r = obj.getAccountType();
//                        System.out.println("KEY = " + r);
                    }
                    if (line.equals("create")) {
                        System.out.print("==> ");
                        System.out.flush();
                        line = in.readLine();
                        String[] params = line.split(" ");

                        AccountCreated accountCreated = accountFactoryPrx.createAccount(params[0], params[1], params[2], Double.parseDouble(params[3]));

                        AccountPrx accountPrx1 = accountFactoryPrx.obtainAccess(new Credentials(params[2], accountCreated.password));

                        System.out.println(accountPrx1.getAccountType());
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
