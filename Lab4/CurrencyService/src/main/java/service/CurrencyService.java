package service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

public class CurrencyService {

    static public void main(String [] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new CurrencyServiceImpl()).build();

        System.out.println("Starting server...");
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }

    public static class CurrencyServiceImpl extends CurrencyServiceGrpc.CurrencyServiceImplBase {

        private static final int FREQUENCY = 5000;
        private CurrencyValuesProvider currencyValuesProvider = new CurrencyValuesProvider();

        @Override
        public void subscribe(ExchangeRateRequest request, StreamObserver<ExchangeRateSequence> responseObserver) {
            System.out.println(request);

            Currency baseCurrency = request.getBaseCurrency();

            while(true) {

                responseObserver.onNext(getExchangeRates(baseCurrency));
                try {
                    Thread.sleep(FREQUENCY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private ExchangeRateSequence getExchangeRates(Currency baseCurrency) {
            Currency[] currencies = Currency.values();

            ExchangeRateSequence.Builder builder = ExchangeRateSequence.newBuilder();
            for(Currency currency : currencies) {
                if(currency == baseCurrency || currency == Currency.UNKNOWN || currency == Currency.UNRECOGNIZED) {
                    continue;
                }

                builder.addExchangeRateResponse(ExchangeRateResponse.newBuilder()
                        .setCurrency(currency)
                        .setPurchase(currencyValuesProvider.getCurrencyPurhchaseValue(currency))
                        .setSale(currencyValuesProvider.getCurrencySaleValue(currency))
                        .build());
            }

            return builder.build();
        }

        @Override
        public void baseCurrency(ExchangeRateRequest request, StreamObserver<ExchangeRateResponse> responseObserver) {
            Currency baseCurrency = request.getBaseCurrency();

            responseObserver.onNext(ExchangeRateResponse.newBuilder()
                    .setCurrency(baseCurrency)
                    .setPurchase(currencyValuesProvider.getCurrencyPurhchaseValue(baseCurrency))
                    .setSale(currencyValuesProvider.getCurrencySaleValue(baseCurrency))
                    .build());
            responseObserver.onCompleted();
        }

    }
}
