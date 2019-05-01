package currencyService;

import javafx.util.Pair;
import service.Currency;
import service.ExchangeRateResponse;
import service.ExchangeRateSequence;

import java.util.HashMap;

public class CurrencyService {

    private HashMap<Currency, Pair<Double, Double>> currencyValues = new HashMap<>();

    CurrencyService() {
        currencyValues.put(Currency.PLN, new Pair<>(0.0, 0.0));
        currencyValues.put(Currency.EUR, new Pair<>(0.0, 0.0));
        currencyValues.put(Currency.USD, new Pair<>(0.0, 0.0));
        currencyValues.put(Currency.CHF, new Pair<>(0.0, 0.0));
    }

    public void updateBaseCurrency(ExchangeRateResponse exchangeRateResponse) {
        currencyValues.put(exchangeRateResponse.getCurrency(), new Pair<>(exchangeRateResponse.getPurchase(), exchangeRateResponse.getSale()));
    }

    public void updateCurrencies(ExchangeRateSequence exchangeRateSequence) {
        for(ExchangeRateResponse exchangeRateResponse : exchangeRateSequence.getExchangeRateResponseList()) {
            currencyValues.put(exchangeRateResponse.getCurrency(), new Pair<>(exchangeRateResponse.getPurchase(), exchangeRateResponse.getSale()));
        }
    }
}
