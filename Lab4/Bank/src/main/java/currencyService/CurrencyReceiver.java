package currencyService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import service.*;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CurrencyReceiver {

    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceBlockingStub blockingStub;

    private CurrencyService currencyService;

    private CurrencyReceiver(String host, int port, CurrencyService currencyService) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());

        this.currencyService = currencyService;
    }

    private CurrencyReceiver(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CurrencyServiceGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void subscribe() {
        ExchangeRateRequest request = ExchangeRateRequest.newBuilder().setBaseCurrency(Currency.USD).build();
        Iterator<ExchangeRateSequence> response;

        try {
            response = blockingStub.subscribe(request);

            while(response.hasNext()) {
                currencyService.updateCurrencies(response.next());
                System.out.println("Response: " + response.next());
            }
        } catch (StatusRuntimeException ignored) {
        }
    }

    private void baseCurrency() {
        ExchangeRateRequest request = ExchangeRateRequest.newBuilder().setBaseCurrency(Currency.USD).build();
        ExchangeRateResponse response;

        response = blockingStub.baseCurrency(request);

        currencyService.updateBaseCurrency(response);
        System.out.println("Base Response: " + response);
    }

    public static void main(String[] args) throws Exception {
        CurrencyService currencyService = new CurrencyService();
        CurrencyReceiver client = new CurrencyReceiver("localhost", 8080, currencyService);

        try {
            client.baseCurrency();
            client.subscribe();
        } finally {
            client.shutdown();
        }
    }
}
