package currencyService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import service.Currency;
import service.CurrencyServiceGrpc;
import service.ExchangeRateRequest;
import service.ExchangeRateSequence;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CurrencyReceiver {

    private final ManagedChannel channel;
    private final CurrencyServiceGrpc.CurrencyServiceBlockingStub blockingStub;

    private CurrencyReceiver(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
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
                System.out.println("Response: " + response.next());
            }
        } catch (StatusRuntimeException ignored) {
        }
    }

    public static void main(String[] args) throws Exception {
        CurrencyReceiver client = new CurrencyReceiver("localhost", 8080);

        try {
            client.subscribe();
        } finally {
            client.shutdown();
        }
    }
}
