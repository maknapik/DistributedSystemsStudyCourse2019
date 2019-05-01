package service;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Random;

class CurrencyValuesProvider {

    private static final double MIN = -0.5;
    private static final double MAX = 0.5;
    private static final HashMap<Currency, Pair<Double, Double>> CURRENCY_VALUES = new HashMap<>();

    static {
        CURRENCY_VALUES.put(Currency.PLN, new Pair<>(2.15, 2.10));
        CURRENCY_VALUES.put(Currency.EUR, new Pair<>(4.22, 4.15));
        CURRENCY_VALUES.put(Currency.USD, new Pair<>(3.68, 3.50));
        CURRENCY_VALUES.put(Currency.CHF, new Pair<>(3.78, 3.62));
    }

    double getCurrencyPurhchaseValue(Currency currency) {
        Random random = new Random();
        double randomValue = MIN + (MAX - MIN) * random.nextDouble();
        double currencyBaseValue = CURRENCY_VALUES.get(currency).getKey();

        return currencyBaseValue + randomValue;
    }

    double getCurrencySaleValue(Currency currency) {
        Random random = new Random();
        double randomValue = MIN + (MAX - MIN) * random.nextDouble();
        double currencyBaseValue = CURRENCY_VALUES.get(currency).getValue();

        return currencyBaseValue + randomValue;
    }
}
