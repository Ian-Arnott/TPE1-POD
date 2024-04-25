package ar.edu.itba.pod.grpc.client;

import airport.NotifyServiceGrpc;
import airport.NotifyServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.ClientUtils;
import ar.edu.itba.pod.grpc.client.utils.callback.NotificationResponseFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.observers.NotificationStreamObserver;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class NotifyClient {
    private static final Logger logger = LoggerFactory.getLogger(NotifyClient.class);
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Notify Client Starting...");
        Map<String,String> argMap = ClientUtils.parseArgs(args);

        final String serverAddress = argMap.get(ClientArgs.SERVER_ADDRESS.getValue());
        final String action = argMap.get(ClientArgs.ACTION.getValue());
        final String airlineName = argMap.get(ClientArgs.AIRLINE.getValue());

        ClientUtils.checkNullArgs(serverAddress, "Server address is required");
        ClientUtils.checkNullArgs(airlineName, "Airline is required");
        ClientUtils.checkNullArgs(action, "Action is required");

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        NotifyServiceGrpc.NotifyServiceFutureStub futureStub = NotifyServiceGrpc.newFutureStub(channel);
        NotifyServiceGrpc.NotifyServiceStub serviceStub = NotifyServiceGrpc.newStub(channel);

        switch (action) {
            case "register" -> {
                NotifyServiceOuterClass.NotifyRequest request = NotifyServiceOuterClass.NotifyRequest.newBuilder()
                        .setAirlineName(airlineName).build();
                StreamObserver<NotifyServiceOuterClass.Notification> observer = new NotificationStreamObserver(latch);
                serviceStub.notifyAirline(request, observer);
                latch.await();
            }
            case "unregister" -> {
                NotifyServiceOuterClass.NotifyRequest request = NotifyServiceOuterClass.NotifyRequest.newBuilder()
                        .setAirlineName(airlineName).build();
                ListenableFuture<NotifyServiceOuterClass.NotificationResponse> listenableFuture = futureStub.notifyRemoveAirline(request);
                Futures.addCallback(listenableFuture, new NotificationResponseFutureCallback(logger,latch,airlineName), Runnable::run);
            }
        }

        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
