package currencyService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import service.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CurrencyReceiver {

    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceBlockingStub blockingStub;

    private Currency nativeCurrency;
    private CurrencyService currencyService;

    private ExchangeRateResponse nativeExchangeRateResponse;
    private List<ExchangeRateResponse> exchangeRateResponses = new ArrayList<>();

    public CurrencyReceiver(String host, int port, CurrencyService currencyService, Currency nativeCurrency) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());

        this.currencyService = currencyService;
        this.nativeCurrency = nativeCurrency;
    }

    private CurrencyReceiver(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CurrencyServiceGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void subscribe() {
        ExchangeRateRequest request = ExchangeRateRequest.newBuilder().setBaseCurrency(nativeCurrency).build();
        Iterator<ExchangeRateSequence> response;

        try {
            response = blockingStub.subscribe(request);

            while(response.hasNext()) {
                currencyService.updateCurrencies(response.next());
                System.out.println("Response: " + response.next());

                exchangeRateResponses.clear();
                exchangeRateResponses.addAll(response.next().getExchangeRateResponseList());
            }
        } catch (StatusRuntimeException ignored) {
        }
    }

    private void nativeCurrency() {
        ExchangeRateRequest request = ExchangeRateRequest.newBuilder().setBaseCurrency(nativeCurrency).build();
        ExchangeRateResponse response;

        response = blockingStub.baseCurrency(request);

        currencyService.updateBaseCurrency(response);
        System.out.println("Base Response: " + response);

        nativeExchangeRateResponse = response;
    }

    public double getNativePurchaseValue() {
        return nativeExchangeRateResponse.getPurchase();
    }

    public double getNativeSaleValue() {
        return nativeExchangeRateResponse.getSale();
    }

    public double getPurchaseValue(Currency currency) {
        Optional<Double> purchaseValue = exchangeRateResponses.stream()
                .filter(exchangeRateResponse -> exchangeRateResponse.getCurrency() == currency)
                .map(ExchangeRateResponse::getPurchase)
                .findFirst();

        if (purchaseValue.isPresent()) {
            return purchaseValue.get();
        } else {
            return -1;
        }
    }

    public double getSaleValue(Currency currency) {
        return exchangeRateResponses.stream()
                .filter(exchangeRateResponse -> exchangeRateResponse.getCurrency() == currency)
                .map(ExchangeRateResponse::getSale)
                .findFirst()
                .get();
    }

    public void start() throws InterruptedException {
        try {
            nativeCurrency();
            subscribe();
        } finally {
            shutdown();
        }
    }

}
