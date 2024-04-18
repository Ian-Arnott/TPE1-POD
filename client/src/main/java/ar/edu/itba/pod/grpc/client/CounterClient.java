package ar.edu.itba.pod.grpc.client;

import airport.CounterAssignmentServiceGrpc;
import airport.CounterAssignmentServiceOuterClass;
import ar.edu.itba.pod.grpc.client.utils.ClientArgs;
import ar.edu.itba.pod.grpc.client.utils.callback.ListSectorsResponseFutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

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

        CounterAssignmentServiceGrpc.CounterAssignmentServiceFutureStub stub = CounterAssignmentServiceGrpc
                .newFutureStub(channel);

        switch (action) {
            case "listSectors" -> {
                latch = new CountDownLatch(1);

//                for (String[] data : csvData) {
//                    ListenableFuture<AdminAirportServiceOuterClass.ManifestResponse> listenableFuture;
//                    AdminAirportServiceOuterClass.ManifestRequest manifestRequest = AdminAirportServiceOuterClass.ManifestRequest.newBuilder().setBooking(data[0])
//                            .setFlight(data[1]).setAirline(data[2]).build();
//                    listenableFuture = stub.manifest(manifestRequest);
//                    Futures.addCallback(listenableFuture,
//                            new ManifestResponseFutureCallback(logger,latch,data[0],data[1],data[2]), Runnable::run);
//                }
                ListenableFuture<CounterAssignmentServiceOuterClass.ListSectorsResponse> listenableFuture = stub.listSectors(Empty.getDefaultInstance());
                Futures.addCallback(
                        listenableFuture,
                        new ListSectorsResponseFutureCallback(logger, latch),
                        Runnable::run
                );
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
