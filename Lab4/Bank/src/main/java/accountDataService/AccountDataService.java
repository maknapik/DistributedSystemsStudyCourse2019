package accountDataService;

import Bank.InvalidCredentialsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDataService {

    private List<Account> accounts = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.add(account);

    }

    public Account getAccountByPesel(String pesel) throws InvalidCredentialsException {
        Optional<Account> optionalAccount = accounts.stream().filter(account -> account.getPesel().equals(pesel)).findFirst();

        if(optionalAccount.isPresent()) {
            return optionalAccount.get();
        } else {
            throw new InvalidCredentialsException();
        }
    }
}
