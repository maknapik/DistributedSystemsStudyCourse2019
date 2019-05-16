package validationService;

import Bank.Credentials;
import accountDataService.Account;

public class ValidationService {

    public boolean validateCredentials(Account account, Credentials credentials) {

        return account.getPassword().equals(credentials.password);
    }
}
