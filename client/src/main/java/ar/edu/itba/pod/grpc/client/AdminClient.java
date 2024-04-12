package ar.edu.itba.pod.grpc.client;

import airport.AdminAirportServiceGrpc;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.BoolValueFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import airport.AdminAirportServiceOuterClass.*;

import static ar.edu.itba.pod.grpc.client.utils.ClientUtils.*;

public class AdminClient {
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

        final AtomicInteger couldAdd = new AtomicInteger(0);
        final AtomicInteger couldNotAdd = new AtomicInteger(0);

        switch (action) {
            case "addSector" -> {
                final String sectorName = argMap.get(ClientArgs.SECTOR.getValue());
                checkNullArgs(sectorName, "Sector Name Not Specified");
                latch = new CountDownLatch(1);
                AddSectorRequest addSectorRequest = AddSectorRequest.newBuilder()
                        .setSectorName(sectorName)
                        .build();
                ListenableFuture<BoolValue> listenableFuture = stub.addSector(addSectorRequest);
                Futures.addCallback(listenableFuture, new BoolValueFutureCallback(logger,latch,couldAdd,couldNotAdd), Runnable::run);
            }
            case "addCounters" -> {
                // do stuff
            }
        }

        try {
            logger.info("Waiting for response...");
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage());
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
