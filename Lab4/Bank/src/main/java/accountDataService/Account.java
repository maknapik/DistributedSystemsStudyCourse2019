package accountDataService;

import Bank.*;
import com.zeroc.Ice.Current;
import currencyService.CurrencyReceiver;
import currencyService.CurrencyService;

public class Account implements Bank.Account {

    private static final service.Currency NATIVE_CURRENCY = service.Currency.EUR;
    private static final double CREDIT_PERCENT = 1.1;

    private AccountPrx accountPrx;

    private String pesel;
    private String password;

    private AccountType accountType;

    private String name;
    private String surName;

    double balance;

    private CurrencyReceiverThread currencyReceiverThread;

    public Account(String pesel, String password, AccountType accountType, String name, String surName, double balance) {
        this.pesel = pesel;
        this.password = password;
        this.accountType = accountType;
        this.name = name;
        this.surName = surName;
        this.balance = balance;

        currencyReceiverThread = new CurrencyReceiverThread();
        currencyReceiverThread.start();
    }

    public void setAccountPrx(AccountPrx accountPrx) {
        this.accountPrx = accountPrx;
    }

    public String getPesel() {
        return pesel;
    }

    public String getPassword() {
        return password;
    }

    public AccountPrx getAccountPrx() {
        return accountPrx;
    }

    @Override
    public AccountType getAccountType(Current current) {
        return accountType;
    }

    @Override
    public double getAccountBalance(Current current) {
        return balance;
    }

    @Override
    public CreditEstimate applyForCredit(Currency creditCurrency, double balance, String period, Current current) throws InvalidAccountTypeException {
        if (accountType != AccountType.PREMIUM) {
            throw new InvalidAccountTypeException();
        }

        service.Currency currency;
        switch(creditCurrency) {
            case PLN:
                currency = service.Currency.PLN;
                break;
            case USD:
                currency = service.Currency.USD;
                break;
            case CHF:
                currency = service.Currency.CHF;
                break;
            case EUR:
                currency = service.Currency.EUR;
                break;
            default:
                currency = NATIVE_CURRENCY;
        }
        return new CreditEstimate(currencyReceiverThread.getCurrencyReceiver().getNativePurchaseValue() * balance * CREDIT_PERCENT,
                currencyReceiverThread.getCurrencyReceiver().getPurchaseValue(currency) * balance * CREDIT_PERCENT);
    }

    private class CurrencyReceiverThread extends Thread {

        private CurrencyReceiver currencyReceiver;

        public CurrencyReceiverThread() {
            CurrencyService currencyService = new CurrencyService();

            currencyReceiver = new CurrencyReceiver("localhost", 8080, currencyService, NATIVE_CURRENCY);
        }

        public void run() {
            try {
                currencyReceiver.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public CurrencyReceiver getCurrencyReceiver() {
            return currencyReceiver;
        }
    }
}
