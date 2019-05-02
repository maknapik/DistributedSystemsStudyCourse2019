package accountService;

import Bank.*;

public class AccountService implements Account {

    @Override
    public AccountType getAccountType(com.zeroc.Ice.Current current) {
        return AccountType.PREMIUM;
    }

    @Override
    public double getAccountBalance(com.zeroc.Ice.Current current) {
        return 0;
    }

    @Override
    public CreditEstimate applyForCredit(Currency creditCurrency, double balance, String period, com.zeroc.Ice.Current current) throws InvalidAccountTypeException {
        return null;
    }
}
