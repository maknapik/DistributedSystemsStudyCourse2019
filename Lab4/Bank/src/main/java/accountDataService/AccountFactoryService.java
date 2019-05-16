package accountDataService;

import Bank.*;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Identity;
import validationService.ValidationService;

import java.util.Random;

public class AccountFactoryService implements AccountFactory {

    private static final double PREMIUM_BALANCE_LEVEL = 8000;

    private AccountDataService accountDataService;
    private ValidationService validationService;

    public AccountFactoryService(AccountDataService accountDataService, ValidationService validationService) {
        this.accountDataService = accountDataService;
        this.validationService = validationService;
    }
    @Override
    public AccountCreated createAccount(String name, String surname, String pesel, double balance, Current current) {

        AccountType accountType = AccountType.STANDARD;

        if(balance >= PREMIUM_BALANCE_LEVEL) {
            accountType = AccountType.PREMIUM;
        }

        String password = generatePassword();

        Account account = new Account(pesel, password, accountType, name, surname, balance);

        AccountPrx accountPrx = AccountPrx.uncheckedCast(current.adapter.add(account, new Identity(pesel, accountType.toString())));

        account.setAccountPrx(accountPrx);

        accountDataService.addAccount(account);

        AccountCreated accountCreated = new AccountCreated();
        accountCreated.password = password;
        accountCreated.userAccountType = accountType;

        return accountCreated;
    }

    private String generatePassword() {
        String password = "";
        Random random = new Random();
        for (int i = 0 ; i < 5 ; i++) {
            password += (random.nextInt(9) + 1);
        }

        return password;
    }

    @Override
    public AccountPrx obtainAccess(Credentials userCredentials, Current current) throws InvalidCredentialsException {
        Account account = accountDataService.getAccountByPesel(userCredentials.pesel);

        if(validationService.validateCredentials(account, userCredentials)) {
            return account.getAccountPrx();
        } else {
            throw new InvalidCredentialsException();
        }
    }
}
