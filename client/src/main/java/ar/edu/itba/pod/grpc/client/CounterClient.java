package ar.edu.itba.pod.grpc.client;

import airport.AdminAirportServiceGrpc;
import airport.AdminAirportServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.BoolValueFutureCallback;
import ar.edu.itba.pod.grpc.client.utils.callback.ManifestResponseFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;

public class CounterClient {
    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
        logger.info("Admin Client Starting ...");
        Map<String,String> argMap = parseArgs(args);
        final String serverAddress = argMap.get(ClientArgs.SERVER_ADDRESS.getValue());
        final String action = argMap.get(ClientArgs.ACTION.getValue());

        checkNullArgs(serverAddress, "Server Address Not Specified");
        checkNullArgs(action, "Action Not Specified");

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        AdminAirportServiceGrpc.AdminAirportServiceFutureStub stub = AdminAirportServiceGrpc
                .newFutureStub(channel);

        switch (action) {
            case "listSectors" -> {

            }
        }

        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage());
        }
    }
}
