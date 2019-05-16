#ifndef BANK_ICE
#define BANK_ICE

module Bank {

  enum Currency { PLN, GBP, USD, CHF, EUR };
  enum AccountType { STANDARD, PREMIUM };

  struct AccountCreated { string password; AccountType userAccountType; };
  struct Credentials { string pesel; string password; };
  struct CreditEstimate { double nativeCurrency; double foreignCurrency; };

  interface Account {
    AccountType getAccountType();
    double getAccountBalance();
    CreditEstimate applyForCredit(Currency creditCurrency, double balance) throws InvalidAccountTypeException;
  };

  interface AccountFactory {
    AccountCreated createAccount(string name, string surname, string pesel, double balance);
    Account* obtainAccess(Credentials userCredentials) throws InvalidCredentialsException;
  };

  exception InvalidCredentialsException {
      string reason = "Invalid credentials";
  };

  exception InvalidAccountTypeException {
      string reason = "Invalid account type";
  };

};

#endif